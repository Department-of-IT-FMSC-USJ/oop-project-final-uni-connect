<script>
  const API_BASE = 'http://localhost:8080';

  export let userId = 1;
  export let userRole = 'STUDENT';

  let pointsList = [];
  let loading = true;
  let errorMessage = '';

  async function fetchPointsHistory() {
    loading = true;
    errorMessage = '';
    try {
      const response = await fetch(`${API_BASE}/api/points/student/${userId}`, {
        headers: {
          'X-User-Id': String(userId),
          'X-User-Role': userRole
        }
      });

      const result = await response.json();

      if (response.ok && result.success) {
        pointsList = result.data || [];
      } else {
        errorMessage = result.message || 'Failed to load points history.';
      }
    } catch (err) {
      errorMessage = 'Network error. Please check your connection.';
    } finally {
      loading = false;
    }
  }

  fetchPointsHistory();

  function formatDate(dateStr) {
    if (!dateStr) return 'N/A';
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
  }

  function getActivityTypeLabel(type) {
    switch (type) {
      case 'EVENT_PARTICIPATION': return 'Event Participation';
      case 'EVENT_ORGANIZING': return 'Event Organizing';
      case 'EVENT_WINNING': return 'Event Winning';
      case 'COMMUNITY_SERVICE': return 'Community Service';
      case 'ACADEMIC_ACHIEVEMENT': return 'Academic Achievement';
      default: return type;
    }
  }

  function getActivityIcon(type) {
    switch (type) {
      case 'EVENT_PARTICIPATION': return '🎯';
      case 'EVENT_ORGANIZING': return '📋';
      case 'EVENT_WINNING': return '🏆';
      case 'COMMUNITY_SERVICE': return '🤝';
      case 'ACADEMIC_ACHIEVEMENT': return '📚';
      default: return '⭐';
    }
  }

  function getActivityBadgeClass(type) {
    switch (type) {
      case 'EVENT_PARTICIPATION': return 'badge-participation';
      case 'EVENT_ORGANIZING': return 'badge-organizing';
      case 'EVENT_WINNING': return 'badge-winning';
      case 'COMMUNITY_SERVICE': return 'badge-service';
      case 'ACADEMIC_ACHIEVEMENT': return 'badge-academic';
      default: return 'badge-other';
    }
  }
</script>

