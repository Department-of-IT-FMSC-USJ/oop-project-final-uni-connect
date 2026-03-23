<script>
  import Sidebar from './Sidebar.svelte';
  import Header from './Header.svelte';

  export let navItems = [];
  export let activeItem = '';
  export let pageTitle = '';
</script>

<div class="dashboard">
  <Sidebar items={navItems} {activeItem} />
  <Header title={pageTitle} />
  <main class="main">
    <slot />
  </main>
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
  }
</style>
