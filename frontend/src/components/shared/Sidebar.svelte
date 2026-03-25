<script>
  import { getCurrentUser, logout } from '../../lib/api.js';

  export let items = [];
  export let activeItem = '';
  export let brandName = 'UniConnect';

  const user = getCurrentUser();

  function formatRole(role) {
    return (role || 'USER')
      .split('_')
      .map((part) => part.charAt(0) + part.slice(1).toLowerCase())
      .join(' ');
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
        return '/';
    }
  }
</script>

<aside class="sidebar">

  <nav class="nav">
    {#each items as item}
      <a
        href={item.href}
        class="nav-item"
        class:active={activeItem === item.id}
        title={item.label}
        aria-label={item.label}
      >
        <span class="nav-icon">{@html item.icon}</span>
        <span class="nav-label">{item.label}</span>
        {#if item.badge}
          <span class="nav-badge">{item.badge}</span>
        {/if}
      </a>
    {/each}
  </nav>

  <div class="sidebar-footer">
    <div class="account-card">
      <div class="account-summary">
        <div class="account-avatar">
          {#if user?.profilePicture}
            <img src={user.profilePicture} alt="" />
          {:else}
            <span>{user?.fullName?.charAt(0) || 'U'}</span>
          {/if}
        </div>
        <div class="account-copy">
          <strong>{user?.fullName || brandName}</strong>
          <span>{formatRole(user?.role)}</span>
        </div>
      </div>

      <div class="account-actions">
        <a class="account-btn" href={getProfilePath()} aria-label="Open profile">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
            <circle cx="12" cy="7" r="4"/>
          </svg>
          <span>Profile</span>
        </a>
        <button type="button" class="account-btn logout-btn" on:click={logout} aria-label="Logout">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
            <polyline points="16 17 21 12 16 7"/>
            <line x1="21" y1="12" x2="9" y2="12"/>
          </svg>
          <span>Logout</span>
        </button>
      </div>
    </div>
  </div>
</aside>

<style>
  .sidebar {
    position: fixed;
    left: 0;
    top: var(--header-height);
    bottom: 0;
    width: var(--sidebar-width);
    background: var(--bg-main);
    color: var(--text-main);
    border-right: 1px solid var(--border-light);
    display: flex;
    flex-direction: column;
    z-index: 100;
    overflow: hidden;
    transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }

  .nav {
    flex: 1;
    padding: 1rem 0.6rem;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 0.15rem;
  }

  .nav-item {
    display: flex;
    align-items: center;
    width: 100%;
    gap: 0.6rem;
    padding: 0.5rem 0.8rem;
    border-radius: var(--radius-sm);
    color: var(--text-secondary);
    font-family: var(--font-ui);
    font-size: 0.8rem;
    font-weight: 400;
    text-decoration: none;
    transition: background 0.15s ease, color 0.15s ease;
  }

  .nav-item:hover {
    background: var(--bg-alt);
    color: var(--text-main);
    text-decoration: none;
  }

  .nav-item.active {
    background: var(--primary-50);
    color: var(--primary);
    font-weight: 500;
  }

  .nav-icon {
    display: flex;
    width: 18px;
    height: 18px;
    flex-shrink: 0;
    color: var(--text-muted);
    transition: color 0.15s ease;
  }

  .nav-item:hover .nav-icon {
    color: var(--text-secondary);
  }

  .nav-item.active .nav-icon {
    color: var(--primary);
  }

  .nav-icon :global(svg) {
    width: 18px;
    height: 18px;
    stroke-width: 1.5;
  }

  .nav-badge {
    margin-left: auto;
    background: var(--primary-50);
    color: var(--primary);
    font-size: 0.65rem;
    padding: 0.1rem 0.4rem;
    border-radius: 12px;
    font-family: var(--font-ui);
    font-weight: 600;
  }

  .nav-label {
    white-space: nowrap;
    opacity: 1;
    max-width: 10rem;
    overflow: hidden;
    transition: opacity 0.2s ease;
  }

  .sidebar-footer {
    padding: 0.75rem 0.6rem 0.9rem;
    border-top: 1px solid var(--border-light);
    background: linear-gradient(180deg, rgba(255, 255, 255, 0), var(--bg-main));
    transition: opacity 0.2s ease;
  }

  .account-card {
    display: grid;
    gap: 0.75rem;
    padding: 0.8rem;
    border-radius: var(--radius);
    border: 1px solid var(--border-light);
    background: linear-gradient(135deg, var(--bg-main), var(--primary-50));
  }

  .account-summary {
    display: flex;
    align-items: center;
    gap: 0.7rem;
    min-width: 0;
  }

  .account-avatar {
    width: 2.3rem;
    height: 2.3rem;
    border-radius: 50%;
    background: var(--primary-50);
    color: var(--primary);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 0.9rem;
    font-weight: 700;
    overflow: hidden;
    flex-shrink: 0;
    box-shadow: 0 0 0 1px var(--border-light);
  }

  .account-avatar img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .account-copy {
    display: grid;
    min-width: 0;
    font-family: var(--font-ui);
  }

  .account-copy strong {
    font-size: 0.78rem;
    color: var(--text-main);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .account-copy span {
    font-size: 0.68rem;
    color: var(--text-muted);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .account-actions {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 0.5rem;
  }

  .account-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 0.45rem;
    padding: 0.55rem 0.65rem;
    border-radius: var(--radius-sm);
    border: 1px solid var(--border-light);
    background: rgba(255, 255, 255, 0.8);
    color: var(--text-secondary);
    font-size: 0.75rem;
    font-weight: 600;
    font-family: var(--font-ui);
    text-decoration: none;
    cursor: pointer;
    transition: all 0.15s ease;
  }

  .account-btn:hover {
    background: var(--primary-50);
    color: var(--primary);
    border-color: var(--primary-light);
    text-decoration: none;
  }

  .account-btn svg {
    width: 14px;
    height: 14px;
    flex-shrink: 0;
  }

  .logout-btn:hover {
    background: rgba(239, 68, 68, 0.08);
    color: var(--danger);
    border-color: rgba(239, 68, 68, 0.24);
  }

  :global(body.sidebar-collapsed) .sidebar {
    width: 0;
    border-right: none;
  }

  :global(body.sidebar-collapsed) .sidebar-footer {
    opacity: 0;
    pointer-events: none;
  }
</style>
