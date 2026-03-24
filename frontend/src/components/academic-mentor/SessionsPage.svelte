<script>
  import { onMount } from 'svelte';
  import { api, getCurrentUser, getRoleDashboardPath } from '../../lib/api.js';
  import { academicMentorNavItems } from '../../lib/navigation.js';
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
    if (user.role !== 'ACADEMIC_MENTOR') { window.location.href = getRoleDashboardPath(user.role); return; }
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
      const res = await api.get(`/academic/sessions/mentor/${user.userId || user.id}`, { cache: false });
      sessions = res.data || [];
    } catch (e) {
      error = e?.data?.message || 'Failed to load sessions.';
    } finally {
      loading = false;
    }
  }

  async function loadStudents() {
    try {
      const mentorId = user.userId || user.id;
      const allocData = await api.get(`/allocations/mentor/${mentorId}`, { cache: false });
      const allocations = allocData.data || [];
      students = await Promise.all(
        allocations.map(async (alloc) => {
          const res = await api.get(`/users/${alloc.studentId}`, { cache: false });
          return { ...alloc, ...(res.data || {}) };
        })
      );
    } catch (e) {
      error = e?.data?.message || 'Failed to load assigned students.';
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

  function toggleStudent(studentId) {
    if (form.targetStudentIds.includes(studentId)) {
      form.targetStudentIds = form.targetStudentIds.filter((id) => id !== studentId);
      return;
    }
    form.targetStudentIds = [...form.targetStudentIds, studentId];
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
      await api.post('/academic/sessions', normalizedPayload());
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
      await api.delete(`/academic/sessions/${pendingCancelSession.sessionId}?mentorId=${mentorId}`);
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
    return 'All assigned students';
  }

  const yearOptions = () => [...new Set(students.map((student) => student.yearOfStudy).filter(Boolean))];
</script>

<DashboardLayout navItems={academicMentorNavItems} activeItem="sessions" pageTitle="Sessions">
  <div class="page-grid">
    <section class="card hero-card">
      <div>
        <p class="eyebrow">Academic Mentor</p>
        <h2>Mentor-Created Sessions</h2>
        <p class="hero-copy">Create one-to-one or group sessions for assigned students. Undergraduates only see sessions targeted to them.</p>
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
  </div>

  {#if showCreate}
    <div class="modal-overlay" on:click|self={() => showCreate = false}>
      <div class="modal-content">
        <h2>Create Academic Session</h2>
        {#if error}<div class="alert alert-error">{error}</div>{/if}
        <div class="form-grid">
          <div class="form-group full-width">
            <label for="academic-topic">Topic</label>
            <input id="academic-topic" class="input" bind:value={form.sessionTopic} placeholder="Career planning for Year 2 students" />
          </div>
          <div class="form-group">
            <label for="academic-type">Session Type</label>
            <select id="academic-type" class="input" bind:value={form.sessionType}>
              <option value="ONE_TO_ONE">One To One</option>
              <option value="ALL_ASSIGNED">All Assigned Students</option>
              <option value="YEAR">All Students in One Year</option>
              <option value="CUSTOM">Custom Student List</option>
            </select>
          </div>

          {#if form.sessionType === 'YEAR'}
            <div class="form-group full-width">
              <label for="academic-year">Year</label>
              <select id="academic-year" class="input" bind:value={form.targetYearOfStudy}>
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
                      name="academic-target-student"
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
                    <span>{student.fullName || student.studentName} • Year {student.yearOfStudy || student.academicYear || '-'}</span>
                  </label>
                {/each}
              </div>
            </div>
          {/if}

          <div class="form-group full-width">
            <label for="academic-description">Description</label>
            <textarea id="academic-description" class="input" rows="4" bind:value={form.sessionDescription}></textarea>
          </div>
          <div class="form-group">
            <label for="academic-date">Date</label>
            <input id="academic-date" type="date" class="input" bind:value={form.sessionDate} min={todayDate()} />
          </div>
          <div class="form-group">
            <label for="academic-time">Time</label>
            <input id="academic-time" type="time" class="input" bind:value={form.sessionTime} min={minTimeForSelectedDate(form.sessionDate)} />
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
  /* -- Page layout -- */
  .page-grid {
    display: grid;
    gap: 1.5rem;
  }

  /* -- Hero card -- */
  .hero-card {
    display: flex;
    justify-content: space-between;
    gap: 1rem;
    align-items: start;
    padding: 2rem;
    background: linear-gradient(135deg, var(--bg-main, #FFFFFF), var(--primary-50, #EEF2FB));
    border-radius: var(--radius, 12px);
  }
  .eyebrow {
    font-size: 0.75rem;
    text-transform: uppercase;
    letter-spacing: 0.08em;
    font-weight: 700;
    color: var(--accent, #2BA89C);
    margin-bottom: 0.5rem;
  }
  .hero-copy, .muted {
    color: var(--text-secondary, #475569);
  }

  /* -- Session list & cards -- */
  .session-list {
    display: grid;
    gap: 1rem;
  }
  .session-card {
    border: 1px solid var(--border-light, #E2E8F0);
    border-radius: var(--radius, 12px);
    padding: 1rem 1.25rem;
    display: flex;
    justify-content: space-between;
    gap: 1rem;
    transition: box-shadow 0.2s ease, border-color 0.2s ease;
  }
  .session-card:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    border-color: var(--border-medium, #CBD5E1);
  }
  .session-meta {
    display: grid;
    gap: 0.6rem;
    text-align: right;
    min-width: 220px;
  }
  .session-actions {
    display: flex;
    justify-content: flex-end;
  }

  /* -- Badges (pill-shaped, semantic) -- */
  .badge-info {
    display: inline-block;
    padding: 0.2rem 0.7rem;
    border-radius: 999px;
    font-size: 0.75rem;
    font-weight: 500;
    background: var(--primary-100, #D4DFFA);
    color: var(--primary-dark, #3A62B5);
  }
  .badge-soft {
    display: inline-block;
    padding: 0.2rem 0.7rem;
    border-radius: 999px;
    font-size: 0.75rem;
    font-weight: 500;
    background: var(--bg-secondary, #F1F5F9);
    color: var(--text-secondary, #475569);
  }

  /* -- Form card & inputs -- */
  .form-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 1rem;
    margin-top: 1rem;
  }
  .full-width {
    grid-column: 1 / -1;
  }
  .form-group label {
    display: block;
    margin-bottom: 0.35rem;
    font-size: 0.82rem;
    color: var(--text-secondary, #475569);
    font-weight: 500;
  }

  /* -- Modal actions -- */
  .modal-actions {
    display: flex;
    justify-content: flex-end;
    gap: 0.75rem;
    margin-top: 1.25rem;
    padding-top: 1rem;
    border-top: 1px solid var(--border-light, #E2E8F0);
  }

  /* -- Empty state -- */
  .empty-state {
    color: var(--text-muted, #94A3B8);
    padding: 1rem 0;
  }

  /* -- Alert -- */
  .alert {
    padding: 0.8rem 1rem;
    border-radius: var(--radius-sm, 8px);
    margin-bottom: 1rem;
  }
  .alert-error {
    background: var(--danger-light, #FEE2E2);
    color: var(--danger, #EF4444);
  }

  /* -- Student picker -- */
  .student-picker {
    max-height: 220px;
    overflow-y: auto;
    border: 1px solid var(--border-light, #E2E8F0);
    border-radius: var(--radius-sm, 8px);
    padding: 0.75rem;
    display: grid;
    gap: 0.65rem;
    background: var(--bg-alt, #F8FAFC);
  }
  .student-option {
    display: flex;
    gap: 0.6rem;
    align-items: center;
    color: var(--text-secondary, #475569);
    font-size: 0.875rem;
  }
</style>
