<script>
  import { onMount } from 'svelte';
  import { logout, getCurrentUser, api } from '../../lib/api.js';

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

  function toggleDropdown() {
    showDropdown = !showDropdown;
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
      }
      notificationError = '';
    }
  }

  async function toggleNotifications() {
    showNotifications = !showNotifications;
    if (showNotifications) {
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

  function formatWhen(value) {
    if (!value) return '';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return '';
    return date.toLocaleString();
  }

  onMount(() => {
    loadNotifications();
    notificationTimer = window.setInterval(loadNotifications, 15000);
    const handler = () => loadNotifications();
    window.addEventListener('messages:updated', handler);
    return () => {
      if (notificationTimer) window.clearInterval(notificationTimer);
      window.removeEventListener('messages:updated', handler);
    };
  });
</script>

<header class="header">
  <h1 class="header-title">{title}</h1>

  <div class="header-actions">
    <div class="notifications">
      <button class="icon-btn" title="Notifications" on:click={toggleNotifications}>
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
            <strong>Notifications</strong>
            {#if messageUnreadCount + systemUnreadCount > 0}
              <span class="notification-count">{messageUnreadCount + systemUnreadCount} unread</span>
            {/if}
          </div>
          {#if notificationError}
            <p class="notification-empty">{notificationError}</p>
          {:else if notificationsUnavailable}
            <p class="notification-empty">Notifications will appear after the backend is restarted with the latest routes.</p>
          {:else if systemNotifications.length === 0 && messageNotifications.length === 0}
            <p class="notification-empty">No notifications.</p>
          {:else}
            {#if systemNotifications.length > 0}
              <div class="notification-section-title">System</div>
              {#each systemNotifications as item}
                <button class="notification-item" on:click={() => openSystemNotification(item)}>
                  <div class="notification-row">
                    <strong>{item.title}</strong>
                  </div>
                  <p>{item.message}</p>
                  <span class="notification-time">{formatWhen(item.createdAt)}</span>
                </button>
              {/each}
            {/if}
            {#if messageNotifications.length > 0}
              <div class="notification-section-title">Messages</div>
              {#each messageNotifications as item}
                <button class="notification-item" on:click={() => openConversation(item.userId)}>
                  <div class="notification-row">
                    <strong>{item.fullName}</strong>
                    {#if item.unreadCount > 0}
                      <span class="notification-pill">{item.unreadCount}</span>
                    {/if}
                  </div>
                  <p>{item.lastMessage || 'New message'}</p>
                  <span class="notification-time">{formatWhen(item.lastMessageAt)}</span>
                </button>
              {/each}
            {/if}
          {/if}
        </div>
      {/if}
    </div>

    <div class="user-menu">
      <button class="user-btn" on:click={toggleDropdown}>
        <div class="avatar">
          {#if user?.profilePicture}
            <img src={user.profilePicture} alt="" />
          {:else}
            <span>{user?.fullName?.charAt(0) || 'U'}</span>
          {/if}
        </div>
        <span class="user-name">{user?.fullName || 'User'}</span>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M6 9l6 6 6-6"/>
        </svg>
      </button>

      {#if showDropdown}
        <div class="dropdown">
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
    background: white;
    border-bottom: 1px solid var(--gray-200);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 2rem;
    z-index: 50;
  }

  .header-title {
    font-size: 1.25rem;
    font-weight: 600;
    color: var(--gray-900);
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 1rem;
  }

  .icon-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: transparent;
    color: var(--gray-500);
    transition: all 0.15s;
  }
  .icon-btn:hover { background: var(--gray-100); color: var(--gray-700); }
  .notifications { position: relative; }
  .notification-badge {
    position: absolute;
    top: -2px;
    right: -1px;
    min-width: 18px;
    height: 18px;
    padding: 0 0.3rem;
    border-radius: 999px;
    background: #dc2626;
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
    width: 320px;
    max-height: 420px;
    overflow-y: auto;
    background: white;
    border: 1px solid var(--gray-200);
    border-radius: var(--radius);
    box-shadow: var(--shadow-lg);
    padding: 0.75rem;
    z-index: 200;
  }
  .notification-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 0.75rem;
  }
  .notification-count {
    color: var(--accent);
    font-size: 0.78rem;
    font-weight: 600;
  }
  .notification-empty {
    color: var(--gray-500);
    font-size: 0.85rem;
    padding: 0.75rem 0.25rem;
  }
  .notification-item {
    width: 100%;
    text-align: left;
    padding: 0.75rem;
    border-radius: 12px;
    background: transparent;
    border: 1px solid var(--gray-100);
    margin-bottom: 0.5rem;
  }
  .notification-item:hover { background: var(--gray-50); }
  .notification-row {
    display: flex;
    justify-content: space-between;
    gap: 0.75rem;
    margin-bottom: 0.35rem;
  }
  .notification-item p {
    color: var(--gray-600);
    font-size: 0.84rem;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
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
    margin: 0.5rem 0;
  }

  .user-menu { position: relative; }

  .user-btn {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.25rem 0.5rem;
    border-radius: var(--radius);
    background: transparent;
    color: var(--gray-700);
    transition: background 0.15s;
  }
  .user-btn:hover { background: var(--gray-100); }

  .avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: var(--primary);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 0.875rem;
    font-weight: 600;
    overflow: hidden;
  }
  .avatar img { width: 100%; height: 100%; object-fit: cover; }

  .user-name {
    font-size: 0.875rem;
    font-weight: 500;
  }

  .dropdown {
    position: absolute;
    top: 100%;
    right: 0;
    margin-top: 0.5rem;
    background: white;
    border-radius: var(--radius);
    box-shadow: var(--shadow-lg);
    border: 1px solid var(--gray-200);
    min-width: 160px;
    overflow: hidden;
    z-index: 200;
  }

  .dropdown-item {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.625rem 1rem;
    font-size: 0.875rem;
    color: var(--gray-700);
    transition: background 0.15s;
  }
  .dropdown-item:hover { background: var(--gray-50); }
</style>
