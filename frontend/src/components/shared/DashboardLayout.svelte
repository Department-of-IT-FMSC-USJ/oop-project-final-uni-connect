<script>
  import Sidebar from './Sidebar.svelte';
  import Header from './Header.svelte';
  import ToastViewport from './ToastViewport.svelte';

  export let navItems = [];
  export let activeItem = '';
  export let pageTitle = '';
</script>

<div class="dashboard">
  <Sidebar items={navItems} {activeItem} />
  <Header title={pageTitle} />
  <main class="main">
    <div class="content-shell">
      <slot />
    </div>
  </main>
  <ToastViewport />
</div>

<style>
  .dashboard {
    min-height: 100vh;
  }

  .main {
    margin-left: var(--sidebar-width);
    margin-top: var(--header-height);
    padding: 4rem 5rem;
    min-height: calc(100vh - var(--header-height));
    position: relative;
    transition: margin-left 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    animation: slideIn 0.4s ease-out;
  }

  .content-shell {
    display: grid;
    gap: 3.5rem;
    max-width: 1200px;
    width: 100%;
    margin: 0 auto;
  }

  @media (max-width: 1200px) {
    .main {
      padding: 2rem 2.5rem;
    }
  }

  @media (max-width: 900px) {
    .main {
      padding: 1.5rem;
    }

    .content-shell {
      gap: 1.5rem;
    }
  }

  :global(body.sidebar-collapsed) .main {
    margin-left: 0;
  }
</style>
