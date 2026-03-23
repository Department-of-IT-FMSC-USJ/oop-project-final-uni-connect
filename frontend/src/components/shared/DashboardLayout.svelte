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
    padding: 2rem;
    min-height: calc(100vh - var(--header-height));
    position: relative;
  }

  .content-shell {
    display: grid;
    gap: 1.5rem;
  }

  @media (max-width: 900px) {
    .main {
      padding: 1.25rem;
    }
  }
</style>
