<script>
  import { onMount } from 'svelte';
  import { api, getCurrentUser, getRoleDashboardPath } from '../../lib/api.js';
  import { undergraduateNavItems } from '../../lib/navigation.js';
  import DashboardLayout from '../shared/DashboardLayout.svelte';
  import FeedbackDialog from '../shared/FeedbackDialog.svelte';

  let user = getCurrentUser();
  let points = 0;
  let level = 'Beginner';
  let nextLevelPoints = 500;
  let mentors = { academic: null, industry: null };
  let events = [];
  let showFeedback = false;
  let feedbackSessionId = null;
  let contributions = { lectures: 0, research: 0, notes: 0, ideas: 0 };

  function computeLevel(pts) {
    if (pts >= 2000) return { level: 'Level 5: Expert', next: 0 };
    if (pts >= 1000) return { level: 'Level 4: Advanced', next: 2000 - pts };
    if (pts >= 500) return { level: 'Level 3: Scholar', next: 1000 - pts };
    if (pts >= 100) return { level: 'Level 2: Explorer', next: 500 - pts };
    return { level: 'Level 1: Beginner', next: 100 - pts };
  }

  onMount(async () => {
    if (!user) { window.location.href = '/'; return; }
    if (user.role !== 'UNDERGRADUATE') { window.location.href = getRoleDashboardPath(user.role); return; }

    try {
      const profile = await api.get('/users/profile');
      points = profile.cumulativePoints || 0;
      const lvl = computeLevel(points);
      level = lvl.level;
      nextLevelPoints = lvl.next;
      user = { ...user, ...profile };
    } catch (e) { console.error('Failed to load profile', e); }

    try {
      const mentorData = await api.get(`/mentor/connections/${user.userId || user.id}`);
      if (mentorData.success && mentorData.data) {
        for (const conn of mentorData.data) {
          if (conn.mentorType === 'Academic' && conn.connectionStatus === 'Approved') {
            try {
              const mUser = await api.get(`/users/${conn.mentorId}`);
              if (mUser.success) mentors.academic = mUser.data;
            } catch {}
          }
          if (conn.mentorType === 'Industry' && conn.connectionStatus === 'Approved') {
            try {
              const mUser = await api.get(`/users/${conn.mentorId}`);
              if (mUser.success) mentors.industry = mUser.data;
            } catch {}
          }
        }
        mentors = mentors;
      }
    } catch (e) { console.error('Failed to load mentors', e); }
  });

  function triggerFeedback(sessionId) {
    feedbackSessionId = sessionId;
    showFeedback = true;
  }
</script>

