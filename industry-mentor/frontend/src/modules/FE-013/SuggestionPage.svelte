<script>
  import SuggestionForm from './SuggestionForm.svelte';
  import SuggestionList from './SuggestionList.svelte';

  let refreshTrigger = 0;

  function handleSuggestionCreated() {
    refreshTrigger += 1;
  }
</script>

<div class="page-container">
  <SuggestionForm onSuggestionCreated={handleSuggestionCreated} />
  <SuggestionList bind:refreshTrigger />
</div>

<style>
  .page-container { display: flex; flex-direction: column; gap: 32px; }
</style>
