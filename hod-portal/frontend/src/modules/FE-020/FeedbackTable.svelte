<script>
  const API_BASE = 'http://localhost:9091';

  let feedbacks = [];
  let loading = true;
  let errorMessage = '';

  loadFeedbacks();

  async function loadFeedbacks() {
    loading = true;
    errorMessage = '';

    try {
      const response = await fetch(`${API_BASE}/api/feedback-reviews`, {
        headers: {
          'X-User-Role': 'HOD'
        }
      });

      const result = await response.json();

      if (response.ok && result.success) {
        feedbacks = result.data || [];
      } else {
        errorMessage = result.message || 'Failed to load feedback reviews.';
      }
    } catch (err) {
      errorMessage = 'Network error. Could not load feedback reviews.';
    } finally {
      loading = false;
    }
  }

  function formatDate(dateStr) {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('en-US', {
      year: 'numeric', month: 'short', day: 'numeric'
    });
  }

  function renderStars(rating) {
    const stars = [];
    for (let i = 1; i <= 5; i++) {
      stars.push(i <= rating ? '★' : '☆');
    }
    return stars.join('');
  }
</script>

<div class="table-section">
  <div class="section-header">
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
    </svg>
    <h2>Feedback Review Dashboard</h2>
    <span class="badge">{feedbacks.length}</span>
  </div>

  {#if loading}
    <div class="loading-state">
      <span class="spinner"></span>
      <p>Loading feedback reviews...</p>
    </div>
  {:else if errorMessage}
    <div class="alert alert-error">
      <span>{errorMessage}</span>
    </div>
  {:else if feedbacks.length === 0}
    <div class="empty-state">
      <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
      </svg>
      <p>No feedback reviews found.</p>
    </div>
  {:else}
    <div class="table-wrapper">
      <table>
        <thead>
          <tr>
            <th>Session ID</th>
            <th>Mentor Name</th>
            <th>Student Rating</th>
            <th>Feedback Comment</th>
            <th>Submission Date</th>
          </tr>
        </thead>
        <tbody>
          {#each feedbacks as feedback}
            <tr>
              <td class="td-id">#{feedback.sessionId}</td>
              <td class="td-name">{feedback.mentorName}</td>
              <td>
                <span class="stars">{renderStars(feedback.rating)}</span>
                <span class="rating-num">({feedback.rating}/5)</span>
              </td>
              <td class="td-comment">{feedback.feedbackComment || '—'}</td>
              <td class="td-date">{formatDate(feedback.submittedDate)}</td>
            </tr>
          {/each}
        </tbody>
      </table>
    </div>
  {/if}
</div>

<style>
  .table-section {
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

  .table-wrapper {
    overflow-x: auto;
  }

  table {
    width: 100%;
    border-collapse: collapse;
    font-size: 0.9rem;
  }

  thead th {
    background: #f8f9fb;
    padding: 12px 16px;
    text-align: left;
    font-weight: 600;
    color: #555;
    border-bottom: 2px solid #e8ecf1;
    white-space: nowrap;
  }

  tbody td {
    padding: 14px 16px;
    border-bottom: 1px solid #f0f0f0;
    vertical-align: middle;
  }

  tbody tr:hover {
    background: #f8f9fb;
  }

  .td-id { font-weight: 600; color: #4361ee; }
  .td-name { font-weight: 600; color: #1a1a2e; }
  .td-comment {
    max-width: 300px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .td-date { color: #888; white-space: nowrap; }

  .stars {
    color: #f59e0b;
    font-size: 1rem;
    letter-spacing: 1px;
  }

  .rating-num {
    font-size: 0.78rem;
    color: #888;
    margin-left: 4px;
  }

  @media (max-width: 768px) {
    .table-section { padding: 20px 16px; }
    thead th, tbody td { padding: 10px 12px; font-size: 0.82rem; }
  }
</style>
