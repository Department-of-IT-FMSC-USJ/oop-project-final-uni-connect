<script>
  import PointsSummary from './PointsSummary.svelte';
  import PointsHistory from './PointsHistory.svelte';

  export let userId = 1;
  export let userRole = 'STUDENT';
</script>

<div class="page-container">
  <PointsSummary {userId} {userRole} />
  <PointsHistory {userId} {userRole} />
</div>

<style>
  .page-container {
    display: flex;
    flex-direction: column;
    gap: 32px;
  }
</style>
