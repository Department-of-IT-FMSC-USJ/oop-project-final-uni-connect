<script>
  import { api } from '../../lib/api.js';

  export let sessionId = null;
  export let studentId = null;
  export let onClose = () => {};
  export let onSubmit = () => {};

  let rating = 0;
  let hoverRating = 0;
  let comment = '';
  let submitting = false;
  let error = '';

  async function handleSubmit() {
    if (rating === 0) {
      error = 'Please select a rating';
      return;
    }
    submitting = true;
    error = '';
    try {
      await api.post('/feedback', {
        sessionId,
        studentId,
        rating,
        feedbackComment: comment
      });
      onSubmit();
      onClose();
    } catch (e) {
      error = 'Failed to submit feedback. Please try again.';
    } finally {
      submitting = false;
    }
  }
</script>

<div class="modal-overlay" on:click|self={onClose}>
  <div class="modal-content">
    <div class="modal-header">
      <h2>Session Feedback</h2>
      <button class="close-btn" on:click={onClose}>
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
        </svg>
      </button>
    </div>

    <p class="modal-desc">How was your mentoring session? Your feedback helps us improve.</p>

    <div class="rating-section">
      <label>Rating</label>
      <div class="stars">
        {#each [1, 2, 3, 4, 5] as star}
          <button
            class="star"
            class:filled={star <= (hoverRating || rating)}
            on:mouseenter={() => hoverRating = star}
            on:mouseleave={() => hoverRating = 0}
            on:click={() => rating = star}
          >
            <svg width="28" height="28" viewBox="0 0 24 24"
              fill={star <= (hoverRating || rating) ? '#f59e0b' : 'none'}
              stroke={star <= (hoverRating || rating) ? '#f59e0b' : '#d1d5db'}
              stroke-width="2">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
            </svg>
          </button>
        {/each}
      </div>
    </div>

    <div class="form-group">
      <label for="comment">Comments (optional)</label>
      <textarea
        id="comment"
        class="input"
        rows="4"
        placeholder="Share your experience..."
        bind:value={comment}
      ></textarea>
    </div>

    {#if error}
      <p class="error">{error}</p>
    {/if}

    <div class="modal-actions">
      <button class="btn btn-outline" on:click={onClose}>Skip</button>
      <button class="btn btn-primary" on:click={handleSubmit} disabled={submitting}>
        {submitting ? 'Submitting...' : 'Submit Feedback'}
      </button>
    </div>
  </div>
</div>

<style>
  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 0.5rem;
  }

  .modal-header h2 {
    font-size: 1.25rem;
    font-weight: 600;
  }

  .close-btn {
    background: none;
    color: var(--gray-400);
    padding: 0.25rem;
    border-radius: 4px;
  }
  .close-btn:hover { color: var(--gray-600); background: var(--gray-100); }

  .modal-desc {
    color: var(--gray-500);
    font-size: 0.875rem;
    margin-bottom: 1.5rem;
  }

  .rating-section {
    margin-bottom: 1.25rem;
  }

  .rating-section label {
    display: block;
    font-size: 0.875rem;
    font-weight: 500;
    color: var(--gray-700);
    margin-bottom: 0.5rem;
  }

  .stars {
    display: flex;
    gap: 0.25rem;
  }

  .star {
    background: none;
    padding: 0;
    transition: transform 0.1s;
  }
  .star:hover { transform: scale(1.15); }

  .form-group {
    margin-bottom: 1.25rem;
  }

  .form-group label {
    display: block;
    font-size: 0.875rem;
    font-weight: 500;
    color: var(--gray-700);
    margin-bottom: 0.375rem;
  }

  textarea.input {
    resize: vertical;
    min-height: 80px;
  }

  .error {
    color: var(--danger);
    font-size: 0.8rem;
    margin-bottom: 1rem;
  }

  .modal-actions {
    display: flex;
    justify-content: flex-end;
    gap: 0.75rem;
    padding-top: 0.5rem;
  }
</style>
