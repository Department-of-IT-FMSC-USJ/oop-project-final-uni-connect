<script>
  const API_BASE = 'http://localhost:8080';

  export let userId = 1;
  export let userRole = 'STUDENT';
  export let refreshTrigger = 0;

  let connections = [];
  let loading = true;
  let errorMessage = '';

  async function fetchConnections() {
    loading = true;
    errorMessage = '';
    try {
      const response = await fetch(`${API_BASE}/api/mentor/connections/${userId}`, {
        headers: { 'X-User-Id': String(userId), 'X-User-Role': userRole }
      });
      const result = await response.json();
      if (response.ok && result.success) {
        connections = result.data || [];
      } else {
        errorMessage = result.message || 'Failed to load connections.';
      }
    } catch (err) {
      errorMessage = 'Network error. Please check your connection.';
    } finally {
      loading = false;
    }
  }

  $: if (refreshTrigger >= 0) { fetchConnections(); }

  function formatDate(dateStr) {
    if (!dateStr) return 'N/A';
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
  }

  function getMentorTypeIcon(type) {
    switch (type) { case 'Academic': return '🎓'; case 'Industry': return '🏢'; default: return '👤'; }
  }

  function getMentorTypeBadgeClass(type) {
    switch (type) { case 'Academic': return 'badge-academic'; case 'Industry': return 'badge-industry'; default: return 'badge-other'; }
  }

  function getStatusBadgeClass(status) {
    switch (status) { case 'Approved': return 'status-approved'; case 'Pending': return 'status-pending'; default: return 'status-pending'; }
  }

  function getStatusIcon(status) {
    switch (status) { case 'Approved': return '✅'; case 'Pending': return '⏳'; default: return '⏳'; }
  }
</script>

<div class="connections-section">
  <div class="section-header">
    <div class="header-left">
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
        <circle cx="9" cy="7" r="4"/>
        <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
        <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
      </svg>
      <h2>Your Connections</h2>
    </div>
    <button class="btn-refresh" on:click={fetchConnections} title="Refresh list">
      <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="23 4 23 10 17 10"/>
        <polyline points="1 20 1 14 7 14"/>
        <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
      </svg>
    </button>
  </div>

  {#if loading}
    <div class="loading-state"><div class="loading-spinner"></div><p>Loading connections...</p></div>
  {:else if errorMessage}
    <div class="error-state">
      <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
      </svg>
      <p>{errorMessage}</p>
      <button class="btn-retry" on:click={fetchConnections}>Try Again</button>
    </div>
  {:else if connections.length === 0}
    <div class="empty-state">
      <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/>
        <path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>
      </svg>
      <p class="empty-title">No Connections Yet</p>
      <p class="empty-subtitle">Request your first mentor connection using the form above!</p>
    </div>
  {:else}
    <div class="connections-grid">
      {#each connections as conn (conn.connectionId)}
        <div class="connection-card">
          <div class="card-header">
            <span class="mentor-type-badge {getMentorTypeBadgeClass(conn.mentorType)}">
              {getMentorTypeIcon(conn.mentorType)} {conn.mentorType} Mentor
            </span>
            <span class="status-badge {getStatusBadgeClass(conn.connectionStatus)}">
              {getStatusIcon(conn.connectionStatus)} {conn.connectionStatus}
            </span>
          </div>
          <div class="card-body">
            <div class="mentor-info">
              <div class="mentor-avatar">
                <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
                </svg>
              </div>
              <div class="mentor-details">
                <span class="mentor-label">Mentor ID</span>
                <span class="mentor-id">#{conn.mentorId}</span>
              </div>
            </div>
          </div>
          <div class="card-footer">
            <div class="date-info">
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
              </svg>
              <span>{formatDate(conn.createdDate)}</span>
            </div>
          </div>
        </div>
      {/each}
    </div>
  {/if}
</div>

<style>
  .connections-section { background: #ffffff; border-radius: 14px; padding: 32px; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06); border: 1px solid #e8ecf1; }
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

  .connections-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 20px; }

  .connection-card { display: flex; flex-direction: column; border: 1.5px solid #e8ecf1; border-radius: 12px; overflow: hidden; background: #fafbfc; transition: transform 0.2s, box-shadow 0.2s; }
  .connection-card:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08); }

  .card-header { padding: 14px 18px 0; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 8px; }

  .mentor-type-badge { display: inline-flex; align-items: center; gap: 4px; padding: 4px 12px; border-radius: 20px; font-size: 0.78rem; font-weight: 600; }
  .badge-academic { background: #e0f2fe; color: #0369a1; }
  .badge-industry { background: #fef3c7; color: #92400e; }
  .badge-other { background: #f1f5f9; color: #475569; }

  .status-badge { display: inline-flex; align-items: center; gap: 4px; padding: 4px 10px; border-radius: 20px; font-size: 0.75rem; font-weight: 600; }
  .status-pending { background: #fff3cd; color: #856404; }
  .status-approved { background: #d4edda; color: #155724; }

  .card-body { padding: 18px; flex: 1; }
  .mentor-info { display: flex; align-items: center; gap: 14px; }
  .mentor-avatar { display: flex; align-items: center; justify-content: center; width: 48px; height: 48px; border-radius: 50%; background: #f0f4ff; color: #4361ee; }
  .mentor-details { display: flex; flex-direction: column; gap: 2px; }
  .mentor-label { font-size: 0.78rem; color: #888; font-weight: 500; }
  .mentor-id { font-size: 1.1rem; font-weight: 700; color: #1a1a2e; }

  .card-footer { display: flex; align-items: center; padding: 12px 18px; border-top: 1px solid #e8ecf1; background: #fff; }
  .date-info { display: flex; align-items: center; gap: 5px; font-size: 0.8rem; color: #888; }

  @media (max-width: 600px) {
    .connections-section { padding: 20px 16px; }
    .connections-grid { grid-template-columns: 1fr; }
    .section-header h2 { font-size: 1.15rem; }
  }
</style>
