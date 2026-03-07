<script>
  const API_BASE = 'http://localhost:8080';

  export let userId = 1;
  export let userRole = 'STUDENT';
  export let onFeedbackSuccess = () => {};

  let sessionId = '';
  let rating = 0;
  let hoverRating = 0;
  let feedbackComment = '';

  let errors = {};
  let isSubmitting = false;
  let successMessage = '';
  let errorMessage = '';

  function validateSessionId(value) {
    if (!value || !String(value).trim()) return 'Session ID is required';
    if (isNaN(Number(value)) || Number(value) <= 0) return 'Session ID must be a positive number';
    return '';
  }

  function validateRating(value) {
    if (!value || value < 1 || value > 5) return 'Please select a rating (1-5 stars)';
    return '';
  }

  function validateFeedbackComment(value) {
    if (value && value.length > 2000) return 'Feedback comment cannot exceed 2000 characters';
    return '';
  }

  function validateAll() {
    const newErrors = {};
    const e1 = validateSessionId(sessionId);
    if (e1) newErrors.sessionId = e1;
    const e2 = validateRating(rating);
    if (e2) newErrors.rating = e2;
    const e3 = validateFeedbackComment(feedbackComment);
    if (e3) newErrors.feedbackComment = e3;
    errors = newErrors;
    return Object.keys(newErrors).length === 0;
  }

  function setRating(value) {
    rating = value;
    errors = { ...errors, rating: '' };
  }

  function resetForm() {
    sessionId = '';
    rating = 0;
    hoverRating = 0;
    feedbackComment = '';
    errors = {};
  }

  async function handleSubmit() {
    successMessage = '';
    errorMessage = '';

    if (!validateAll()) return;

    isSubmitting = true;

    try {
      const requestBody = {
        sessionId: Number(sessionId),
        studentId: userId,
        rating: rating,
        feedbackComment: feedbackComment.trim() || null
      };

      const response = await fetch(`${API_BASE}/api/feedback`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-User-Id': String(userId),
          'X-User-Role': userRole
        },
        body: JSON.stringify(requestBody)
      });

      const result = await response.json();

      if (response.ok && result.success) {
        successMessage = result.message || 'Feedback submitted successfully!';
        resetForm();
        onFeedbackSuccess();
      } else if (response.status === 409) {
        errorMessage = 'You have already submitted feedback for this session.';
      } else if (response.status === 400 && result.message && result.message.includes('completed')) {
        errorMessage = 'Feedback can only be submitted for completed sessions.';
      } else {
        errorMessage = result.message || 'Failed to submit feedback. Please try again.';
      }
    } catch (err) {
      errorMessage = 'Network error. Please check your connection and try again.';
    } finally {
      isSubmitting = false;
    }
  }
</script>

