<script>
  import { onMount } from 'svelte';
  import { api, getCurrentUser, getRoleDashboardPath } from '../../lib/api.js';
  import { industryMentorNavItems } from '../../lib/navigation.js';
  import DashboardLayout from '../shared/DashboardLayout.svelte';

  let user = getCurrentUser();
  let suggestions = [];
  let loading = true;
  let error = '';
  let form = {
    title: '',
    category: 'CURRICULUM_UPDATE',
    description: '',
    suggestedCourse: ''
  };

  onMount(async () => {
    if (!user) { window.location.href = '/'; return; }
    if (user.role !== 'INDUSTRY_MENTOR') { window.location.href = getRoleDashboardPath(user.role); return; }
    await loadSuggestions();
  });

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

  async function submitSuggestion() {
    error = '';
    try {
      await api.post('/suggestions', {
        mentorId: user.userId || user.id,
        title: form.title,
        category: form.category,
        description: form.description,
        suggestedCourse: form.suggestedCourse
      });
      form = { title: '', category: 'CURRICULUM_UPDATE', description: '', suggestedCourse: '' };
      await loadSuggestions();
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
      <div class="form-grid">
        <div class="form-group">
          <label>Title</label>
          <input class="input" bind:value={form.title} />
        </div>
        <div class="form-group">
          <label>Category</label>
          <select class="input" bind:value={form.category}>
            <option value="CURRICULUM_UPDATE">Curriculum Update</option>
            <option value="NEW_COURSE">New Course</option>
            <option value="SKILL_GAP">Skill Gap</option>
            <option value="INDUSTRY_TREND">Industry Trend</option>
          </select>
        </div>
        <div class="form-group">
          <label>Suggested Course</label>
          <input class="input" bind:value={form.suggestedCourse} />
        </div>
        <div class="form-group full-width">
          <label>Description</label>
          <textarea class="input" rows="5" bind:value={form.description}></textarea>
        </div>
      </div>
      <div class="actions"><button class="btn btn-success" on:click={submitSuggestion}>Submit</button></div>
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
  .eyebrow { font-size:0.75rem; text-transform:uppercase; letter-spacing:0.08em; font-weight:700; color:var(--accent); margin-bottom:0.5rem; }
  .form-grid { display:grid; grid-template-columns:repeat(2,minmax(0,1fr)); gap:1rem; margin-top:1rem; }
  .full-width { grid-column:1 / -1; }
  .form-group label { display:block; margin-bottom:0.35rem; font-size:0.82rem; color:var(--gray-600); }
  .actions { margin-top:1rem; }
  .list { display:grid; gap:1rem; }
  .list-card { border:1px solid var(--gray-200); border-radius:var(--radius); padding:1rem; }
  .list-top { display:flex; justify-content:space-between; gap:1rem; margin-bottom:0.5rem; }
  .muted,.empty-state { color:var(--gray-500); }
  .alert { padding:0.8rem 1rem; border-radius:var(--radius); margin-top:1rem; }
  .alert-error { background:#fee2e2; color:#991b1b; }
</style>
