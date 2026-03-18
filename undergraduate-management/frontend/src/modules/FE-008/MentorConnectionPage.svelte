<script>
  import ConnectionRequestForm from './ConnectionRequestForm.svelte';
  import ConnectionsList from './ConnectionsList.svelte';

  export let userId = 1;
  export let userRole = 'STUDENT';

  let refreshTrigger = 0;

  function handleConnectionSuccess() {
    refreshTrigger++;
  }
</script>

<div class="page-container">
  <ConnectionRequestForm
    {userId}
    {userRole}
    onConnectionSuccess={handleConnectionSuccess}
  />

  <ConnectionsList
    {userId}
    {userRole}
    {refreshTrigger}
  />
</div>

<style>
  .page-container {
    display: flex;
    flex-direction: column;
    gap: 32px;
  }
</style>
