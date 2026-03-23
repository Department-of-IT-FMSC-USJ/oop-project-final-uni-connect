<script>
  import { onMount } from 'svelte';
  import { api, getCurrentUser, getRoleDashboardPath } from '../../lib/api.js';
  import DashboardLayout from '../shared/DashboardLayout.svelte';

  export let navItems = [];
  export let activeItem = 'feedback';
  export let pageTitle = 'Feedback';

  const user = getCurrentUser();

  let loading = true;
  let error = '';
  let feedbacks = [];
  let selectedFeedback = null;
  let maxRating = '';
  let minRating = '';
  let query = '';

  onMount(() => {
    if (!user) {
      window.location.href = '/';
      return;
    }

    if (user.role !== 'DEPARTMENT_HEAD') {
      window.location.href = getRoleDashboardPath(user.role);
      return;
    }

    loadFeedbacks();
  });

  async function loadFeedbacks() {
    loading = true;
    error = '';

    try {
      const params = new URLSearchParams();
      if (minRating) params.set('minRating', minRating);
      if (maxRating) params.set('maxRating', maxRating);

      const queryString = params.toString();
      const res = await api.get(`/feedback-reviews${queryString ? `?${queryString}` : ''}`, { cache: false });
      feedbacks = res.data || [];
    } catch (e) {
      error = 'Failed to load feedback records.';
      feedbacks = [];
    } finally {
      loading = false;
    }
  }

  function renderStars(rating) {
    return Array.from({ length: 5 }, (_, i) => (i < rating ? '★' : '☆')).join('');
  }

  function formatDate(value) {
    if (!value) return '-';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return date.toLocaleString();
  }

  function getFilteredFeedbacks() {
    const normalized = query.trim().toLowerCase();
    if (!normalized) return feedbacks;

    return feedbacks.filter((entry) =>
      [entry.mentorName, entry.feedbackComment, entry.sessionId?.toString()]
        .filter(Boolean)
        .some((value) => value.toLowerCase().includes(normalized))
    );
  }

  const visibleFeedbacks = () => getFilteredFeedbacks();
</script>

