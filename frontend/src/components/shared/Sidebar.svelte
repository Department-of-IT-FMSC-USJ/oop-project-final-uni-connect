<script>
  import { getCurrentUser, logout } from '../../lib/api.js';
  import ConfirmDialog from './ConfirmDialog.svelte';

  export let items = [];
  export let activeItem = '';

  const user = getCurrentUser();
  let showLogoutConfirm = false;
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

    <div class="nav-bottom">
      <button
        type="button"
        class="nav-item nav-logout"
        on:click={() => showLogoutConfirm = true}
        aria-label="Logout"
      >
        <span class="nav-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
            <polyline points="16 17 21 12 16 7"/>
            <line x1="21" y1="12" x2="9" y2="12"/>
          </svg>
        </span>
        <span class="nav-label">Logout</span>
      </button>
    </div>
  </nav>
</aside>

<ConfirmDialog
  open={showLogoutConfirm}
  title="Sign out?"
  message="Are you sure you want to log out of UniConnect?"
  confirmLabel="Sign Out"
  tone="danger"
  on:cancel={() => showLogoutConfirm = false}
  on:confirm={logout}
/>

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

  .nav-item:hover .nav-icon { color: var(--text-secondary); }
  .nav-item.active .nav-icon { color: var(--primary); }

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

  /* Logout sits at the bottom of the nav */
  .nav-bottom {
    margin-top: auto;
    padding-top: 0.5rem;
    border-top: 1px solid var(--border-light);
  }

  .nav-logout {
    background: none;
    border: none;
    cursor: pointer;
    text-align: left;
  }

  .nav-logout:hover {
    background: rgba(239, 68, 68, 0.06) !important;
    color: var(--danger) !important;
  }

  .nav-logout:hover .nav-icon {
    color: var(--danger) !important;
  }

  :global(body.sidebar-collapsed) .sidebar {
    width: 0;
    border-right: none;
  }
</style>
