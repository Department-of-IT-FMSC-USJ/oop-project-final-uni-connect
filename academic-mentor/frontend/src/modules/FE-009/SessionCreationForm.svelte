<script>
  const API_BASE = 'http://localhost:9090';

  export let mentorId = 1;
  export let onSessionCreated = () => {};

  let sessionTitle = '';
  let sessionType = '';
  let sessionTopic = '';
  let sessionDescription = '';
  let sessionDate = '';
  let sessionTime = '';

  let errors = {};
  let isSubmitting = false;
  let successMessage = '';
  let errorMessage = '';

  function validateTitle(value) {
    if (!value || !value.trim()) return 'Session title is required';
    if (value.trim().length < 3) return 'Session title must be at least 3 characters';
    if (value.trim().length > 100) return 'Session title cannot exceed 100 characters';
    return '';
  }

  function validateType(value) {
    if (!value) return 'Session type is required';
    return '';
  }

  function validateTopic(value) {
    if (!value || !value.trim()) return 'Session topic is required';
    if (value.trim().length > 200) return 'Session topic cannot exceed 200 characters';
    return '';
  }

  function validateDate(value) {
    if (!value) return 'Session date is required';
    const selected = new Date(value);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    if (selected <= today) return 'Session date must be a future date';
    return '';
  }

  function validateTime(value) {
    if (!value) return 'Session time is required';
    return '';
  }

  function validateDescription(value) {
    if (value && value.length > 500) return 'Description cannot exceed 500 characters';
    return '';
  }

  function validateAll() {
    const newErrors = {};
    const e1 = validateTitle(sessionTitle); if (e1) newErrors.sessionTitle = e1;
    const e2 = validateType(sessionType); if (e2) newErrors.sessionType = e2;
    const e3 = validateTopic(sessionTopic); if (e3) newErrors.sessionTopic = e3;
    const e4 = validateDate(sessionDate); if (e4) newErrors.sessionDate = e4;
    const e5 = validateTime(sessionTime); if (e5) newErrors.sessionTime = e5;
    const e6 = validateDescription(sessionDescription); if (e6) newErrors.sessionDescription = e6;
    errors = newErrors;
    return Object.keys(newErrors).length === 0;
  }

  function resetForm() {
    sessionTitle = '';
    sessionType = '';
    sessionTopic = '';
    sessionDescription = '';
    sessionDate = '';
    sessionTime = '';
    errors = {};
  }

  async function handleSubmit() {
    successMessage = '';
    errorMessage = '';

    if (!validateAll()) return;

    isSubmitting = true;

    try {
      const requestBody = {
        mentorId: mentorId,
        sessionTitle: sessionTitle.trim(),
        sessionType: sessionType,
        sessionTopic: sessionTopic.trim(),
        sessionDescription: sessionDescription.trim() || null,
        sessionDate: sessionDate,
        sessionTime: sessionTime + ':00'
      };

      const response = await fetch(`${API_BASE}/api/sessions`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-User-Id': String(mentorId),
          'X-User-Role': 'MENTOR'
        },
        body: JSON.stringify(requestBody)
      });

      const result = await response.json();

      if (response.ok && result.success) {
        successMessage = result.message || 'Mentoring session created successfully!';
        resetForm();
        onSessionCreated();
      } else if (response.status === 409) {
        errorMessage = 'A session already exists at this date and time.';
      } else {
        errorMessage = result.message || 'Failed to create session. Please try again.';
      }
    } catch (err) {
      errorMessage = 'Network error. Please check your connection and try again.';
    } finally {
      isSubmitting = false;
    }
  }

  function getTodayDate() {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    return tomorrow.toISOString().split('T')[0];
  }
</script>

