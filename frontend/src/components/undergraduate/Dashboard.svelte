<script>
  import { onMount } from 'svelte';
  import { api, getCurrentUser, getRoleDashboardPath } from '../../lib/api.js';
  import { undergraduateNavItems } from '../../lib/navigation.js';
  import DashboardLayout from '../shared/DashboardLayout.svelte';
  import FeedbackDialog from '../shared/FeedbackDialog.svelte';
  import { toast } from '../../lib/toast.js';

  let user = getCurrentUser();
  let points = 0;
  let level = 'Beginner';
  let nextLevelPoints = 500;
  let mentors = { academic: null, industry: null };
  let events = [];
  let showFeedback = false;
  let feedbackSessionId = null;
  let showInterestForm = false;
  let mentorFlowError = '';
  let mentorFlowLoading = false;
  let refreshTimer = null;
  let interestForm = {
    interestAreas: '',
    specialization: '',
    preferredCompany: ''
  };

  function computeLevel(pts) {
    if (pts >= 2000) return { level: 'Level 5: Expert', next: 0 };
    if (pts >= 1000) return { level: 'Level 4: Advanced', next: 2000 - pts };
    if (pts >= 500) return { level: 'Level 3: Scholar', next: 1000 - pts };
    if (pts >= 100) return { level: 'Level 2: Explorer', next: 500 - pts };
    return { level: 'Level 1: Beginner', next: 100 - pts };
  }

  onMount(() => {
    initializeDashboard();
    refreshTimer = window.setInterval(() => refreshLiveData(), 15000);
    return () => {
      if (refreshTimer) window.clearInterval(refreshTimer);
    };
  });

  async function initializeDashboard() {
    if (!user) { window.location.href = '/'; return; }
    if (user.role !== 'UNDERGRADUATE') { window.location.href = getRoleDashboardPath(user.role); return; }
    await loadProfileSummary();
    await loadMentors();
    await loadUpcomingEvents();
  }

  async function loadProfileSummary() {
    try {
      const profile = await api.get('/users/profile', { cache: false });
      points = profile.cumulativePoints || 0;
      const lvl = computeLevel(points);
      level = lvl.level;
      nextLevelPoints = lvl.next;
      user = { ...user, ...profile };
    } catch (e) {
      console.error('Failed to load profile', e);
    }
  }

  async function loadMentors() {
    mentors = { academic: null, industry: null };
    try {
      const mentorData = await api.get(`/mentor/connections/${user.userId || user.id}`, { cache: false });
      const connections = (mentorData.data || []).filter((entry) => entry.connectionStatus === 'Approved');
      for (const conn of connections) {
        if (conn.mentorType === 'Academic') {
          try {
            const mUser = await api.get(`/users/${conn.mentorId}`, { cache: false });
            mentors.academic = mUser.data;
          } catch (e) { console.error('Failed to load academic mentor', e); }
        }
        if (conn.mentorType === 'Industry') {
          try {
            const mUser = await api.get(`/users/${conn.mentorId}`, { cache: false });
            mentors.industry = mUser.data;
          } catch (e) { console.error('Failed to load industry mentor', e); }
        }
      }
    } catch (e) {
      console.error('Failed to load mentors', e);
    }
  }

  async function loadUpcomingEvents() {
    events = [];
    try {
      const sessionsRes = await api.get('/scheduling/sessions/my', { cache: false });
      const allSessions = sessionsRes?.data || [];
      const now = new Date();
      events = allSessions
        .filter((session) => session.status === 'SCHEDULED' || session.status === 'CREATED')
        .map((session) => {
          const startsAt = session.startsAt ? new Date(session.startsAt) : null;
          if (!startsAt || Number.isNaN(startsAt.getTime())) return null;
          const mentor = (session.participants || []).find(p => p.participantRole === 'MENTOR');
          return {
            name: session.title || session.topic || 'Mentor Session',
            dateLabel: `${session.startsAt.split('T')[0]} ${(session.startsAt.split('T')[1] || '').substring(0, 5)}`.trim(),
            mentorName: mentor?.fullName || '',
            duration: session.durationMinutes,
            joinUrl: session.joinUrl || session.meetingJoinUrl,
            canJoin: session.canJoin,
            startsAt
          };
        })
        .filter(Boolean)
        .filter((entry) => entry.startsAt >= now)
        .sort((left, right) => left.startsAt - right.startsAt);
    } catch (e) {
      console.error('Failed to load upcoming events', e);
      events = [];
    }
  }

  async function refreshLiveData() {
    await Promise.all([loadProfileSummary(), loadMentors(), loadUpcomingEvents()]);
  }

  async function submitIndustryInterestProfile() {
    mentorFlowError = '';
    const studentId = user?.userId || user?.id;
    if (!studentId) {
      mentorFlowError = 'Unable to identify your account. Please re-login.';
      return;
    }
    const tags = [interestForm.interestAreas, interestForm.specialization, interestForm.preferredCompany]
      .map((v) => v?.trim())
      .filter(Boolean)
      .flatMap((v) => v.split(','))
      .map((v) => v.trim())
      .filter(Boolean);
    if (tags.length < 2) {
      mentorFlowError = 'Add at least two interests/specialization tags.';
      return;
    }
    mentorFlowLoading = true;
    try {
      await api.post('/recommendations/student-profile', {
        studentId,
        studentName: user.fullName,
        department: user.department,
        interestTags: [...new Set(tags)].join(', ')
      });
      await api.post(`/mentor/industry/auto-assign/${studentId}`, {});
      await loadMentors();
      showInterestForm = false;
      interestForm = { interestAreas: '', specialization: '', preferredCompany: '' };
      toast.success({ title: 'Mentor matched', message: 'Your industry mentor has been assigned successfully.' });
    } catch (e) {
      mentorFlowError = e?.data?.message || 'Unable to assign an industry mentor yet. Try refining your interests.';
    } finally {
      mentorFlowLoading = false;
    }
  }

  function triggerFeedback(sessionId) {
    feedbackSessionId = sessionId;
    showFeedback = true;
  }
