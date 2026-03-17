<script>
  const API_BASE = 'http://localhost:9092';

  let suggestionTitle = '';
  let category = '';
  let description = '';
  let suggestedCourse = '';
  let errorMessage = '';
  let successMessage = '';
  let submitting = false;

  export let onSuggestionCreated = () => {};

  async function handleSubmit() {
    errorMessage = '';
    successMessage = '';

    if (!suggestionTitle || suggestionTitle.length < 5 || suggestionTitle.length > 120) {
      errorMessage = 'Suggestion title must be between 5 and 120 characters.';
      return;
    }
    if (!category) {
      errorMessage = 'Please select a category.';
      return;
    }
    if (!description || description.trim().length < 10) {
      errorMessage = 'Description is required and must be meaningful (at least 10 characters).';
      return;
    }

    submitting = true;

    try {
      const response = await fetch(`${API_BASE}/api/suggestions`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-User-Role': 'MENTOR'
        },
        body: JSON.stringify({
          mentorId: 1,
          suggestionTitle,
          category,
          description,
          suggestedCourse: suggestedCourse || null
        })
      });

      const result = await response.json();

      if (response.ok && result.success) {
        successMessage = 'Curriculum suggestion submitted successfully!';
        suggestionTitle = '';
        category = '';
        description = '';
        suggestedCourse = '';
        onSuggestionCreated();
      } else {
        errorMessage = result.message || 'Failed to submit suggestion.';
      }
    } catch (err) {
      errorMessage = 'Network error. Could not submit suggestion.';
    } finally {
      submitting = false;
    }
  }
</script>

<div class="form-section">
  <div class="section-header">
    <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M12 20h9"/>
      <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/>
    </svg>
    <h2>Submit Curriculum Suggestion</h2>
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
      <label for="sug-title">Suggestion Title *</label>
      <input id="sug-title" type="text" bind:value={suggestionTitle} placeholder="e.g., Add Machine Learning Elective" minlength="5" maxlength="120" />
      <span class="char-count">{suggestionTitle.length}/120</span>
    </div>

    <div class="form-group">
      <label for="sug-category">Category *</label>
      <select id="sug-category" bind:value={category}>
        <option value="">— Select Category —</option>
        <option value="NEW_COURSE">New Course</option>
        <option value="COURSE_IMPROVEMENT">Course Improvement</option>
        <option value="SKILL_DEVELOPMENT">Skill Development</option>
        <option value="TECHNOLOGY_UPDATE">Technology Update</option>
      </select>
    </div>

    <div class="form-group">
      <label for="sug-desc">Description *</label>
      <textarea id="sug-desc" bind:value={description} placeholder="Describe your suggestion in detail..." maxlength="1000" rows="5"></textarea>
      <span class="char-count">{description.length}/1000</span>
    </div>

    <div class="form-group">
      <label for="sug-course">Suggested Course / Subject (Optional)</label>
      <input id="sug-course" type="text" bind:value={suggestedCourse} placeholder="e.g., CS401 - Advanced AI" maxlength="100" />
    </div>

    <button type="submit" class="submit-btn" disabled={submitting}>
      {submitting ? 'Submitting...' : 'Submit Suggestion'}
    </button>
  </form>
</div>

<style>
  .form-section { background: #fff; border-radius: 14px; padding: 32px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); border: 1px solid #e8ecf1; }
  .section-header { display: flex; align-items: center; gap: 10px; margin-bottom: 24px; color: #1a1a2e; }
  .section-header svg { color: #4361ee; }
  .section-header h2 { margin: 0; font-size: 1.4rem; font-weight: 700; }
  .alert { padding: 14px 18px; border-radius: 10px; font-size: 0.9rem; margin-bottom: 16px; display: flex; justify-content: space-between; align-items: center; }
  .alert-success { background: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
  .alert-error { background: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
  .alert-close { background: none; border: none; cursor: pointer; font-size: 1rem; color: inherit; }
  form { display: flex; flex-direction: column; gap: 16px; }
  .form-group { display: flex; flex-direction: column; }
  .form-group label { font-size: 0.82rem; font-weight: 600; color: #555; margin-bottom: 6px; }
  input, select, textarea { padding: 10px 14px; border: 1.5px solid #e8ecf1; border-radius: 8px; font-size: 0.88rem; font-family: inherit; transition: border-color 0.2s; }
  input:focus, select:focus, textarea:focus { outline: none; border-color: #4361ee; box-shadow: 0 0 0 3px rgba(67,97,238,0.1); }
  textarea { resize: vertical; }
  .char-count { font-size: 0.72rem; color: #999; text-align: right; margin-top: 2px; }
  .submit-btn { background: #4361ee; color: white; border: none; padding: 12px 28px; border-radius: 8px; font-size: 0.92rem; font-weight: 600; cursor: pointer; transition: background 0.2s; align-self: flex-start; }
  .submit-btn:hover { background: #3651d4; }
  .submit-btn:disabled { opacity: 0.6; cursor: not-allowed; }
</style>
