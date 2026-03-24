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
    position: fixed; left: 0; top: var(--header-height); bottom: 0;
    width: var(--sidebar-width);
    background: var(--bg-main);
    color: var(--text-main);
    border-right: 1px solid var(--border-light);
    display: flex; flex-direction: column;
    z-index: 100; overflow: hidden;
    transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }
  .nav {
    flex: 1; padding: 2.5rem 2rem; overflow-y: auto;
    display: flex; flex-direction: column; gap: 1.5rem;
  }
  .nav-item {
    display: flex; align-items: center; width: 100%; gap: 1.25rem;
    padding: 0; border-radius: 0;
    color: var(--text-secondary); font-family: var(--font-ui);
    font-size: 1.15rem; font-weight: 400; text-decoration: none;
    transition: color 0.15s ease;
  }
  .nav-item:hover { color: var(--text-main); background: transparent; }
  .nav-item.active { color: var(--text-main); background: transparent; font-weight: 600; border-left: none; padding-left: 0; }
  .nav-icon {
    display: flex; width: 26px; height: 26px; flex-shrink: 0;
    opacity: 0.8; transition: opacity 0.15s ease;
  }
  .nav-item:hover .nav-icon, .nav-item.active .nav-icon { opacity: 1; }
  .nav-icon :global(svg) { width: 26px; height: 26px; stroke-width: 1.25; }
  .nav-badge {
    margin-left: auto; background: var(--accent);
    color: white; font-size: 0.7rem;
    padding: 0.2rem 0.5rem; border-radius: 12px; font-family: var(--font-ui); font-weight: 600;
  }
  .nav-label {
    white-space: nowrap; opacity: 1; max-width: 10rem;
    overflow: hidden; transition: opacity 0.2s ease;
  }
  :global(body.sidebar-collapsed) .sidebar { width: 0; border-right: none; }
</style>
