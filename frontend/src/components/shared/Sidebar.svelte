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
    background:
      radial-gradient(circle at top left, rgba(96, 165, 250, 0.18), transparent 10rem),
      linear-gradient(180deg, #0f2440 0%, #163559 52%, #1f4d7f 100%);
    color: white;
    display: flex;
    flex-direction: column;
    z-index: 100;
    box-shadow: 24px 0 60px rgba(15, 23, 42, 0.2);
  }

  .brand {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 1.35rem 1.5rem;
    border-bottom: 1px solid rgba(255,255,255,0.08);
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
    padding: 1rem 0.8rem;
    overflow-y: auto;
  }

  .nav-item {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 0.82rem 1rem;
    margin-bottom: 0.35rem;
    border-radius: 16px;
    color: rgba(255,255,255,0.7);
    font-size: 0.875rem;
    font-weight: 500;
    transition: all 0.15s;
    position: relative;
  }

  .nav-item:hover {
    color: white;
    background: rgba(255,255,255,0.09);
  }

  .nav-item.active {
    color: white;
    background: rgba(255,255,255,0.14);
    box-shadow: inset 0 0 0 1px rgba(255,255,255,0.08);
  }

  .nav-item.active::before {
    content: '';
    position: absolute;
    left: 0.35rem;
    top: 0.45rem;
    bottom: 0.45rem;
    width: 3px;
    background: linear-gradient(180deg, #7dd3fc, white);
    border-radius: 999px;
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
