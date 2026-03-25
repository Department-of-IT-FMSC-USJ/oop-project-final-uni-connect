<script>
  import { onMount } from 'svelte';
  import { api, getCurrentUser, getRoleDashboardPath } from '../../lib/api.js';
  import { industryMentorNavItems } from '../../lib/navigation.js';
  import DashboardLayout from '../shared/DashboardLayout.svelte';
  import CustomSelect from '../shared/CustomSelect.svelte';

  let user = getCurrentUser();
  let suggestions = [];
  let loading = true;
  let error = '';
  let submissionOpen = false;
  let pollingTimer = null;
  let form = {
    title: '',
    category: 'COURSE_IMPROVEMENT',
    description: '',
    suggestedCourse: ''
  };

  const categoryOptions = [
    { value: 'COURSE_IMPROVEMENT', label: 'Course Improvement' },
    { value: 'NEW_COURSE', label: 'New Course' },
    { value: 'SKILL_DEVELOPMENT', label: 'Skill Development' },
    { value: 'TECHNOLOGY_UPDATE', label: 'Technology Update' }
  ];

  onMount(() => {
    if (!user) { window.location.href = '/'; return; }
    if (user.role !== 'INDUSTRY_MENTOR') { window.location.href = getRoleDashboardPath(user.role); return; }
    loadAll();
    pollingTimer = window.setInterval(() => loadAll(), 15000);
    return () => {
      if (pollingTimer) {
        window.clearInterval(pollingTimer);
      }
    };
  });

  async function loadAll() {
    await Promise.all([loadSuggestions(), loadSubmissionWindow()]);
  }

  async function loadSuggestions() {
    loading = true;
    error = '';
    try {
      const res = await api.get(`/suggestions/mentor/${user.userId || user.id}`, { cache: false });
      suggestions = res.data || [];
    } catch (e) {
      error = e?.data?.message || 'Failed to load suggestions.';
    } finally {
      loading = false;
    }
  }

  async function loadSubmissionWindow() {
    try {
      const res = await api.get('/suggestions/submission-window', { cache: false });
      submissionOpen = !!res?.data?.open;
    } catch (e) {
      submissionOpen = false;
    }
  }

  async function submitSuggestion() {
    error = '';
    if (!submissionOpen) {
      error = 'Suggestions are currently closed. Wait until HOD opens the request window.';
      return;
    }
    try {
      await api.post('/suggestions', {
        mentorId: user.userId || user.id,
        suggestionTitle: form.title,
        category: form.category,
        description: form.description,
        suggestedCourse: form.suggestedCourse
      });
      form = { title: '', category: 'COURSE_IMPROVEMENT', description: '', suggestedCourse: '' };
      await loadAll();
    } catch (e) {
      error = e?.data?.message || 'Failed to submit suggestion.';
    }
  }
</script>

<DashboardLayout navItems={industryMentorNavItems} activeItem="suggestions" pageTitle="Curriculum Suggestions">
  <div class="page-grid">
    <section class="card">
      <p class="eyebrow">Industry Mentor</p>
      <h2>Submit Curriculum Suggestion</h2>
      <p class="window-status" class:open={submissionOpen}>
        {submissionOpen ? 'HOD request is active. You can submit suggestions now.' : 'Submissions are currently closed. Wait for HOD to request suggestions.'}
      </p>
      <div class="form-grid">
        <div class="form-group">
          <label>Title</label>
          <input class="input" bind:value={form.title} disabled={!submissionOpen} />
        </div>
        <div class="form-group">
          <label>Category</label>
          <CustomSelect options={categoryOptions} bind:value={form.category} disabled={!submissionOpen} />
        </div>
        <div class="form-group">
          <label>Suggested Course</label>
          <input class="input" bind:value={form.suggestedCourse} disabled={!submissionOpen} />
        </div>
        <div class="form-group full-width">
          <label>Description</label>
          <textarea class="input" rows="5" bind:value={form.description} disabled={!submissionOpen}></textarea>
        </div>
      </div>
      <div class="actions"><button class="btn btn-success" on:click={submitSuggestion} disabled={!submissionOpen}>Submit</button></div>
      {#if error}<div class="alert alert-error">{error}</div>{/if}
    </section>

    <section class="card">
      <h2>Your Submitted Suggestions</h2>
      {#if loading}
        <p class="empty-state">Loading suggestions...</p>
      {:else if suggestions.length === 0}
        <p class="empty-state">No suggestions submitted yet.</p>
      {:else}
        <div class="list">
          {#each suggestions as suggestion}
            <article class="list-card">
              <div class="list-top">
                <strong>{suggestion.suggestionTitle}</strong>
                <span class="badge badge-info">{suggestion.reviewStatus || 'PENDING'}</span>
              </div>
              <p class="muted">{suggestion.category}</p>
              <p>{suggestion.description}</p>
            </article>
          {/each}
        </div>
      {/if}
    </section>
  </div>
</DashboardLayout>

<style>
  .page-grid { display:grid; gap:1.5rem; }
  .eyebrow { font-size:0.75rem; text-transform:uppercase; letter-spacing:0.08em; font-weight:700; color:var(--primary, #4F7CDB); margin-bottom:0.5rem; }
  .form-grid { display:grid; grid-template-columns:repeat(2,minmax(0,1fr)); gap:1rem; margin-top:1rem; }
  .full-width { grid-column:1 / -1; }
  .form-group label { display:block; margin-bottom:0.35rem; font-size:0.82rem; color:var(--text-secondary, #475569); }
  .actions { margin-top:1rem; }
  .window-status {
    margin-top: 0.45rem;
    color: var(--text-secondary, #475569);
    font-size: 0.9rem;
  }
  .window-status.open {
    color: #065f46;
  }
  .list { display:grid; gap:1rem; }
  .list-card { border:1px solid var(--border-light, #E2E8F0); border-radius:var(--radius, 12px); padding:1rem; transition:background 0.2s ease; }
  .list-card:hover { background:var(--bg-alt, #F8FAFC); }
  .list-top { display:flex; justify-content:space-between; gap:1rem; margin-bottom:0.5rem; }
  .muted,.empty-state { color:var(--text-muted, #94A3B8); }
  .alert { padding:0.8rem 1rem; border-radius:var(--radius-sm, 8px); margin-top:1rem; }
  .alert-error { background:var(--danger-light, #FEE2E2); color:#991b1b; }
</style>