</script>

<DashboardLayout navItems={undergraduateNavItems} activeItem="dashboard" pageTitle="Student Dashboard">

  <!-- Points / Progress — full-width hero -->
  <section class="card points-hero">
    <div class="points-left">
      <p class="points-eyebrow">Your Progress</p>
      <div class="points-display">
        <span class="points-number">{points}</span>
        <span class="points-unit">Points</span>
      </div>
    </div>
    <div class="points-right">
      <p class="level-name">{level}</p>
      <div class="progress-bar">
        <div class="progress-fill" style="width: {Math.min(100, (points / (points + nextLevelPoints)) * 100)}%"></div>
      </div>
      <p class="next-text">
        {nextLevelPoints > 0 ? `${nextLevelPoints} points to next level` : 'Maximum level reached!'}
      </p>
    </div>
  </section>

  <!-- Two-column grid -->
  <div class="dash-grid">

    <!-- Left: Mentors stacked -->
    <div class="mentors-col">

      {#if mentors.academic}
        <div class="card mentor-card">
          <p class="mentor-type-label">Academic Mentor</p>
          <div class="mentor-identity">
            {#if mentors.academic.profilePicture}
              <img src={mentors.academic.profilePicture} alt={mentors.academic.fullName} class="mentor-avatar mentor-avatar-img" />
            {:else}
              <div class="mentor-avatar">{mentors.academic.fullName?.charAt(0) || 'A'}</div>
            {/if}
            <div class="mentor-info">
              <h3 class="mentor-name">{mentors.academic.fullName}</h3>
              <p class="mentor-dept">{mentors.academic.department || 'Department not set'}</p>
            </div>
          </div>
          <div class="mentor-actions">
            <a href="/undergraduate/mentors" class="btn btn-primary btn-sm">View Sessions</a>
            <a href="/undergraduate/mentors" class="btn btn-outline btn-sm">Send Message</a>
          </div>
        </div>
      {:else}
        <div class="card mentor-card mentor-empty">
          <p class="mentor-type-label">Academic Mentor</p>
          <p class="mentor-pending-text">You will be automatically assigned an academic mentor.</p>
        </div>
      {/if}

      {#if mentors.industry}
        <div class="card mentor-card mentor-industry">
          <p class="mentor-type-label industry">Industry Mentor</p>
          <div class="mentor-identity">
            {#if mentors.industry.profilePicture}
              <img src={mentors.industry.profilePicture} alt={mentors.industry.fullName} class="mentor-avatar industry-avatar mentor-avatar-img" />
            {:else}
              <div class="mentor-avatar industry-avatar">{mentors.industry.fullName?.charAt(0) || 'I'}</div>
            {/if}
            <div class="mentor-info">
              <h3 class="mentor-name">{mentors.industry.fullName}</h3>
              <p class="mentor-dept">{mentors.industry.department || 'Industry Professional'}</p>
            </div>
          </div>
          <div class="mentor-actions">
            <a href="/undergraduate/mentors" class="btn btn-accent btn-sm">View Sessions</a>
            <a href="/undergraduate/mentors" class="btn btn-outline btn-sm">Send Message</a>
          </div>
        </div>
      {:else if points >= 30}
        <div class="card mentor-card mentor-empty">
          <p class="mentor-type-label industry">Industry Mentor</p>
          <p class="mentor-pending-text">You're eligible. Set your interest profile to get a mentor match.</p>
          <button class="btn btn-accent btn-sm" on:click={() => showInterestForm = true}>Set Interests & Match Mentor</button>
        </div>
      {:else}
        <div class="card mentor-card mentor-locked">
          <p class="mentor-type-label">Industry Mentor</p>
          <p class="mentor-locked-text">Earn {30 - points} more points to unlock industry mentor recommendations.</p>
          <div class="progress-bar small">
            <div class="progress-fill accent" style="width: {Math.min(100, (points / 30) * 100)}%"></div>
          </div>
        </div>
      {/if}

    </div>

    <!-- Right: Upcoming sessions -->
    <section class="card sessions-col">
      <div class="section-header">
        <h2 class="section-title">Upcoming Sessions</h2>
        <a href="/undergraduate/mentors" class="mini-link">View all</a>
      </div>
      {#if events.length === 0}
        <p class="empty-state">No upcoming sessions scheduled.</p>
      {:else}
        <div class="session-event-list">
          {#each events as event}
            <div class="session-event-item">
              <div class="session-event-head">
                <div class="session-event-name">{event.name}</div>
                {#if event.canJoin}
                  <a href={event.joinUrl} target="_blank" rel="noopener noreferrer" class="btn btn-primary btn-xs">Join</a>
                {/if}
              </div>
              <span class="session-event-date">{event.dateLabel}{event.mentorName ? ` • ${event.mentorName}` : ''}{event.duration ? ` • ${event.duration} min` : ''}</span>
            </div>
          {/each}
        </div>
      {/if}
    </section>

  </div>

  {#if showInterestForm}
    <div class="modal-overlay" on:click|self={() => showInterestForm = false}>
      <div class="modal-content">
        <h2>Industry Mentor Matching</h2>
        <p class="modal-copy">Tell us your focus areas so we can match you with the right industry mentor.</p>
        {#if mentorFlowError}
          <div class="alert alert-error">{mentorFlowError}</div>
        {/if}
        <div class="form-grid">
          <div class="form-group full-width">
            <label for="interest-areas">Areas of Interest</label>
            <input id="interest-areas" class="input" bind:value={interestForm.interestAreas} placeholder="AI, Cloud Computing, Cyber Security" />
          </div>
          <div class="form-group">
            <label for="interest-specialization">Specialization</label>
            <input id="interest-specialization" class="input" bind:value={interestForm.specialization} placeholder="Backend Engineering, Data Science" />
          </div>
          <div class="form-group">
            <label for="interest-company">Preferred Company / Domain</label>
            <input id="interest-company" class="input" bind:value={interestForm.preferredCompany} placeholder="FinTech, Google, AWS" />
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn btn-outline" on:click={() => showInterestForm = false}>Cancel</button>
          <button class="btn btn-primary" on:click={submitIndustryInterestProfile} disabled={mentorFlowLoading}>
            {mentorFlowLoading ? 'Matching...' : 'Find Mentor'}
          </button>
        </div>
      </div>
    </div>
  {/if}

  {#if showFeedback}
    <FeedbackDialog
      sessionId={feedbackSessionId}
      studentId={user?.userId || user?.id}
      onClose={() => showFeedback = false}
      onSubmit={() => showFeedback = false}
    />
  {/if}

</DashboardLayout>

<style>
  /* Points hero */
  .points-hero {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 2rem;
    padding: 2rem 2.5rem;
    margin-bottom: 1.5rem;
  }

  .points-left {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
  }

  .points-eyebrow {
    font-size: 0.68rem;
    font-weight: 600;
    letter-spacing: 0.14em;
    text-transform: uppercase;
    color: var(--primary);
    opacity: 0.7;
  }

  .points-display {
    display: flex;
    align-items: baseline;
    gap: 0.5rem;
  }

  .points-number {
    font-family: var(--font-heading);
    font-size: clamp(2.5rem, 6vw, 4rem);
    font-weight: 700;
    letter-spacing: -0.04em;
    color: var(--text-main);
    line-height: 1;
  }

  .points-unit {
    font-size: 1rem;
    font-weight: 500;
    color: var(--text-muted);
  }

  .points-right {
    flex: 1;
    max-width: 320px;
    display: flex;
    flex-direction: column;
    gap: 0.4rem;
  }

  .level-name {
    font-size: 0.875rem;
    font-weight: 600;
    color: var(--text-secondary);
  }

  .progress-bar {
    height: 8px;
    background: var(--bg-secondary);
    border-radius: 4px;
    overflow: hidden;
  }

  .progress-bar.small {
    height: 6px;
    margin-top: 0.5rem;
  }

  .progress-fill {
    height: 100%;
    background: linear-gradient(90deg, var(--primary), var(--primary-light, #6B93E4));
    border-radius: 4px;
    transition: width 0.5s ease;
  }

  .progress-fill.accent {
    background: linear-gradient(90deg, var(--accent, #2BA89C), var(--accent-light, #3DC0B3));
  }

  .next-text {
    font-size: 0.75rem;
    color: var(--text-muted);
  }

  /* Two-column grid */
  .dash-grid {
    display: grid;
    grid-template-columns: 1fr 1.4fr;
    gap: 1.5rem;
    align-items: start;
  }

  .mentors-col {
    display: grid;
    gap: 1rem;
  }

  /* Mentor cards */
  .mentor-card {
    display: grid;
    gap: 0.85rem;
  }

  .mentor-type-label {
    font-size: 0.68rem;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.1em;
    color: var(--primary);
  }

  .mentor-type-label.industry {
    color: var(--accent, #2BA89C);
  }

  .mentor-identity {
    display: flex;
    align-items: center;
    gap: 0.85rem;
  }

  .mentor-avatar {
    width: 46px;
    height: 46px;
    border-radius: 50%;
    background: var(--primary-50);
    color: var(--primary);
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 1.1rem;
    flex-shrink: 0;
  }

  .mentor-avatar-img {
    object-fit: cover;
  }

  .industry-avatar {
    background: rgba(43, 168, 156, 0.12);
    color: var(--accent, #2BA89C);
  }

  .mentor-name {
    font-family: var(--font-heading);
    font-size: 0.9rem;
    font-weight: 700;
    color: var(--text-main);
    margin: 0;
    letter-spacing: -0.01em;
  }

  .mentor-dept {
    font-size: 0.78rem;
    color: var(--text-muted);
    margin: 0;
  }

  .mentor-actions {
    display: flex;
    gap: 0.65rem;
    flex-wrap: wrap;
  }

  .mentor-empty,
  .mentor-locked {
    border-style: dashed;
  }

  .mentor-pending-text,
  .mentor-locked-text {
    font-size: 0.85rem;
    color: var(--text-secondary);
    margin: 0;
  }

  /* Sessions column */
  .sessions-col {
    min-height: 180px;
  }

  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 0.75rem;
    margin-bottom: 1rem;
  }

  .section-title {
    font-family: var(--font-heading);
    font-size: 1rem;
    font-weight: 700;
    letter-spacing: -0.01em;
    color: var(--text-main);
    margin: 0;
  }

  .mini-link {
    color: var(--primary);
    font-size: 0.8rem;
    font-weight: 600;
    text-decoration: none;
  }

  .mini-link:hover { text-decoration: underline; }

  .session-event-list { display: grid; gap: 0.5rem; max-height: 26rem; overflow-y: auto; }

  .session-event-item {
    padding: 0.7rem 0.9rem;
    border: 1px solid var(--border-light);
    border-radius: var(--radius-sm);
    background: var(--bg-alt);
    display: grid;
    gap: 0.2rem;
    transition: border-color 0.15s ease;
  }

  .session-event-item:hover { border-color: var(--border-medium); }

  .session-event-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 0.75rem;
  }

  .session-event-name {
    font-size: 0.875rem;
    font-weight: 600;
    color: var(--text-main);
    line-height: 1.3;
  }

  .session-event-date { font-size: 0.75rem; color: var(--text-muted); }

  .empty-state { color: var(--text-muted); font-size: 0.875rem; }

  /* Modal */
  .modal-copy { margin: 0.5rem 0 1rem; color: var(--text-secondary); font-size: 0.875rem; }
  .alert { padding: 0.8rem 1rem; border-radius: var(--radius-sm); margin-bottom: 1rem; }
  .alert-error { background: var(--danger-light); color: #991b1b; }
  .form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 1rem; margin-top: 1rem; }
  .full-width { grid-column: 1 / -1; }
  .form-group label { display: block; margin-bottom: 0.35rem; font-size: 0.82rem; color: var(--text-secondary); }
  .modal-actions { display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1rem; }
  .btn-sm { padding: 0.375rem 0.8rem; font-size: 0.8rem; }
  .btn-xs { padding: 0.25rem 0.6rem; font-size: 0.72rem; }

  @media (max-width: 900px) {
    .points-hero { flex-direction: column; align-items: flex-start; gap: 1rem; }
    .points-right { max-width: 100%; width: 100%; }
    .dash-grid { grid-template-columns: 1fr; }
  }
</style>
