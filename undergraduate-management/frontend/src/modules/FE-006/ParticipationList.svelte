<script>
  const API_BASE = 'http://localhost:8080';

  export let userId = 1;
  export let userRole = 'STUDENT';
  export let refreshTrigger = 0;

  let participations = [];
  let loading = true;
  let errorMessage = '';

  async function fetchParticipations() {
    loading = true;
    errorMessage = '';
    try {
      const response = await fetch(`${API_BASE}/api/events/participation/student/${userId}`, {
        headers: {
          'X-User-Id': String(userId),
          'X-User-Role': userRole
        }
      });

      const result = await response.json();

      if (response.ok && result.success) {
        participations = result.data || [];
      } else {
        errorMessage = result.message || 'Failed to load participations.';
      }
    } catch (err) {
      errorMessage = 'Network error. Please check your connection.';
    } finally {
      loading = false;
    }
  }

  $: if (refreshTrigger >= 0) {
    fetchParticipations();
  }

  function formatDate(dateStr) {
    if (!dateStr) return 'N/A';
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }

  function getEventTypeIcon(type) {
    switch (type) {
      case 'Competition': return '🏆';
      case 'Workshop': return '🔧';
      case 'Seminar': return '🎤';
      case 'Organizing Committee': return '📋';
      default: return '📌';
    }
  }

  function getEventTypeBadgeClass(type) {
    switch (type) {
      case 'Competition': return 'badge-competition';
      case 'Workshop': return 'badge-workshop';
      case 'Seminar': return 'badge-seminar';
      case 'Organizing Committee': return 'badge-organizing';
      default: return 'badge-other';
    }
  }

  function getStatusBadgeClass(status) {
    switch (status) {
      case 'Approved': return 'status-approved';
      case 'Rejected': return 'status-rejected';
      case 'Pending': return 'status-pending';
      default: return 'status-pending';
    }
  }

  function getStatusIcon(status) {
    switch (status) {
      case 'Approved': return '✅';
      case 'Rejected': return '❌';
      case 'Pending': return '⏳';
      default: return '⏳';
    }
  }
</script>

<div class="participations-section">
  <div class="section-header">
    <div class="header-left">
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
        <line x1="16" y1="2" x2="16" y2="6"/>
        <line x1="8" y1="2" x2="8" y2="6"/>
        <line x1="3" y1="10" x2="21" y2="10"/>
      </svg>
      <h2>Your Participations</h2>
    </div>
    <button class="btn-refresh" on:click={fetchParticipations} title="Refresh list">
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
      <p>Loading participations...</p>
    </div>
  {:else if errorMessage}
    <div class="error-state">
      <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10"/>
        <line x1="12" y1="8" x2="12" y2="12"/>
        <line x1="12" y1="16" x2="12.01" y2="16"/>
      </svg>
      <p>{errorMessage}</p>
      <button class="btn-retry" on:click={fetchParticipations}>Try Again</button>
    </div>
  {:else if participations.length === 0}
    <div class="empty-state">
      <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
        <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
        <line x1="16" y1="2" x2="16" y2="6"/>
        <line x1="8" y1="2" x2="8" y2="6"/>
        <line x1="3" y1="10" x2="21" y2="10"/>
      </svg>
      <p class="empty-title">No Participations Yet</p>
      <p class="empty-subtitle">Submit your first event participation using the form above!</p>
    </div>
  {:else}
    <div class="participations-grid">
      {#each participations as p (p.participationId)}
        <div class="participation-card">
          <div class="card-header">
            <span class="event-type-badge {getEventTypeBadgeClass(p.eventType)}">
              {getEventTypeIcon(p.eventType)} {p.eventType}
            </span>
            <span class="status-badge {getStatusBadgeClass(p.verificationStatus)}">
              {getStatusIcon(p.verificationStatus)} {p.verificationStatus}
            </span>
          </div>
          <div class="card-body">
            <h3 class="event-name">{p.eventName}</h3>
            <div class="role-tag">
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              <span>{p.participationRole}</span>
            </div>
            {#if p.achievementDescription}
              <p class="achievement-desc">{p.achievementDescription}</p>
            {:else}
              <p class="achievement-desc no-desc">No description provided</p>
            {/if}
          </div>
          <div class="card-footer">
            <div class="submitted-info">
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"/>
                <polyline points="12 6 12 12 16 14"/>
              </svg>
              <span>{formatDate(p.submittedDate)}</span>
            </div>
          </div>
        </div>
      {/each}
    </div>
  {/if}
</div>

<style>
  .participations-section {
    background: #ffffff;
    border-radius: 14px;
    padding: 32px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    border: 1px solid #e8ecf1;
  }

  .section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px; }

  .header-left { display: flex; align-items: center; gap: 10px; color: #1a1a2e; }
  .header-left h2 { margin: 0; font-size: 1.4rem; font-weight: 700; }
  .header-left svg { color: #4361ee; }

  .btn-refresh {
    display: inline-flex; align-items: center; justify-content: center;
    width: 38px; height: 38px; border: 1.5px solid #d1d5db; border-radius: 8px;
    background: #fafbfc; color: #555; cursor: pointer; transition: all 0.2s;
  }
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

  .participations-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 20px; }

  .participation-card {
    display: flex; flex-direction: column; border: 1.5px solid #e8ecf1; border-radius: 12px;
    overflow: hidden; background: #fafbfc; transition: transform 0.2s, box-shadow 0.2s;
  }
  .participation-card:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08); }

  .card-header { padding: 14px 18px 0; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 8px; }

  .event-type-badge { display: inline-flex; align-items: center; gap: 4px; padding: 4px 12px; border-radius: 20px; font-size: 0.78rem; font-weight: 600; }
  .badge-competition { background: #fef3c7; color: #92400e; }
  .badge-workshop { background: #e0f2fe; color: #0369a1; }
  .badge-seminar { background: #ede9fe; color: #6d28d9; }
  .badge-organizing { background: #d1fae5; color: #065f46; }
  .badge-other { background: #f1f5f9; color: #475569; }

  .status-badge { display: inline-flex; align-items: center; gap: 4px; padding: 4px 10px; border-radius: 20px; font-size: 0.75rem; font-weight: 600; }
  .status-pending { background: #fff3cd; color: #856404; }
  .status-approved { background: #d4edda; color: #155724; }
  .status-rejected { background: #f8d7da; color: #721c24; }

  .card-body { padding: 14px 18px; flex: 1; }
  .event-name { margin: 0 0 8px; font-size: 1.05rem; font-weight: 700; color: #1a1a2e; line-height: 1.3; }

  .role-tag { display: inline-flex; align-items: center; gap: 4px; font-size: 0.82rem; font-weight: 500; color: #4361ee; margin-bottom: 8px; }
  .role-tag svg { color: #4361ee; }

  .achievement-desc { margin: 0; font-size: 0.88rem; color: #555; line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; }
  .no-desc { color: #bbb; font-style: italic; }

  .card-footer { display: flex; align-items: center; justify-content: space-between; padding: 12px 18px; border-top: 1px solid #e8ecf1; background: #fff; }
  .submitted-info { display: flex; align-items: center; gap: 5px; font-size: 0.8rem; color: #888; }

  @media (max-width: 600px) {
    .participations-section { padding: 20px 16px; }
    .participations-grid { grid-template-columns: 1fr; }
    .section-header h2 { font-size: 1.15rem; }
  }
</style>
