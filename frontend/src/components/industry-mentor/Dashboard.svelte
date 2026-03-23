<script>
  import { onMount } from 'svelte';
  import { api, getCurrentUser, getRoleDashboardPath } from '../../lib/api.js';
  import { industryMentorNavItems } from '../../lib/navigation.js';
  import DashboardLayout from '../shared/DashboardLayout.svelte';

  let user = getCurrentUser();
  let sessions = [];
  let students = [];
  let yearStats = { '1': 0, '2': 0, '3': 0, '4': 0 };
  let totalStudents = 0;
  let showCreateSession = false;
  let showSuggestionForm = false;
  let filterType = 'all';

  let sessionTitle = '';
  let sessionDate = '';
  let sessionTime = '';
  let sessionType = 'WORKSHOP';
  let sessionTopic = '';
  let sessionDescription = '';
  let sessionError = '';
  let sessionLoading = false;

  let suggestionTitle = '';
  let suggestionCategory = 'CURRICULUM_UPDATE';
  let suggestionDescription = '';
  let suggestionError = '';
  let suggestionLoading = false;
  let submissionOpen = false;

  let filteredSessions = [];
  $: {
    if (filterType === 'all') filteredSessions = sessions;
    else filteredSessions = sessions.filter(s => s.sessionType === filterType);
  }

  onMount(async () => {
    if (!user) { window.location.href = '/'; return; }
    if (user.role !== 'INDUSTRY_MENTOR') { window.location.href = getRoleDashboardPath(user.role); return; }

    try {
      const sessData = await api.get(`/industry/sessions/mentor/${user.userId || user.id}`);
      sessions = Array.isArray(sessData) ? sessData : (sessData.data || []);
    } catch (e) { console.error('Failed to load sessions', e); }

    try {
      const connections = await api.get(`/mentor/mentor/${user.userId || user.id}`);
      const approved = (connections.data || []).filter((entry) => entry.connectionStatus === 'Approved');
      students = approved;
      totalStudents = approved.length;
    } catch (e) { console.error('Failed to load student connections', e); }

    try {
      const windowRes = await api.get('/suggestions/submission-window', { cache: false });
      submissionOpen = !!windowRes?.data?.open;
    } catch (e) {
      submissionOpen = false;
    }
  });

  async function createSession() {
    sessionError = '';
    sessionLoading = true;
    try {
      await api.post('/industry/sessions', {
        mentorId: user.userId || user.id,
        sessionTitle, sessionDate, sessionTime, sessionType, sessionTopic, sessionDescription
      });
      showCreateSession = false;
      sessionTitle = ''; sessionDate = ''; sessionTime = ''; sessionTopic = ''; sessionDescription = '';
      const sessData = await api.get(`/industry/sessions/mentor/${user.userId || user.id}`);
      sessions = Array.isArray(sessData) ? sessData : (sessData.data || []);
    } catch (e) {
      sessionError = 'Failed to create session';
    } finally { sessionLoading = false; }
  }

  async function submitSuggestion() {
    suggestionError = '';
    suggestionLoading = true;
    try {
      await api.post('/suggestions', {
        mentorId: user.userId || user.id,
        title: suggestionTitle,
        category: suggestionCategory,
        description: suggestionDescription
      });
      showSuggestionForm = false;
      suggestionTitle = ''; suggestionDescription = '';
    } catch (e) {
      suggestionError = 'Failed to submit suggestion';
    } finally { suggestionLoading = false; }
  }
</script>