<DashboardLayout {navItems} {activeItem} {pageTitle}>
  <div class="feedback-page">
    <section class="card hero-card">
      <div>
        <p class="eyebrow">Head of Department</p>
        <h2>Feedback Oversight</h2>
        <p class="hero-copy">Review all submitted mentor feedback, filter by rating, and inspect each comment in full.</p>
      </div>
      <div class="hero-meta">
        <span class="hero-count">{visibleFeedbacks().length}</span>
        <span class="hero-label">Feedback Records</span>
      </div>
    </section>

    <section class="card feedback-card">
      <div class="toolbar">
        <input class="input search-input" bind:value={query} placeholder="Search by mentor, session ID, or feedback text" />
        <select class="input compact-input" bind:value={minRating} on:change={loadFeedbacks}>
          <option value="">Min rating</option>
          <option value="1">1 star</option>
          <option value="2">2 stars</option>
          <option value="3">3 stars</option>
          <option value="4">4 stars</option>
          <option value="5">5 stars</option>
        </select>
        <select class="input compact-input" bind:value={maxRating} on:change={loadFeedbacks}>
          <option value="">Max rating</option>
          <option value="1">1 star</option>
          <option value="2">2 stars</option>
          <option value="3">3 stars</option>
          <option value="4">4 stars</option>
          <option value="5">5 stars</option>
        </select>
      </div>

      {#if error}
        <div class="alert alert-error">{error}</div>
      {/if}

      {#if loading}
        <p class="empty-state">Loading feedback...</p>
      {:else if visibleFeedbacks().length === 0}
        <p class="empty-state">No feedback records found.</p>
      {:else}
        <div class="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Mentor</th>
                <th>Session</th>
                <th>Rating</th>
                <th>Submitted</th>
                <th>Preview</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {#each visibleFeedbacks() as entry}
                <tr>
                  <td><strong>{entry.mentorName || 'Unknown'}</strong></td>
                  <td>Session #{entry.sessionId || '-'}</td>
                  <td>
                    <span class="stars">{renderStars(entry.rating || 0)}</span>
                  </td>
                  <td>{formatDate(entry.submittedDate)}</td>
                  <td class="preview-cell">{entry.feedbackComment || 'No comment provided.'}</td>
                  <td>
                    <button class="btn btn-outline btn-sm" on:click={() => selectedFeedback = entry}>
                      View Feedback
                    </button>
                  </td>
                </tr>
              {/each}
            </tbody>
          </table>
        </div>
      {/if}
    </section>
  </div>

  {#if selectedFeedback}
    <div class="modal-overlay" on:click|self={() => selectedFeedback = null}>
      <div class="modal-content feedback-modal">
        <div class="modal-header">
          <div>
            <p class="eyebrow">Feedback Detail</p>
            <h3>{selectedFeedback.mentorName || 'Unknown Mentor'}</h3>
          </div>
          <button class="close-btn" on:click={() => selectedFeedback = null} aria-label="Close feedback dialog">✕</button>
        </div>

        <div class="detail-grid">
          <div class="detail-item">
            <span class="detail-label">Session</span>
            <span class="detail-value">#{selectedFeedback.sessionId || '-'}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Rating</span>
            <span class="detail-value stars">{renderStars(selectedFeedback.rating || 0)}</span>
          </div>
          <div class="detail-item full-width">
            <span class="detail-label">Submitted</span>
            <span class="detail-value">{formatDate(selectedFeedback.submittedDate)}</span>
          </div>
          <div class="detail-item full-width">
            <span class="detail-label">Feedback Comment</span>
            <p class="comment-copy">{selectedFeedback.feedbackComment || 'No comment provided.'}</p>
          </div>
        </div>
      </div>
    </div>
  {/if}
</DashboardLayout>

<style>
  .feedback-page {
    display: grid;
    gap: 1.5rem;
  }

  .hero-card {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 1.5rem;
    padding: 2rem;
    background:
      radial-gradient(circle at top right, rgba(16, 185, 129, 0.15), transparent 28%),
      linear-gradient(135deg, #ffffff, #f7fbff);
  }

  .eyebrow {
    margin-bottom: 0.65rem;
    font-size: 0.75rem;
    font-weight: 700;
    letter-spacing: 0.08em;
    text-transform: uppercase;
    color: var(--accent);
  }

  h2 {
    margin-bottom: 0.5rem;
    font-size: 1.8rem;
    color: var(--gray-900);
  }

  .hero-copy {
    max-width: 42rem;
    color: var(--gray-600);
  }

  .hero-meta {
    min-width: 140px;
    text-align: right;
  }

  .hero-count {
    display: block;
    font-size: 2rem;
    font-weight: 700;
    color: var(--success);
  }

  .hero-label {
    color: var(--gray-500);
    font-size: 0.875rem;
  }

  .feedback-card {
    padding: 1.5rem;
  }

  .toolbar {
    display: flex;
    gap: 0.75rem;
    flex-wrap: wrap;
    margin-bottom: 1rem;
  }

  .search-input {
    flex: 1 1 320px;
  }

  .compact-input {
    width: 140px;
  }

  .table-wrapper {
    overflow-x: auto;
  }

  table {
    width: 100%;
    border-collapse: collapse;
  }

  th, td {
    padding: 0.95rem 0.8rem;
    text-align: left;
    border-bottom: 1px solid var(--gray-200);
    vertical-align: top;
  }

  th {
    font-size: 0.76rem;
    text-transform: uppercase;
    letter-spacing: 0.05em;
    color: var(--gray-500);
  }

  .preview-cell {
    max-width: 360px;
    color: var(--gray-600);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .stars {
    color: #d97706;
    letter-spacing: 0.05em;
  }

  .empty-state {
    color: var(--gray-500);
    padding: 1rem 0;
  }

  .alert {
    padding: 0.8rem 1rem;
    border-radius: var(--radius);
    margin-bottom: 1rem;
  }

  .alert-error {
    background: #fee2e2;
    color: #991b1b;
  }

  .btn-sm {
    padding: 0.4rem 0.8rem;
    font-size: 0.8rem;
  }

  .feedback-modal {
    max-width: 720px;
    width: min(720px, 92vw);
  }

  .modal-header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 1rem;
    margin-bottom: 1rem;
  }

  h3 {
    color: var(--gray-900);
  }

  .close-btn {
    background: transparent;
    color: var(--gray-500);
    font-size: 1rem;
  }

  .detail-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 1rem;
  }

  .detail-item {
    padding: 1rem;
    border: 1px solid var(--gray-200);
    border-radius: var(--radius);
    background: var(--gray-50);
  }

  .full-width {
    grid-column: 1 / -1;
  }

  .detail-label {
    display: block;
    margin-bottom: 0.35rem;
    font-size: 0.72rem;
    font-weight: 700;
    letter-spacing: 0.05em;
    text-transform: uppercase;
    color: var(--gray-500);
  }

  .detail-value {
    color: var(--gray-900);
    font-weight: 500;
  }

  .comment-copy {
    color: var(--gray-700);
    line-height: 1.6;
    white-space: pre-wrap;
  }

  @media (max-width: 800px) {
    .hero-card {
      flex-direction: column;
    }

    .hero-meta {
      text-align: left;
    }

    .detail-grid {
      grid-template-columns: 1fr;
    }
  }
</style>
