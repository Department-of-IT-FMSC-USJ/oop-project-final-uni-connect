<script>
  const API_BASE = 'http://localhost:9092';

  let sessionTitle = '';
  let sessionType = '';
  let sessionTopic = '';
  let sessionDescription = '';
  let sessionDate = '';
  let sessionTime = '';
  let errorMessage = '';
  let successMessage = '';
  let submitting = false;

  export let onSessionCreated = () => {};

  async function handleSubmit() {
    errorMessage = '';
    successMessage = '';

    if (!sessionTitle || sessionTitle.length < 5 || sessionTitle.length > 100) {
      errorMessage = 'Session title must be between 5 and 100 characters.';
      return;
    }
    if (!sessionType) {
      errorMessage = 'Please select a session type.';
      return;
    }
    if (!sessionTopic || sessionTopic.length > 200) {
      errorMessage = 'Session topic is required (max 200 characters).';
      return;
    }
    if (!sessionDate) {
      errorMessage = 'Session date is required.';
      return;
    }
    const today = new Date().toISOString().split('T')[0];
    if (sessionDate <= today) {
      errorMessage = 'Session date must be a future date.';
      return;
    }
    if (!sessionTime) {
      errorMessage = 'Session time is required.';
      return;
    }

    submitting = true;

    try {
      const response = await fetch(`${API_BASE}/api/sessions`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-User-Role': 'MENTOR'
        },
        body: JSON.stringify({
          mentorId: 1,
          sessionTitle,
          sessionType,
          sessionTopic,
          sessionDescription,
          sessionDate,
          sessionTime: sessionTime + ':00'
        })
      });

      const result = await response.json();

      if (response.ok && result.success) {
        successMessage = 'Mentoring session created successfully!';
        sessionTitle = '';
        sessionType = '';
        sessionTopic = '';
        sessionDescription = '';
        sessionDate = '';
        sessionTime = '';
        onSessionCreated();
      } else {
        errorMessage = result.message || 'Failed to create session.';
      }
    } catch (err) {
      errorMessage = 'Network error. Could not create session.';
    } finally {
      submitting = false;
    }
  }
</script>

<div class="form-section">
  <div class="section-header">
    <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
      <line x1="16" y1="2" x2="16" y2="6"/>
      <line x1="8" y1="2" x2="8" y2="6"/>
      <line x1="3" y1="10" x2="21" y2="10"/>
    </svg>
    <h2>Create Mentoring Session</h2>
  </div>

  {#if successMessage}
    <div class="alert alert-success">
      <span>{successMessage}</span>
      <button class="alert-close" on:click={() => successMessage = ''}>✕</button>
    </div>
  {/if}

  {#if errorMessage}
    <div class="alert alert-error">
      <span>{errorMessage}</span>
      <button class="alert-close" on:click={() => errorMessage = ''}>✕</button>
    </div>
  {/if}

  <form on:submit|preventDefault={handleSubmit}>
    <div class="form-group">
      <label for="session-title">Session Title *</label>
      <input id="session-title" type="text" bind:value={sessionTitle} placeholder="e.g., Introduction to Cloud Computing" minlength="5" maxlength="100" />
      <span class="char-count">{sessionTitle.length}/100</span>
    </div>

    <div class="form-row">
      <div class="form-group">
        <label for="session-type">Session Type *</label>
        <select id="session-type" bind:value={sessionType}>
          <option value="">— Select Type —</option>
          <option value="ONE_TO_ONE">One-to-One</option>
          <option value="GROUP">Group</option>
        </select>
      </div>

      <div class="form-group">
        <label for="session-topic">Session Topic *</label>
        <input id="session-topic" type="text" bind:value={sessionTopic} placeholder="e.g., AWS Services" maxlength="200" />
      </div>
    </div>

    <div class="form-row">
      <div class="form-group">
        <label for="session-date">Session Date *</label>
        <input id="session-date" type="date" bind:value={sessionDate} />
      </div>

      <div class="form-group">
        <label for="session-time">Session Time *</label>
        <input id="session-time" type="time" bind:value={sessionTime} />
      </div>
    </div>

    <div class="form-group">
      <label for="session-desc">Session Description</label>
      <textarea id="session-desc" bind:value={sessionDescription} placeholder="Optional description..." maxlength="500" rows="3"></textarea>
      <span class="char-count">{sessionDescription.length}/500</span>
    </div>

    <button type="submit" class="submit-btn" disabled={submitting}>
      {submitting ? 'Creating...' : 'Create Session'}
    </button>
  </form>
</div>

<style>
  .form-section {
    background: #ffffff;
    border-radius: 14px;
    padding: 32px;
    box-shadow: 0 2px 12px rgba(0,0,0,0.06);
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

  .alert {
    padding: 14px 18px; border-radius: 10px; font-size: 0.9rem;
    margin-bottom: 16px; display: flex; justify-content: space-between; align-items: center;
  }
  .alert-success { background: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
  .alert-error { background: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
  .alert-close { background: none; border: none; cursor: pointer; font-size: 1rem; color: inherit; }

  form { display: flex; flex-direction: column; gap: 16px; }

  .form-row { display: flex; gap: 16px; }
  .form-row .form-group { flex: 1; }

  .form-group { display: flex; flex-direction: column; position: relative; }
  .form-group label { font-size: 0.82rem; font-weight: 600; color: #555; margin-bottom: 6px; }

  input, select, textarea {
    padding: 10px 14px; border: 1.5px solid #e8ecf1; border-radius: 8px;
    font-size: 0.88rem; font-family: inherit; transition: border-color 0.2s;
  }
  input:focus, select:focus, textarea:focus {
    outline: none; border-color: #4361ee; box-shadow: 0 0 0 3px rgba(67,97,238,0.1);
  }
  textarea { resize: vertical; }

  .char-count { font-size: 0.72rem; color: #999; text-align: right; margin-top: 2px; }

  .submit-btn {
    background: #4361ee; color: white; border: none; padding: 12px 28px;
    border-radius: 8px; font-size: 0.92rem; font-weight: 600;
    cursor: pointer; transition: background 0.2s; align-self: flex-start;
  }
  .submit-btn:hover { background: #3651d4; }
  .submit-btn:disabled { opacity: 0.6; cursor: not-allowed; }

  @media (max-width: 768px) {
    .form-section { padding: 20px 16px; }
    .form-row { flex-direction: column; }
  }
</style>
