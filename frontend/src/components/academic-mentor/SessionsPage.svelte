<script>
  import { onMount } from 'svelte';
  import { api, getCurrentUser, getRoleDashboardPath } from '../../lib/api.js';
  import { academicMentorNavItems } from '../../lib/navigation.js';
  import DashboardLayout from '../shared/DashboardLayout.svelte';

  let user = getCurrentUser();
  let sessions = [];
  let loading = true;
  let error = '';
  let showCreate = false;
  let form = {
    sessionTitle: '',
    sessionType: 'GROUP',
    sessionTopic: '',
    sessionDescription: '',
    sessionDate: '',
    sessionTime: ''
  };

  onMount(async () => {
    if (!user) { window.location.href = '/'; return; }
    if (user.role !== 'ACADEMIC_MENTOR') { window.location.href = getRoleDashboardPath(user.role); return; }
    await loadSessions();
  });

  async function loadSessions() {
    loading = true;
    error = '';
    try {
      const res = await api.get(`/academic/sessions/mentor/${user.userId || user.id}`, { cache: false });
      sessions = res.data || [];
    } catch (e) {
      error = e?.data?.message || 'Failed to load sessions.';
    } finally {
      loading = false;
    }
  }

  async function createSession() {
    error = '';
    try {
      await api.post('/academic/sessions', {
        mentorId: user.userId || user.id,
        ...form
      });
      form = { sessionTitle: '', sessionType: 'GROUP', sessionTopic: '', sessionDescription: '', sessionDate: '', sessionTime: '' };
      showCreate = false;
      await loadSessions();
    } catch (e) {
      error = e?.data?.message || 'Failed to create session.';
    }
  }
</script>

<DashboardLayout navItems={academicMentorNavItems} activeItem="sessions" pageTitle="Sessions">
  <div class="page-grid">
    <section class="card hero-card">
      <div>
        <p class="eyebrow">Academic Mentor</p>
        <h2>Mentor-Created Sessions</h2>
        <p class="hero-copy">Create sessions for your assigned students. Undergraduates can view and join these sessions, but they cannot create them.</p>
      </div>
      <button class="btn btn-primary" on:click={() => showCreate = true}>Create Session</button>
    </section>

    <section class="card">
      {#if error}
        <div class="alert alert-error">{error}</div>
      {/if}

      {#if loading}
        <p class="empty-state">Loading sessions...</p>
      {:else if sessions.length === 0}
        <p class="empty-state">No sessions created yet.</p>
      {:else}
        <div class="session-list">
          {#each sessions as session}
            <article class="session-card">
              <div>
                <h3>{session.sessionTitle}</h3>
                <p class="muted">{session.sessionTopic}</p>
                <p class="copy">{session.sessionDescription || 'No description provided.'}</p>
              </div>
              <div class="session-meta">
                <span class="badge badge-info">{session.sessionType}</span>
                <strong>{session.sessionDate} at {session.sessionTime}</strong>
              </div>
            </article>
          {/each}
        </div>
      {/if}
    </section>
  </div>

  {#if showCreate}
    <div class="modal-overlay" on:click|self={() => showCreate = false}>
      <div class="modal-content">
        <h2>Create Academic Session</h2>
        <div class="form-grid">
          <div class="form-group">
            <label>Title</label>
            <input class="input" bind:value={form.sessionTitle} />
          </div>
          <div class="form-group">
            <label>Type</label>
            <select class="input" bind:value={form.sessionType}>
              <option value="GROUP">Group</option>
              <option value="ONE_TO_ONE">One To One</option>
            </select>
          </div>
          <div class="form-group full-width">
            <label>Topic</label>
            <input class="input" bind:value={form.sessionTopic} />
          </div>
          <div class="form-group full-width">
            <label>Description</label>
            <textarea class="input" rows="4" bind:value={form.sessionDescription}></textarea>
          </div>
          <div class="form-group">
            <label>Date</label>
            <input type="date" class="input" bind:value={form.sessionDate} />
          </div>
          <div class="form-group">
            <label>Time</label>
            <input type="time" class="input" bind:value={form.sessionTime} />
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn btn-outline" on:click={() => showCreate = false}>Cancel</button>
          <button class="btn btn-primary" on:click={createSession}>Create</button>
        </div>
      </div>
    </div>
  {/if}
</DashboardLayout>

<style>
  .page-grid { display: grid; gap: 1.5rem; }
  .hero-card { display: flex; justify-content: space-between; gap: 1rem; align-items: start; padding: 2rem; background: linear-gradient(135deg, #fff, #f5faff); }
  .eyebrow { font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.08em; font-weight: 700; color: var(--accent); margin-bottom: 0.5rem; }
  .hero-copy, .muted { color: var(--gray-600); }
  .session-list { display: grid; gap: 1rem; }
  .session-card { border: 1px solid var(--gray-200); border-radius: var(--radius); padding: 1rem; display: flex; justify-content: space-between; gap: 1rem; }
  .session-meta { display: grid; gap: 0.6rem; text-align: right; min-width: 180px; }
  .copy { margin-top: 0.5rem; color: var(--gray-700); }
  .form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 1rem; margin-top: 1rem; }
  .full-width { grid-column: 1 / -1; }
  .form-group label { display: block; margin-bottom: 0.35rem; font-size: 0.82rem; color: var(--gray-600); }
  .modal-actions { display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1rem; }
  .empty-state { color: var(--gray-500); padding: 1rem 0; }
  .alert { padding: 0.8rem 1rem; border-radius: var(--radius); margin-bottom: 1rem; }
  .alert-error { background: #fee2e2; color: #991b1b; }
</style>
