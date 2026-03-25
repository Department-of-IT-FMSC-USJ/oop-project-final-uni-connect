<script>
  export let title = '';
  export let brandName = 'UniConnect';
  
  import { onMount } from 'svelte';
  import { getCurrentUser, api } from '../../lib/api.js';

  let user = getCurrentUser();
  let showNotifications = false;
  let messageNotifications = [];
  let systemNotifications = [];
  let messageUnreadCount = 0;
  let systemUnreadCount = 0;
  let notificationError = '';
  let notificationTimer = null;
  let notificationsUnavailable = false;
  let sidebarCollapsed = false;

  const SIDEBAR_COLLAPSE_KEY = 'ui.sidebarCollapsed';
  const NOTIFICATIONS_CLEARED_AT_KEY = 'notifications.clearedAt';

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
      await loadNotifications();
      if (!notificationsUnavailable && systemUnreadCount > 0) {
        await api.put('/notifications/mark-all-read', {});
        systemUnreadCount = 0;
      }
    }
  }

  function closeNotifications() {
    showNotifications = false;
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

  function isPunishmentNotification(item) {
    if (!item?.message) return false;
    return /\s-\d+\s*points?/i.test(item.message);
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
    // Apply sidebar state without transition to prevent flash on page load
    document.body.setAttribute('data-no-sidebar-transition', '');
    applySidebarState(persisted === '1');
    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        document.body.removeAttribute('data-no-sidebar-transition');
      });
    });
    loadNotifications();
    notificationTimer = window.setInterval(loadNotifications, 15000);
    const handler = () => loadNotifications();
    const sidebarHandler = (e) => sidebarCollapsed = e.detail.collapsed;

    window.addEventListener('messages:updated', handler);
    window.addEventListener('sidebar:state-change', sidebarHandler);

    return () => {
      if (notificationTimer) window.clearInterval(notificationTimer);
      window.removeEventListener('messages:updated', handler);
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
      <a href="/" class="brand-link" title="Go to home">
        <img src="/logo.jpg" alt="UniConnect" class="brand-logo" />
      </a>
      {#if title}
        <span class="divider">/</span>
        <h1 class="header-title">{title}</h1>
      {/if}
    </div>
  </div>

  <div class="header-actions">
    <button
      class="icon-btn notification-trigger"
      class:active={showNotifications}
      title="Notifications"
      on:click={toggleNotifications}
    >
      {#if !showNotifications && messageUnreadCount + systemUnreadCount > 0}
        <span class="notification-badge">{messageUnreadCount + systemUnreadCount}</span>
      {/if}
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
        <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
      </svg>
    </button>
  </div>
</header>

<!-- macOS-style slide-in notification panel -->
{#if showNotifications}
  <div class="notification-backdrop" on:click={closeNotifications}></div>
{/if}
<aside class="notification-drawer" class:open={showNotifications}>
  <div class="drawer-header">
    <h2 class="drawer-title">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
        <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
      </svg>
      Notifications
    </h2>
    <div class="drawer-header-actions">
      <span class="unread-badge">{messageUnreadCount + systemUnreadCount} unread</span>
      <button class="drawer-clear-btn" on:click={clearNotifications} title="Clear all notifications">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="3 6 5 6 21 6"/>
          <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
        </svg>
        Clear
      </button>
      <button class="drawer-close-btn" on:click={closeNotifications} aria-label="Close notifications">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="18" y1="6" x2="6" y2="18"/>
          <line x1="6" y1="6" x2="18" y2="18"/>
        </svg>
      </button>
    </div>
  </div>

  <div class="drawer-scroll">
    {#if notificationError}
      <p class="notification-empty">{notificationError}</p>
    {:else if notificationsUnavailable}
      <p class="notification-empty">Notifications will appear after the backend is restarted with the latest routes.</p>
    {:else if systemNotifications.length === 0 && messageNotifications.length === 0}
      <div class="notification-empty-state">
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
          <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
        </svg>
        <p>All caught up!</p>
        <span>New messages and alerts will appear here.</span>
      </div>
    {:else}
      {#if systemNotifications.length > 0}
        <div class="notification-section-title">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="16" x2="12" y2="12"/>
            <line x1="12" y1="8" x2="12.01" y2="8"/>
          </svg>
          System
        </div>
        {#each systemNotifications as item}
          {@const isPunishment = isPunishmentNotification(item)}
          <button class="notification-item" on:click={() => openSystemNotification(item)}>
            <div class="notification-icon" class:system-icon={!isPunishment} class:punishment-icon={isPunishment}>
              {#if isPunishment}
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
                  <line x1="12" y1="9" x2="12" y2="13"/>
                  <line x1="12" y1="17" x2="12.01" y2="17"/>
                </svg>
              {:else}
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10"/>
                  <line x1="12" y1="16" x2="12" y2="12"/>
                  <line x1="12" y1="8" x2="12.01" y2="8"/>
                </svg>
              {/if}
            </div>
            <div class="notification-body">
              <div class="notification-row">
                <strong class:punishment-title={isPunishment}>{isPunishment ? 'Punishment' : item.title}</strong>
                <span class="notification-time">{formatWhen(item.createdAt)}</span>
              </div>
              <p>{item.message}</p>
            </div>
          </button>
        {/each}
      {/if}
      {#if messageNotifications.length > 0}
        <div class="notification-section-title">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          </svg>
          Messages
        </div>
        {#each messageNotifications as item}
          <button class="notification-item" on:click={() => openConversation(item.userId)}>
            <div class="notification-icon message-icon">
              <span>{item.fullName?.charAt(0) || 'M'}</span>
            </div>
            <div class="notification-body">
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
            </div>
          </button>
        {/each}
      {/if}
    {/if}
  </div>
</aside>


<style>
  .header {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    height: var(--header-height);
    background: rgba(255, 255, 255, 0.98);
    backdrop-filter: blur(12px);
    -webkit-backdrop-filter: blur(12px);
    border-bottom: 1px solid var(--border-light);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 1.5rem;
    z-index: 110;
    transition: left 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }

  .header-copy {
    display: flex;
    align-items: center;
    flex: 1;
    min-width: 0;
  }

  .title-row {
    display: flex;
    align-items: center;
    gap: 0.4rem;
    min-width: 0;
  }

  .brand-link {
    display: flex;
    align-items: center;
    text-decoration: none;
    margin-left: 0.4rem;
    flex-shrink: 0;
    transition: opacity var(--transition-fast);
  }

  .brand-link:hover {
    opacity: 0.8;
    text-decoration: none;
  }

  .brand-logo {
    height: 36px;
    width: auto;
    display: block;
    object-fit: contain;
  }

  .divider {
    margin: 0 0.35rem;
    color: var(--border-light);
    font-size: 1rem;
    font-weight: 300;
  }

  .header-title {
    font-size: 0.9rem;
    font-weight: 600;
    color: var(--text-secondary);
    margin: 0;
    padding: 0;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    flex-shrink: 0;
  }

  .icon-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    border-radius: var(--radius-sm);
    background: transparent;
    color: var(--text-secondary);
    border: 1px solid transparent;
    transition: all var(--transition-fast);
    cursor: pointer;
  }

  .icon-btn:hover {
    background: var(--primary-50);
    color: var(--primary);
  }

  .collapse-toggle svg {
    transition: transform 0.24s cubic-bezier(0.4, 0, 0.2, 1);
  }

  .collapse-toggle.is-collapsed svg {
    transform: scaleX(-1);
  }

  .notification-trigger {
    position: relative;
  }

  .notification-trigger.active {
    background: var(--primary-50);
    color: var(--primary);
  }

  .notification-badge {
    position: absolute;
    top: -3px;
    right: -3px;
    min-width: 17px;
    height: 17px;
    padding: 0 0.3rem;
    border-radius: 99px;
    background: var(--primary);
    color: white;
    font-size: 0.6rem;
    font-weight: 700;
    display: flex;
    align-items: center;
    justify-content: center;
    font-family: var(--font-ui);
    box-shadow: 0 1px 3px rgba(79, 124, 219, 0.3);
  }

  /* ─── Notification Backdrop (click-outside-to-close) ─── */
  .notification-backdrop {
    position: fixed;
    inset: 0;
    background: rgba(15, 23, 42, 0.18);
    backdrop-filter: blur(2px);
    -webkit-backdrop-filter: blur(2px);
    z-index: 199;
    animation: fadeIn 0.2s ease;
  }

  @keyframes fadeIn {
    from { opacity: 0; }
    to { opacity: 1; }
  }

  /* ─── Slide-in Drawer (macOS-style) ─── */
  .notification-drawer {
    position: fixed;
    top: 0;
    right: 0;
    bottom: 0;
    width: 380px;
    max-width: 92vw;
    background: var(--bg-main, #fff);
    border-left: 1px solid var(--border-light, #E2E8F0);
    box-shadow: -8px 0 40px rgba(0, 0, 0, 0.08);
    z-index: 200;
    display: flex;
    flex-direction: column;
    transform: translateX(100%);
    transition: transform 0.32s cubic-bezier(0.32, 0.72, 0, 1);
    will-change: transform;
  }

  .notification-drawer.open {
    transform: translateX(0);
  }

  .drawer-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 1.15rem 1.25rem;
    border-bottom: 1px solid var(--border-light, #E2E8F0);
    background: linear-gradient(135deg, var(--primary-50, #EEF2FB) 0%, var(--bg-main, #fff) 100%);
    flex-shrink: 0;
  }

  .drawer-title {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-size: 0.95rem;
    font-weight: 700;
    color: var(--text-main, #1E293B);
    margin: 0;
    font-family: var(--font-ui, 'Inter', sans-serif);
  }

  .drawer-title svg {
    color: var(--primary, #4F7CDB);
  }

  .drawer-header-actions {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }

  .unread-badge {
    font-size: 0.68rem;
    color: var(--text-muted, #94A3B8);
    font-weight: 500;
    font-family: var(--font-ui);
  }

  .drawer-clear-btn {
    display: flex;
    align-items: center;
    gap: 0.3rem;
    padding: 0.3rem 0.6rem;
    border-radius: var(--radius-sm, 8px);
    background: transparent;
    border: 1px solid var(--border-light, #E2E8F0);
    color: var(--text-muted, #94A3B8);
    font-size: 0.72rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.15s ease;
    font-family: var(--font-ui);
  }

  .drawer-clear-btn:hover {
    background: rgba(239, 68, 68, 0.08);
    border-color: rgba(239, 68, 68, 0.3);
    color: var(--danger, #EF4444);
  }

  .drawer-close-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    border-radius: var(--radius-sm, 8px);
    background: transparent;
    border: none;
    color: var(--text-muted, #94A3B8);
    cursor: pointer;
    transition: all 0.15s ease;
  }

  .drawer-close-btn:hover {
    background: var(--bg-alt, #F8FAFC);
    color: var(--text-main, #1E293B);
  }

  .drawer-scroll {
    flex: 1;
    overflow-y: auto;
    padding: 0.5rem;
  }

  .drawer-scroll::-webkit-scrollbar {
    width: 5px;
  }
  .drawer-scroll::-webkit-scrollbar-track {
    background: transparent;
  }
  .drawer-scroll::-webkit-scrollbar-thumb {
    background: var(--border-light, #E2E8F0);
    border-radius: 99px;
  }

  /* ─── Notification Items ─── */
  .notification-section-title {
    display: flex;
    align-items: center;
    gap: 0.4rem;
    font-size: 0.65rem;
    text-transform: uppercase;
    letter-spacing: 0.1em;
    color: var(--text-muted);
    margin: 0.8rem 0.6rem 0.5rem;
    font-family: var(--font-ui);
    font-weight: 600;
  }

  .notification-section-title svg {
    opacity: 0.6;
  }

  .notification-item {
    display: flex;
    align-items: flex-start;
    gap: 0.75rem;
    width: 100%;
    text-align: left;
    padding: 0.75rem;
    border-radius: var(--radius-sm, 8px);
    background: transparent;
    border: 1px solid transparent;
    margin-bottom: 0.2rem;
    cursor: pointer;
    font-family: var(--font-ui);
    transition: background 0.15s ease, border-color 0.15s ease;
  }

  .notification-item:hover {
    background: var(--primary-50, #EEF2FB);
    border-color: var(--border-light, #E2E8F0);
  }

  .notification-icon {
    width: 34px;
    height: 34px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    font-size: 0.78rem;
    font-weight: 700;
    font-family: var(--font-ui);
  }

  .system-icon {
    background: rgba(245, 158, 11, 0.12);
    color: var(--warning, #F59E0B);
  }

  .punishment-icon {
    background: rgba(239, 68, 68, 0.1);
    color: var(--danger, #EF4444);
  }

  .punishment-title {
    color: var(--danger, #EF4444);
  }

  .message-icon {
    background: var(--primary-50, #EEF2FB);
    color: var(--primary, #4F7CDB);
  }

  .notification-body {
    flex: 1;
    min-width: 0;
  }

  .notification-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 0.5rem;
    margin-bottom: 0.2rem;
    font-weight: 500;
    font-size: 0.8rem;
    color: var(--text-main);
  }

  .notification-item p {
    color: var(--text-muted);
    font-size: 0.75rem;
    margin: 0;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .notification-time {
    color: var(--text-muted);
    font-size: 0.68rem;
    font-weight: 400;
    white-space: nowrap;
  }

  .notification-pill {
    min-width: 20px;
    height: 20px;
    padding: 0 0.35rem;
    border-radius: 99px;
    background: var(--primary);
    color: white;
    font-size: 0.65rem;
    font-weight: 700;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 1px 3px rgba(79, 124, 219, 0.3);
  }

  .notification-empty {
    color: var(--text-muted);
    font-size: 0.8rem;
    padding: 2rem 1rem;
    text-align: center;
    font-family: var(--font-ui);
    line-height: 1.5;
  }

  .notification-empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.5rem;
    padding: 3rem 1rem;
    text-align: center;
    color: var(--text-muted, #94A3B8);
    font-family: var(--font-ui);
  }

  .notification-empty-state svg {
    opacity: 0.3;
    margin-bottom: 0.5rem;
  }

  .notification-empty-state p {
    font-size: 0.9rem;
    font-weight: 600;
    color: var(--text-secondary, #475569);
    margin: 0;
  }

  .notification-empty-state span {
    font-size: 0.78rem;
    color: var(--text-muted, #94A3B8);
  }

  /* Responsive breakpoints */
  @media (max-width: 1024px) {
    .header { padding: 0 1rem; }
  }

  @media (max-width: 768px) {
    .header { padding: 0 0.75rem; }
    .notification-drawer { width: 340px; }
    .header-title { max-width: 120px; }
    .brand-logo { height: 30px; }
  }

  @media (max-width: 480px) {
    .header { padding: 0 0.5rem; }
    .notification-drawer { width: 100vw; max-width: 100vw; }
    .brand-logo { height: 28px; }
    .divider, .header-title { display: none; }
  }

  /* Suppress sidebar/header transitions on initial page load */
  :global([data-no-sidebar-transition] .sidebar),
  :global([data-no-sidebar-transition] .main) {
    transition: none !important;
  }

</style>

