<script>
  import { onMount } from 'svelte';
  import { api, getCurrentUser, getRoleDashboardPath } from '../../lib/api.js';
  import { academicMentorNavItems } from '../../lib/navigation.js';
  import DashboardLayout from '../shared/DashboardLayout.svelte';
  import ConfirmDialog from '../shared/ConfirmDialog.svelte';
  import CustomSelect from '../shared/CustomSelect.svelte';
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

  const sessionTypeOptions = [
    { value: 'ONE_TO_ONE', label: 'One To One' },
    { value: 'ALL_ASSIGNED', label: 'All Assigned Students' },
    { value: 'YEAR', label: 'All Students in One Year' },
    { value: 'CUSTOM', label: 'Custom Student List' }
  ];

  const durationOptions = [
    { value: 15, label: '15 minutes' },
    { value: 30, label: '30 minutes' },
    { value: 45, label: '45 minutes' },
    { value: 60, label: '1 hour' },
    { value: 90, label: '1.5 hours' },
    { value: 120, label: '2 hours' }
  ];

  function formatDate(iso) { return iso ? iso.split('T')[0] : '-'; }
  function formatTime(iso) { if (!iso) return '-'; const t = iso.split('T')[1]; return t ? t.substring(0, 5) : '-'; }
  function statusLabel(s) { return { SCHEDULED: 'Scheduled', CANCELLED: 'Cancelled', COMPLETED: 'Completed', MISSED: 'Missed', CREATED: 'Created' }[s] || s; }

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
      sessionTime: '',
      durationMinutes: 30
    };
  }

  async function loadSessions() {
    loading = true;
    error = '';
    try {
      const res = await api.get('/scheduling/sessions/my', { cache: false });
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

    let participantUserIds;
    if (isOneToOne) {
      participantUserIds = form.targetStudentIds.slice(0, 1);
    } else if (isYearGroup) {
      participantUserIds = students
        .filter(s => String(s.yearOfStudy || s.academicYear) === String(form.targetYearOfStudy))
        .map(s => s.id || s.studentId);
    } else if (isCustomGroup) {
      participantUserIds = [...form.targetStudentIds];
    } else {
      participantUserIds = students.map(s => s.id || s.studentId);
    }

    const startsAt = `${form.sessionDate}T${form.sessionTime}`;

    return {
      title: form.sessionTopic.trim(),
      topic: form.sessionTopic.trim(),
      description: form.sessionDescription.trim(),
      startsAt,
      durationMinutes: Number(form.durationMinutes),
      participantUserIds
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
      await api.post('/scheduling/sessions', normalizedPayload());
      form = initialForm();
      showCreate = false;
      await loadSessions();
      toast.success({
        title: 'Session created',
        message: 'A Jitsi meeting link was auto-generated for this session.'
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
    cancelLoading = true;
    try {
      await api.post(`/scheduling/sessions/${pendingCancelSession.id}/cancel`);
      pendingCancelSession = null;
      await loadSessions();
      toast.success({
        title: 'Session cancelled',
        message: 'Participants were notified.'
      });
    } catch (e) {
      const message = e?.data?.message || 'Failed to cancel session.';
      error = message;
      toast.error({ title: 'Cancel failed', message });
    } finally {
      cancelLoading = false;
    }
  }

  function studentCount(session) {
    return (session.participants || []).filter(p => p.participantRole === 'STUDENT').length;
  }

  const yearOptions = () => [...new Set(students.map((student) => student.yearOfStudy).filter(Boolean))];
</script>

<DashboardLayout navItems={academicMentorNavItems} activeItem="sessions" pageTitle="Sessions">
  <div class="page-grid">
    <section class="card hero-card">
      <div>
        <p class="eyebrow">Academic Mentor</p>
        <h2>Mentor-Created Sessions</h2>
        <p class="hero-copy">Create one-to-one or group sessions for assigned students. A Jitsi meeting link is auto-generated for each session.</p>
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
              <div class="session-card-head">
                <div class="session-card-identity">
                  <span class="session-card-title">{session.title}</span>
                  {#if session.description}
                    <p class="session-card-desc">{session.description}</p>
                  {/if}
                </div>
                <span class="badge" class:badge-scheduled={session.status === 'SCHEDULED'}
                      class:badge-cancelled={session.status === 'CANCELLED'}
                      class:badge-completed={session.status === 'COMPLETED' || session.status === 'MISSED'}>
                  {statusLabel(session.status)}
                </span>
              </div>
              <div class="session-card-footer">
                <div class="session-card-meta">
                  <span class="meta-chip">{formatDate(session.startsAt)}</span>
                  <span class="meta-chip meta-chip-muted">{formatTime(session.startsAt)}</span>
                  <span class="meta-chip">{session.durationMinutes} min</span>
                  <span class="meta-chip">{studentCount(session)} student(s)</span>
                </div>
                <div class="session-actions">
                  {#if session.canJoin}
                    <a href={session.joinUrl || session.meetingJoinUrl} target="_blank" rel="noopener noreferrer" class="btn btn-primary btn-sm">Join Session</a>
                  {/if}
                  {#if session.status === 'SCHEDULED'}
                    <button class="btn btn-danger btn-sm" on:click={() => requestCancelSession(session)}>Cancel</button>
                  {/if}
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
            <CustomSelect id="academic-type" options={sessionTypeOptions} bind:value={form.sessionType} />
          </div>

          {#if form.sessionType === 'YEAR'}
            <div class="form-group full-width">
              <label for="academic-year">Year</label>
              <CustomSelect
                id="academic-year"
                options={[{ value: '', label: 'Select year' }, ...yearOptions().map((year) => ({ value: year, label: `Year ${year}` }))]}
                bind:value={form.targetYearOfStudy}
              />
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
          <div class="form-group">
            <label for="academic-duration">Duration</label>
            <CustomSelect id="academic-duration" options={durationOptions} bind:value={form.durationMinutes} />
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
    message={`Cancel "${pendingCancelSession?.title || 'this session'}"? Participants will be notified.`}
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
    position: relative;
    overflow: hidden;
    display: flex;
    justify-content: space-between;
    gap: 1rem;
    align-items: flex-start;
    padding: 2rem 2.5rem;
    background: var(--bg-main);
  }
  .hero-card::before {
    content: '';
    position: absolute;
    inset: -10% 0;
    background-image: radial-gradient(circle, var(--border-light) 1.2px, transparent 1.2px);
    background-size: 28px 28px;
    pointer-events: none;
  }
  .hero-card::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(to right, var(--bg-main) 40%, transparent 85%);
    pointer-events: none;
  }
  .hero-card > * { position: relative; z-index: 1; }
  .eyebrow {
    font-size: 0.68rem;
    text-transform: uppercase;
    letter-spacing: 0.14em;
    font-weight: 600;
    color: var(--primary);
    opacity: 0.7;
    margin-bottom: 0.5rem;
  }
  h2 {
    font-family: var(--font-heading);
    font-size: clamp(1.3rem, 3vw, 1.8rem);
    font-weight: 700;
    letter-spacing: -0.03em;
    margin-bottom: 0.4rem;
    color: var(--text-main);
  }
  .hero-copy {
    color: var(--text-secondary, #475569);
    font-size: 0.875rem;
  }

  /* -- Session list & cards -- */
  .session-list {
    display: grid;
    gap: 0.75rem;
  }
  .session-card {
    border: 1px solid var(--border-light);
    border-radius: var(--radius);
    padding: 1.1rem 1.25rem;
    background: transparent;
    transition: border-color 0.15s ease;
  }
  .session-card:hover {
    border-color: var(--border-medium);
  }
  .session-card-head {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 1rem;
  }
  .session-card-identity {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
    min-width: 0;
  }
  .session-card-title {
    font-size: 0.95rem;
    font-weight: 600;
    color: var(--text-main);
    line-height: 1.3;
  }
  .session-card-desc {
    font-size: 0.82rem;
    color: var(--text-secondary);
    line-height: 1.5;
    margin: 0;
  }
  .session-card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 1rem;
    flex-wrap: wrap;
    padding-top: 0.5rem;
    border-top: 1px solid var(--border-light);
    margin-top: 0.6rem;
  }
  .session-card-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
  }
  .meta-chip {
    display: inline-flex;
    align-items: center;
    padding: 0.2rem 0.6rem;
    border-radius: var(--radius-sm);
    border: 1px solid var(--border-light);
    font-size: 0.75rem;
    color: var(--text-secondary);
    font-weight: 500;
    white-space: nowrap;
  }
  .meta-chip-muted {
    background: var(--bg-main);
  }
  .session-actions {
    display: flex;
    gap: 0.5rem;
    flex-shrink: 0;
  }

  /* -- Status badges -- */
  .badge {
    display: inline-block;
    padding: 0.2rem 0.7rem;
    border-radius: 999px;
    font-size: 0.75rem;
    font-weight: 500;
    flex-shrink: 0;
  }
  .badge-scheduled { background: #D1FAE5; color: #065F46; }
  .badge-cancelled { background: #FEE2E2; color: #991B1B; }
  .badge-completed { background: #E2E8F0; color: #475569; }

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

  @media (max-width: 700px) {
    .hero-card { flex-direction: column; }
    .session-card-footer { flex-direction: column; align-items: flex-start; }
  }
</style>