<DashboardLayout navItems={industryMentorNavItems} activeItem="dashboard" pageTitle="Industry Mentor Dashboard">
  <div class="actions-row">
    <a href="/industry-mentor/sessions" class="btn btn-primary">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/>
      </svg>
      Manage Sessions
    </a>
    <a href="/industry-mentor/students" class="btn btn-outline">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/>
      </svg>
      View Students
    </a>
  </div>

  <div class="dashboard-grid">
    <!-- Sessions -->
    <div class="sessions-section">
      <div class="sessions-header">
        <h2 class="section-title">Upcoming Sessions</h2>
        <select class="filter-select" bind:value={filterType}>
          <option value="all">All Types</option>
          <option value="WORKSHOP">Workshop</option>
          <option value="TECH_TALK">Tech Talk</option>
          <option value="MOCK_INTERVIEW">Mock Interview</option>
          <option value="MENTORING">Mentoring</option>
        </select>
      </div>

      {#if filteredSessions.length === 0}
        <div class="card empty-card">
          <p class="empty-state">No upcoming sessions</p>
        </div>
      {:else}
        {#each filteredSessions as session}
          <div class="card session-card">
            <div class="session-date-block">
              <span class="session-day">{new Date(session.sessionDate || Date.now()).getDate()}</span>
              <span class="session-month">{new Date(session.sessionDate || Date.now()).toLocaleString('default', { month: 'short' })}</span>
            </div>
            <div class="session-details">
              <h3>{session.sessionTitle || 'Session'}</h3>
              <p>📍 {session.sessionLocation || 'TBD'}</p>
              <p class="session-meta">{session.registeredCount || 0} students registered</p>
            </div>
            <div class="session-time">
              <span>Time</span>
              <strong>{session.sessionTime || 'TBD'}</strong>
            </div>
          </div>
        {/each}
      {/if}
    </div>

    <!-- Right Sidebar -->
    <div class="right-sidebar">
      <div class="card stats-card">
        <h2 class="section-title">Total Students Assigned: {totalStudents}</h2>
        {#each Object.entries(yearStats) as [year, count]}
          <div class="stat-row">
            <span>From {year === '1' ? '1st' : year === '2' ? '2nd' : year === '3' ? '3rd' : '4th'} year:</span>
            <div class="stat-bar-bg">
              <div class="stat-bar" style="width: {totalStudents ? (count / totalStudents) * 100 : 0}%"></div>
            </div>
            <strong>{count}</strong>
          </div>
        {/each}
      </div>

      <a href="/industry-mentor/suggestions" class="btn btn-success suggestion-btn">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 1 1 7.072 0l-.548.547A3.374 3.374 0 0 0 14 18.469V19a2 2 0 1 1-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"/>
        </svg>
        {submissionOpen ? 'Submit Curriculum Suggestions' : 'Curriculum Suggestions (Closed)'}
      </a>
    </div>
  </div>

  <!-- Create Session Modal -->
  {#if showCreateSession}
    <div class="modal-overlay" on:click|self={() => showCreateSession = false}>
      <div class="modal-content">
        <h2>Create Session</h2>
        {#if sessionError}<div class="alert-error">{sessionError}</div>{/if}
        <form on:submit|preventDefault={createSession}>
          <div class="form-group">
            <label>Title</label>
            <input class="input" bind:value={sessionTitle} required placeholder="e.g. Resume Review Workshop" />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Date</label>
              <input type="date" class="input" bind:value={sessionDate} required />
            </div>
            <div class="form-group">
              <label>Time</label>
              <input type="time" class="input" bind:value={sessionTime} required />
            </div>
          </div>
          <div class="form-group">
            <label>Topic</label>
            <input class="input" bind:value={sessionTopic} placeholder="e.g. Career Readiness and Resume Review" />
          </div>
          <div class="form-group">
            <label>Type</label>
            <select class="input" bind:value={sessionType}>
              <option value="WORKSHOP">Workshop</option>
              <option value="TECH_TALK">Tech Talk</option>
              <option value="MOCK_INTERVIEW">Mock Interview</option>
              <option value="MENTORING">Mentoring</option>
            </select>
          </div>
          <div class="form-group">
            <label>Description</label>
            <textarea class="input" rows="4" bind:value={sessionDescription} placeholder="Describe what students should expect from this session."></textarea>
          </div>
          <div class="modal-actions">
            <button type="button" class="btn btn-outline" on:click={() => showCreateSession = false}>Cancel</button>
            <button type="submit" class="btn btn-primary" disabled={sessionLoading}>
              {sessionLoading ? 'Creating...' : 'Create'}
            </button>
          </div>
        </form>
      </div>
    </div>
  {/if}

  <!-- Suggestion Modal -->
  {#if showSuggestionForm}
    <div class="modal-overlay" on:click|self={() => showSuggestionForm = false}>
      <div class="modal-content">
        <h2>Submit Curriculum Suggestion</h2>
        <p class="modal-desc">Share your industry insights to improve the curriculum.</p>
        {#if suggestionError}<div class="alert-error">{suggestionError}</div>{/if}
        <form on:submit|preventDefault={submitSuggestion}>
          <div class="form-group">
            <label>Title</label>
            <input class="input" bind:value={suggestionTitle} required placeholder="e.g. Add Cloud Computing Module" />
          </div>
          <div class="form-group">
            <label>Category</label>
            <select class="input" bind:value={suggestionCategory}>
              <option value="CURRICULUM_UPDATE">Curriculum Update</option>
              <option value="NEW_COURSE">New Course</option>
              <option value="SKILL_GAP">Skill Gap</option>
              <option value="INDUSTRY_TREND">Industry Trend</option>
            </select>
          </div>
          <div class="form-group">
            <label>Description</label>
            <textarea class="input" rows="4" bind:value={suggestionDescription} required placeholder="Describe your suggestion in detail..."></textarea>
          </div>
          <div class="modal-actions">
            <button type="button" class="btn btn-outline" on:click={() => showSuggestionForm = false}>Cancel</button>
            <button type="submit" class="btn btn-success" disabled={suggestionLoading}>
              {suggestionLoading ? 'Submitting...' : 'Submit Suggestion'}
            </button>
          </div>
        </form>
      </div>
    </div>
  {/if}
</DashboardLayout>

<style>
  .actions-row {
    display: flex;
    gap: 1rem;
    margin-bottom: 1.5rem;
  }

  .dashboard-grid {
    display: grid;
    grid-template-columns: 1fr 320px;
    gap: 1.5rem;
  }

  .section-title {
    font-size: 1rem;
    font-weight: 600;
    color: var(--gray-800);
  }

  .sessions-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
  }

  .filter-select {
    padding: 0.375rem 0.75rem;
    border: 1px solid var(--gray-300);
    border-radius: var(--radius);
    font-size: 0.8125rem;
    background: white;
  }

  .empty-card { padding: 2rem; }
  .empty-state { color: var(--gray-400); font-size: 0.875rem; text-align: center; }

  .session-card {
    display: flex;
    align-items: center;
    gap: 1.25rem;
    margin-bottom: 0.75rem;
    padding: 1.25rem;
  }

  .session-date-block {
    display: flex;
    flex-direction: column;
    align-items: center;
    background: var(--gray-100);
    border-radius: var(--radius);
    padding: 0.5rem 0.75rem;
    min-width: 56px;
  }
  .session-day { font-size: 1.5rem; font-weight: 700; color: var(--primary); line-height: 1; }
  .session-month { font-size: 0.7rem; color: var(--gray-500); text-transform: uppercase; }

  .session-details { flex: 1; }
  .session-details h3 { font-size: 0.9375rem; font-weight: 600; color: var(--gray-800); }
  .session-details p { font-size: 0.8125rem; color: var(--gray-500); }
  .session-meta { margin-top: 0.25rem; }

  .session-time {
    text-align: center;
    background: var(--gray-50);
    padding: 0.5rem 0.75rem;
    border-radius: var(--radius);
    font-size: 0.75rem;
    color: var(--gray-500);
  }
  .session-time strong { display: block; color: var(--gray-800); font-size: 0.8125rem; }

  .stats-card { margin-bottom: 1rem; }
  .stat-row {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    margin-top: 0.75rem;
    font-size: 0.8125rem;
    color: var(--gray-600);
  }
  .stat-bar-bg {
    flex: 1;
    height: 8px;
    background: var(--gray-200);
    border-radius: 4px;
    overflow: hidden;
  }
  .stat-bar {
    height: 100%;
    background: var(--primary);
    border-radius: 4px;
  }

  .suggestion-btn {
    width: 100%;
    padding: 0.875rem;
    font-size: 0.9375rem;
  }

  .form-group { margin-bottom: 1rem; }
  .form-group label {
    display: block; font-size: 0.8125rem; font-weight: 500;
    color: var(--gray-700); margin-bottom: 0.375rem;
  }
  .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
  .modal-desc { color: var(--gray-500); font-size: 0.875rem; margin: 0.5rem 0 1.25rem; }
  .modal-actions { display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 0.5rem; }
  .alert-error { background: #fee2e2; color: #991b1b; padding: 0.75rem; border-radius: var(--radius); font-size: 0.8125rem; margin-bottom: 1rem; }

  @media (max-width: 900px) {
    .dashboard-grid { grid-template-columns: 1fr; }
  }
</style>
