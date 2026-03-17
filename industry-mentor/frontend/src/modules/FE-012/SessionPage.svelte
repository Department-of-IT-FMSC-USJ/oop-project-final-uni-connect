<script>
  import SessionCreationForm from './SessionCreationForm.svelte';
  import SessionList from './SessionList.svelte';

  let refreshTrigger = 0;

  function handleSessionCreated() {
    refreshTrigger += 1;
  }
</script>

<div class="page-container">
  <SessionCreationForm onSessionCreated={handleSessionCreated} />
  <SessionList bind:refreshTrigger />
</div>

<style>
  .page-container { display: flex; flex-direction: column; gap: 32px; }
</style>
