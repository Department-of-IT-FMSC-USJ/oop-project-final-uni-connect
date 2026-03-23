<script>
  import { onMount } from 'svelte';
  import { api, getCurrentUser, getRoleDashboardPath } from '../../lib/api.js';
  import { academicMentorNavItems } from '../../lib/navigation.js';
  import DashboardLayout from '../shared/DashboardLayout.svelte';

  let user = getCurrentUser();
  let sessions = [];
  let allocations = [];
  let yearStats = { '1': 0, '2': 0, '3': 0, '4': 0 };
  let totalStudents = 0;
  let showCreateSession = false;

  // Session form
  let sessionTitle = '';
  let sessionDate = '';
  let sessionTime = '';
  let sessionTopic = '';
  let sessionDescription = '';
  let sessionType = 'GROUP';
  let sessionError = '';
  let sessionLoading = false;

  onMount(async () => {
    if (!user) { window.location.href = '/'; return; }
    if (user.role !== 'ACADEMIC_MENTOR') { window.location.href = getRoleDashboardPath(user.role); return; }

    try {
      const mentorId = user.userId || user.id;
      const allocData = await api.get(`/allocations/mentor/${mentorId}`);
      if (allocData.success !== false) {
        allocations = Array.isArray(allocData) ? allocData : (allocData.data || []);
        totalStudents = allocations.length;
        yearStats = { '1': 0, '2': 0, '3': 0, '4': 0 };
        for (const alloc of allocations) {
          const yr = String(alloc.academicYear || '');
          if (yearStats[yr] !== undefined) yearStats[yr]++;
        }
        yearStats = yearStats;
      }
    } catch (e) { console.error('Failed to load allocations', e); }

    try {
      const sessData = await api.get(`/academic/sessions/mentor/${user.userId || user.id}`);
      if (sessData.success !== false) {
        sessions = Array.isArray(sessData) ? sessData : (sessData.data || []);
      }
    } catch (e) { console.error('Failed to load sessions', e); }
  });

  async function createSession() {
    sessionError = '';
    sessionLoading = true;
    try {
      await api.post('/academic/sessions', {
        mentorId: user.userId || user.id,
        sessionTitle,
        sessionDate,
        sessionTime,
        sessionType,
        sessionTopic,
        sessionDescription
      });
      showCreateSession = false;
      sessionTitle = ''; sessionDate = ''; sessionTime = ''; sessionTopic = ''; sessionDescription = '';
      const sessData = await api.get(`/academic/sessions/mentor/${user.userId || user.id}`);
      sessions = Array.isArray(sessData) ? sessData : (sessData.data || []);
    } catch (e) {
      sessionError = 'Failed to create session';
    } finally {
      sessionLoading = false;
    }
  }
</script>

