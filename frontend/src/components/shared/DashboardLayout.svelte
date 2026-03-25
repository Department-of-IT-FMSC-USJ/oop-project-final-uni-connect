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
    padding: 2rem 2.5rem;
    min-height: calc(100vh - var(--header-height));
    background: var(--bg-alt);
    position: relative;
    transition: margin-left 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }

  .content-shell {
    display: grid;
    gap: 1.5rem;
    max-width: var(--content-max-width, 1000px);
    width: 100%;
    margin: 0 auto;
  }

  @media (max-width: 1280px) {
    .main {
      padding: 1.5rem 1.5rem;
    }
  }

  @media (max-width: 900px) {
    .main {
      padding: 1rem;
      margin-left: 0;
    }

    .content-shell {
      gap: 1rem;
    }
  }

  @media (max-width: 600px) {
    .main {
      padding: 0.75rem;
    }

    .content-shell {
      gap: 0.75rem;
    }
  }

  :global(body.sidebar-collapsed) .main {
    margin-left: 0;
  }
</style>