<div class="form-section">
  <div class="section-header">
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/>
    </svg>
    <h2>Submit Session Feedback</h2>
  </div>

  <div class="info-banner">
    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <circle cx="12" cy="12" r="10"/>
      <line x1="12" y1="16" x2="12" y2="12"/>
      <line x1="12" y1="8" x2="12.01" y2="8"/>
    </svg>
    <span>Feedback can only be submitted for <strong>completed</strong> mentor sessions. Each session can receive only one feedback submission.</span>
  </div>

  {#if successMessage}
    <div class="alert alert-success">
      <span>{successMessage}</span>
      <button class="alert-dismiss" on:click={() => successMessage = ''}>✕</button>
    </div>
  {/if}

  {#if errorMessage}
    <div class="alert alert-error">
      <span>{errorMessage}</span>
      <button class="alert-dismiss" on:click={() => errorMessage = ''}>✕</button>
    </div>
  {/if}

  <form on:submit|preventDefault={handleSubmit} class="feedback-form">
    <div class="form-group">
      <label for="sessionId">
        Session ID <span class="required">*</span>
      </label>
      <input
        id="sessionId"
        type="number"
        bind:value={sessionId}
        placeholder="Enter the mentor session ID"
        class:input-error={errors.sessionId}
        min="1"
      />
      {#if errors.sessionId}
        <span class="error-text">{errors.sessionId}</span>
      {/if}
    </div>

    <div class="form-group">
      <label>
        Rating <span class="required">*</span>
      </label>
      <div class="star-rating" class:rating-error={errors.rating}>
        {#each [1, 2, 3, 4, 5] as star}
          <button
            type="button"
            class="star-btn"
            class:active={star <= (hoverRating || rating)}
            on:click={() => setRating(star)}
            on:mouseenter={() => hoverRating = star}
            on:mouseleave={() => hoverRating = 0}
            aria-label={`Rate ${star} star${star > 1 ? 's' : ''}`}
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24"
              fill={star <= (hoverRating || rating) ? 'currentColor' : 'none'}
              stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
            </svg>
          </button>
        {/each}
        {#if rating > 0}
          <span class="rating-label">
            {rating === 1 ? 'Poor' : rating === 2 ? 'Fair' : rating === 3 ? 'Good' : rating === 4 ? 'Very Good' : 'Excellent'}
          </span>
        {/if}
      </div>
      {#if errors.rating}
        <span class="error-text">{errors.rating}</span>
      {/if}
    </div>

    <div class="form-group">
      <label for="feedbackComment">
        Feedback Comment
      </label>
      <textarea
        id="feedbackComment"
        bind:value={feedbackComment}
        placeholder="Share your experience with this mentoring session (optional)"
        rows="5"
        class:input-error={errors.feedbackComment}
        maxlength="2000"
      ></textarea>
      <div class="field-footer">
        {#if errors.feedbackComment}
          <span class="error-text">{errors.feedbackComment}</span>
        {/if}
        <span class="char-count">{feedbackComment.length}/2000</span>
      </div>
    </div>

    <button type="submit" class="btn-submit" disabled={isSubmitting}>
      {#if isSubmitting}
        <span class="spinner"></span>
        Submitting...
      {:else}
        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="22" y1="2" x2="11" y2="13"/>
          <polygon points="22 2 15 22 11 13 2 9 22 2"/>
        </svg>
        Submit Feedback
      {/if}
    </button>
  </form>
</div>

<style>
  .form-section {
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
    margin-bottom: 20px;
    color: #1a1a2e;
  }

  .section-header svg {
    color: #4361ee;
  }

  .section-header h2 {
    margin: 0;
    font-size: 1.4rem;
    font-weight: 700;
  }

  .info-banner {
    display: flex;
    align-items: flex-start;
    gap: 10px;
    padding: 14px 18px;
    background: #f0f4ff;
    border: 1px solid #d0daff;
    border-radius: 10px;
    margin-bottom: 20px;
    font-size: 0.88rem;
    color: #3651d4;
  }

  .info-banner svg {
    flex-shrink: 0;
    margin-top: 1px;
  }

  .alert {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 14px 18px;
    border-radius: 10px;
    margin-bottom: 20px;
    font-size: 0.9rem;
    font-weight: 500;
  }

  .alert-success {
    background: #d4edda;
    color: #155724;
    border: 1px solid #c3e6cb;
  }

  .alert-error {
    background: #f8d7da;
    color: #721c24;
    border: 1px solid #f5c6cb;
  }

  .alert-dismiss {
    background: none;
    border: none;
    font-size: 1.1rem;
    cursor: pointer;
    color: inherit;
    opacity: 0.7;
    padding: 0 4px;
  }

  .alert-dismiss:hover {
    opacity: 1;
  }

  .feedback-form {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .form-group {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  .form-group label {
    font-size: 0.88rem;
    font-weight: 600;
    color: #333;
  }

  .required {
    color: #dc3545;
  }

  .form-group input,
  .form-group textarea {
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

  .form-group input:focus,
  .form-group textarea:focus {
    border-color: #4361ee;
    box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.1);
  }

  .input-error {
    border-color: #dc3545 !important;
  }

  .field-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    min-height: 20px;
  }

  .error-text {
    font-size: 0.8rem;
    color: #dc3545;
    font-weight: 500;
  }

  .char-count {
    font-size: 0.75rem;
    color: #999;
    margin-left: auto;
  }

  .star-rating {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 8px 0;
  }

  .rating-error {
    border-bottom: 2px solid #dc3545;
    padding-bottom: 6px;
  }

  .star-btn {
    background: none;
    border: none;
    cursor: pointer;
    color: #d1d5db;
    padding: 2px;
    transition: color 0.15s, transform 0.1s;
    display: flex;
    align-items: center;
  }

  .star-btn:hover {
    transform: scale(1.15);
  }

  .star-btn.active {
    color: #f59e0b;
  }

  .rating-label {
    margin-left: 8px;
    font-size: 0.88rem;
    font-weight: 600;
    color: #f59e0b;
  }

  .btn-submit {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 12px 28px;
    background: #4361ee;
    color: #fff;
    border: none;
    border-radius: 10px;
    font-size: 0.95rem;
    font-weight: 600;
    font-family: 'Inter', sans-serif;
    cursor: pointer;
    transition: background 0.2s, transform 0.1s;
    align-self: flex-start;
  }

  .btn-submit:hover:not(:disabled) {
    background: #3651d4;
    transform: translateY(-1px);
  }

  .btn-submit:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }

  .spinner {
    display: inline-block;
    width: 18px;
    height: 18px;
    border: 2.5px solid rgba(255, 255, 255, 0.3);
    border-top-color: #fff;
    border-radius: 50%;
    animation: spin 0.6s linear infinite;
  }

  @keyframes spin {
    to { transform: rotate(360deg); }
  }

  @media (max-width: 600px) {
    .form-section {
      padding: 20px 16px;
    }

    .section-header h2 {
      font-size: 1.15rem;
    }
  }
</style>