<DashboardLayout navItems={academicMentorNavItems} activeItem="dashboard" pageTitle="Academic Mentor Dashboard">
  <!-- Action Buttons -->
  <div class="actions-row">
    <button class="btn btn-primary" on:click={() => showCreateSession = true}>
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/>
      </svg>
      Create New Session
    </button>
    <a href="/academic-mentor/students" class="btn btn-outline">View Students</a>
  </div>

  <div class="dashboard-grid">
    <!-- Sessions Table -->
    <div class="card sessions-card">
      <h2 class="section-title">Upcoming Sessions</h2>
      {#if sessions.length === 0}
        <p class="empty-state">No upcoming sessions. Create one to get started.</p>
      {:else}
        <div class="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Date</th>
                <th>Time</th>
                <th>Student</th>
                <th>Type</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {#each sessions as session}
                <tr>
                  <td>{session.sessionDate || '-'}</td>
                  <td>{session.sessionTime || '-'}</td>
                  <td>{session.studentName || `Student #${session.studentId}`}</td>
                  <td><span class="badge badge-info">{session.sessionType || '-'}</span></td>
                  <td><button class="link-action">Join Session</button></td>
                </tr>
              {/each}
            </tbody>
          </table>
        </div>
      {/if}
    </div>

    <!-- Stats Card -->
    <div class="card stats-card">
      <h2 class="section-title">Student Statistics</h2>
      <p class="stat-total">Total Students: <strong>{totalStudents}</strong></p>
      {#each Object.entries(yearStats) as [year, count]}
        <div class="stat-row">
          <span class="stat-label">{year === '1' ? '1st' : year === '2' ? '2nd' : year === '3' ? '3rd' : '4th'} Year</span>
          <div class="stat-bar-container">
            <div class="stat-bar" style="width: {totalStudents ? (count / totalStudents) * 100 : 0}%"></div>
          </div>
          <span class="stat-count">{count} ({totalStudents ? Math.round((count / totalStudents) * 100) : 0}%)</span>
        </div>
      {/each}
    </div>
  </div>

  <!-- Create Session Modal -->
  {#if showCreateSession}
    <div class="modal-overlay" on:click|self={() => showCreateSession = false}>
      <div class="modal-content">
        <h2>Create New Session</h2>
        {#if sessionError}
          <div class="alert alert-error">{sessionError}</div>
        {/if}
        <form on:submit|preventDefault={createSession}>
          <div class="form-group">
            <label>Session Title</label>
            <input class="input" bind:value={sessionTitle} required placeholder="e.g. Project Review" />
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
          <div class="form-row">
            <div class="form-group">
              <label>Session Type</label>
              <select class="input" bind:value={sessionType}>
                <option value="GROUP">Group</option>
                <option value="ONE_TO_ONE">One To One</option>
              </select>
            </div>
            <div class="form-group">
              <label>Topic</label>
              <input class="input" bind:value={sessionTopic} required />
            </div>
          </div>
          <div class="form-group">
            <label>Description</label>
            <textarea class="input" rows="3" bind:value={sessionDescription}></textarea>
          </div>
          <div class="modal-actions">
            <button type="button" class="btn btn-outline" on:click={() => showCreateSession = false}>Cancel</button>
            <button type="submit" class="btn btn-primary" disabled={sessionLoading}>
              {sessionLoading ? 'Creating...' : 'Create Session'}
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
    grid-template-columns: 2fr 1fr;
    gap: 1.5rem;
  }

  .section-title {
    font-size: 1rem;
    font-weight: 600;
    margin-bottom: 1rem;
    color: var(--gray-800);
  }

  .empty-state {
    color: var(--gray-400);
    font-size: 0.875rem;
    text-align: center;
    padding: 3rem 0;
  }

  .table-wrapper { overflow-x: auto; }

  table {
    width: 100%;
    border-collapse: collapse;
    font-size: 0.8125rem;
  }
  th {
    text-align: left;
    padding: 0.75rem;
    font-weight: 600;
    color: var(--gray-600);
    border-bottom: 2px solid var(--gray-200);
    white-space: nowrap;
  }
  td {
    padding: 0.75rem;
    border-bottom: 1px solid var(--gray-100);
    color: var(--gray-700);
  }
  tr:hover td { background: var(--gray-50); }

  .link-action {
    background: none;
    color: var(--accent);
    font-weight: 500;
    font-size: 0.8125rem;
  }
  .link-action:hover { text-decoration: underline; }

  .stat-total {
    font-size: 0.9375rem;
    color: var(--gray-700);
    margin-bottom: 1.25rem;
  }

  .stat-row {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    margin-bottom: 0.875rem;
  }
  .stat-label {
    width: 70px;
    font-size: 0.8125rem;
    color: var(--gray-600);
    flex-shrink: 0;
  }
  .stat-bar-container {
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
    transition: width 0.5s ease;
    min-width: 2px;
  }
  .stat-count {
    font-size: 0.8125rem;
    color: var(--gray-700);
    font-weight: 500;
    width: 70px;
    text-align: right;
    flex-shrink: 0;
  }

  .form-group { margin-bottom: 1rem; }
  .form-group label {
    display: block;
    font-size: 0.8125rem;
    font-weight: 500;
    color: var(--gray-700);
    margin-bottom: 0.375rem;
  }
  .form-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1rem;
  }
  .modal-actions {
    display: flex;
    justify-content: flex-end;
    gap: 0.75rem;
    margin-top: 0.5rem;
  }
  .alert-error {
    background: #fee2e2;
    color: #991b1b;
    border: 1px solid #fecaca;
    padding: 0.75rem;
    border-radius: var(--radius);
    font-size: 0.8125rem;
    margin-bottom: 1rem;
  }

  @media (max-width: 900px) {
    .dashboard-grid { grid-template-columns: 1fr; }
  }
</style>
