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

  onMount(async () => {
    if (!user) { window.location.href = '/'; return; }
    if (user.role !== 'ACADEMIC_MENTOR') { window.location.href = getRoleDashboardPath(user.role); return; }

    try {
      const mentorId = user.userId || user.id;
      const allocData = await api.get(`/allocations/mentor/${mentorId}`, { cache: false });
      allocations = allocData.data || [];
      totalStudents = allocations.length;
      yearStats = { '1': 0, '2': 0, '3': 0, '4': 0 };
      for (const alloc of allocations) {
        const yr = String(alloc.academicYear || '');
        if (yearStats[yr] !== undefined) yearStats[yr]++;
      }
    } catch (e) {
      console.error('Failed to load allocations', e);
    }

    try {
      const sessData = await api.get(`/academic/sessions/mentor/${user.userId || user.id}`, { cache: false });
      sessions = sessData.data || [];
    } catch (e) {
      console.error('Failed to load sessions', e);
      sessions = [];
    }
  });

  function sessionAudienceLabel(session) {
    if (session.sessionType === 'ONE_TO_ONE' || session.audienceMode === 'ONE_TO_ONE') return 'One student';
    if (session.audienceMode === 'YEAR') return `Year ${session.targetYearOfStudy || '-'}`;
    if (session.audienceMode === 'CUSTOM') return 'Custom student group';
    return 'All assigned students';
  }
</script>

