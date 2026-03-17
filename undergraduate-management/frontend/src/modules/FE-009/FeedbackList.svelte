<script>
  const API_BASE = 'http://localhost:8080';

  export let userId = 1;
  export let userRole = 'STUDENT';
  export let refreshTrigger = 0;

  let feedbackList = [];
  let loading = false;
  let errorMessage = '';
  let searchSessionId = '';

  async function fetchFeedback() {
    if (!searchSessionId || isNaN(Number(searchSessionId))) {
      errorMessage = 'Please enter a valid session ID to search.';
      return;
    }

    loading = true;
    errorMessage = '';
    feedbackList = [];

    try {
      const response = await fetch(`${API_BASE}/api/feedback/session/${searchSessionId}`, {
        headers: {
          'X-User-Id': String(userId),
          'X-User-Role': userRole
        }
      });

      const result = await response.json();

      if (response.ok && result.success) {
        feedbackList = result.data || [];
      } else {
        errorMessage = result.message || 'Failed to load feedback.';
      }
    } catch (err) {
      errorMessage = 'Network error. Please check your connection.';
    } finally {
      loading = false;
    }
  }

  $: if (refreshTrigger > 0 && searchSessionId) {
    fetchFeedback();
  }

  function formatDate(dateStr) {
    if (!dateStr) return 'N/A';
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  function renderStars(rating) {
    return '★'.repeat(rating) + '☆'.repeat(5 - rating);
  }
</script>

<div class="feedback-section">
  <div class="section-header">
    <div class="header-left">
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
      </svg>
      <h2>Session Feedback</h2>
    </div>
  </div>

  <div class="search-bar">
    <input
      type="number"
      bind:value={searchSessionId}
      placeholder="Enter Session ID to search feedback"
      min="1"
      on:keydown={(e) => e.key === 'Enter' && fetchFeedback()}
    />
    <button class="btn-search" on:click={fetchFeedback} disabled={loading}>
      {#if loading}
        <span class="search-spinner"></span>
      {:else}
        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="11" cy="11" r="8"/>
          <line x1="21" y1="21" x2="16.65" y2="16.65"/>
        </svg>
      {/if}
      Search
    </button>
  </div>

  {#if errorMessage}
    <div class="error-state">
      <p>{errorMessage}</p>
    </div>
  {:else if feedbackList.length === 0 && !loading && searchSessionId}
    <div class="empty-state">
      <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
      </svg>
      <p class="empty-title">No Feedback Found</p>
      <p class="empty-subtitle">No feedback has been submitted for session #{searchSessionId} yet.</p>
    </div>
  {:else if !searchSessionId && !loading}
    <div class="empty-state">
      <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="11" cy="11" r="8"/>
        <line x1="21" y1="21" x2="16.65" y2="16.65"/>
      </svg>
      <p class="empty-title">Search for Feedback</p>
      <p class="empty-subtitle">Enter a session ID above to view feedback for that session.</p>
    </div>
  {:else if feedbackList.length > 0}
    <div class="feedback-grid">
      {#each feedbackList as fb (fb.feedbackId)}
        <div class="feedback-card">
          <div class="card-header">
            <div class="session-tag">
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                <line x1="16" y1="2" x2="16" y2="6"/>
                <line x1="8" y1="2" x2="8" y2="6"/>
                <line x1="3" y1="10" x2="21" y2="10"/>
              </svg>
              Session #{fb.sessionId}
            </div>
            <div class="rating-display">
              <span class="stars">{renderStars(fb.rating)}</span>
              <span class="rating-number">{fb.rating}/5</span>
            </div>
          </div>
          <div class="card-body">
            {#if fb.feedbackComment}
              <p class="feedback-comment">{fb.feedbackComment}</p>
            {:else}
              <p class="feedback-comment no-comment">No comment provided</p>
            {/if}
          </div>
          <div class="card-footer">
            <div class="student-info">
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              <span>Student #{fb.studentId}</span>
            </div>
            <div class="date-info">
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"/>
                <polyline points="12 6 12 12 16 14"/>
              </svg>
              <span>{formatDate(fb.submittedDate)}</span>
            </div>
          </div>
        </div>
      {/each}
    </div>
  {/if}
</div>

<style>
  .feedback-section {
    background: #ffffff;
    border-radius: 14px;
    padding: 32px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    border: 1px solid #e8ecf1;
  }

  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20px;
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: 10px;
    color: #1a1a2e;
  }

  .header-left h2 {
    margin: 0;
    font-size: 1.4rem;
    font-weight: 700;
  }

  .header-left svg {
    color: #4361ee;
  }

  .search-bar {
    display: flex;
    gap: 10px;
    margin-bottom: 24px;
  }

  .search-bar input {
    flex: 1;
    padding: 10px 14px;
    border: 1.5px solid #d1d5db;
    border-radius: 8px;
    font-size: 0.9rem;
    font-family: 'Inter', sans-serif;
    color: #1a1a2e;
    background: #fafbfc;
    transition: border-color 0.2s, box-shadow 0.2s;
    outline: none;
  }

  .search-bar input:focus {
    border-color: #4361ee;
    box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.1);
  }

  .btn-search {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 10px 20px;
    background: #4361ee;
    color: #fff;
    border: none;
    border-radius: 8px;
    font-size: 0.88rem;
    font-weight: 600;
    font-family: 'Inter', sans-serif;
    cursor: pointer;
    transition: background 0.2s;
    white-space: nowrap;
  }

  .btn-search:hover:not(:disabled) {
    background: #3651d4;
  }

  .btn-search:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }

  .search-spinner {
    display: inline-block;
    width: 16px;
    height: 16px;
    border: 2px solid rgba(255, 255, 255, 0.3);
    border-top-color: #fff;
    border-radius: 50%;
    animation: spin 0.6s linear infinite;
  }

  @keyframes spin {
    to { transform: rotate(360deg); }
  }

  .error-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 30px 20px;
    color: #dc3545;
    text-align: center;
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 48px 20px;
    gap: 8px;
    color: #aaa;
    text-align: center;
  }

  .empty-title {
    font-size: 1.1rem;
    font-weight: 600;
    color: #666;
    margin: 8px 0 0;
  }

  .empty-subtitle {
    font-size: 0.9rem;
    color: #999;
    margin: 0;
  }

  .feedback-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 20px;
  }

  .feedback-card {
    display: flex;
    flex-direction: column;
    border: 1.5px solid #e8ecf1;
    border-radius: 12px;
    overflow: hidden;
    background: #fafbfc;
    transition: transform 0.2s, box-shadow 0.2s;
  }

  .feedback-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
  }

  .card-header {
    padding: 14px 18px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 8px;
    border-bottom: 1px solid #f0f2f5;
  }

  .session-tag {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    font-size: 0.85rem;
    font-weight: 600;
    color: #4361ee;
  }

  .rating-display {
    display: flex;
    align-items: center;
    gap: 6px;
  }

  .stars {
    font-size: 1rem;
    color: #f59e0b;
    letter-spacing: 1px;
  }

  .rating-number {
    font-size: 0.8rem;
    font-weight: 600;
    color: #888;
  }

  .card-body {
    padding: 14px 18px;
    flex: 1;
  }

  .feedback-comment {
    margin: 0;
    font-size: 0.9rem;
    color: #555;
    line-height: 1.6;
    display: -webkit-box;
    -webkit-line-clamp: 4;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .no-comment {
    color: #bbb;
    font-style: italic;
  }

  .card-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 18px;
    border-top: 1px solid #e8ecf1;
    background: #fff;
    flex-wrap: wrap;
    gap: 8px;
  }

  .student-info,
  .date-info {
    display: flex;
    align-items: center;
    gap: 5px;
    font-size: 0.8rem;
    color: #888;
  }

  @media (max-width: 600px) {
    .feedback-section {
      padding: 20px 16px;
    }

    .feedback-grid {
      grid-template-columns: 1fr;
    }

    .section-header h2 {
      font-size: 1.15rem;
    }

    .search-bar {
      flex-direction: column;
    }
  }
</style>
