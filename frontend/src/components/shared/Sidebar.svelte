<script>
  export let items = [];
  export let activeItem = '';
  export let brandName = 'UniConnect';
</script>

<aside class="sidebar">
  <div class="brand">
    <svg class="brand-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
      <path d="M12 14l9-5-9-5-9 5 9 5z"/>
      <path d="M12 14l6.16-3.422a12.083 12.083 0 01.665 6.479A11.952 11.952 0 0012 20.055a11.952 11.952 0 00-6.824-2.998 12.078 12.078 0 01.665-6.479L12 14z"/>
    </svg>
    <span class="brand-text">{brandName}</span>
  </div>

  <nav class="nav">
    {#each items as item}
      <a
        href={item.href}
        class="nav-item"
        class:active={activeItem === item.id}
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
    top: 0;
    bottom: 0;
    width: var(--sidebar-width);
    background: var(--primary);
    color: white;
    display: flex;
    flex-direction: column;
    z-index: 100;
  }

  .brand {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 1.25rem 1.5rem;
    border-bottom: 1px solid rgba(255,255,255,0.1);
  }

  .brand-icon {
    width: 28px;
    height: 28px;
    flex-shrink: 0;
  }

  .brand-text {
    font-size: 1.125rem;
    font-weight: 700;
    letter-spacing: -0.02em;
  }

  .nav {
    flex: 1;
    padding: 0.75rem 0;
    overflow-y: auto;
  }

  .nav-item {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 0.75rem 1.5rem;
    color: rgba(255,255,255,0.7);
    font-size: 0.875rem;
    font-weight: 500;
    transition: all 0.15s;
    position: relative;
  }

  .nav-item:hover {
    color: white;
    background: rgba(255,255,255,0.1);
  }

  .nav-item.active {
    color: white;
    background: rgba(255,255,255,0.15);
  }

  .nav-item.active::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 3px;
    background: white;
    border-radius: 0 2px 2px 0;
  }

  .nav-icon {
    display: flex;
    width: 20px;
    height: 20px;
    flex-shrink: 0;
  }

  .nav-icon :global(svg) {
    width: 20px;
    height: 20px;
  }

  .nav-badge {
    margin-left: auto;
    background: var(--danger);
    color: white;
    font-size: 0.7rem;
    padding: 0.125rem 0.375rem;
    border-radius: 9999px;
    font-weight: 600;
    min-width: 18px;
    text-align: center;
  }

  .nav-label {
    white-space: nowrap;
  }
</style>
