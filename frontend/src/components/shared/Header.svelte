<script>
  import { logout, getCurrentUser } from '../../lib/api.js';

  export let title = '';

  let user = getCurrentUser();
  let showDropdown = false;

  function toggleDropdown() {
    showDropdown = !showDropdown;
  }

  function handleLogout() {
    logout();
  }
</script>

<header class="header">
  <h1 class="header-title">{title}</h1>

  <div class="header-actions">
    <button class="icon-btn" title="Notifications">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
        <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
      </svg>
    </button>

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
          <a href="#" class="dropdown-item" on:click|preventDefault={handleLogout}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
              <polyline points="16 17 21 12 16 7"/>
              <line x1="21" y1="12" x2="9" y2="12"/>
            </svg>
            Logout
          </a>
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
