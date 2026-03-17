<script>
  const API_BASE = 'http://localhost:9090';

  export let mentorId = 1;
  export let refreshTrigger = 0;

  let sessions = [];
  let loading = true;
  let errorMessage = '';

  $: if (refreshTrigger >= 0) {
    loadSessions();
  }

  async function loadSessions() {
    loading = true;
    errorMessage = '';

    try {
      const response = await fetch(`${API_BASE}/api/sessions/mentor/${mentorId}`, {
        headers: {
          'X-User-Id': String(mentorId),
          'X-User-Role': 'MENTOR'
        }
      });

      const result = await response.json();

      if (response.ok && result.success) {
        sessions = result.data || [];
      } else {
        errorMessage = result.message || 'Failed to load sessions.';
      }
    } catch (err) {
      errorMessage = 'Network error. Could not load sessions.';
    } finally {
      loading = false;
    }
  }

  function formatDate(dateStr) {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('en-US', {
      year: 'numeric', month: 'long', day: 'numeric'
    });
  }

  function formatTime(timeStr) {
    if (!timeStr) return '—';
    const [h, m] = timeStr.split(':');
    const hour = parseInt(h);
    const amPm = hour >= 12 ? 'PM' : 'AM';
    const displayHour = hour % 12 || 12;
    return `${displayHour}:${m} ${amPm}`;
  }

  function formatSessionType(type) {
    if (type === 'ONE_TO_ONE') return 'One-to-One';
    if (type === 'GROUP') return 'Group';
    return type;
  }
</script>

<div class="list-section">
  <div class="section-header">
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <line x1="8" y1="6" x2="21" y2="6"/>
      <line x1="8" y1="12" x2="21" y2="12"/>
      <line x1="8" y1="18" x2="21" y2="18"/>
      <line x1="3" y1="6" x2="3.01" y2="6"/>
      <line x1="3" y1="12" x2="3.01" y2="12"/>
      <line x1="3" y1="18" x2="3.01" y2="18"/>
    </svg>
    <h2>Your Mentoring Sessions</h2>
    <span class="badge">{sessions.length}</span>
  </div>

  {#if loading}
    <div class="loading-state">
      <span class="spinner"></span>
      <p>Loading sessions...</p>
    </div>
  {:else if errorMessage}
    <div class="alert alert-error">
      <span>{errorMessage}</span>
    </div>
  {:else if sessions.length === 0}
    <div class="empty-state">
      <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
        <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
        <line x1="16" y1="2" x2="16" y2="6"/>
        <line x1="8" y1="2" x2="8" y2="6"/>
        <line x1="3" y1="10" x2="21" y2="10"/>
      </svg>
      <p>No mentoring sessions yet. Create your first session above!</p>
    </div>
  {:else}
    <div class="sessions-grid">
      {#each sessions as session}
        <div class="session-card">
          <div class="card-header">
            <h3>{session.sessionTitle}</h3>
            <span class="type-badge" class:group={session.sessionType === 'GROUP'}>
              {formatSessionType(session.sessionType)}
            </span>
          </div>
          <div class="card-topic">{session.sessionTopic}</div>
          {#if session.sessionDescription}
            <p class="card-description">{session.sessionDescription}</p>
          {/if}
          <div class="card-meta">
            <div class="meta-item">
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                <line x1="16" y1="2" x2="16" y2="6"/>
                <line x1="8" y1="2" x2="8" y2="6"/>
                <line x1="3" y1="10" x2="21" y2="10"/>
              </svg>
              <span>{formatDate(session.sessionDate)}</span>
            </div>
            <div class="meta-item">
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"/>
                <polyline points="12 6 12 12 16 14"/>
              </svg>
              <span>{formatTime(session.sessionTime)}</span>
            </div>
          </div>
        </div>
      {/each}
    </div>
  {/if}
</div>

<style>
  .list-section {
    background: #ffffff;
    border-radius: 14px;
    padding: 32px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    border: 1px solid #e8ecf1;
  }

  .section-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 24px;
    color: #1a1a2e;
  }

  .section-header svg { color: #4361ee; }

  .section-header h2 { margin: 0; font-size: 1.4rem; font-weight: 700; }

  .badge {
    background: #4361ee;
    color: white;
    padding: 2px 10px;
    border-radius: 20px;
    font-size: 0.8rem;
    font-weight: 600;
  }

  .loading-state, .empty-state {
    text-align: center;
    padding: 48px 24px;
    color: #999;
  }

  .empty-state svg { color: #d1d5db; margin-bottom: 12px; }

  .spinner {
    display: inline-block; width: 24px; height: 24px;
    border: 3px solid #e8ecf1; border-top-color: #4361ee;
    border-radius: 50%; animation: spin 0.6s linear infinite;
    margin-bottom: 12px;
  }

  @keyframes spin { to { transform: rotate(360deg); } }

  .alert-error {
    background: #f8d7da; color: #721c24; border: 1px solid #f5c6cb;
    padding: 14px 18px; border-radius: 10px; font-size: 0.9rem;
  }

  .sessions-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 16px;
  }

  .session-card {
    border: 1.5px solid #e8ecf1;
    border-radius: 12px;
    padding: 20px;
    transition: transform 0.2s, box-shadow 0.2s;
  }

  .session-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  }

  .card-header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 8px;
  }

  .card-header h3 {
    font-size: 1rem;
    font-weight: 700;
    color: #1a1a2e;
    margin: 0;
  }

  .type-badge {
    background: #e0f2fe;
    color: #0277bd;
    padding: 3px 10px;
    border-radius: 20px;
    font-size: 0.72rem;
    font-weight: 600;
    white-space: nowrap;
  }

  .type-badge.group {
    background: #f3e5f5;
    color: #7b1fa2;
  }

  .card-topic {
    font-size: 0.88rem;
    color: #4361ee;
    font-weight: 500;
    margin-bottom: 8px;
  }

  .card-description {
    font-size: 0.85rem;
    color: #666;
    line-height: 1.5;
    margin-bottom: 12px;
  }

  .card-meta {
    display: flex;
    gap: 16px;
    padding-top: 12px;
    border-top: 1px solid #f0f0f0;
  }

  .meta-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 0.8rem;
    color: #888;
  }

  .meta-item svg { color: #aaa; }

  @media (max-width: 600px) {
    .list-section { padding: 20px 16px; }
    .sessions-grid { grid-template-columns: 1fr; }
  }
</style>
