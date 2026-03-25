<script>
  import { onMount } from 'svelte';
  import DashboardLayout from '../shared/DashboardLayout.svelte';
  import { hodNavItems } from '../../lib/navigation.js';
  import { api, getCurrentUser } from '../../lib/api.js';
  import { addToast } from '../../lib/toast.js';
  import CustomSelect from '../shared/CustomSelect.svelte';

  let user = null;
  let loading = false;
  let reportData = null;

  let startDate = '';
  let endDate = '';
  let mentorType = 'ALL';
  let preset = '';
  let expandedSessions = {};

  const mentorTypeOptions = [
    { value: 'ALL', label: 'All Mentors' },
    { value: 'ACADEMIC', label: 'Academic Only' },
    { value: 'INDUSTRY', label: 'Industry Only' }
  ];

  onMount(() => {
    user = getCurrentUser();
    if (!user) { window.location.href = '/'; return; }
    const now = new Date();
    const y = now.getFullYear();
    const m = String(now.getMonth() + 1).padStart(2, '0');
    startDate = `${y}-${m}-01`;
    const lastDay = new Date(y, now.getMonth() + 1, 0).getDate();
    endDate = `${y}-${m}-${String(lastDay).padStart(2, '0')}`;
    preset = 'this-month';
  });

  function applyPreset(p) {
    preset = p;
    const now = new Date();
    const y = now.getFullYear();
    const m = now.getMonth();
    if (p === 'this-month') {
      startDate = `${y}-${String(m + 1).padStart(2, '0')}-01`;
      const lastDay = new Date(y, m + 1, 0).getDate();
      endDate = `${y}-${String(m + 1).padStart(2, '0')}-${String(lastDay).padStart(2, '0')}`;
    } else if (p === 'last-month') {
      const pm = m === 0 ? 11 : m - 1;
      const py = m === 0 ? y - 1 : y;
      startDate = `${py}-${String(pm + 1).padStart(2, '0')}-01`;
      const lastDay = new Date(py, pm + 1, 0).getDate();
      endDate = `${py}-${String(pm + 1).padStart(2, '0')}-${String(lastDay).padStart(2, '0')}`;
    } else if (p === 'this-year') {
      startDate = `${y}-01-01`;
      endDate = `${y}-12-31`;
    }
  }

  async function generateReport() {
    if (!startDate || !endDate) {
      addToast('Please select a date range', 'warning');
      return;
    }
    loading = true;
    reportData = null;
    expandedSessions = {};
    try {
      const res = await api.get(`/reports/hod/session-report?startDate=${startDate}&endDate=${endDate}&mentorType=${mentorType}`);
      reportData = res.data;
    } catch (err) {
      addToast('Failed to generate report', 'error');
    } finally {
      loading = false;
    }
  }

  function toggleSession(id) {
    expandedSessions[id] = !expandedSessions[id];
    expandedSessions = expandedSessions;
  }

  function printReport() {
    window.print();
  }

  function formatDate(d) {
    if (!d) return '-';
    const dt = new Date(d + 'T00:00:00');
    return dt.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
  }

  function formatTime(t) {
    if (!t) return '-';
    const [h, m] = t.split(':');
    const hr = parseInt(h);
    const ampm = hr >= 12 ? 'PM' : 'AM';
    const hr12 = hr % 12 || 12;
    return `${hr12}:${m} ${ampm}`;
  }
</script>

