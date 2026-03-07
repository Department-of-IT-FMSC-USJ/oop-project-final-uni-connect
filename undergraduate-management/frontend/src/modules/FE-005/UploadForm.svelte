<script>
  import { onMount } from 'svelte';

  const API_BASE = 'http://localhost:8080';

  // --- State ---
  let title = '';
  let description = '';
  let materialType = '';
  let file = null;
  let resourceUrl = '';

  let errors = {};
  let submitMessage = '';
  let submitSuccess = false;
  let isSubmitting = false;

  // --- Props ---
  export let isLoggedIn = false;
  export let userId = null;
  export let userRole = '';
  export let onUploadSuccess = () => {};

  // --- Validation ---
  function validateTitle(value) {
    if (!value || !value.trim()) return 'Title is required';
    if (value.trim().length < 3) return 'Title must be at least 3 characters';
    if (value.trim().length > 100) return 'Title must not exceed 100 characters';
    if (!/[a-zA-Z]/.test(value)) return 'Title cannot contain only numbers or special characters';
    return '';
  }

  function validateDescription(value) {
    if (value && value.length > 500) return 'Description cannot exceed 500 characters';
    return '';
  }

  function validateMaterialType(value) {
    if (!value) return 'Material type is required';
    return '';
  }

  function validateFile(f) {
    if (!f) return 'File is required';
    if (!f.name.toLowerCase().endsWith('.pdf')) return 'Only PDF files are accepted';
    if (f.size > 20 * 1024 * 1024) return 'File size must not exceed 20MB';
    return '';
  }

  function validateResourceUrl(value) {
    if (!value || !value.trim()) return '';
    const trimmed = value.trim();
    if (!trimmed.startsWith('http://') && !trimmed.startsWith('https://')) {
      return 'URL must start with http:// or https://';
    }
    try {
      new URL(trimmed);
    } catch {
      return 'Please enter a valid URL';
    }
    return '';
  }

  function validateAll() {
    const newErrors = {};

    const titleErr = validateTitle(title);
    if (titleErr) newErrors.title = titleErr;

    const descErr = validateDescription(description);
    if (descErr) newErrors.description = descErr;

    const typeErr = validateMaterialType(materialType);
    if (typeErr) newErrors.materialType = typeErr;

    const fileErr = validateFile(file);
    if (fileErr) newErrors.file = fileErr;

    const urlErr = validateResourceUrl(resourceUrl);
    if (urlErr) newErrors.resourceUrl = urlErr;

    errors = newErrors;
    return Object.keys(newErrors).length === 0;
  }

  // --- File Handling ---
  function handleFileChange(e) {
    const selected = e.target.files[0];
    if (selected) {
      if (!selected.name.toLowerCase().endsWith('.pdf')) {
        errors = { ...errors, file: 'Only PDF files are accepted' };
        file = null;
        e.target.value = '';
        return;
      }
      if (selected.size > 20 * 1024 * 1024) {
        errors = { ...errors, file: 'File size must not exceed 20MB' };
        file = null;
        e.target.value = '';
        return;
      }
      file = selected;
      const { file: _, ...rest } = errors;
      errors = rest;
    }
  }

  // --- Submit ---
  async function handleSubmit() {
    submitMessage = '';
    submitSuccess = false;

    if (!validateAll()) return;

    isSubmitting = true;

    try {
      const materialData = {
        title: title.trim(),
        description: description.trim() || null,
        materialType: materialType
      };

      const formData = new FormData();
      formData.append('material', new Blob([JSON.stringify(materialData)], { type: 'application/json' }));
      formData.append('file', file);

      const response = await fetch(`${API_BASE}/api/materials/upload`, {
        method: 'POST',
        headers: {
          'X-User-Id': String(userId),
          'X-User-Role': userRole
        },
        body: formData
      });

      const result = await response.json();

      if (response.ok && result.success) {
        submitSuccess = true;
        submitMessage = result.message || 'Study material uploaded successfully!';
        resetForm();
        onUploadSuccess();
      } else {
        submitSuccess = false;
        submitMessage = result.message || 'Upload failed. Please try again.';
      }
    } catch (err) {
      submitSuccess = false;
      submitMessage = 'Network error. Please check your connection and try again.';
    } finally {
      isSubmitting = false;
    }
  }

  function resetForm() {
    title = '';
    description = '';
    materialType = '';
    file = null;
    resourceUrl = '';
    errors = {};
    const fileInput = document.getElementById('file-input');
    if (fileInput) fileInput.value = '';
  }

  $: descCharCount = description.length;
  $: titleCharCount = title.trim().length;