<div class="form-section">
  <div class="section-header">
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
      <line x1="16" y1="2" x2="16" y2="6"/>
      <line x1="8" y1="2" x2="8" y2="6"/>
      <line x1="3" y1="10" x2="21" y2="10"/>
    </svg>
    <h2>Create Mentoring Session</h2>
  </div>

  <div class="info-banner">
    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <circle cx="12" cy="12" r="10"/>
      <line x1="12" y1="16" x2="12" y2="12"/>
      <line x1="12" y1="8" x2="12.01" y2="8"/>
    </svg>
    <span>Schedule <strong>one-to-one</strong> or <strong>group</strong> mentoring sessions. Session dates must be in the future.</span>
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

  <form on:submit|preventDefault={handleSubmit} class="session-form">
    <div class="form-row">
      <div class="form-group">
        <label for="sessionTitle">
          Session Title <span class="required">*</span>
        </label>
        <input
          id="sessionTitle"
          type="text"
          bind:value={sessionTitle}
          placeholder="Enter session title"
          class:input-error={errors.sessionTitle}
          maxlength="100"
        />
        {#if errors.sessionTitle}
          <span class="error-text">{errors.sessionTitle}</span>
        {/if}
        <span class="char-count">{sessionTitle.length}/100</span>
      </div>

      <div class="form-group">
        <label for="sessionType">
          Session Type <span class="required">*</span>
        </label>
        <select
          id="sessionType"
          bind:value={sessionType}
          class:input-error={errors.sessionType}
        >
          <option value="">Select session type</option>
          <option value="ONE_TO_ONE">One-to-One</option>
          <option value="GROUP">Group</option>
        </select>
        {#if errors.sessionType}
          <span class="error-text">{errors.sessionType}</span>
        {/if}
      </div>
    </div>

    <div class="form-group">
      <label for="sessionTopic">
        Session Topic <span class="required">*</span>
      </label>
      <input
        id="sessionTopic"
        type="text"
        bind:value={sessionTopic}
        placeholder="Enter the topic to be discussed"
        class:input-error={errors.sessionTopic}
        maxlength="200"
      />
      <div class="field-footer">
        {#if errors.sessionTopic}
          <span class="error-text">{errors.sessionTopic}</span>
        {/if}
        <span class="char-count">{sessionTopic.length}/200</span>
      </div>
    </div>

    <div class="form-row">
      <div class="form-group">
        <label for="sessionDate">
          Session Date <span class="required">*</span>
        </label>
        <input
          id="sessionDate"
          type="date"
          bind:value={sessionDate}
          min={getTodayDate()}
          class:input-error={errors.sessionDate}
        />
        {#if errors.sessionDate}
          <span class="error-text">{errors.sessionDate}</span>
        {/if}
      </div>

      <div class="form-group">
        <label for="sessionTime">
          Session Time <span class="required">*</span>
        </label>
        <input
          id="sessionTime"
          type="time"
          bind:value={sessionTime}
          class:input-error={errors.sessionTime}
        />
        {#if errors.sessionTime}
          <span class="error-text">{errors.sessionTime}</span>
        {/if}
      </div>
    </div>

    <div class="form-group">
      <label for="sessionDescription">
        Session Description
      </label>
      <textarea
        id="sessionDescription"
        bind:value={sessionDescription}
        placeholder="Provide additional details about the session (optional)"
        rows="4"
        class:input-error={errors.sessionDescription}
        maxlength="500"
      ></textarea>
      <div class="field-footer">
        {#if errors.sessionDescription}
          <span class="error-text">{errors.sessionDescription}</span>
        {/if}
        <span class="char-count">{sessionDescription.length}/500</span>
      </div>
    </div>

    <button type="submit" class="btn-submit" disabled={isSubmitting}>
      {#if isSubmitting}
        <span class="spinner"></span>
        Creating Session...
      {:else}
        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="12" y1="5" x2="12" y2="19"/>
          <line x1="5" y1="12" x2="19" y2="12"/>
        </svg>
        Create Session
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

  .section-header svg { color: #4361ee; }

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

  .info-banner svg { flex-shrink: 0; margin-top: 1px; }

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

  .alert-success { background: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
  .alert-error { background: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }

  .alert-dismiss {
    background: none; border: none; font-size: 1.1rem;
    cursor: pointer; color: inherit; opacity: 0.7; padding: 0 4px;
  }
  .alert-dismiss:hover { opacity: 1; }

  .session-form {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .form-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
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

  .required { color: #dc3545; }

  .form-group input,
  .form-group textarea,
  .form-group select {
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
  .form-group textarea:focus,
  .form-group select:focus {
    border-color: #4361ee;
    box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.1);
  }

  .input-error { border-color: #dc3545 !important; }

  .field-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    min-height: 20px;
  }

  .error-text { font-size: 0.8rem; color: #dc3545; font-weight: 500; }
  .char-count { font-size: 0.75rem; color: #999; margin-left: auto; }

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

  .btn-submit:disabled { opacity: 0.7; cursor: not-allowed; }

  .spinner {
    display: inline-block; width: 18px; height: 18px;
    border: 2.5px solid rgba(255, 255, 255, 0.3);
    border-top-color: #fff; border-radius: 50%;
    animation: spin 0.6s linear infinite;
  }

  @keyframes spin { to { transform: rotate(360deg); } }

  @media (max-width: 600px) {
    .form-section { padding: 20px 16px; }
    .section-header h2 { font-size: 1.15rem; }
    .form-row { grid-template-columns: 1fr; }
  }
</style>
