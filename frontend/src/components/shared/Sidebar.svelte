<script>
  export let items = [];
  export let activeItem = '';
  export let brandName = 'UniConnect';
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
    padding: 1.25rem 0.75rem;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
  }

  .nav-item {
    display: flex;
    align-items: center;
    width: 100%;
    gap: 0.75rem;
    padding: 0.65rem 1rem;
    border-radius: var(--radius-sm);
    color: var(--text-secondary);
    font-family: var(--font-ui);
    font-size: 0.875rem;
    font-weight: 400;
    text-decoration: none;
    border-left: 3px solid transparent;
    transition: background 0.15s ease, color 0.15s ease, border-color 0.15s ease;
  }

  .nav-item:hover {
    background: var(--bg-alt);
    color: var(--text-main);
  }

  .nav-item.active {
    background: var(--primary-50);
    color: var(--primary);
    font-weight: 500;
    border-left: 3px solid var(--primary);
  }

  .nav-icon {
    display: flex;
    width: 20px;
    height: 20px;
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
    width: 20px;
    height: 20px;
    stroke-width: 1.5;
  }

  .nav-badge {
    margin-left: auto;
    background: var(--primary-50);
    color: var(--primary);
    font-size: 0.7rem;
    padding: 0.15rem 0.5rem;
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

  :global(body.sidebar-collapsed) .sidebar {
    width: 0;
    border-right: none;
  }
</style>