<DashboardLayout navItems={hodNavItems} activeItem="reports" pageTitle="Reports">
  <div class="reports-page">
    <div class="page-header no-print">
      <div>
        <p class="eyebrow">Analytics</p>
        <h2>Session Reports</h2>
        <p class="page-desc">Generate detailed reports of mentoring sessions in your department</p>
      </div>
    </div>

    <div class="filters-card card no-print">
      <div class="filters-row">
        <div class="preset-btns">
          <button class="preset-btn" class:active={preset === 'this-month'} on:click={() => applyPreset('this-month')}>This Month</button>
          <button class="preset-btn" class:active={preset === 'last-month'} on:click={() => applyPreset('last-month')}>Last Month</button>
          <button class="preset-btn" class:active={preset === 'this-year'} on:click={() => applyPreset('this-year')}>This Year</button>
        </div>
        <div class="filter-inputs">
          <div class="filter-group">
            <label>From</label>
            <input type="date" class="input" bind:value={startDate} on:change={() => preset = ''} />
          </div>
          <div class="filter-group">
            <label>To</label>
            <input type="date" class="input" bind:value={endDate} on:change={() => preset = ''} />
          </div>
          <div class="filter-group">
            <label>Mentor Type</label>
            <CustomSelect options={mentorTypeOptions} bind:value={mentorType} />
          </div>
          <button class="btn btn-primary generate-btn" on:click={generateReport} disabled={loading}>
            {#if loading}Generating...{:else}Generate Report{/if}
          </button>
        </div>
      </div>
    </div>

    {#if reportData}
      <div class="report-content">
        <div class="report-header">
          <div class="report-title-section">
            <h2 class="report-title">{reportData.reportTitle}</h2>
            <p class="report-meta">Department: {reportData.department} &bull; {reportData.dateRange}</p>
            <p class="report-meta">Generated by {reportData.generatedBy} on {reportData.generatedAt}</p>
          </div>
          <button class="btn btn-outline no-print" on:click={printReport}>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><polyline points="6 9 6 2 18 2 18 9"/><path d="M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2"/><rect x="6" y="14" width="12" height="8"/></svg>
            Print / Save PDF
          </button>
        </div>

        <div class="summary-cards">
          <div class="stat-card">
            <div class="stat-icon blue">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="22" height="22"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
            </div>
            <div>
              <div class="stat-value">{reportData.totalSessions}</div>
              <div class="stat-label">Total Sessions</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon teal">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="22" height="22"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
            </div>
            <div>
              <div class="stat-value">{reportData.totalParticipants}</div>
              <div class="stat-label">Total Participants</div>
            </div>
          </div>
        </div>

        {#if reportData.sessions && reportData.sessions.length > 0}
          <div class="sessions-table-wrapper">
            <table class="report-table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Session</th>
                  <th>Mentor</th>
                  <th>Type</th>
                  <th>Date</th>
                  <th>Time</th>
                  <th>Participants</th>
                  <th class="no-print">Details</th>
                </tr>
              </thead>
              <tbody>
                {#each reportData.sessions as session, i}
                  <tr>
                    <td>{i + 1}</td>
                    <td>
                      <div class="session-title">{session.sessionTitle || '-'}</div>
                      <div class="session-topic">{session.sessionTopic || ''}</div>
                    </td>
                    <td>{session.mentorName}</td>
                    <td><span class="badge" class:badge-info={session.mentorType === 'Academic'} class:badge-primary={session.mentorType === 'Industry'}>{session.mentorType}</span></td>
                    <td>{formatDate(session.sessionDate)}</td>
                    <td>{formatTime(session.sessionTime)}</td>
                    <td><span class="participant-count">{session.participantCount}</span></td>
                    <td class="no-print">
                      {#if session.participants && session.participants.length > 0}
                        <button class="btn btn-text btn-sm" on:click={() => toggleSession(session.sessionId)}>
                          {expandedSessions[session.sessionId] ? 'Hide' : 'View'}
                        </button>
                      {:else}
                        <span class="text-muted">-</span>
                      {/if}
                    </td>
                  </tr>
                  {#if expandedSessions[session.sessionId] && session.participants}
                    <tr class="expanded-row">
                      <td colspan="8">
                        <div class="participant-list">
                          <div class="participant-header">
                            <span>Student Name</span>
                            <span>Registration No.</span>
                            <span>CPM Number</span>
                            <span>Year</span>
                          </div>
                          {#each session.participants as student}
                            <div class="participant-item">
                              <span>{student.fullName}</span>
                              <span>{student.registrationNumber || '-'}</span>
                              <span>{student.cpmNumber || '-'}</span>
                              <span>{student.yearOfStudy || '-'}</span>
                            </div>
                          {/each}
                        </div>
                      </td>
                    </tr>
                  {/if}
                  <!-- Print mode: always show participants -->
                  {#if session.participants && session.participants.length > 0}
                    <tr class="print-only-row">
                      <td colspan="8">
                        <div class="participant-list">
                          <div class="participant-header">
                            <span>Student Name</span>
                            <span>Registration No.</span>
                            <span>CPM Number</span>
                            <span>Year</span>
                          </div>
                          {#each session.participants as student}
                            <div class="participant-item">
                              <span>{student.fullName}</span>
                              <span>{student.registrationNumber || '-'}</span>
                              <span>{student.cpmNumber || '-'}</span>
                              <span>{student.yearOfStudy || '-'}</span>
                            </div>
                          {/each}
                        </div>
                      </td>
                    </tr>
                  {/if}
                {/each}
              </tbody>
            </table>
          </div>
        {:else}
          <div class="empty-state">No sessions found for the selected date range.</div>
        {/if}
      </div>
    {/if}
  </div>
</DashboardLayout>

<style>
  .reports-page {
    display: grid;
    gap: 1.5rem;
  }

  .page-header {
    position: relative;
    overflow: hidden;
    padding: 2rem 2.5rem;
    background: var(--bg-main);
    border: 1px solid var(--border-light);
    border-radius: var(--radius);
  }
  /* Dot-grid — mirrors landing page hero pattern */
  .page-header::before {
    content: '';
    position: absolute;
    inset: -10% 0;
    background-image: radial-gradient(circle, var(--border-light) 1.2px, transparent 1.2px);
    background-size: 28px 28px;
    pointer-events: none;
  }
  .page-header::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(to right, var(--bg-main) 50%, transparent 100%);
    pointer-events: none;
  }
  .page-header > * { position: relative; z-index: 1; }

  .eyebrow {
    display: block;
    font-size: 0.68rem;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 0.14em;
    color: var(--primary);
    opacity: 0.7;
    margin-bottom: 0.4rem;
  }

  .page-header h2 {
    font-family: var(--font-heading);
    font-size: clamp(1.4rem, 3vw, 2rem);
    font-weight: 700;
    letter-spacing: -0.03em;
    line-height: 1.1;
    color: var(--text-main);
    margin-bottom: 0.3rem;
  }

  .page-desc {
    color: var(--text-secondary);
    font-size: 0.875rem;
    margin-top: 0.25rem;
  }

  .filters-card {
    padding: 1.5rem;
  }

  .filters-row {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }

  .preset-btns {
    display: flex;
    gap: 0.5rem;
  }

  .preset-btn {
    padding: 0.4rem 1rem;
    border-radius: var(--radius-sm);
    font-size: 0.8rem;
    font-weight: 500;
    background: var(--bg-secondary);
    color: var(--text-secondary);
    border: 1px solid var(--border-light);
    cursor: pointer;
    transition: all var(--transition-fast);
  }

  .preset-btn:hover {
    background: var(--primary-50);
    color: var(--primary);
    border-color: var(--primary-100);
  }

  .preset-btn.active {
    background: var(--primary-50);
    color: var(--primary);
    border-color: var(--primary);
    font-weight: 600;
  }

  .filter-inputs {
    display: flex;
    gap: 0.75rem;
    align-items: flex-end;
    flex-wrap: wrap;
  }

  .filter-group {
    display: flex;
    flex-direction: column;
    gap: 0.3rem;
  }

  .filter-group label {
    font-size: 0.75rem;
    font-weight: 500;
    color: var(--text-muted);
    text-transform: uppercase;
    letter-spacing: 0.05em;
  }

  .filter-group .input {
    width: auto;
    min-width: 160px;
  }

  .generate-btn {
    height: fit-content;
  }

  .report-content {
    display: grid;
    gap: 1.5rem;
  }

  .report-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    padding: 1.5rem;
    background: var(--bg-main);
    border: 1px solid var(--border-light);
    border-radius: var(--radius);
  }

  .report-title {
    font-family: var(--font-heading);
    font-size: 1.25rem;
    font-weight: 700;
    letter-spacing: -0.02em;
    color: var(--text-main);
  }

  .report-meta {
    font-size: 0.8rem;
    color: var(--text-muted);
    margin-top: 0.2rem;
  }

  .summary-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 1rem;
  }

  .stat-value {
    font-size: 1.75rem;
    font-weight: 700;
    color: var(--text-main);
    line-height: 1.2;
  }

  .stat-label {
    font-size: 0.8rem;
    color: var(--text-muted);
    margin-top: 0.15rem;
  }

  .sessions-table-wrapper {
    overflow-x: auto;
    border: 1px solid var(--border-light);
    border-radius: var(--radius);
    background: var(--bg-main);
  }

  .report-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 0.85rem;
  }

  .report-table th {
    padding: 0.75rem 1rem;
    text-align: left;
    font-size: 0.75rem;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 0.05em;
    color: var(--text-muted);
    background: var(--bg-secondary);
    border-bottom: 1px solid var(--border-light);
  }

  .report-table th:first-child { border-radius: var(--radius) 0 0 0; }
  .report-table th:last-child { border-radius: 0 var(--radius) 0 0; }

  .report-table td {
    padding: 0.75rem 1rem;
    border-bottom: 1px solid var(--border-light);
    color: var(--text-secondary);
    vertical-align: top;
  }

  .report-table tbody tr:hover td {
    background: var(--bg-alt);
  }

  .report-table tbody tr:last-child td {
    border-bottom: none;
  }

  .session-title {
    font-weight: 500;
    color: var(--text-main);
  }

  .session-topic {
    font-size: 0.78rem;
    color: var(--text-muted);
    margin-top: 0.1rem;
  }

  .participant-count {
    font-weight: 600;
    color: var(--primary);
  }

  .text-muted {
    color: var(--text-muted);
    font-size: 0.8rem;
  }

  .btn-sm {
    padding: 0.3rem 0.6rem;
    font-size: 0.78rem;
  }

  .expanded-row td {
    padding: 0;
    background: var(--bg-alt);
  }

  .participant-list {
    padding: 0.75rem 1.5rem;
  }

  .participant-header {
    display: grid;
    grid-template-columns: 1fr 1fr 1fr 0.5fr;
    gap: 0.5rem;
    font-size: 0.72rem;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 0.05em;
    color: var(--text-muted);
    padding-bottom: 0.5rem;
    border-bottom: 1px solid var(--border-light);
    margin-bottom: 0.4rem;
  }

  .participant-item {
    display: grid;
    grid-template-columns: 1fr 1fr 1fr 0.5fr;
    gap: 0.5rem;
    font-size: 0.82rem;
    color: var(--text-secondary);
    padding: 0.35rem 0;
    border-bottom: 1px solid var(--border-light);
  }

  .participant-item:last-child {
    border-bottom: none;
  }

  .print-only-row {
    display: none;
  }

  .empty-state {
    text-align: center;
    padding: 3rem;
    color: var(--text-muted);
    font-size: 0.9rem;
    background: var(--bg-main);
    border: 1px solid var(--border-light);
    border-radius: var(--radius);
  }

  @media print {
    .no-print { display: none !important; }
    .print-only-row { display: table-row !important; }
    .expanded-row { display: none !important; }
    .report-header { border: none; padding: 0 0 1rem 0; }
    .sessions-table-wrapper { border: 1px solid #ccc; }
    .stat-card { border: 1px solid #ccc; box-shadow: none; }
  }

  @media (max-width: 900px) {
    .filter-inputs {
      flex-direction: column;
      align-items: stretch;
    }
    .filter-group .input {
      min-width: 100%;
    }
    .participant-header, .participant-item {
      grid-template-columns: 1fr 1fr;
    }
  }
</style>
