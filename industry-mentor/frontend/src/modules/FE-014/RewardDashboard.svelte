<script>
  const API_BASE = 'http://localhost:9092';

  let summary = null;
  let loading = true;
  let errorMessage = '';

  loadRewards();

  async function loadRewards() {
    loading = true;
    errorMessage = '';
    try {
      const response = await fetch(`${API_BASE}/api/rewards/mentor/1`, {
        headers: { 'X-User-Role': 'MENTOR' }
      });
      const result = await response.json();
      if (response.ok && result.success) {
        summary = result.data;
      } else {
        errorMessage = result.message || 'Failed to load reward points.';
      }
    } catch (err) {
      errorMessage = 'Network error. Could not load reward points.';
    } finally {
      loading = false;
    }
  }

  function formatDate(d) {
    if (!d) return '—';
    return new Date(d).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
  }

  function formatType(t) {
    return t === 'SESSION' ? 'Session' : 'Curriculum Suggestion';
  }
</script>

<div class="dashboard">
  {#if loading}
    <div class="loading-state"><span class="spinner"></span><p>Loading reward points...</p></div>
  {:else if errorMessage}
    <div class="alert alert-error"><span>{errorMessage}</span></div>
  {:else if summary}
    <div class="stats-grid">
      <div class="stat-card stat-total">
        <span class="stat-icon">🏆</span>
        <div class="stat-info">
          <span class="stat-value">{summary.totalRewardPoints}</span>
          <span class="stat-label">Total Points</span>
        </div>
      </div>

      <div class="stat-card stat-sessions">
        <span class="stat-icon">📅</span>
        <div class="stat-info">
          <span class="stat-value">{summary.sessionPoints}</span>
          <span class="stat-label">Session Points</span>
        </div>
      </div>

      <div class="stat-card stat-suggestions">
        <span class="stat-icon">💡</span>
        <div class="stat-info">
          <span class="stat-value">{summary.suggestionPoints}</span>
          <span class="stat-label">Suggestion Points</span>
        </div>
      </div>
    </div>

    <div class="table-section">
      <div class="section-header">
        <h2>Contribution History</h2>
        <span class="badge">{summary.contributionHistory ? summary.contributionHistory.length : 0}</span>
      </div>

      {#if !summary.contributionHistory || summary.contributionHistory.length === 0}
        <div class="empty-state"><p>No contribution history yet.</p></div>
      {:else}
        <div class="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Type</th>
                <th>Points Awarded</th>
                <th>Running Total</th>
                <th>Date</th>
              </tr>
            </thead>
            <tbody>
              {#each summary.contributionHistory as entry}
                <tr>
                  <td><span class="type-badge {entry.contributionType === 'SESSION' ? 'type-session' : 'type-suggestion'}">{formatType(entry.contributionType)}</span></td>
                  <td class="td-points">+{entry.pointsAwarded}</td>
                  <td>{entry.updatedTotalPoints}</td>
                  <td class="td-date">{formatDate(entry.updateDate)}</td>
                </tr>
              {/each}
            </tbody>
          </table>
        </div>
      {/if}
    </div>
  {/if}
</div>

<style>
  .dashboard { display: flex; flex-direction: column; gap: 24px; }

  .stats-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 16px; }

  .stat-card {
    background: #fff; border-radius: 14px; padding: 24px;
    box-shadow: 0 2px 12px rgba(0,0,0,0.06); border: 1px solid #e8ecf1;
    display: flex; align-items: center; gap: 16px;
  }

  .stat-icon { font-size: 2rem; }
  .stat-info { display: flex; flex-direction: column; }
  .stat-value { font-size: 2rem; font-weight: 800; color: #1a1a2e; }
  .stat-label { font-size: 0.82rem; font-weight: 500; color: #888; }

  .stat-total { border-left: 4px solid #4361ee; }
  .stat-sessions { border-left: 4px solid #0ea5e9; }
  .stat-suggestions { border-left: 4px solid #f59e0b; }

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
  .td-points { font-weight: 700; color: #059669; }
  .td-date { color: #888; white-space: nowrap; }
  .type-badge { padding: 4px 12px; border-radius: 20px; font-size: 0.78rem; font-weight: 600; }
  .type-session { background: #e0f2fe; color: #0277bd; }
  .type-suggestion { background: #fff3cd; color: #856404; }
</style>