<DashboardLayout navItems={undergraduateNavItems} activeItem="dashboard" pageTitle="Student Dashboard">
  <div class="grid">
    <!-- Points Card -->
    <div class="card points-card">
      <h2 class="section-title">Your Progress & Points</h2>
      <div class="points-display">
        <span class="points-number">{points}</span>
        <span class="points-label">Points</span>
      </div>
      <div class="progress-bar">
        <div class="progress-fill" style="width: {Math.min(100, (points / (points + nextLevelPoints)) * 100)}%"></div>
      </div>
      <p class="level-text">{level} <span class="next-text">{nextLevelPoints > 0 ? `${nextLevelPoints} points to go` : 'Max level!'}</span></p>
    </div>

    <!-- Events Card -->
    <div class="card events-card">
      <div class="section-header">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
        </svg>
        <h2>Upcoming Events</h2>
      </div>
      {#if events.length === 0}
        <p class="empty-state">No upcoming events</p>
      {:else}
        {#each events as event}
          <div class="event-item">
            <strong>{event.name}</strong>
            <span>{event.date}</span>
          </div>
        {/each}
      {/if}
    </div>

    <!-- Contributions Card -->
    <div class="card contributions-card">
      <h2 class="section-title">Contributions</h2>
      <div class="donut-container">
        <div class="donut-center">
          <span class="donut-number">{contributions.lectures + contributions.research + contributions.notes + contributions.ideas}</span>
          <span class="donut-label">Total</span>
        </div>
      </div>
      <div class="contrib-legend">
        <div class="legend-item"><span class="dot" style="background: #3b82f6"></span> Lectures <strong>{contributions.lectures}</strong></div>
        <div class="legend-item"><span class="dot" style="background: #10b981"></span> Research <strong>{contributions.research}</strong></div>
        <div class="legend-item"><span class="dot" style="background: #f59e0b"></span> Notes <strong>{contributions.notes}</strong></div>
        <div class="legend-item"><span class="dot" style="background: #8b5cf6"></span> Ideas <strong>{contributions.ideas}</strong></div>
      </div>
    </div>
  </div>

  <!-- Mentors Section -->
  <div class="mentors-section">
    <h2 class="section-title">Your Mentors</h2>
    <div class="mentor-grid">
      {#if mentors.academic}
        <div class="card mentor-card">
          <div class="mentor-type">Academic Mentor</div>
          <div class="mentor-info">
            <div class="mentor-avatar">
              {mentors.academic.fullName?.charAt(0) || 'A'}
            </div>
            <div>
              <h3>{mentors.academic.fullName}</h3>
              <p>{mentors.academic.department || 'Computer Science Dept'}</p>
            </div>
          </div>
          <div class="mentor-actions">
            <a href="/undergraduate/mentors" class="btn btn-primary btn-sm">View Sessions</a>
            <a href="/undergraduate/mentors" class="btn btn-outline btn-sm">Send Message</a>
          </div>
        </div>
      {:else}
        <div class="card mentor-card pending">
          <div class="mentor-type">Academic Mentor</div>
          <p class="pending-text">You will be automatically assigned an academic mentor.</p>
        </div>
      {/if}

      {#if mentors.industry}
        <div class="card mentor-card industry">
          <div class="mentor-type">Industry Mentor</div>
          <div class="mentor-info">
            <div class="mentor-avatar industry-avatar">
              {mentors.industry.fullName?.charAt(0) || 'I'}
            </div>
            <div>
              <h3>{mentors.industry.fullName}</h3>
              <p>{mentors.industry.department || 'Industry Professional'}</p>
            </div>
          </div>
          <div class="mentor-actions">
            <a href="/undergraduate/mentors" class="btn btn-accent btn-sm">View Sessions</a>
            <a href="/undergraduate/mentors" class="btn btn-outline btn-sm">Send Message</a>
          </div>
        </div>
      {:else if points >= 30}
        <div class="card mentor-card pending">
          <div class="mentor-type">Industry Mentor</div>
          <p class="pending-text">You're eligible! An industry mentor will be recommended soon.</p>
        </div>
      {:else}
        <div class="card mentor-card locked">
          <div class="mentor-type">Industry Mentor</div>
          <p class="locked-text">Earn {30 - points} more points to unlock industry mentor recommendations.</p>
          <div class="progress-bar small">
            <div class="progress-fill accent" style="width: {Math.min(100, (points / 30) * 100)}%"></div>
          </div>
        </div>
      {/if}
    </div>
  </div>

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
  .grid {
    display: grid;
    grid-template-columns: 1fr 1fr 1fr;
    gap: 1.5rem;
    margin-bottom: 2rem;
  }

  .section-title {
    font-size: 1rem;
    font-weight: 600;
    margin-bottom: 1rem;
    color: var(--gray-800);
  }

  .section-header {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin-bottom: 1rem;
    color: var(--gray-700);
  }
  .section-header h2 {
    font-size: 1rem;
    font-weight: 600;
  }

  /* Points */
  .points-display {
    text-align: center;
    margin-bottom: 1rem;
  }
  .points-number {
    display: block;
    font-size: 3rem;
    font-weight: 700;
    color: var(--accent);
    line-height: 1;
  }
  .points-label {
    font-size: 1rem;
    color: var(--gray-500);
    font-weight: 500;
  }

  .progress-bar {
    height: 8px;
    background: var(--gray-200);
    border-radius: 4px;
    overflow: hidden;
    margin-bottom: 0.5rem;
  }
  .progress-bar.small { height: 6px; margin-top: 0.75rem; }
  .progress-fill {
    height: 100%;
    background: var(--accent);
    border-radius: 4px;
    transition: width 0.5s ease;
  }
  .progress-fill.accent { background: var(--accent); }

  .level-text {
    font-size: 0.8125rem;
    font-weight: 500;
    color: var(--gray-700);
  }
  .next-text {
    color: var(--gray-400);
    font-weight: 400;
    margin-left: 0.5rem;
  }

  /* Events */
  .empty-state {
    color: var(--gray-400);
    font-size: 0.875rem;
    text-align: center;
    padding: 2rem 0;
  }
  .event-item {
    display: flex;
    justify-content: space-between;
    padding: 0.75rem;
    border-left: 3px solid var(--accent);
    background: var(--gray-50);
    border-radius: 0 var(--radius) var(--radius) 0;
    margin-bottom: 0.5rem;
    font-size: 0.8125rem;
  }

  /* Contributions */
  .donut-container {
    display: flex;
    justify-content: center;
    padding: 1rem 0;
  }
  .donut-center {
    width: 120px;
    height: 120px;
    border-radius: 50%;
    border: 12px solid var(--gray-200);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
  }
  .donut-number {
    font-size: 1.5rem;
    font-weight: 700;
    color: var(--gray-800);
  }
  .donut-label {
    font-size: 0.75rem;
    color: var(--gray-500);
  }
  .contrib-legend {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 0.5rem;
    font-size: 0.8125rem;
  }
  .legend-item {
    display: flex;
    align-items: center;
    gap: 0.375rem;
    color: var(--gray-600);
  }
  .dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    flex-shrink: 0;
  }
  .legend-item strong { margin-left: auto; color: var(--gray-800); }

  /* Mentors */
  .mentors-section {
    margin-top: 0.5rem;
  }
  .mentor-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1.5rem;
  }
  .mentor-card {
    padding: 1.5rem;
  }
  .mentor-type {
    font-size: 0.75rem;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 0.05em;
    color: var(--gray-500);
    margin-bottom: 1rem;
  }
  .mentor-info {
    display: flex;
    align-items: center;
    gap: 1rem;
    margin-bottom: 1.25rem;
  }
  .mentor-avatar {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    background: var(--primary);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1.25rem;
    font-weight: 600;
    flex-shrink: 0;
  }
  .industry-avatar { background: var(--accent); }
  .mentor-info h3 {
    font-size: 1rem;
    font-weight: 600;
    color: var(--gray-900);
  }
  .mentor-info p {
    font-size: 0.8125rem;
    color: var(--gray-500);
  }
  .mentor-actions {
    display: flex;
    gap: 0.75rem;
  }
  .btn-sm {
    padding: 0.375rem 0.75rem;
    font-size: 0.8125rem;
  }
  .pending-text, .locked-text {
    font-size: 0.875rem;
    color: var(--gray-500);
    text-align: center;
    padding: 1rem 0;
  }

  @media (max-width: 1024px) {
    .grid { grid-template-columns: 1fr; }
    .mentor-grid { grid-template-columns: 1fr; }
  }
</style>
