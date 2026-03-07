<script>
  import UploadForm from './UploadForm.svelte';
  import MaterialsList from './MaterialsList.svelte';

  export let isLoggedIn = false;
  export let userId = null;
  export let userRole = '';

  let refreshTrigger = 0;

  function handleUploadSuccess() {
    refreshTrigger += 1;
  }
</script>

<div class="study-materials-wrapper">
  <section class="upload-area">
    <UploadForm
      {isLoggedIn}
      {userId}
      {userRole}
      onUploadSuccess={handleUploadSuccess}
    />
  </section>

  <section class="list-area">
    <MaterialsList
      {isLoggedIn}
      {userId}
      {userRole}
      {refreshTrigger}
    />
  </section>
</div>

<style>
  .study-materials-wrapper {
    display: flex;
    flex-direction: column;
    gap: 32px;
  }
</style>
