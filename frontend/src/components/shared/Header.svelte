<script>
  import { onMount } from 'svelte';
  import { logout, getCurrentUser, api, getRoleDashboardPath } from '../../lib/api.js';

  export let title = '';

  let user = getCurrentUser();
  let showDropdown = false;
  let showNotifications = false;
  let messageNotifications = [];
  let systemNotifications = [];
  let messageUnreadCount = 0;
  let systemUnreadCount = 0;
  let notificationError = '';
  let notificationTimer = null;
  let notificationsUnavailable = false;
  let notificationRoot;
  let dropdownRoot;

  function toggleDropdown() {
    showDropdown = !showDropdown;
    if (showDropdown) {
      showNotifications = false;
    }
  }

  async function loadNotifications() {
    if (notificationsUnavailable) {
      return;
    }
    notificationError = '';
    try {
      const [conversations, unread, systemItems, systemUnread] = await Promise.all([
        api.get('/messages/conversations', { cache: false }),
        api.get('/messages/unread-count', { cache: false }),
        api.get('/notifications', { cache: false }),
        api.get('/notifications/unread-count', { cache: false })
      ]);
      messageNotifications = Array.isArray(conversations) ? conversations : [];
      messageUnreadCount = unread?.unreadCount || 0;
      systemNotifications = Array.isArray(systemItems) ? systemItems : [];
      systemUnreadCount = systemUnread?.unreadCount || 0;
    } catch (e) {
      messageNotifications = [];
      messageUnreadCount = 0;
      systemNotifications = [];
      systemUnreadCount = 0;
      if (e?.status === 404 || e?.status === 405) {
        notificationsUnavailable = true;
      } else {
        notificationError = 'Unable to load notifications right now.';
      }
    }
  }

  async function toggleNotifications() {
    showNotifications = !showNotifications;
    if (showNotifications) {
      showDropdown = false;
      await loadNotifications();
      if (!notificationsUnavailable && systemUnreadCount > 0) {
        await api.put('/notifications/mark-all-read', {});
        systemUnreadCount = 0;
      }
    }
  }

  function handleLogout() {
    logout();
  }

  function openConversation(userId) {
    showNotifications = false;
    if (user?.role === 'UNDERGRADUATE') {
      window.location.href = `/undergraduate/mentors?contact=${userId}`;
      return;
    }
    if (user?.role === 'ACADEMIC_MENTOR') {
      window.location.href = `/academic-mentor/students?contact=${userId}`;
      return;
    }
    if (user?.role === 'INDUSTRY_MENTOR') {
      window.location.href = `/industry-mentor/students?contact=${userId}`;
    }
  }

  function openSystemNotification(item) {
    showNotifications = false;
    if (item?.link) {
      window.location.href = item.link;
    }
  }

  function formatRole(role) {
    return (role || 'USER')
      .split('_')
      .map((part) => part.charAt(0) + part.slice(1).toLowerCase())
      .join(' ');
  }

  function goToDashboard() {
    showDropdown = false;
    window.location.href = getRoleDashboardPath(user?.role);
  }

  function getProfilePath() {
    switch (user?.role) {
      case 'UNDERGRADUATE':
        return '/undergraduate/profile';
      case 'ACADEMIC_MENTOR':
        return '/academic-mentor/profile';
      case 'INDUSTRY_MENTOR':
        return '/industry-mentor/profile';
      case 'DEPARTMENT_HEAD':
      case 'DEPARTMENT_ASSISTANT':
        return '/hod/profile';
      default:
        return '';
    }
  }

  function goToProfile() {
    const profilePath = getProfilePath();
    if (!profilePath) return;
    showDropdown = false;
    window.location.href = profilePath;
  }

  function formatWhen(value) {
    if (!value) return '';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return '';
    const diffMs = date.getTime() - Date.now();
    const diffMinutes = Math.round(diffMs / 60000);
    const absMinutes = Math.abs(diffMinutes);
    if (absMinutes < 1) return 'Just now';
    if (absMinutes < 60) return `${absMinutes}m ago`;
    const absHours = Math.abs(Math.round(diffMinutes / 60));
    if (absHours < 24) return `${absHours}h ago`;
    return date.toLocaleDateString(undefined, {
      month: 'short',
      day: 'numeric',
      hour: 'numeric',
      minute: '2-digit'
    });
  }

  onMount(() => {
    loadNotifications();
    notificationTimer = window.setInterval(loadNotifications, 15000);
    const handler = () => loadNotifications();
    const clickHandler = (event) => {
      if (showNotifications && notificationRoot && !notificationRoot.contains(event.target)) {
        showNotifications = false;
      }
      if (showDropdown && dropdownRoot && !dropdownRoot.contains(event.target)) {
        showDropdown = false;
      }
    };
    window.addEventListener('messages:updated', handler);
    document.addEventListener('click', clickHandler);
    return () => {
      if (notificationTimer) window.clearInterval(notificationTimer);
      window.removeEventListener('messages:updated', handler);
      document.removeEventListener('click', clickHandler);
    };
  });
