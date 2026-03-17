<script>
  const API_BASE = 'http://localhost:9091';

  let reviews = [];
  let loading = true;
  let errorMessage = '';
  let successMessage = '';

  loadReviews();

  async function loadReviews() {
    loading = true;
    errorMessage = '';

    try {
      const response = await fetch(`${API_BASE}/api/contribution-reviews`, {
        headers: {
          'X-User-Role': 'HOD'
        }
      });

      const result = await response.json();

      if (response.ok && result.success) {
        reviews = result.data || [];
      } else {
        errorMessage = result.message || 'Failed to load contribution reviews.';
      }
    } catch (err) {
      errorMessage = 'Network error. Could not load contribution reviews.';
    } finally {
      loading = false;
    }
  }

  let selectedDecisions = {};
  let reviewComments = {};

  async function submitDecision(reviewId) {
    successMessage = '';
    errorMessage = '';

    const decision = selectedDecisions[reviewId];
    const comment = reviewComments[reviewId];

    if (!decision) {
      errorMessage = 'Please select a decision (Approve or Reject) before submitting.';
      return;
    }

    if (!comment || comment.trim() === '') {
      errorMessage = 'Review comment is required before closing the review.';
      return;
    }

    try {
      const response = await fetch(`${API_BASE}/api/contribution-reviews/${reviewId}/decide`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'X-User-Id': '1',
          'X-User-Role': 'HOD'
        },
        body: JSON.stringify({
          decisionStatus: decision,
          reviewComment: comment
        })
      });

      const result = await response.json();

      if (response.ok && result.success) {
        successMessage = `Contribution review decision finalized successfully.`;
        selectedDecisions[reviewId] = '';
        reviewComments[reviewId] = '';
        await loadReviews();
      } else {
        errorMessage = result.message || 'Failed to finalize decision.';
      }
    } catch (err) {
      errorMessage = 'Network error. Could not finalize decision.';
    }
  }

  function getDecisionClass(status) {
    if (status === 'APPROVED') return 'decision-approved';
    if (status === 'REJECTED') return 'decision-rejected';
    return 'decision-pending';
  }

  function formatDate(dateStr) {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('en-US', {
      year: 'numeric', month: 'short', day: 'numeric'
    });
  }
</script>

