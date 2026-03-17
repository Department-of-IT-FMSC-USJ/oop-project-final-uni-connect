<script>
  import SessionCreationForm from './SessionCreationForm.svelte';
  import SessionList from './SessionList.svelte';

  export let mentorId = 1;

  let refreshTrigger = 0;

  function handleSessionCreated() {
    refreshTrigger++;
  }
</script>

<div class="page-container">
  <SessionCreationForm
    {mentorId}
    onSessionCreated={handleSessionCreated}
  />

  <SessionList
    {mentorId}
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