</script>

{#if !isLoggedIn}
  <div class="auth-banner">
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
      <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
    </svg>
    <span>Please login to upload study materials</span>
  </div>
{:else}
  <div class="upload-section">
    <div class="section-header">
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
        <polyline points="17 8 12 3 7 8"/>
        <line x1="12" y1="3" x2="12" y2="15"/>
      </svg>
      <h2>Upload Study Material</h2>
    </div>

    {#if submitMessage}
      <div class="alert" class:alert-success={submitSuccess} class:alert-error={!submitSuccess}>
        <span>{submitMessage}</span>
        <button class="alert-close" on:click={() => submitMessage = ''}>×</button>
      </div>
    {/if}

    <form on:submit|preventDefault={handleSubmit} novalidate>
      <div class="form-group">
        <label for="title">
          Material Title <span class="required">*</span>
        </label>
        <input
          id="title"
          type="text"
          bind:value={title}
          placeholder="e.g. Data Structures - Linked Lists Notes"
          class:input-error={errors.title}
          maxlength="100"
        />
        <div class="field-footer">
          {#if errors.title}
            <span class="error-text">{errors.title}</span>
          {/if}
          <span class="char-count" class:char-warning={titleCharCount > 90}>{titleCharCount}/100</span>
        </div>
      </div>

      <div class="form-group">
        <label for="description">
          Material Description <span class="optional">(optional)</span>
        </label>
        <textarea
          id="description"
          bind:value={description}
          placeholder="Briefly describe the study material..."
          class:input-error={errors.description}
          rows="3"
          maxlength="500"
        ></textarea>
        <div class="field-footer">
          {#if errors.description}
            <span class="error-text">{errors.description}</span>
          {/if}
          <span class="char-count" class:char-warning={descCharCount > 450}>{descCharCount}/500</span>
        </div>
      </div>

      <div class="form-group">
        <label for="material-type">
          Material Type <span class="required">*</span>
        </label>
        <select
          id="material-type"
          bind:value={materialType}
          class:input-error={errors.materialType}
        >
          <option value="" disabled>Select material type</option>
          <option value="NOTES">Notes</option>
          <option value="PAST_PAPERS">Past Papers</option>
          <option value="LECTURE_SLIDES">Lecture Slides</option>
          <option value="OTHER">Other</option>
        </select>
        {#if errors.materialType}
          <span class="error-text">{errors.materialType}</span>
        {/if}
      </div>

      <div class="form-group">
        <label for="file-input">
          Upload File <span class="required">*</span>
        </label>
        <div class="file-upload-area" class:input-error={errors.file}>
          <input
            id="file-input"
            type="file"
            accept=".pdf"
            on:change={handleFileChange}
          />
          <div class="file-upload-content">
            <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
              <line x1="12" y1="18" x2="12" y2="12"/>
              <line x1="9" y1="15" x2="15" y2="15"/>
            </svg>
            {#if file}
              <p class="file-name">{file.name}</p>
              <p class="file-size">{(file.size / (1024 * 1024)).toFixed(2)} MB</p>
            {:else}
              <p>Click to choose a PDF file or drag and drop</p>
              <p class="file-hint">PDF only, max 20MB</p>
            {/if}
          </div>
        </div>
        {#if errors.file}
          <span class="error-text">{errors.file}</span>
        {/if}
      </div>

      <div class="form-group">
        <label for="resource-url">
          Resource URL <span class="optional">(optional)</span>
        </label>
        <input
          id="resource-url"
          type="url"
          bind:value={resourceUrl}
          placeholder="https://example.com/resource"
          class:input-error={errors.resourceUrl}
        />
        {#if errors.resourceUrl}
          <span class="error-text">{errors.resourceUrl}</span>
        {/if}
      </div>

      <button type="submit" class="btn-upload" disabled={isSubmitting}>
        {#if isSubmitting}
          <span class="spinner"></span>
          Uploading...
        {:else}
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
            <polyline points="17 8 12 3 7 8"/>
            <line x1="12" y1="3" x2="12" y2="15"/>
          </svg>
          Upload Material
        {/if}
      </button>
    </form>
  </div>
{/if}

<style>
  .auth-banner {
    display: flex;
    align-items: center;
    gap: 10px;
    background: #fef3cd;
    border: 1px solid #ffc107;
    border-radius: 10px;
    padding: 16px 20px;
    color: #856404;
    font-size: 0.95rem;
    font-weight: 500;
  }

  .upload-section {
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

  .section-header h2 {
    margin: 0;
    font-size: 1.4rem;
    font-weight: 700;
  }

  .section-header svg {
    color: #4361ee;
  }

  .alert {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    border-radius: 8px;
    margin-bottom: 20px;
    font-size: 0.9rem;
    font-weight: 500;
  }

  .alert-success {
    background: #d4edda;
    border: 1px solid #28a745;
    color: #155724;
  }

  .alert-error {
    background: #f8d7da;
    border: 1px solid #dc3545;
    color: #721c24;
  }

  .alert-close {
    background: none;
    border: none;
    font-size: 1.2rem;
    cursor: pointer;
    color: inherit;
    padding: 0 4px;
  }

  form {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .form-group {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  label {
    font-weight: 600;
    font-size: 0.9rem;
    color: #333;
  }

  .required {
    color: #dc3545;
  }

  .optional {
    color: #888;
    font-weight: 400;
    font-size: 0.82rem;
  }

  input[type="text"],
  input[type="url"],
  textarea,
  select {
    padding: 10px 14px;
    border: 1.5px solid #d1d5db;
    border-radius: 8px;
    font-size: 0.92rem;
    font-family: inherit;
    color: #333;
    background: #fafbfc;
    transition: border-color 0.2s, box-shadow 0.2s;
  }

  input[type="text"]:focus,
  input[type="url"]:focus,
  textarea:focus,
  select:focus {
    outline: none;
    border-color: #4361ee;
    box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.12);
    background: #fff;
  }

  .input-error {
    border-color: #dc3545 !important;
    box-shadow: 0 0 0 3px rgba(220, 53, 69, 0.1) !important;
  }

  textarea {
    resize: vertical;
    min-height: 80px;
  }

  select {
    cursor: pointer;
    appearance: auto;
  }

  .field-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    min-height: 20px;
  }

  .char-count {
    font-size: 0.78rem;
    color: #999;
    margin-left: auto;
  }

  .char-warning {
    color: #e67e22;
    font-weight: 600;
  }

  .error-text {
    color: #dc3545;
    font-size: 0.82rem;
    font-weight: 500;
  }

  .file-upload-area {
    position: relative;
    border: 2px dashed #d1d5db;
    border-radius: 10px;
    padding: 24px;
    text-align: center;
    background: #fafbfc;
    transition: border-color 0.2s, background 0.2s;
    cursor: pointer;
  }

  .file-upload-area:hover {
    border-color: #4361ee;
    background: #f0f4ff;
  }

  .file-upload-area input[type="file"] {
    position: absolute;
    inset: 0;
    opacity: 0;
    cursor: pointer;
  }

  .file-upload-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;
    pointer-events: none;
    color: #666;
  }

  .file-upload-content svg {
    color: #4361ee;
  }

  .file-upload-content p {
    margin: 0;
    font-size: 0.88rem;
  }

  .file-name {
    font-weight: 600;
    color: #333;
  }

  .file-size {
    color: #888;
    font-size: 0.82rem !important;
  }

  .file-hint {
    color: #aaa;
    font-size: 0.8rem !important;
  }

  .btn-upload {
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
    cursor: pointer;
    transition: background 0.2s, transform 0.1s;
    align-self: flex-start;
  }

  .btn-upload:hover:not(:disabled) {
    background: #3651d4;
    transform: translateY(-1px);
  }

  .btn-upload:disabled {
    opacity: 0.65;
    cursor: not-allowed;
  }

  .spinner {
    display: inline-block;
    width: 16px;
    height: 16px;
    border: 2px solid rgba(255, 255, 255, 0.3);
    border-top-color: #fff;
    border-radius: 50%;
    animation: spin 0.6s linear infinite;
  }

  @keyframes spin {
    to { transform: rotate(360deg); }
  }

  @media (max-width: 600px) {
    .upload-section {
      padding: 20px 16px;
    }

    .section-header h2 {
      font-size: 1.15rem;
    }

    .btn-upload {
      width: 100%;
    }
  }
</style>
