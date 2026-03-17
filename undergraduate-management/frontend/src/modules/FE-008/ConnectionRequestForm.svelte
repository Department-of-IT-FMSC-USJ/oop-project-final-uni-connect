<script>
  const API_BASE = 'http://localhost:8080';

  export let userId = 1;
  export let userRole = 'STUDENT';
  export let onConnectionSuccess = () => {};

  let mentorId = '';
  let mentorType = '';

  let errors = {};
  let isSubmitting = false;
  let successMessage = '';
  let errorMessage = '';

  const MENTOR_TYPES = [
    { value: 'Academic', label: 'Academic Mentor' },
    { value: 'Industry', label: 'Industry Mentor' }
  ];

  function validateMentorId(value) {
    if (!value || !String(value).trim()) return 'Mentor ID is required';
    if (isNaN(Number(value)) || Number(value) <= 0) return 'Mentor ID must be a positive number';
    return '';
  }

  function validateMentorType(value) {
    if (!value) return 'Please select a mentor type';
    return '';
  }

  function validateAll() {
    const newErrors = {};
    const e1 = validateMentorId(mentorId);
    if (e1) newErrors.mentorId = e1;
    const e2 = validateMentorType(mentorType);
    if (e2) newErrors.mentorType = e2;
    errors = newErrors;
    return Object.keys(newErrors).length === 0;
  }

  function resetForm() {
    mentorId = '';
    mentorType = '';
    errors = {};
  }

  async function handleSubmit() {
    successMessage = '';
    errorMessage = '';

    if (!validateAll()) return;

    isSubmitting = true;

    try {
      const requestBody = {
        studentId: userId,
        mentorId: Number(mentorId),
        mentorType: mentorType
      };

      const response = await fetch(`${API_BASE}/api/mentor/connect`, {
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
        successMessage = result.message || 'Mentor connection request submitted successfully!';
        resetForm();
        onConnectionSuccess();
      } else if (response.status === 400 && result.message && result.message.includes('Insufficient')) {
        errorMessage = 'You need at least 50 contribution points to connect with an industry mentor.';
      } else {
        errorMessage = result.message || 'Failed to submit connection request. Please try again.';
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
      <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
      <circle cx="9" cy="7" r="4"/>
      <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
      <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
    </svg>
    <h2>Request Mentor Connection</h2>
  </div>

  <div class="info-banner">
    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <circle cx="12" cy="12" r="10"/>
      <line x1="12" y1="16" x2="12" y2="12"/>
      <line x1="12" y1="8" x2="12.01" y2="8"/>
    </svg>
    <span>Industry mentor connections require a minimum of <strong>50 contribution points</strong>. Academic mentors are available to all students.</span>
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

  <form on:submit|preventDefault={handleSubmit} class="connection-form">
    <div class="form-row">
      <div class="form-group">
        <label for="mentorId">Mentor ID <span class="required">*</span></label>
        <input id="mentorId" type="number" bind:value={mentorId} placeholder="Enter mentor ID" class:input-error={errors.mentorId} min="1" />
        {#if errors.mentorId}<span class="error-text">{errors.mentorId}</span>{/if}
      </div>

      <div class="form-group">
        <label for="mentorType">Mentor Type <span class="required">*</span></label>
        <select id="mentorType" bind:value={mentorType} class:input-error={errors.mentorType}>
          <option value="">Select mentor type</option>
          {#each MENTOR_TYPES as type}
            <option value={type.value}>{type.label}</option>
          {/each}
        </select>
        {#if errors.mentorType}<span class="error-text">{errors.mentorType}</span>{/if}
      </div>
    </div>

    <button type="submit" class="btn-submit" disabled={isSubmitting}>
      {#if isSubmitting}
        <span class="spinner"></span>
        Requesting...
      {:else}
        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="22" y1="2" x2="11" y2="13"/>
          <polygon points="22 2 15 22 11 13 2 9 22 2"/>
        </svg>
        Request Connection
      {/if}
    </button>
  </form>
</div>

<style>
  .form-section { background: #ffffff; border-radius: 14px; padding: 32px; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06); border: 1px solid #e8ecf1; }
  .section-header { display: flex; align-items: center; gap: 10px; margin-bottom: 20px; color: #1a1a2e; }
  .section-header svg { color: #4361ee; }
  .section-header h2 { margin: 0; font-size: 1.4rem; font-weight: 700; }

  .info-banner { display: flex; align-items: flex-start; gap: 10px; padding: 14px 18px; background: #f0f4ff; border: 1px solid #d0daff; border-radius: 10px; margin-bottom: 20px; font-size: 0.88rem; color: #3651d4; }
  .info-banner svg { flex-shrink: 0; margin-top: 1px; }

  .alert { display: flex; align-items: center; justify-content: space-between; padding: 14px 18px; border-radius: 10px; margin-bottom: 20px; font-size: 0.9rem; font-weight: 500; }
  .alert-success { background: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
  .alert-error { background: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
  .alert-dismiss { background: none; border: none; font-size: 1.1rem; cursor: pointer; color: inherit; opacity: 0.7; padding: 0 4px; }
  .alert-dismiss:hover { opacity: 1; }

  .connection-form { display: flex; flex-direction: column; gap: 20px; }
  .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
  .form-group { display: flex; flex-direction: column; gap: 6px; }
  .form-group label { font-size: 0.88rem; font-weight: 600; color: #333; }
  .required { color: #dc3545; }

  .form-group input, .form-group select {
    padding: 10px 14px; border: 1.5px solid #d1d5db; border-radius: 8px; font-size: 0.9rem;
    font-family: 'Inter', sans-serif; color: #1a1a2e; background: #fafbfc;
    transition: border-color 0.2s, box-shadow 0.2s; outline: none;
  }
  .form-group input:focus, .form-group select:focus { border-color: #4361ee; box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.1); }
  .input-error { border-color: #dc3545 !important; }
  .error-text { font-size: 0.8rem; color: #dc3545; font-weight: 500; }

  .btn-submit {
    display: inline-flex; align-items: center; justify-content: center; gap: 8px;
    padding: 12px 28px; background: #4361ee; color: #fff; border: none; border-radius: 10px;
    font-size: 0.95rem; font-weight: 600; font-family: 'Inter', sans-serif; cursor: pointer;
    transition: background 0.2s, transform 0.1s; align-self: flex-start;
  }
  .btn-submit:hover:not(:disabled) { background: #3651d4; transform: translateY(-1px); }
  .btn-submit:disabled { opacity: 0.7; cursor: not-allowed; }

  .spinner { display: inline-block; width: 18px; height: 18px; border: 2.5px solid rgba(255, 255, 255, 0.3); border-top-color: #fff; border-radius: 50%; animation: spin 0.6s linear infinite; }
  @keyframes spin { to { transform: rotate(360deg); } }

  @media (max-width: 600px) {
    .form-section { padding: 20px 16px; }
    .form-row { grid-template-columns: 1fr; }
    .section-header h2 { font-size: 1.15rem; }
  }
</style>
