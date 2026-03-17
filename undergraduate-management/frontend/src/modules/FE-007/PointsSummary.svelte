<script>
  const API_BASE = 'http://localhost:8080';

  export let userId = 1;
  export let userRole = 'STUDENT';

  let totalPoints = 0;
  let loading = true;
  let errorMessage = '';

  async function fetchTotalPoints() {
    loading = true;
    errorMessage = '';
    try {
      const response = await fetch(`${API_BASE}/api/points/student/${userId}/total`, {
        headers: {
          'X-User-Id': String(userId),
          'X-User-Role': userRole
        }
      });

      const result = await response.json();

      if (response.ok && result.success) {
        totalPoints = result.data?.totalPoints || 0;
      } else {
        errorMessage = result.message || 'Failed to load total points.';
      }
    } catch (err) {
      errorMessage = 'Network error. Please check your connection.';
    } finally {
      loading = false;
    }
  }

  fetchTotalPoints();
</script>

<div class="summary-section">
  <div class="section-header">
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <circle cx="12" cy="8" r="7"/>
      <polyline points="8.21 13.89 7 23 12 20 17 23 15.79 13.88"/>
    </svg>
    <h2>Points Overview</h2>
  </div>

  {#if loading}
    <div class="loading-state">
      <div class="loading-spinner"></div>
      <p>Loading points...</p>
    </div>
  {:else if errorMessage}
    <div class="error-state">
      <p>{errorMessage}</p>
      <button class="btn-retry" on:click={fetchTotalPoints}>Try Again</button>
    </div>
  {:else}
    <div class="points-cards">
      <div class="points-card total-card">
        <div class="card-icon">🏆</div>
        <div class="card-info">
          <span class="card-label">Total Points</span>
          <span class="card-value">{totalPoints}</span>
        </div>
      </div>
    </div>
  {/if}
</div>

<style>
  .summary-section { background: #ffffff; border-radius: 14px; padding: 32px; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06); border: 1px solid #e8ecf1; }
  .section-header { display: flex; align-items: center; gap: 10px; margin-bottom: 24px; color: #1a1a2e; }
  .section-header svg { color: #4361ee; }
  .section-header h2 { margin: 0; font-size: 1.4rem; font-weight: 700; }

  .loading-state { display: flex; flex-direction: column; align-items: center; padding: 30px 20px; gap: 12px; color: #888; }
  .loading-spinner { width: 36px; height: 36px; border: 3px solid #e8ecf1; border-top-color: #4361ee; border-radius: 50%; animation: spin 0.7s linear infinite; }
  @keyframes spin { to { transform: rotate(360deg); } }

  .error-state { display: flex; flex-direction: column; align-items: center; padding: 30px 20px; gap: 12px; color: #dc3545; text-align: center; }
  .btn-retry { padding: 8px 20px; border: 1.5px solid #dc3545; background: transparent; color: #dc3545; border-radius: 8px; font-weight: 600; cursor: pointer; transition: all 0.2s; }
  .btn-retry:hover { background: #dc3545; color: #fff; }

  .points-cards { display: grid; grid-template-columns: 1fr; gap: 16px; }
  .points-card { display: flex; align-items: center; gap: 16px; padding: 24px; border-radius: 12px; border: 1.5px solid #e8ecf1; background: #fafbfc; }
  .total-card { background: linear-gradient(135deg, #4361ee 0%, #3651d4 100%); border-color: #4361ee; color: #fff; }
  .card-icon { font-size: 2rem; }
  .card-info { display: flex; flex-direction: column; gap: 2px; }
  .card-label { font-size: 0.85rem; font-weight: 500; opacity: 0.9; }
  .card-value { font-size: 2rem; font-weight: 800; line-height: 1.1; }

  @media (max-width: 600px) {
    .summary-section { padding: 20px 16px; }
    .section-header h2 { font-size: 1.15rem; }
  }
</style>
