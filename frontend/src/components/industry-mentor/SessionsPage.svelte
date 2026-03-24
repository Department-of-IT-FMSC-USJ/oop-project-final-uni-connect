<script>
  import { onMount } from 'svelte';
  import { api, getCurrentUser, getRoleDashboardPath } from '../../lib/api.js';
  import { industryMentorNavItems } from '../../lib/navigation.js';
  import DashboardLayout from '../shared/DashboardLayout.svelte';
  import ConfirmDialog from '../shared/ConfirmDialog.svelte';
  import { toast } from '../../lib/toast.js';

  let user = getCurrentUser();
  let sessions = [];
  let students = [];
  let loading = true;
  let error = '';
  let showCreate = false;
  let form = initialForm();
  let refreshTimer = null;
  let pendingCancelSession = null;
  let cancelLoading = false;

  onMount(() => {
    if (!user) { window.location.href = '/'; return; }
    if (user.role !== 'INDUSTRY_MENTOR') { window.location.href = getRoleDashboardPath(user.role); return; }
    Promise.all([loadSessions(), loadStudents()]);
    refreshTimer = window.setInterval(() => loadSessions(), 15000);
    return () => {
      if (refreshTimer) {
        window.clearInterval(refreshTimer);
      }
    };
  });

  function initialForm() {
    return {
      sessionTopic: '',
      sessionType: 'ALL_ASSIGNED',
      targetYearOfStudy: '',
      targetStudentIds: [],
      sessionDescription: '',
      sessionDate: '',
      sessionTime: ''
    };
  }

  async function loadSessions() {
    loading = true;
    error = '';
    try {
      const res = await api.get(`/industry/sessions/mentor/${user.userId || user.id}`, { cache: false });
      sessions = res.data || [];
    } catch (e) {
      error = e?.data?.message || 'Failed to load sessions.';
    } finally {
      loading = false;
    }
  }

  async function loadStudents() {
    try {
      const connectionsRes = await api.get(`/mentor/mentor/${user.userId || user.id}`, { cache: false });
      const connections = (connectionsRes.data || []).filter((entry) => entry.connectionStatus === 'Approved');
      students = await Promise.all(connections.map(async (entry) => {
        const res = await api.get(`/users/${entry.studentId}`, { cache: false });
        return res.data || entry;
      }));
    } catch (e) {
      error = e?.data?.message || 'Failed to load connected students.';
    }
  }

  function todayDate() {
    return new Date().toISOString().split('T')[0];
  }

  function minTimeForSelectedDate(dateValue) {
    if (dateValue !== todayDate()) return null;
    const now = new Date();
    return `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
  }

  function validateFutureDateTime() {
    if (!form.sessionDate || !form.sessionTime) return 'Date and time are required.';
    const scheduled = new Date(`${form.sessionDate}T${form.sessionTime}`);
    if (Number.isNaN(scheduled.getTime()) || scheduled <= new Date()) {
      return 'Session date and time must be in the future.';
    }
    return '';
  }

  function normalizedPayload() {
    const isOneToOne = form.sessionType === 'ONE_TO_ONE';
    const isYearGroup = form.sessionType === 'YEAR';
    const isCustomGroup = form.sessionType === 'CUSTOM';
    const targetStudentIds = isOneToOne
      ? form.targetStudentIds.slice(0, 1)
      : isCustomGroup
        ? form.targetStudentIds
        : [];

    return {
      mentorId: user.userId || user.id,
      sessionTitle: form.sessionTopic.trim(),
      sessionTopic: form.sessionTopic.trim(),
      sessionType: isOneToOne ? 'ONE_TO_ONE' : 'GROUP',
      audienceMode: isOneToOne ? 'ONE_TO_ONE' : isYearGroup ? 'YEAR' : isCustomGroup ? 'CUSTOM' : 'ALL_ASSIGNED',
      targetYearOfStudy: isYearGroup ? form.targetYearOfStudy : '',
      targetStudentIds,
      sessionDescription: form.sessionDescription.trim(),
      sessionDate: form.sessionDate,
      sessionTime: form.sessionTime
    };
  }

  async function createSession() {
    error = '';

    if (!form.sessionTopic.trim()) {
      error = 'Topic is required.';
      return;
    }

    const futureError = validateFutureDateTime();
    if (futureError) {
      error = futureError;
      return;
    }

    if (form.sessionType === 'ONE_TO_ONE' && form.targetStudentIds.length !== 1) {
      error = 'Select exactly one student for a one-to-one session.';
      return;
    }

    if (form.sessionType === 'YEAR' && !form.targetYearOfStudy) {
      error = 'Select a year for this group session.';
      return;
    }

    if (form.sessionType === 'CUSTOM' && form.targetStudentIds.length === 0) {
      error = 'Select at least one student for a custom group session.';
      return;
    }

    try {
      await api.post('/industry/sessions', normalizedPayload());
      form = initialForm();
      showCreate = false;
      await loadSessions();
      toast.success({
        title: 'Session created',
        message: 'Students and the HOD workspace were notified.'
      });
    } catch (e) {
      error = e?.data?.message || 'Failed to create session.';
      toast.error({ title: 'Create failed', message: error });
    }
  }

  function requestCancelSession(session) {
    pendingCancelSession = session;
  }

  async function cancelSession() {
    if (!pendingCancelSession) return;
    const mentorId = user.userId || user.id;
    cancelLoading = true;
    try {
      await api.delete(`/industry/sessions/${pendingCancelSession.sessionId}?mentorId=${mentorId}`);
      pendingCancelSession = null;
      await loadSessions();
      toast.success({
        title: 'Session cancelled',
        message: 'Students and the HOD workspace were notified.'
      });
    } catch (e) {
      const message = e?.data?.message || 'Failed to cancel session.';
      error = message;
      toast.error({ title: 'Cancel failed', message });
    } finally {
      cancelLoading = false;
    }
  }

  function sessionAudienceLabel(session) {
    if (session.sessionType === 'ONE_TO_ONE' || session.audienceMode === 'ONE_TO_ONE') return 'One student';
    if (session.audienceMode === 'YEAR') return `Year ${session.targetYearOfStudy || '-'}`;
    if (session.audienceMode === 'CUSTOM') return 'Custom student group';
    return 'All connected students';
  }

  const yearOptions = () => [...new Set(students.map((student) => student.yearOfStudy).filter(Boolean))];
</script>

<DashboardLayout navItems={industryMentorNavItems} activeItem="sessions" pageTitle="Sessions">
  <section class="card hero-card">
    <div>
      <p class="eyebrow">Industry Mentor</p>
      <h2>Mentor-Created Sessions</h2>
      <p class="hero-copy">Create targeted mentoring sessions. Students only see sessions that are meant for them.</p>
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
              <p class="muted">{session.sessionDescription || 'No description provided.'}</p>
            </div>
              <div class="session-meta">
                <span class="badge badge-info">{session.sessionType === 'ONE_TO_ONE' ? 'One to one' : 'Group'}</span>
                <span class="badge badge-soft">{sessionAudienceLabel(session)}</span>
                <strong>{session.sessionDate} at {session.sessionTime}</strong>
                <div class="session-actions">
                  <button class="btn btn-danger btn-sm" on:click={() => requestCancelSession(session)}>Cancel Session</button>
                </div>
              </div>
            </article>
          {/each}
      </div>
    {/if}
  </section>

  {#if showCreate}
    <div class="modal-overlay" on:click|self={() => showCreate = false}>
      <div class="modal-content">
        <h2>Create Industry Session</h2>
        {#if error}<div class="alert alert-error">{error}</div>{/if}
        <div class="form-grid">
          <div class="form-group full-width">
            <label for="industry-topic">Topic</label>
            <input id="industry-topic" class="input" bind:value={form.sessionTopic} placeholder="CV review workshop" />
          </div>
          <div class="form-group">
            <label for="industry-type">Session Type</label>
            <select id="industry-type" class="input" bind:value={form.sessionType}>
              <option value="ONE_TO_ONE">One To One</option>
              <option value="ALL_ASSIGNED">All Connected Students</option>
              <option value="YEAR">All Students in One Year</option>
              <option value="CUSTOM">Custom Student List</option>
            </select>
          </div>

          {#if form.sessionType === 'YEAR'}
            <div class="form-group full-width">
              <label for="industry-year">Year</label>
              <select id="industry-year" class="input" bind:value={form.targetYearOfStudy}>
                <option value="">Select year</option>
                {#each yearOptions() as year}
                  <option value={year}>Year {year}</option>
                {/each}
              </select>
            </div>
          {/if}

          {#if form.sessionType === 'ONE_TO_ONE' || form.sessionType === 'CUSTOM'}
            <div class="form-group full-width">
              <label>{form.sessionType === 'ONE_TO_ONE' ? 'Student' : 'Custom Student List'}</label>
              <div class="student-picker">
                {#each students as student}
                  <label class="student-option">
                    <input
                      type={form.sessionType === 'ONE_TO_ONE' ? 'radio' : 'checkbox'}
                      name="industry-target-student"
                      checked={form.targetStudentIds.includes(student.id || student.studentId)}
                      on:change={() => {
                        const studentId = student.id || student.studentId;
                        form.targetStudentIds = form.sessionType === 'ONE_TO_ONE'
                          ? [studentId]
                          : form.targetStudentIds.includes(studentId)
                            ? form.targetStudentIds.filter((id) => id !== studentId)
                            : [...form.targetStudentIds, studentId];
                      }}
                    />
                    <span>{student.fullName} • Year {student.yearOfStudy || '-'}</span>
                  </label>
                {/each}
              </div>
            </div>
          {/if}

          <div class="form-group full-width">
            <label for="industry-description">Description</label>
            <textarea id="industry-description" class="input" rows="4" bind:value={form.sessionDescription}></textarea>
          </div>
          <div class="form-group">
            <label for="industry-date">Date</label>
            <input id="industry-date" type="date" class="input" bind:value={form.sessionDate} min={todayDate()} />
          </div>
          <div class="form-group">
            <label for="industry-time">Time</label>
            <input id="industry-time" type="time" class="input" bind:value={form.sessionTime} min={minTimeForSelectedDate(form.sessionDate)} />
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn btn-outline" on:click={() => showCreate = false}>Cancel</button>
          <button class="btn btn-primary" on:click={createSession}>Create</button>
        </div>
      </div>
    </div>
  {/if}

  <ConfirmDialog
    open={!!pendingCancelSession}
    title="Cancel session?"
    message={`Cancel "${pendingCancelSession?.sessionTitle || 'this session'}"? Students and the HOD workspace will be notified.`}
    confirmLabel="Cancel session"
    tone="danger"
    busy={cancelLoading}
    on:cancel={() => pendingCancelSession = null}
    on:confirm={cancelSession}
  />
</DashboardLayout>

<style>
  .hero-card { display:flex; justify-content:space-between; gap:1rem; align-items:start; padding:2rem; margin-bottom:1.5rem; background:linear-gradient(135deg, var(--bg-main, #FFFFFF), var(--primary-50, #EEF2FB)); border:1px solid var(--border-light, #E2E8F0); border-radius:var(--radius, 12px); }
  .eyebrow { font-size:0.75rem; text-transform:uppercase; letter-spacing:0.08em; font-weight:700; color:var(--primary, #4F7CDB); margin-bottom:0.5rem; }
  .hero-copy,.muted { color:var(--text-secondary, #475569); }
  .session-list { display:grid; gap:1rem; }
  .session-card { border:1px solid var(--border-light, #E2E8F0); border-radius:var(--radius, 12px); padding:1rem; display:flex; justify-content:space-between; gap:1rem; transition:background 0.2s ease; }
  .session-card:hover { background:var(--primary-50, #EEF2FB); }
  .session-meta { display:grid; gap:0.6rem; text-align:right; min-width:220px; }
  .session-actions { display:flex; justify-content:flex-end; }
  .form-grid { display:grid; grid-template-columns:repeat(2,minmax(0,1fr)); gap:1rem; margin-top:1rem; }
  .full-width { grid-column:1 / -1; }
  .form-group label { display:block; margin-bottom:0.35rem; font-size:0.82rem; color:var(--text-secondary, #475569); }
  .modal-actions { display:flex; justify-content:flex-end; gap:0.75rem; margin-top:1rem; }
  .empty-state { color:var(--text-muted, #94A3B8); padding:1rem 0; }
  .alert { padding:0.8rem 1rem; border-radius:var(--radius-sm, 8px); margin-bottom:1rem; }
  .alert-error { background:var(--danger-light, #FEE2E2); color:#991b1b; }
  .badge-soft { background:var(--bg-secondary, #F1F5F9); color:var(--text-secondary, #475569); padding:0.25rem 0.65rem; border-radius:9999px; font-size:0.78rem; font-weight:600; }
  .student-picker { max-height:220px; overflow-y:auto; border:1px solid var(--border-light, #E2E8F0); border-radius:var(--radius, 12px); padding:0.75rem; display:grid; gap:0.65rem; }
  .student-option { display:flex; gap:0.6rem; align-items:center; color:var(--text-secondary, #475569); }
</style>