<div class="table-section">
  <div class="section-header">
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
      <polyline points="14 2 14 8 20 8"/>
      <line x1="12" y1="18" x2="12" y2="12"/>
      <line x1="9" y1="15" x2="15" y2="15"/>
    </svg>
    <h2>Contribution Record Review</h2>
    <span class="badge">{reviews.length}</span>
  </div>

  {#if successMessage}
    <div class="alert alert-success">
      <span>{successMessage}</span>
      <button class="alert-close" on:click={() => successMessage = ''}>✕</button>
    </div>
  {/if}

  {#if loading}
    <div class="loading-state">
      <span class="spinner"></span>
      <p>Loading contribution reviews...</p>
    </div>
  {:else if errorMessage}
    <div class="alert alert-error">
      <span>{errorMessage}</span>
    </div>
  {:else if reviews.length === 0}
    <div class="empty-state">
      <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
        <polyline points="14 2 14 8 20 8"/>
      </svg>
      <p>No flagged contribution records found.</p>
    </div>
  {:else}
    <div class="reviews-list">
      {#each reviews as review}
        <div class="review-card">
          <div class="review-header">
            <div class="review-ids">
              <span class="id-badge">Student #{review.studentId}</span>
              <span class="id-badge secondary">Contribution #{review.contributionId}</span>
            </div>
            {#if review.decisionStatus}
              <span class="decision-badge {getDecisionClass(review.decisionStatus)}">
                {review.decisionStatus}
              </span>
            {:else}
              <span class="decision-badge decision-pending">Pending Review</span>
            {/if}
          </div>

          <div class="review-body">
            <div class="field-group">
              <label>Flagged Reason</label>
              <p>{review.flaggedReason || '—'}</p>
            </div>

            {#if review.evidenceFilePath}
              <div class="field-group">
                <label>Evidence File</label>
                <a href={review.evidenceFilePath} target="_blank" class="file-link">
                  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                    <polyline points="14 2 14 8 20 8"/>
                  </svg>
                  View PDF Evidence
                </a>
              </div>
            {/if}

            {#if review.decisionStatus}
              <div class="field-group">
                <label>Review Comment</label>
                <p>{review.reviewComment || '—'}</p>
              </div>
              <div class="field-group">
                <label>Decision Date</label>
                <p>{formatDate(review.decisionDate)}</p>
              </div>
            {:else}
              <div class="decision-form">
                <div class="form-row">
                  <div class="form-group">
                    <label for="decision-{review.reviewId}">Final Decision</label>
                    <select
                      id="decision-{review.reviewId}"
                      class="decision-select"
                      bind:value={selectedDecisions[review.reviewId]}
                    >
                      <option value="">— Select —</option>
                      <option value="APPROVED">Approve</option>
                      <option value="REJECTED">Reject</option>
                    </select>
                  </div>
                </div>
                <div class="form-group">
                  <label for="comment-{review.reviewId}">Review Comment</label>
                  <textarea
                    id="comment-{review.reviewId}"
                    class="comment-input"
                    maxlength="300"
                    placeholder="Enter review comment (required)..."
                    bind:value={reviewComments[review.reviewId]}
                  ></textarea>
                </div>
                <button
                  class="submit-btn"
                  on:click={() => submitDecision(review.reviewId)}
                >
                  Submit Decision
                </button>
              </div>
            {/if}
          </div>
        </div>
      {/each}
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

  .alert {
    padding: 14px 18px;
    border-radius: 10px;
    font-size: 0.9rem;
    margin-bottom: 16px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .alert-success {
    background: #d4edda; color: #155724; border: 1px solid #c3e6cb;
  }

  .alert-error {
    background: #f8d7da; color: #721c24; border: 1px solid #f5c6cb;
  }

  .alert-close {
    background: none; border: none; cursor: pointer;
    font-size: 1rem; color: inherit; padding: 0 4px;
  }

  .reviews-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .review-card {
    border: 1.5px solid #e8ecf1;
    border-radius: 12px;
    overflow: hidden;
    transition: box-shadow 0.2s;
  }

  .review-card:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  }

  .review-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    background: #f8f9fb;
    border-bottom: 1px solid #e8ecf1;
  }

  .review-ids {
    display: flex;
    gap: 8px;
  }

  .id-badge {
    background: #e0f2fe;
    color: #0277bd;
    padding: 4px 12px;
    border-radius: 20px;
    font-size: 0.78rem;
    font-weight: 600;
  }

  .id-badge.secondary {
    background: #f3e5f5;
    color: #7b1fa2;
  }

  .decision-badge {
    padding: 4px 12px;
    border-radius: 20px;
    font-size: 0.78rem;
    font-weight: 600;
  }

  .decision-approved { background: #d4edda; color: #155724; }
  .decision-rejected { background: #f8d7da; color: #721c24; }
  .decision-pending { background: #fff3cd; color: #856404; }

  .review-body {
    padding: 20px;
  }

  .field-group {
    margin-bottom: 16px;
  }

  .field-group label {
    display: block;
    font-size: 0.78rem;
    font-weight: 600;
    color: #888;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    margin-bottom: 4px;
  }

  .field-group p {
    font-size: 0.9rem;
    color: #333;
    line-height: 1.5;
  }

  .file-link {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    color: #4361ee;
    text-decoration: none;
    font-size: 0.88rem;
    font-weight: 500;
  }

  .file-link:hover { text-decoration: underline; }

  .decision-form {
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid #f0f0f0;
  }

  .form-row {
    display: flex;
    gap: 16px;
    margin-bottom: 12px;
  }

  .form-group {
    flex: 1;
    margin-bottom: 12px;
  }

  .form-group label {
    display: block;
    font-size: 0.82rem;
    font-weight: 600;
    color: #555;
    margin-bottom: 6px;
  }

  .decision-select {
    width: 100%;
    padding: 8px 12px;
    border: 1.5px solid #e8ecf1;
    border-radius: 8px;
    font-size: 0.88rem;
    background: white;
    cursor: pointer;
  }

  .decision-select:focus {
    outline: none;
    border-color: #4361ee;
    box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.1);
  }

  .comment-input {
    width: 100%;
    padding: 10px 14px;
    border: 1.5px solid #e8ecf1;
    border-radius: 8px;
    font-size: 0.88rem;
    font-family: inherit;
    resize: vertical;
    min-height: 80px;
  }

  .comment-input:focus {
    outline: none;
    border-color: #4361ee;
    box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.1);
  }

  .submit-btn {
    background: #4361ee;
    color: white;
    border: none;
    padding: 10px 24px;
    border-radius: 8px;
    font-size: 0.88rem;
    font-weight: 600;
    cursor: pointer;
    transition: background 0.2s;
  }

  .submit-btn:hover { background: #3651d4; }

  @media (max-width: 768px) {
    .table-section { padding: 20px 16px; }
    .review-header { flex-direction: column; gap: 8px; align-items: flex-start; }
    .form-row { flex-direction: column; }
  }
</style>
