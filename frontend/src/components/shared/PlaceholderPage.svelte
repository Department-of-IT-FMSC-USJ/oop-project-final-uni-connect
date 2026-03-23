<script>
  import { onMount } from 'svelte';
  import { getCurrentUser, getRoleDashboardPath } from '../../lib/api.js';
  import DashboardLayout from './DashboardLayout.svelte';

  export let navItems = [];
  export let activeItem = '';
  export let pageTitle = '';
  export let heading = '';
  export let description = '';

  const user = getCurrentUser();

  onMount(() => {
    if (!user) {
      window.location.href = '/';
      return;
    }

    if (user.role !== 'DEPARTMENT_HEAD') {
      window.location.href = getRoleDashboardPath(user.role);
    }
  });
</script>

<DashboardLayout {navItems} {activeItem} {pageTitle}>
  <div class="placeholder-wrap">
    <section class="card placeholder-card">
      <p class="eyebrow">Head of Department</p>
      <h2>{heading || pageTitle}</h2>
      <p class="description">{description}</p>
    </section>
  </div>
</DashboardLayout>

<style>
  .placeholder-wrap {
    max-width: 960px;
  }

  .placeholder-card {
    padding: 2rem;
  }

  .eyebrow {
    font-size: 0.75rem;
    font-weight: 700;
    letter-spacing: 0.08em;
    text-transform: uppercase;
    color: var(--accent);
    margin-bottom: 0.75rem;
  }

  h2 {
    font-size: 1.875rem;
    line-height: 1.2;
    margin-bottom: 0.75rem;
    color: var(--gray-900);
  }

  .description {
    color: var(--gray-600);
    font-size: 1rem;
    max-width: 42rem;
  }
</style>
