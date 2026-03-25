<script>
  import { onMount } from 'svelte';
  import { api, getCurrentUser, getRoleDashboardPath, isHodWorkspaceRole } from '../../lib/api.js';
  import DashboardLayout from '../shared/DashboardLayout.svelte';
  import CustomSelect from '../shared/CustomSelect.svelte';

  export let navItems = [];
  export let activeItem = 'feedback';
  export let pageTitle = 'Feedback';

  const user = getCurrentUser();

  let loading = true;
  let error = '';
  let feedbacks = [];
  let totalCount = 0;
  let selectedFeedback = null;
  let maxRating = '';
  let minRating = '';
  let query = '';

  const minRatingOptions = [
    { value: '', label: 'Min rating' },
    { value: '1', label: '1 star' },
    { value: '2', label: '2 stars' },
    { value: '3', label: '3 stars' },
    { value: '4', label: '4 stars' },
    { value: '5', label: '5 stars' }
  ];

  const maxRatingOptions = [
    { value: '', label: 'Max rating' },
    { value: '1', label: '1 star' },
    { value: '2', label: '2 stars' },
    { value: '3', label: '3 stars' },
    { value: '4', label: '4 stars' },
    { value: '5', label: '5 stars' }
  ];

  onMount(() => {
    if (!user) {
      window.location.href = '/';
      return;
    }

    if (!isHodWorkspaceRole(user.role)) {
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
      totalCount = feedbacks.length;
    } catch (e) {
      error = 'Failed to load feedback records.';
      feedbacks = [];
      totalCount = 0;
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
        <span class="hero-count">{totalCount}</span>
        <span class="hero-label">Feedback Records</span>
      </div>
    </section>

    <section class="card feedback-card">
      <div class="toolbar">
        <input class="input search-input" bind:value={query} placeholder="Search by mentor, session ID, or feedback text" />
        <div class="compact-input">
          <CustomSelect options={minRatingOptions} bind:value={minRating} compact on:change={loadFeedbacks} />
        </div>
        <div class="compact-input">
          <CustomSelect options={maxRatingOptions} bind:value={maxRating} compact on:change={loadFeedbacks} />
        </div>
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
    position: relative;
    overflow: hidden;
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 1.5rem;
    padding: 2rem 2.5rem;
    background: var(--bg-main);
  }
  /* Dot-grid — mirrors landing page hero pattern */
  .hero-card::before {
    content: '';
    position: absolute;
    inset: -10% 0;
    background-image: radial-gradient(circle, var(--border-light) 1.2px, transparent 1.2px);
    background-size: 28px 28px;
    pointer-events: none;
  }
  .hero-card::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(to right, var(--bg-main) 40%, transparent 85%);
    pointer-events: none;
  }
  .hero-card > * { position: relative; z-index: 1; }

  .eyebrow {
    display: block;
    margin-bottom: 0.6rem;
    font-size: 0.68rem;
    font-weight: 600;
    letter-spacing: 0.14em;
    text-transform: uppercase;
    color: var(--primary);
    opacity: 0.7;
  }

  h2 {
    margin-bottom: 0.5rem;
    font-family: var(--font-heading);
    font-size: clamp(1.4rem, 3vw, 2rem);
    font-weight: 700;
    letter-spacing: -0.03em;
    line-height: 1.1;
    color: var(--text-main);
  }

  .hero-copy {
    max-width: 42rem;
    color: var(--text-secondary);
    font-size: 0.875rem;
    line-height: 1.6;
  }

  .hero-meta {
    min-width: 120px;
    text-align: right;
    flex-shrink: 0;
  }

  .hero-count {
    display: block;
    font-family: var(--font-heading);
    font-size: 3rem;
    font-weight: 700;
    letter-spacing: -0.04em;
    color: var(--text-main);
    line-height: 1;
  }

  .hero-label {
    display: block;
    color: var(--text-muted);
    font-size: 0.72rem;
    font-weight: 500;
    letter-spacing: 0.04em;
    margin-top: 0.2rem;
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
    border-bottom: 1px solid var(--border-light);
    vertical-align: top;
  }

  th {
    font-size: 0.76rem;
    text-transform: uppercase;
    letter-spacing: 0.05em;
    color: var(--text-muted);
    background: var(--bg-secondary);
  }

  .preview-cell {
    max-width: 360px;
    color: var(--text-secondary);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .stars {
    color: var(--warning);
    letter-spacing: 0.05em;
  }

  .empty-state {
    color: var(--text-muted);
    padding: 1rem 0;
  }

  .alert {
    padding: 0.8rem 1rem;
    border-radius: var(--radius);
    margin-bottom: 1rem;
  }

  .alert-error {
    background: var(--danger-light);
    color: var(--danger);
  }

  .btn-sm {
    padding: 0.4rem 0.8rem;
    font-size: 0.8rem;
  }

  .feedback-modal {
    max-width: 720px;
    width: min(720px, 92vw);
    border-radius: var(--radius-lg);
  }

  .modal-header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 1rem;
    margin-bottom: 1rem;
  }

  h3 {
    font-family: var(--font-heading);
    font-size: 1.1rem;
    font-weight: 700;
    letter-spacing: -0.02em;
    color: var(--text-main);
  }

  .close-btn {
    background: transparent;
    color: var(--text-muted);
    font-size: 1rem;
  }

  .detail-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 1rem;
  }

  .detail-item {
    padding: 1rem;
    border: 1px solid var(--border-light);
    border-radius: var(--radius);
    background: var(--bg-alt);
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
    color: var(--text-muted);
  }

  .detail-value {
    color: var(--text-main);
    font-weight: 500;
  }

  .comment-copy {
    color: var(--text-secondary);
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