<DashboardLayout navItems={academicMentorNavItems} activeItem="dashboard" pageTitle="Academic Mentor Dashboard">
  <div class="actions-row">
    <a href="/academic-mentor/sessions" class="btn btn-primary">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/>
      </svg>
      Create New Session
    </a>
    <a href="/academic-mentor/students" class="btn btn-outline">View Students</a>
  </div>

  <div class="dashboard-grid">
    <div class="card sessions-card">
      <div class="section-head">
        <h2 class="section-title">Upcoming Sessions</h2>
        <a href="/academic-mentor/sessions" class="mini-link">Manage all sessions</a>
      </div>

      {#if sessions.length === 0}
        <p class="empty-state">No sessions created yet.</p>
      {:else}
        <div class="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Topic</th>
                <th>Type</th>
                <th>Audience</th>
                <th>Date</th>
                <th>Time</th>
              </tr>
            </thead>
            <tbody>
              {#each sessions as session}
                <tr>
                  <td>
                    <a href="/academic-mentor/sessions" class="link-btn">
                      <strong>{session.sessionTitle || session.sessionTopic || 'Session'}</strong>
                    </a>
                    <div class="row-copy">{session.sessionDescription || 'No description provided.'}</div>
                  </td>
                  <td><span class="badge badge-info">{session.sessionType === 'ONE_TO_ONE' ? 'One to one' : 'Group'}</span></td>
                  <td>{sessionAudienceLabel(session)}</td>
                  <td>{session.sessionDate || '-'}</td>
                  <td>{session.sessionTime || '-'}</td>
                </tr>
              {/each}
            </tbody>
          </table>
        </div>
      {/if}
    </div>

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
</DashboardLayout>

<style>
  /* -- Link button -- */
  .link-btn {
    background: none;
    border: none;
    padding: 0;
    color: var(--primary, #4F7CDB);
    font-weight: 600;
    cursor: pointer;
    text-decoration: none;
  }
  .link-btn:hover {
    text-decoration: underline;
    color: var(--primary-dark, #3A62B5);
  }

  /* -- Actions row -- */
  .actions-row {
    display: flex;
    gap: 1rem;
    margin-bottom: 1.5rem;
  }

  /* -- Dashboard grid -- */
  .dashboard-grid {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: 1.5rem;
  }

  /* -- Section header -- */
  .section-head {
    display: flex;
    justify-content: space-between;
    gap: 1rem;
    align-items: center;
    margin-bottom: 1rem;
  }
  .section-title {
    font-size: 1rem;
    font-weight: 600;
    color: var(--text-main, #1E293B);
  }
  .mini-link {
    color: var(--accent, #2BA89C);
    font-size: 0.85rem;
    font-weight: 600;
    text-decoration: none;
  }
  .mini-link:hover {
    color: var(--accent-light, #3DC0B3);
    text-decoration: underline;
  }

  /* -- Empty state -- */
  .empty-state {
    color: var(--text-muted, #94A3B8);
    font-size: 0.875rem;
    text-align: center;
    padding: 3rem 0;
  }

  /* -- Sessions table -- */
  .table-wrapper {
    overflow-x: auto;
    border: 1px solid var(--border-light, #E2E8F0);
    border-radius: var(--radius, 12px);
  }
  table {
    width: 100%;
    border-collapse: collapse;
    font-size: 0.8125rem;
  }
  th {
    text-align: left;
    padding: 0.75rem;
    font-weight: 600;
    color: var(--text-secondary, #475569);
    background: var(--bg-secondary, #F1F5F9);
    border-bottom: 2px solid var(--border-light, #E2E8F0);
    white-space: nowrap;
  }
  th:first-child {
    border-radius: var(--radius, 12px) 0 0 0;
  }
  th:last-child {
    border-radius: 0 var(--radius, 12px) 0 0;
  }
  td {
    padding: 0.75rem;
    border-bottom: 1px solid var(--border-light, #E2E8F0);
    color: var(--text-secondary, #475569);
    vertical-align: top;
  }
  tr:last-child td {
    border-bottom: none;
  }
  tr:hover td {
    background: var(--bg-alt, #F8FAFC);
  }
  .row-copy {
    margin-top: 0.35rem;
    color: var(--text-muted, #94A3B8);
    font-size: 0.8rem;
  }

  /* -- Badges (pill-shaped, semantic) -- */
  .badge {
    display: inline-block;
    padding: 0.2rem 0.7rem;
    border-radius: 999px;
    font-size: 0.75rem;
    font-weight: 500;
    line-height: 1.4;
  }
  .badge-info {
    background: var(--primary-100, #D4DFFA);
    color: var(--primary-dark, #3A62B5);
  }

  /* -- Student statistics -- */
  .stat-total {
    font-size: 0.9375rem;
    color: var(--text-secondary, #475569);
    margin-bottom: 1.25rem;
  }
  .stat-row {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    margin-bottom: 0.875rem;
    padding: 0.5rem 0.75rem;
    border-left: 3px solid var(--primary, #4F7CDB);
    border-radius: var(--radius-sm, 8px);
    background: var(--bg-alt, #F8FAFC);
  }
  /* Year 1 = primary */
  .stat-row:nth-of-type(1) {
    border-left-color: var(--primary, #4F7CDB);
  }
  /* Year 2 = accent */
  .stat-row:nth-of-type(2) {
    border-left-color: var(--accent, #2BA89C);
  }
  /* Year 3 = warning */
  .stat-row:nth-of-type(3) {
    border-left-color: var(--warning, #F59E0B);
  }
  /* Year 4 = success */
  .stat-row:nth-of-type(4) {
    border-left-color: var(--success, #22C55E);
  }
  .stat-label {
    width: 70px;
    font-size: 0.75rem;
    color: var(--text-secondary, #475569);
    font-weight: 500;
  }
  .stat-bar-container {
    flex: 1;
    height: 8px;
    background: var(--border-light, #E2E8F0);
    border-radius: 999px;
    overflow: hidden;
  }
  .stat-bar {
    height: 100%;
    background: var(--primary, #4F7CDB);
    border-radius: 999px;
    transition: width 0.4s ease;
  }
  .stat-row:nth-of-type(1) .stat-bar {
    background: var(--primary, #4F7CDB);
  }
  .stat-row:nth-of-type(2) .stat-bar {
    background: var(--accent, #2BA89C);
  }
  .stat-row:nth-of-type(3) .stat-bar {
    background: var(--warning, #F59E0B);
  }
  .stat-row:nth-of-type(4) .stat-bar {
    background: var(--success, #22C55E);
  }
  .stat-count {
    width: 60px;
    font-size: 0.75rem;
    color: var(--text-muted, #94A3B8);
    text-align: right;
  }

  /* -- Responsive -- */
  @media (max-width: 900px) {
    .dashboard-grid { grid-template-columns: 1fr; }
    .actions-row { flex-wrap: wrap; }
  }
</style>
