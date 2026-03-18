<script>
  import SubmitParticipationForm from './SubmitParticipationForm.svelte';
  import ParticipationList from './ParticipationList.svelte';

  export let userId = 1;
  export let userRole = 'STUDENT';

  let refreshTrigger = 0;

  function handleUploadSuccess() {
    refreshTrigger++;
  }
</script>

<div class="page-container">
  <SubmitParticipationForm
    {userId}
    {userRole}
    onUploadSuccess={handleUploadSuccess}
  />

  <ParticipationList
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