<div class="history-section">
  <div class="section-header">
    <div class="header-left">
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>
      </svg>
      <h2>Activity History</h2>
    </div>
    <button class="btn-refresh" on:click={fetchPointsHistory} title="Refresh list">
      <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="23 4 23 10 17 10"/>
        <polyline points="1 20 1 14 7 14"/>
        <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
      </svg>
    </button>
  </div>

  {#if loading}
    <div class="loading-state">
      <div class="loading-spinner"></div>
      <p>Loading activity history...</p>
    </div>
  {:else if errorMessage}
    <div class="error-state">
      <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10"/>
        <line x1="12" y1="8" x2="12" y2="12"/>
        <line x1="12" y1="16" x2="12.01" y2="16"/>
      </svg>
      <p>{errorMessage}</p>
      <button class="btn-retry" on:click={fetchPointsHistory}>Try Again</button>
    </div>
  {:else if pointsList.length === 0}
    <div class="empty-state">
      <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>
      </svg>
      <p class="empty-title">No Activity Yet</p>
      <p class="empty-subtitle">Your contribution points will appear here once activities are verified.</p>
    </div>
  {:else}
    <div class="history-table-wrapper">
      <table class="history-table">
        <thead>
          <tr>
            <th>Activity</th>
            <th>Points Earned</th>
            <th>Total After</th>
            <th>Date</th>
          </tr>
        </thead>
        <tbody>
          {#each pointsList as point (point.pointId)}
            <tr>
              <td>
                <span class="activity-badge {getActivityBadgeClass(point.activityType)}">
                  {getActivityIcon(point.activityType)} {getActivityTypeLabel(point.activityType)}
                </span>
              </td>
              <td><span class="points-earned">+{point.pointsEarned}</span></td>
              <td><span class="total-points">{point.updatedTotalPoints}</span></td>
              <td><span class="date-text">{formatDate(point.createdDate)}</span></td>
            </tr>
          {/each}
        </tbody>
      </table>
    </div>
  {/if}
</div>

<style>
  .history-section { background: #ffffff; border-radius: 14px; padding: 32px; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06); border: 1px solid #e8ecf1; }
  .section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px; }
  .header-left { display: flex; align-items: center; gap: 10px; color: #1a1a2e; }
  .header-left h2 { margin: 0; font-size: 1.4rem; font-weight: 700; }
  .header-left svg { color: #4361ee; }

  .btn-refresh { display: inline-flex; align-items: center; justify-content: center; width: 38px; height: 38px; border: 1.5px solid #d1d5db; border-radius: 8px; background: #fafbfc; color: #555; cursor: pointer; transition: all 0.2s; }
  .btn-refresh:hover { border-color: #4361ee; color: #4361ee; background: #f0f4ff; }

  .loading-state { display: flex; flex-direction: column; align-items: center; padding: 40px 20px; gap: 12px; color: #888; }
  .loading-spinner { width: 36px; height: 36px; border: 3px solid #e8ecf1; border-top-color: #4361ee; border-radius: 50%; animation: spin 0.7s linear infinite; }
  @keyframes spin { to { transform: rotate(360deg); } }

  .error-state { display: flex; flex-direction: column; align-items: center; padding: 40px 20px; gap: 12px; color: #dc3545; text-align: center; }
  .btn-retry { padding: 8px 20px; border: 1.5px solid #dc3545; background: transparent; color: #dc3545; border-radius: 8px; font-weight: 600; cursor: pointer; transition: all 0.2s; }
  .btn-retry:hover { background: #dc3545; color: #fff; }

  .empty-state { display: flex; flex-direction: column; align-items: center; padding: 48px 20px; gap: 8px; color: #aaa; text-align: center; }
  .empty-title { font-size: 1.1rem; font-weight: 600; color: #666; margin: 8px 0 0; }
  .empty-subtitle { font-size: 0.9rem; color: #999; margin: 0; }

  .history-table-wrapper { overflow-x: auto; }
  .history-table { width: 100%; border-collapse: collapse; font-size: 0.9rem; }
  .history-table thead th { text-align: left; padding: 12px 16px; font-weight: 600; font-size: 0.82rem; color: #888; text-transform: uppercase; letter-spacing: 0.04em; border-bottom: 2px solid #e8ecf1; }
  .history-table tbody td { padding: 14px 16px; border-bottom: 1px solid #f0f2f5; vertical-align: middle; }
  .history-table tbody tr:hover { background: #f8f9fc; }

  .activity-badge { display: inline-flex; align-items: center; gap: 6px; padding: 4px 12px; border-radius: 20px; font-size: 0.8rem; font-weight: 600; }
  .badge-participation { background: #e0f2fe; color: #0369a1; }
  .badge-organizing { background: #d1fae5; color: #065f46; }
  .badge-winning { background: #fef3c7; color: #92400e; }
  .badge-service { background: #ede9fe; color: #6d28d9; }
  .badge-academic { background: #fce7f3; color: #9d174d; }
  .badge-other { background: #f1f5f9; color: #475569; }

  .points-earned { font-weight: 700; color: #28a745; font-size: 0.95rem; }
  .total-points { font-weight: 600; color: #1a1a2e; }
  .date-text { color: #888; font-size: 0.85rem; }

  @media (max-width: 600px) {
    .history-section { padding: 20px 16px; }
    .section-header h2 { font-size: 1.15rem; }
    .history-table thead th, .history-table tbody td { padding: 10px 10px; font-size: 0.82rem; }
  }
</style>
