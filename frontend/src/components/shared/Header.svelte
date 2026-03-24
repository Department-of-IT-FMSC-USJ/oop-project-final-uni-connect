<script>
  export let title = '';
  export let brandName = 'UniConnect';
  
  import { onMount, createEventDispatcher } from 'svelte';
  import { logout, getCurrentUser, api, getRoleDashboardPath } from '../../lib/api.js';

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
  let sidebarCollapsed = false;

  const SIDEBAR_COLLAPSE_KEY = 'ui.sidebarCollapsed';
  const NOTIFICATIONS_CLEARED_AT_KEY = 'notifications.clearedAt';

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

      const clearedAt = localStorage.getItem(NOTIFICATIONS_CLEARED_AT_KEY);
      if (clearedAt) {
        const clearedDate = new Date(clearedAt);
        systemNotifications = systemNotifications.filter(
          (item) => item.createdAt && new Date(item.createdAt) > clearedDate
        );
        messageNotifications = messageNotifications.filter(
          (item) => item.lastMessageAt && new Date(item.lastMessageAt) > clearedDate
        );
        systemUnreadCount = systemNotifications.length;
        messageUnreadCount = messageNotifications.reduce((sum, m) => sum + (m.unreadCount || 0), 0);
      }
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

  async function clearNotifications() {
    try {
      await api.put('/notifications/mark-all-read', {});
      localStorage.setItem(NOTIFICATIONS_CLEARED_AT_KEY, new Date().toISOString());
      systemNotifications = [];
      systemUnreadCount = 0;
      messageNotifications = [];
      messageUnreadCount = 0;
    } catch (e) {
      console.error('Failed to clear', e);
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

  function applySidebarState(collapsed) {
    sidebarCollapsed = !!collapsed;
    if (typeof document !== 'undefined') {
      document.body.classList.toggle('sidebar-collapsed', sidebarCollapsed);
    }
    if (typeof window !== 'undefined') {
      localStorage.setItem(SIDEBAR_COLLAPSE_KEY, sidebarCollapsed ? '1' : '0');
      window.dispatchEvent(new CustomEvent('sidebar:state-change', { detail: { collapsed: sidebarCollapsed } }));
    }
  }

  function toggleSidebar() {
    let collapsed = document.body.classList.contains('sidebar-collapsed');
    collapsed = !collapsed;
    document.body.classList.toggle('sidebar-collapsed', collapsed);
    localStorage.setItem(SIDEBAR_COLLAPSE_KEY, collapsed ? '1' : '0');
    window.dispatchEvent(new CustomEvent('sidebar:state-change', { detail: { collapsed } }));
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
    const persisted = localStorage.getItem(SIDEBAR_COLLAPSE_KEY);
    applySidebarState(persisted === '1');
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
    const sidebarHandler = (e) => sidebarCollapsed = e.detail.collapsed;

    window.addEventListener('messages:updated', handler);
    document.addEventListener('click', clickHandler);
    window.addEventListener('sidebar:state-change', sidebarHandler);

    return () => {
      if (notificationTimer) window.clearInterval(notificationTimer);
      window.removeEventListener('messages:updated', handler);
      document.removeEventListener('click', clickHandler);
      window.removeEventListener('sidebar:state-change', sidebarHandler);
    };
  });
</script>

<header class="header">
  <div class="header-copy">
    <div class="title-row">
      <button class="icon-btn collapse-toggle" class:is-collapsed={sidebarCollapsed} type="button" on:click={toggleSidebar} title={sidebarCollapsed ? 'Expand sidebar' : 'Collapse sidebar'} aria-label={sidebarCollapsed ? 'Expand sidebar' : 'Collapse sidebar'}>
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <line x1="3" y1="12" x2="21" y2="12"></line>
          <line x1="3" y1="6" x2="21" y2="6"></line>
          <line x1="3" y1="18" x2="21" y2="18"></line>
        </svg>
      </button>
      <span class="brand-text">{brandName}</span>
      {#if title}
        <span class="divider">/</span>
        <h1 class="header-title">{title}</h1>
      {/if}
    </div>
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
              <span class="notification-count">{messageUnreadCount + systemUnreadCount} unread</span>
            </div>
            <button class="btn btn-text btn-xs" style="color: var(--danger); padding: 0.2rem 0.6rem;" on:click={clearNotifications}>Clear</button>
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
    left: 0;
    right: 0;
    height: var(--header-height);
    background: rgba(255, 255, 255, 0.96);
    backdrop-filter: blur(12px);
    border-bottom: 1px solid var(--border-light);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 3rem;
    z-index: 110;
    transition: left 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }

  .header-copy {
    display: flex;
    align-items: center;
    flex: 1;
  }

  .title-row {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }

  .brand-text {
    font-family: 'Times New Roman', serif;
    font-size: 1.6rem; font-weight: 700; letter-spacing: -0.05em;
    color: var(--text-main); margin-left: 0.5rem;
  }
  
  .divider { 
    margin: 0 0.5rem; 
    color: var(--border-medium); 
    font-size: 1.25rem; font-weight: 300; 
  }

  .header-title {
    font-size: 1.1rem;
    font-weight: 600;
    color: var(--text-main);
    margin: 0; padding: 0;
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 0.75rem;
  }

  .icon-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    border-radius: 8px;
    background: transparent;
    color: var(--text-muted);
    border: 1px solid transparent;
    transition: all var(--transition-fast);
    cursor: pointer;
  }
  
  .icon-btn:hover {
    background: var(--bg-alt);
    color: var(--text-secondary);
  }

  .collapse-toggle svg {
    transition: transform 0.24s cubic-bezier(0.4, 0, 0.2, 1);
  }
  
  .collapse-toggle.is-collapsed svg {
    transform: scaleX(-1);
  }

  .panel-toggle.active {
    background: var(--bg-alt);
    color: var(--text-main);
  }

  .notifications { position: relative; }

  .notification-badge {
    position: absolute;
    top: -4px;
    right: -4px;
    min-width: 20px;
    height: 20px;
    padding: 0 0.4rem;
    border-radius: 99px;
    background: var(--accent);
    color: white;
    font-size: 0.65rem;
    font-weight: 700;
    display: flex;
    align-items: center;
    justify-content: center;
    font-family: var(--font-ui);
  }

  .notification-panel {
    position: absolute;
    top: calc(100% + 0.75rem);
    right: 0;
    width: 360px;
    max-height: 480px;
    background: var(--bg-main);
    border: 1px solid var(--border-light);
    border-radius: 10px;
    box-shadow: var(--shadow-lg);
    padding: 0.75rem;
    z-index: 200;
    animation: slideIn 0.25s ease-out;
  }

  .notification-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0.75rem;
    margin-bottom: 0.5rem;
    border-bottom: 1px solid var(--border-light);
    font-family: var(--font-ui);
    font-size: 0.95rem;
    font-weight: 600;
    color: var(--text-main);
  }

  .notification-count {
    color: var(--text-muted);
    font-size: 0.85rem;
    font-weight: 400;
  }

  .notification-empty {
    color: var(--text-muted);
    font-size: 0.9rem;
    padding: 2rem 1rem;
    text-align: center;
    font-family: var(--font-ui);
  }

  .notification-scroll {
    max-height: 380px;
    overflow-y: auto;
  }

  .notification-item {
    width: 100%;
    text-align: left;
    padding: 0.75rem;
    border-radius: 8px;
    background: transparent;
    border: 1px solid transparent;
    margin-bottom: 0.25rem;
    cursor: pointer;
    font-family: var(--font-ui);
    transition: background var(--transition-fast), border-color var(--transition-fast);
  }

  .notification-item:hover {
    background: var(--bg-alt);
    border-color: var(--border-light);
  }

  .notification-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 0.75rem;
    margin-bottom: 0.35rem;
    font-weight: 500;
    color: var(--text-main);
  }

  .notification-item p {
    color: var(--text-muted);
    font-size: 0.85rem;
    margin: 0;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .notification-chip {
    display: inline-flex;
    margin-bottom: 0.5rem;
    font-size: 0.65rem;
    font-weight: 700;
    color: var(--text-muted);
    text-transform: uppercase;
    letter-spacing: 0.08em;
  }

  .notification-time {
    color: var(--text-muted);
    font-size: 0.8rem;
    font-weight: 400;
  }

  .notification-pill {
    min-width: 22px;
    height: 22px;
    padding: 0 0.4rem;
    border-radius: 99px;
    background: var(--accent);
    color: white;
    font-size: 0.75rem;
    font-weight: 700;
    display: inline-flex;
    align-items: center;
    justify-content: center;
  }

  .notification-section-title {
    font-size: 0.75rem;
    text-transform: uppercase;
    letter-spacing: 0.08em;
    color: var(--text-muted);
    margin: 0.75rem 0.5rem 0.5rem;
    font-family: var(--font-ui);
    font-weight: 600;
  }

  .user-menu { position: relative; }

  .user-btn {
    display: flex;
    align-items: center;
    gap: 0.6rem;
    padding: 0.4rem 0.6rem 0.4rem 0.4rem;
    border-radius: 24px;
    background: transparent;
    color: var(--text-main);
    border: 1px solid var(--border-light);
    transition: all var(--transition-fast);
    cursor: pointer;
  }

  .user-btn:hover,
  .user-btn.active {
    background: var(--bg-alt);
    border-color: var(--border-medium);
  }

  .avatar {
    width: 34px;
    height: 34px;
    border-radius: 50%;
    background: var(--bg-alt);
    color: var(--accent);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 0.9rem;
    font-weight: 600;
    font-family: var(--font-ui);
    overflow: hidden;
    border: 1px solid var(--border-light);
    flex-shrink: 0;
  }

  .avatar img { width: 100%; height: 100%; object-fit: cover; }

  .dropdown-avatar {
    width: 40px;
    height: 40px;
    font-size: 1rem;
  }

  .user-copy {
    display: grid;
    text-align: left;
    line-height: 1.3;
    font-family: var(--font-ui);
  }

  .user-name {
    font-size: 0.85rem;
    font-weight: 500;
    color: var(--text-main);
  }

  .user-role {
    font-size: 0.75rem;
    color: var(--text-muted);
    font-weight: 400;
  }

  .dropdown {
    position: absolute;
    top: 100%;
    right: 0;
    margin-top: 0.75rem;
    background: var(--bg-main);
    border-radius: 10px;
    border: 1px solid var(--border-light);
    box-shadow: var(--shadow-lg);
    min-width: 240px;
    padding: 0.5rem;
    font-family: var(--font-ui);
    z-index: 200;
    animation: slideIn 0.25s ease-out;
  }

  .dropdown-profile {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 0.75rem;
    margin-bottom: 0.5rem;
    border-radius: 8px;
    background: var(--bg-alt);
  }

  .dropdown-profile strong {
    color: var(--text-main);
    font-size: 0.9rem;
    font-weight: 600;
    display: block;
  }

  .dropdown-profile p {
    margin-top: 0.15rem;
    font-size: 0.8rem;
    margin-bottom: 0;
    color: var(--text-muted);
  }

  .dropdown-divider {
    height: 1px;
    margin: 0.5rem 0;
    background: var(--border-light);
  }

  .dropdown-item {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    width: 100%;
    padding: 0.65rem 0.75rem;
    border-radius: 8px;
    font-size: 0.9rem;
    color: var(--text-main);
    background: transparent;
    border: none;
    cursor: pointer;
    transition: all var(--transition-fast);
  }

  .dropdown-item:hover { 
    background: var(--bg-alt);
    color: var(--accent);
  }

  .dropdown-item svg {
    opacity: 0.7;
    transition: opacity var(--transition-fast);
  }

  .dropdown-item:hover svg {
    opacity: 1;
  }

  @media (max-width: 900px) {
    .header { padding: 0 1rem; }
    .user-copy { display: none; }
    .notification-panel { width: min(360px, calc(100vw - 2rem)); right: -0.5rem; }
  }

</style>

