<script>
  import FeedbackForm from './FeedbackForm.svelte';
  import FeedbackList from './FeedbackList.svelte';

  export let userId = 1;
  export let userRole = 'STUDENT';

  let refreshTrigger = 0;

  function handleFeedbackSuccess() {
    refreshTrigger++;
  }
</script>

<div class="page-container">
  <FeedbackForm
    {userId}
    {userRole}
    onFeedbackSuccess={handleFeedbackSuccess}
  />

  <FeedbackList
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