</script>

<header class="header">
  <div class="header-copy">
    <span class="header-kicker">Workspace</span>
    <h1 class="header-title">{title}</h1>
  </div>

  <div class="header-actions">
    <div class="notifications" bind:this={notificationRoot}>
      <button class="icon-btn panel-toggle" class:active={showNotifications} title="Notifications" on:click={toggleNotifications}>
        {#if messageUnreadCount + systemUnreadCount > 0}
          <span class="notification-badge">{messageUnreadCount + systemUnreadCount}</span>
        {/if}
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
          <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
        </svg>
      </button>

      {#if showNotifications}
        <div class="notification-panel">
          <div class="notification-head">
            <div>
              <strong>Notifications</strong>
              <p>Messages and system updates in one place</p>
            </div>
            <span class="notification-count">{messageUnreadCount + systemUnreadCount} unread</span>
          </div>

          <div class="notification-scroll">
            {#if notificationError}
              <p class="notification-empty">{notificationError}</p>
            {:else if notificationsUnavailable}
              <p class="notification-empty">Notifications will appear after the backend is restarted with the latest routes.</p>
            {:else if systemNotifications.length === 0 && messageNotifications.length === 0}
              <p class="notification-empty">No notifications yet. New messages and alerts will appear here.</p>
            {:else}
              {#if systemNotifications.length > 0}
                <div class="notification-section-title">System</div>
                {#each systemNotifications as item}
                  <button class="notification-item" on:click={() => openSystemNotification(item)}>
                    <div class="notification-chip system">System</div>
                    <div class="notification-row">
                      <strong>{item.title}</strong>
                      <span class="notification-time">{formatWhen(item.createdAt)}</span>
                    </div>
                    <p>{item.message}</p>
                  </button>
                {/each}
              {/if}
              {#if messageNotifications.length > 0}
                <div class="notification-section-title">Messages</div>
                {#each messageNotifications as item}
                  <button class="notification-item" on:click={() => openConversation(item.userId)}>
                    <div class="notification-chip message">Direct message</div>
                    <div class="notification-row">
                      <strong>{item.fullName}</strong>
                      {#if item.unreadCount > 0}
                        <span class="notification-pill">{item.unreadCount}</span>
                      {:else}
                        <span class="notification-time">{formatWhen(item.lastMessageAt)}</span>
                      {/if}
                    </div>
                    <p>{item.lastMessage || 'New message'}</p>
                    {#if item.unreadCount > 0}
                      <span class="notification-time">{formatWhen(item.lastMessageAt)}</span>
                    {/if}
                  </button>
                {/each}
              {/if}
            {/if}
          </div>
        </div>
      {/if}
    </div>

    <div class="user-menu" bind:this={dropdownRoot}>
      <button class="user-btn" class:active={showDropdown} on:click={toggleDropdown}>
        <div class="avatar">
          {#if user?.profilePicture}
            <img src={user.profilePicture} alt="" />
          {:else}
            <span>{user?.fullName?.charAt(0) || 'U'}</span>
          {/if}
        </div>
        <div class="user-copy">
          <span class="user-name">{user?.fullName || 'User'}</span>
          <span class="user-role">{formatRole(user?.role)}</span>
        </div>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M6 9l6 6 6-6"/>
        </svg>
      </button>

      {#if showDropdown}
        <div class="dropdown">
          <div class="dropdown-profile">
            <div class="avatar dropdown-avatar">
              {#if user?.profilePicture}
                <img src={user.profilePicture} alt="" />
              {:else}
                <span>{user?.fullName?.charAt(0) || 'U'}</span>
              {/if}
            </div>
            <div>
              <strong>{user?.fullName || 'User'}</strong>
              <p>{user?.email || formatRole(user?.role)}</p>
            </div>
          </div>
          <button type="button" class="dropdown-item" on:click={goToDashboard}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="7" height="9" rx="1"/>
              <rect x="14" y="3" width="7" height="5" rx="1"/>
              <rect x="14" y="12" width="7" height="9" rx="1"/>
              <rect x="3" y="16" width="7" height="5" rx="1"/>
            </svg>
            Dashboard
          </button>
          {#if getProfilePath()}
            <button type="button" class="dropdown-item" on:click={goToProfile}>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21a8 8 0 0 0-16 0"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              Profile
            </button>
          {/if}
          <div class="dropdown-divider"></div>
          <button type="button" class="dropdown-item" on:click={handleLogout}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
              <polyline points="16 17 21 12 16 7"/>
              <line x1="21" y1="12" x2="9" y2="12"/>
            </svg>
            Logout
          </button>
        </div>
      {/if}
    </div>
  </div>
</header>

<style>
  .header {
    position: fixed;
    top: 0;
    left: var(--sidebar-width);
    right: 0;
    height: var(--header-height);
    background: rgba(255, 255, 255, 0.78);
    border-bottom: 1px solid rgba(148, 163, 184, 0.16);
    backdrop-filter: blur(20px);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 2rem;
    z-index: 50;
  }

  .header-copy {
    display: grid;
  }

  .header-kicker {
    font-size: 0.68rem;
    font-weight: 700;
    letter-spacing: 0.12em;
    text-transform: uppercase;
    color: var(--accent);
  }

  .header-title {
    font-size: 1.25rem;
    font-weight: 600;
    color: var(--gray-900);
    letter-spacing: -0.02em;
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 0.85rem;
  }

  .icon-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 42px;
    height: 42px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.76);
    color: var(--gray-500);
    border: 1px solid rgba(148, 163, 184, 0.2);
    box-shadow: 0 10px 20px rgba(148, 163, 184, 0.12);
    transition: all 0.18s ease;
  }
  .icon-btn:hover { background: white; color: var(--gray-700); }
  .panel-toggle.active {
    color: var(--primary);
    background: rgba(219, 234, 254, 0.9);
    border-color: rgba(96, 165, 250, 0.36);
  }
  .notifications { position: relative; }
  .notification-badge {
    position: absolute;
    top: -3px;
    right: -2px;
    min-width: 18px;
    height: 18px;
    padding: 0 0.3rem;
    border-radius: 999px;
    background: linear-gradient(135deg, #dc2626, #f97316);
    color: white;
    font-size: 0.7rem;
    font-weight: 700;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .notification-panel {
    position: absolute;
    top: calc(100% + 0.5rem);
    right: 0;
    width: 380px;
    max-height: 520px;
    background: rgba(255, 255, 255, 0.94);
    border: 1px solid rgba(148, 163, 184, 0.2);
    border-radius: 24px;
    box-shadow: 0 28px 80px rgba(15, 23, 42, 0.18);
    padding: 0.85rem;
    backdrop-filter: blur(20px);
    z-index: 200;
  }
  .notification-head {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 1rem;
    padding: 0.35rem 0.35rem 0.95rem;
    margin-bottom: 0.35rem;
    border-bottom: 1px solid rgba(226, 232, 240, 0.9);
  }
  .notification-head p {
    margin-top: 0.2rem;
    font-size: 0.78rem;
    color: var(--gray-500);
  }
  .notification-count {
    color: #1d4ed8;
    font-size: 0.78rem;
    font-weight: 700;
    background: rgba(219, 234, 254, 0.9);
    border-radius: 999px;
    padding: 0.35rem 0.65rem;
  }
  .notification-empty {
    color: var(--gray-500);
    font-size: 0.85rem;
    padding: 1rem 0.35rem;
    line-height: 1.55;
  }
  .notification-scroll {
    max-height: 420px;
    overflow-y: auto;
    padding-right: 0.2rem;
  }
  .notification-item {
    width: 100%;
    text-align: left;
    padding: 0.85rem;
    border-radius: 18px;
    background: rgba(248, 250, 252, 0.72);
    border: 1px solid rgba(226, 232, 240, 0.92);
    margin-bottom: 0.65rem;
    transition: transform 0.18s ease, border-color 0.18s ease, background 0.18s ease;
  }
  .notification-item:hover {
    transform: translateY(-1px);
    background: white;
    border-color: rgba(96, 165, 250, 0.3);
  }
  .notification-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 0.75rem;
    margin-bottom: 0.3rem;
  }
  .notification-item p {
    color: var(--gray-600);
    font-size: 0.84rem;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
  .notification-chip {
    display: inline-flex;
    align-items: center;
    margin-bottom: 0.55rem;
    padding: 0.2rem 0.55rem;
    border-radius: 999px;
    font-size: 0.68rem;
    font-weight: 700;
    letter-spacing: 0.08em;
    text-transform: uppercase;
  }
  .notification-chip.system {
    background: rgba(254, 243, 199, 0.82);
    color: #92400e;
  }
  .notification-chip.message {
    background: rgba(219, 234, 254, 0.82);
    color: #1d4ed8;
  }
  .notification-time {
    display: block;
    margin-top: 0.35rem;
    color: var(--gray-400);
    font-size: 0.74rem;
  }
  .notification-pill {
    min-width: 20px;
    height: 20px;
    padding: 0 0.35rem;
    border-radius: 999px;
    background: #dbeafe;
    color: #1d4ed8;
    font-size: 0.72rem;
    font-weight: 700;
    display: inline-flex;
    align-items: center;
    justify-content: center;
  }
  .notification-section-title {
    font-size: 0.72rem;
    text-transform: uppercase;
    letter-spacing: 0.08em;
    color: var(--gray-500);
    margin: 0.75rem 0 0.6rem;
    padding: 0 0.35rem;
  }

  .user-menu { position: relative; }

  .user-btn {
    display: flex;
    align-items: center;
    gap: 0.7rem;
    padding: 0.35rem 0.55rem 0.35rem 0.4rem;
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.78);
    color: var(--gray-700);
    border: 1px solid rgba(148, 163, 184, 0.2);
    box-shadow: 0 12px 24px rgba(148, 163, 184, 0.13);
    transition: background 0.18s ease, border-color 0.18s ease;
  }
  .user-btn:hover,
  .user-btn.active {
    background: white;
    border-color: rgba(96, 165, 250, 0.32);
  }

  .avatar {
    width: 34px;
    height: 34px;
    border-radius: 50%;
    background: linear-gradient(135deg, var(--primary), var(--primary-light));
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 0.875rem;
    font-weight: 600;
    overflow: hidden;
  }
  .avatar img { width: 100%; height: 100%; object-fit: cover; }

  .user-copy {
    display: grid;
    text-align: left;
    line-height: 1.1;
  }

  .user-name {
    font-size: 0.875rem;
    font-weight: 600;
    color: var(--gray-900);
  }

  .user-role {
    font-size: 0.72rem;
    color: var(--gray-500);
  }

  .dropdown {
    position: absolute;
    top: 100%;
    right: 0;
    margin-top: 0.5rem;
    background: rgba(255, 255, 255, 0.96);
    border-radius: 22px;
    box-shadow: 0 28px 80px rgba(15, 23, 42, 0.18);
    border: 1px solid rgba(148, 163, 184, 0.18);
    min-width: 260px;
    overflow: hidden;
    padding: 0.5rem;
    backdrop-filter: blur(20px);
    z-index: 200;
  }

  .dropdown-profile {
    display: flex;
    align-items: center;
    gap: 0.85rem;
    padding: 0.65rem;
    margin-bottom: 0.35rem;
    border-radius: 16px;
    background: linear-gradient(135deg, rgba(219, 234, 254, 0.6), rgba(248, 250, 252, 0.86));
  }

  .dropdown-profile strong {
    color: var(--gray-900);
    font-size: 0.88rem;
  }

  .dropdown-profile p {
    margin-top: 0.18rem;
    font-size: 0.78rem;
    color: var(--gray-500);
  }

  .dropdown-avatar {
    width: 40px;
    height: 40px;
  }

  .dropdown-divider {
    height: 1px;
    margin: 0.35rem 0;
    background: rgba(226, 232, 240, 0.9);
  }

  .dropdown-item {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    width: 100%;
    padding: 0.8rem 0.85rem;
    border-radius: 14px;
    font-size: 0.875rem;
    color: var(--gray-700);
    background: transparent;
    transition: background 0.15s ease, color 0.15s ease;
  }
  .dropdown-item:hover { background: rgba(241, 245, 249, 0.88); color: var(--gray-900); }

  @media (max-width: 900px) {
    .header {
      padding: 0 1rem;
    }

    .user-copy {
      display: none;
    }

    .notification-panel {
      width: min(380px, calc(100vw - 2rem));
      right: -0.5rem;
    }
  }
</style>
