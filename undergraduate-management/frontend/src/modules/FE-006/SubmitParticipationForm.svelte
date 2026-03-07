<script>
  const API_BASE = 'http://localhost:8080';

  export let userId = 1;
  export let userRole = 'STUDENT';
  export let onUploadSuccess = () => {};

  let eventName = '';
  let eventType = '';
  let participationRole = '';
  let achievementDescription = '';
  let selectedFile = null;
  let fileName = '';

  let errors = {};
  let isSubmitting = false;
  let successMessage = '';
  let errorMessage = '';

  const EVENT_TYPES = [
    { value: 'Competition', label: 'Competition' },
    { value: 'Workshop', label: 'Workshop' },
    { value: 'Seminar', label: 'Seminar' },
    { value: 'Organizing Committee', label: 'Organizing Committee' }
  ];

  const PARTICIPATION_ROLES = [
    { value: 'Participant', label: 'Participant' },
    { value: 'Organizer', label: 'Organizer' },
    { value: 'Winner', label: 'Winner' },
    { value: 'Volunteer', label: 'Volunteer' }
  ];

  function validateEventName(value) {
    if (!value || !value.trim()) return 'Event name is required';
    if (value.trim().length < 3) return 'Event name must be at least 3 characters';
    if (value.trim().length > 150) return 'Event name cannot exceed 150 characters';
    return '';
  }

  function validateEventType(value) {
    if (!value) return 'Please select an event type';
    return '';
  }

  function validateParticipationRole(value) {
    if (!value) return 'Please select a participation role';
    return '';
  }

  function validateAchievementDescription(value) {
    if (value && value.length > 2000) return 'Description cannot exceed 2000 characters';
    return '';
  }

  function validateFile(file) {
    if (!file) return 'Evidence file is required (PDF only)';
    if (file.type !== 'application/pdf') return 'Only PDF files are accepted';
    if (file.size > 10 * 1024 * 1024) return 'File size cannot exceed 10MB';
    return '';
  }

  function validateAll() {
    const newErrors = {};
    const e1 = validateEventName(eventName);
    if (e1) newErrors.eventName = e1;
    const e2 = validateEventType(eventType);
    if (e2) newErrors.eventType = e2;
    const e3 = validateParticipationRole(participationRole);
    if (e3) newErrors.participationRole = e3;
    const e4 = validateAchievementDescription(achievementDescription);
    if (e4) newErrors.achievementDescription = e4;
    const e5 = validateFile(selectedFile);
    if (e5) newErrors.file = e5;
    errors = newErrors;
    return Object.keys(newErrors).length === 0;
  }

  function handleFileChange(event) {
    const file = event.target.files[0];
    if (file) {
      selectedFile = file;
      fileName = file.name;
      errors = { ...errors, file: '' };
    }
  }

  function removeFile() {
    selectedFile = null;
    fileName = '';
    const input = document.getElementById('file-input');
    if (input) input.value = '';
  }

  function resetForm() {
    eventName = '';
    eventType = '';
    participationRole = '';
    achievementDescription = '';
    selectedFile = null;
    fileName = '';
    errors = {};
    const input = document.getElementById('file-input');
    if (input) input.value = '';
  }

  async function handleSubmit() {
    successMessage = '';
    errorMessage = '';

    if (!validateAll()) return;

    isSubmitting = true;

    try {
      const participationData = {
        eventName: eventName.trim(),
        eventType,
        participationRole,
        achievementDescription: achievementDescription.trim() || null
      };

      const formData = new FormData();
      formData.append('participation', new Blob([JSON.stringify(participationData)], { type: 'application/json' }));
      formData.append('file', selectedFile);

      const response = await fetch(`${API_BASE}/api/events/participation`, {
        method: 'POST',
        headers: {
          'X-User-Id': String(userId),
          'X-User-Role': userRole
        },
        body: formData
      });

      const result = await response.json();

      if (response.ok && result.success) {
        successMessage = result.message || 'Event participation submitted successfully!';
        resetForm();
        onUploadSuccess();
      } else {
        errorMessage = result.message || 'Failed to submit participation. Please try again.';
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
      <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
      <polyline points="17 8 12 3 7 8"/>
      <line x1="12" y1="3" x2="12" y2="15"/>
    </svg>
    <h2>Submit Event Participation</h2>
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

  <form on:submit|preventDefault={handleSubmit} class="participation-form">
    <div class="form-group">
      <label for="eventName">
        Event Name <span class="required">*</span>
      </label>
      <input
        id="eventName"
        type="text"
        bind:value={eventName}
        placeholder="e.g. National Hackathon 2025"
        class:input-error={errors.eventName}
        maxlength="150"
      />
      <div class="field-footer">
        {#if errors.eventName}
          <span class="error-text">{errors.eventName}</span>
        {/if}
        <span class="char-count">{eventName.length}/150</span>
      </div>
    </div>

    <div class="form-row">
      <div class="form-group">
        <label for="eventType">
          Event Type <span class="required">*</span>
        </label>
        <select
          id="eventType"
          bind:value={eventType}
          class:input-error={errors.eventType}
        >
          <option value="">Select event type</option>
          {#each EVENT_TYPES as type}
            <option value={type.value}>{type.label}</option>
          {/each}
        </select>
        {#if errors.eventType}
          <span class="error-text">{errors.eventType}</span>
        {/if}
      </div>

      <div class="form-group">
        <label for="participationRole">
          Participation Role <span class="required">*</span>
        </label>
        <select
          id="participationRole"
          bind:value={participationRole}
          class:input-error={errors.participationRole}
        >
          <option value="">Select your role</option>
          {#each PARTICIPATION_ROLES as role}
            <option value={role.value}>{role.label}</option>
          {/each}
        </select>
        {#if errors.participationRole}
          <span class="error-text">{errors.participationRole}</span>
        {/if}
      </div>
    </div>

    <div class="form-group">
      <label for="achievementDescription">
        Achievement Description
      </label>
      <textarea
        id="achievementDescription"
        bind:value={achievementDescription}
        placeholder="Describe your achievement or contribution (optional)"
        rows="4"
        class:input-error={errors.achievementDescription}
        maxlength="2000"
      ></textarea>
      <div class="field-footer">
        {#if errors.achievementDescription}
          <span class="error-text">{errors.achievementDescription}</span>
        {/if}
        <span class="char-count">{achievementDescription.length}/2000</span>
      </div>
    </div>

    <div class="form-group">
      <label>
        Evidence File (PDF) <span class="required">*</span>
      </label>
      <div
        class="file-drop-area"
        class:input-error={errors.file}
        on:click={() => document.getElementById('file-input').click()}
        on:keydown={(e) => e.key === 'Enter' && document.getElementById('file-input').click()}
        role="button"
        tabindex="0"
      >
        {#if fileName}
          <div class="file-selected">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
            </svg>
            <span class="file-name">{fileName}</span>
            <button type="button" class="btn-remove-file" on:click|stopPropagation={removeFile}>✕</button>
          </div>
        {:else}
          <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
            <polyline points="17 8 12 3 7 8"/>
            <line x1="12" y1="3" x2="12" y2="15"/>
          </svg>
          <p class="drop-text">Click to upload evidence file</p>
          <p class="drop-hint">PDF only, max 10MB</p>
        {/if}
      </div>
      <input
        id="file-input"
        type="file"
        accept=".pdf"
        on:change={handleFileChange}
        style="display: none;"
      />
      {#if errors.file}
        <span class="error-text">{errors.file}</span>
      {/if}
    </div>

    <button type="submit" class="btn-submit" disabled={isSubmitting}>
      {#if isSubmitting}
        <span class="spinner"></span>
        Submitting...
      {:else}
        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="20 6 9 17 4 12"/>
        </svg>
        Submit Participation
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
    margin-bottom: 24px;
    color: #1a1a2e;
  }

  .section-header svg { color: #4361ee; }
  .section-header h2 { margin: 0; font-size: 1.4rem; font-weight: 700; }

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
    background: none;
    border: none;
    font-size: 1.1rem;
    cursor: pointer;
    color: inherit;
    opacity: 0.7;
    padding: 0 4px;
  }

  .alert-dismiss:hover { opacity: 1; }

  .participation-form { display: flex; flex-direction: column; gap: 20px; }

  .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }

  .form-group { display: flex; flex-direction: column; gap: 6px; }
  .form-group label { font-size: 0.88rem; font-weight: 600; color: #333; }
  .required { color: #dc3545; }

  .form-group input,
  .form-group select,
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
  .form-group select:focus,
  .form-group textarea:focus {
    border-color: #4361ee;
    box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.1);
  }

  .input-error { border-color: #dc3545 !important; }

  .field-footer { display: flex; justify-content: space-between; align-items: center; min-height: 20px; }
  .error-text { font-size: 0.8rem; color: #dc3545; font-weight: 500; }
  .char-count { font-size: 0.75rem; color: #999; margin-left: auto; }

  .file-drop-area {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 28px 20px;
    border: 2px dashed #d1d5db;
    border-radius: 10px;
    background: #fafbfc;
    cursor: pointer;
    transition: border-color 0.2s, background 0.2s;
    text-align: center;
  }

  .file-drop-area:hover { border-color: #4361ee; background: #f0f4ff; }
  .file-drop-area svg { color: #999; margin-bottom: 8px; }
  .drop-text { font-size: 0.9rem; font-weight: 600; color: #555; margin: 0; }
  .drop-hint { font-size: 0.8rem; color: #999; margin: 4px 0 0; }

  .file-selected { display: flex; align-items: center; gap: 10px; }
  .file-selected svg { color: #4361ee; margin-bottom: 0; }
  .file-name { font-size: 0.9rem; font-weight: 600; color: #333; }

  .btn-remove-file {
    background: none;
    border: none;
    font-size: 1rem;
    color: #dc3545;
    cursor: pointer;
    padding: 2px 6px;
    border-radius: 4px;
    transition: background 0.2s;
  }

  .btn-remove-file:hover { background: #fde8ea; }

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

  .btn-submit:hover:not(:disabled) { background: #3651d4; transform: translateY(-1px); }
  .btn-submit:disabled { opacity: 0.7; cursor: not-allowed; }

  .spinner {
    display: inline-block;
    width: 18px;
    height: 18px;
    border: 2.5px solid rgba(255, 255, 255, 0.3);
    border-top-color: #fff;
    border-radius: 50%;
    animation: spin 0.6s linear infinite;
  }

  @keyframes spin { to { transform: rotate(360deg); } }

  @media (max-width: 600px) {
    .form-section { padding: 20px 16px; }
    .form-row { grid-template-columns: 1fr; }
    .section-header h2 { font-size: 1.15rem; }
  }
</style>
