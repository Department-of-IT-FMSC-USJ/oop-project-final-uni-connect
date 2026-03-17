<script>
  const API_BASE = 'http://localhost:9092';

  let suggestions = [];
  let loading = true;
  let errorMessage = '';

  export let refreshTrigger = 0;

  $: if (refreshTrigger >= 0) {
    loadSuggestions();
  }

  async function loadSuggestions() {
    loading = true;
    errorMessage = '';
    try {
      const response = await fetch(`${API_BASE}/api/suggestions/mentor/1`, {
        headers: { 'X-User-Role': 'MENTOR' }
      });
      const result = await response.json();
      if (response.ok && result.success) {
        suggestions = result.data || [];
      } else {
        errorMessage = result.message || 'Failed to load suggestions.';
      }
    } catch (err) {
      errorMessage = 'Network error. Could not load suggestions.';
    } finally {
      loading = false;
    }
  }

  function formatDate(d) {
    if (!d) return '—';
    return new Date(d).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
  }

  function formatCategory(c) {
    if (!c) return '—';
    return c.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase());
  }

  function getStatusClass(s) {
    return s === 'REVIEWED' ? 'status-reviewed' : 'status-pending';
  }
</script>

<div class="table-section">
  <div class="section-header">
    <h2>My Suggestions</h2>
    <span class="badge">{suggestions.length}</span>
  </div>

  {#if loading}
    <div class="loading-state"><span class="spinner"></span><p>Loading suggestions...</p></div>
  {:else if errorMessage}
    <div class="alert alert-error"><span>{errorMessage}</span></div>
  {:else if suggestions.length === 0}
    <div class="empty-state"><p>No suggestions submitted yet.</p></div>
  {:else}
    <div class="table-wrapper">
      <table>
        <thead>
          <tr>
            <th>Title</th>
            <th>Category</th>
            <th>Course</th>
            <th>Status</th>
            <th>Submitted</th>
          </tr>
        </thead>
        <tbody>
          {#each suggestions as sug}
            <tr>
              <td class="td-name">{sug.suggestionTitle}</td>
              <td>{formatCategory(sug.category)}</td>
              <td>{sug.suggestedCourse || '—'}</td>
              <td><span class="status-badge {getStatusClass(sug.reviewStatus)}">{sug.reviewStatus}</span></td>
              <td class="td-date">{formatDate(sug.submissionDate)}</td>
            </tr>
          {/each}
        </tbody>
      </table>
    </div>
  {/if}
</div>

<style>
  .table-section { background: #fff; border-radius: 14px; padding: 32px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); border: 1px solid #e8ecf1; }
  .section-header { display: flex; align-items: center; gap: 10px; margin-bottom: 24px; }
  .section-header h2 { margin: 0; font-size: 1.4rem; font-weight: 700; color: #1a1a2e; }
  .badge { background: #4361ee; color: white; padding: 2px 10px; border-radius: 20px; font-size: 0.8rem; font-weight: 600; }
  .loading-state, .empty-state { text-align: center; padding: 48px 24px; color: #999; }
  .spinner { display: inline-block; width: 24px; height: 24px; border: 3px solid #e8ecf1; border-top-color: #4361ee; border-radius: 50%; animation: spin 0.6s linear infinite; margin-bottom: 12px; }
  @keyframes spin { to { transform: rotate(360deg); } }
  .alert-error { background: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; padding: 14px 18px; border-radius: 10px; font-size: 0.9rem; }
  .table-wrapper { overflow-x: auto; }
  table { width: 100%; border-collapse: collapse; font-size: 0.9rem; }
  thead th { background: #f8f9fb; padding: 12px 16px; text-align: left; font-weight: 600; color: #555; border-bottom: 2px solid #e8ecf1; white-space: nowrap; }
  tbody td { padding: 14px 16px; border-bottom: 1px solid #f0f0f0; vertical-align: middle; }
  tbody tr:hover { background: #f8f9fb; }
  .td-name { font-weight: 600; color: #1a1a2e; }
  .td-date { color: #888; white-space: nowrap; }
  .status-badge { padding: 4px 12px; border-radius: 20px; font-size: 0.78rem; font-weight: 600; }
  .status-pending { background: #fff3cd; color: #856404; }
  .status-reviewed { background: #d4edda; color: #155724; }
</style>
