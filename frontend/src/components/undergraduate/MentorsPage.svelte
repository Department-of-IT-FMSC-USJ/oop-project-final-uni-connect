<script>
  import { onMount } from 'svelte';
  import { api, getCurrentUser, getRoleDashboardPath } from '../../lib/api.js';
  import { undergraduateNavItems } from '../../lib/navigation.js';
  import DashboardLayout from '../shared/DashboardLayout.svelte';
  import DirectMessageModal from '../shared/DirectMessageModal.svelte';

  let user = getCurrentUser();
  let loading = true;
  let error = '';
  let mentors = [];
  let sessions = [];
  let selectedContact = null;
  let initialContactId = null;

  function formatDate(iso) { return iso ? iso.split('T')[0] : '-'; }
  function formatTime(iso) { if (!iso) return '-'; const t = iso.split('T')[1]; return t ? t.substring(0, 5) : '-'; }
  function statusLabel(s) { return { SCHEDULED: 'Scheduled', CANCELLED: 'Cancelled', COMPLETED: 'Completed', MISSED: 'Missed', CREATED: 'Created' }[s] || s; }

  onMount(async () => {
    if (!user) { window.location.href = '/'; return; }
    if (user.role !== 'UNDERGRADUATE') { window.location.href = getRoleDashboardPath(user.role); return; }
    initialContactId = new URLSearchParams(window.location.search).get('contact');

    try {
      const profile = await api.get('/users/profile', { cache: false });
      user = { ...user, ...profile };
      const connectionsRes = await api.get(`/mentor/connections/${user.userId || user.id}`, { cache: false });
      const connections = (connectionsRes.data || []).filter((entry) => entry.connectionStatus === 'Approved');

      mentors = await Promise.all(connections.map(async (connection) => {
        const res = await api.get(`/users/${connection.mentorId}`, { cache: false });
        return { ...(res.data || {}), mentorType: connection.mentorType };
      }));
      if (initialContactId) {
        selectedContact = mentors.find((mentor) => String(mentor.id || mentor.userId) === initialContactId) || null;
      }

      const sessionsRes = await api.get('/scheduling/sessions/my', { cache: false });
      sessions = sessionsRes.data || [];
    } catch (e) {
      error = e?.data?.message || 'Failed to load mentors.';
    } finally {
      loading = false;
    }
  });

  function sessionsForMentor(mentorUserId) {
    return sessions.filter(session => {
      const mentor = (session.participants || []).find(p => p.participantRole === 'MENTOR');
      return mentor && (mentor.userId === mentorUserId);
    });
  }

  function getInitial(name) {
    return name?.charAt(0)?.toUpperCase() || 'M';
  }
</script>

<DashboardLayout navItems={undergraduateNavItems} activeItem="mentors" pageTitle="Mentors">
  <div class="page-grid">

    <section class="card hero-card">
      <div>
        <p class="eyebrow">Undergraduate</p>
        <h2>Your Mentors & Their Sessions</h2>
        <p class="hero-copy">View and join sessions created by your approved mentors, and message them directly.</p>
      </div>
      <div class="hero-meta">
        <span class="hero-count">{mentors.length}</span>
        <span class="hero-label">Connected Mentors</span>
      </div>
    </section>

    {#if error}
      <div class="alert alert-error">{error}</div>
    {/if}

    {#if loading}
      <p class="empty-state">Loading mentors...</p>
    {:else if mentors.length === 0}
      <div class="card"><p class="empty-state">No approved mentors connected yet.</p></div>
    {:else}
      {#each mentors as mentor}
        <section class="card mentor-card">
          <!-- Mentor header -->
          <div class="mentor-header">
            <div class="mentor-identity">
              {#if mentor.profilePicture}
                <img src={mentor.profilePicture} alt={mentor.fullName} class="mentor-avatar mentor-avatar-img" class:industry-avatar={mentor.mentorType === 'Industry'} />
              {:else}
                <div class="mentor-avatar" class:industry-avatar={mentor.mentorType === 'Industry'}>
                  {getInitial(mentor.fullName)}
                </div>
              {/if}
              <div class="mentor-info">
                <span class="mentor-type-chip" class:industry={mentor.mentorType === 'Industry'}>
                  {mentor.mentorType} Mentor
                </span>
                <h3 class="mentor-name">{mentor.fullName}</h3>
                <p class="mentor-dept">{mentor.department || 'Department not set'}</p>
              </div>
            </div>
            <button class="btn btn-outline btn-sm" on:click={() => selectedContact = mentor}>Message Mentor</button>
          </div>

          <!-- Sessions list -->
          <div class="sessions-section">
            <p class="sessions-label">Mentor Sessions</p>
            {#if sessionsForMentor(mentor.id).length === 0}
              <p class="empty-state small">No sessions created by this mentor yet.</p>
            {:else}
              <div class="session-list">
                {#each sessionsForMentor(mentor.id) as session}
                  <article class="session-card">
                    <div class="session-card-head">
                      <div class="session-card-identity">
                        <span class="session-card-title">{session.title}</span>
                        {#if session.topic || session.description}
                          <p class="session-card-desc">{session.description || session.topic}</p>
                        {/if}
                      </div>
                      <span class="session-type-badge" class:badge-cancelled={session.status === 'CANCELLED'}
                            class:badge-completed={session.status === 'COMPLETED' || session.status === 'MISSED'}>
                        {statusLabel(session.status)}
                      </span>
                    </div>
                    <div class="session-card-footer">
                      <div class="session-card-meta">
                        <span class="meta-chip">{formatDate(session.startsAt)}</span>
                        <span class="meta-chip meta-chip-muted">{formatTime(session.startsAt)}</span>
                        <span class="meta-chip">{session.durationMinutes} min</span>
                      </div>
                      {#if session.canJoin}
                        <a href={session.joinUrl || session.meetingJoinUrl} target="_blank" rel="noopener noreferrer" class="btn btn-primary btn-sm">Join Session</a>
                      {/if}
                    </div>
                  </article>
                {/each}
              </div>
            {/if}
          </div>
        </section>
      {/each}
    {/if}

  </div>

  {#if selectedContact}
    <DirectMessageModal contact={selectedContact} onClose={() => selectedContact = null} />
  {/if}

</DashboardLayout>

<style>
  .page-grid { display: grid; gap: 1.5rem; }

  /* Hero card – dot grid */
  .hero-card {
    position: relative;
    overflow: hidden;
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 1.5rem;
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
    display: block;
    margin-bottom: 0.5rem;
    font-size: 0.68rem;
    font-weight: 600;
    letter-spacing: 0.14em;
    text-transform: uppercase;
    color: var(--primary);
    opacity: 0.7;
  }
  h2 {
    font-family: var(--font-heading);
    font-size: clamp(1.3rem, 3vw, 1.8rem);
    font-weight: 700;
    letter-spacing: -0.03em;
    margin-bottom: 0.4rem;
    color: var(--text-main);
  }
  .hero-copy { color: var(--text-secondary); font-size: 0.875rem; max-width: 38rem; }
  .hero-meta { text-align: right; flex-shrink: 0; }
  .hero-count {
    display: block;
    font-family: var(--font-heading);
    font-size: 3rem;
    font-weight: 700;
    letter-spacing: -0.04em;
    color: var(--text-main);
    line-height: 1;
  }
  .hero-label { display: block; font-size: 0.72rem; color: var(--text-muted); margin-top: 0.2rem; }

  /* Mentor card */
  .mentor-card { display: grid; gap: 1.25rem; }

  .mentor-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 1rem;
    padding-bottom: 1rem;
    border-bottom: 1px solid var(--border-light);
  }

  .mentor-identity {
    display: flex;
    align-items: center;
    gap: 1rem;
  }

  .mentor-avatar {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    background: var(--primary-50);
    color: var(--primary);
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 1.2rem;
    flex-shrink: 0;
  }

  .mentor-avatar-img {
    object-fit: cover;
  }

  .industry-avatar {
    background: rgba(43, 168, 156, 0.12);
    color: var(--accent, #2BA89C);
  }

  .mentor-type-chip {
    display: inline-flex;
    padding: 0.15rem 0.5rem;
    border-radius: 999px;
    background: var(--primary-50);
    color: var(--primary);
    font-size: 0.7rem;
    font-weight: 700;
    letter-spacing: 0.04em;
    text-transform: uppercase;
    margin-bottom: 0.3rem;
  }

  .mentor-type-chip.industry {
    background: rgba(43, 168, 156, 0.1);
    color: var(--accent, #2BA89C);
  }

  .mentor-name {
    font-family: var(--font-heading);
    font-size: 1rem;
    font-weight: 700;
    letter-spacing: -0.01em;
    color: var(--text-main);
    margin: 0 0 0.15rem;
  }

  .mentor-dept {
    font-size: 0.8rem;
    color: var(--text-muted);
    margin: 0;
  }

  /* Sessions section */
  .sessions-section { display: grid; gap: 0.75rem; }

  .sessions-label {
    font-size: 0.72rem;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.08em;
    color: var(--text-muted);
    margin: 0;
  }

  .session-list { display: grid; gap: 0.65rem; }

  .session-card {
    padding: 0.9rem 1.1rem;
    border: 1px solid var(--border-light);
    border-radius: var(--radius);
    background: transparent;
    transition: border-color 0.15s ease;
  }
  .session-card:hover { border-color: var(--border-medium); }

  .session-card-head {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 1rem;
  }

  .session-card-identity {
    display: flex;
    flex-direction: column;
    gap: 0.2rem;
    min-width: 0;
  }

  .session-card-title {
    font-size: 0.9rem;
    font-weight: 600;
    color: var(--text-main);
    line-height: 1.3;
  }

  .session-card-desc {
    font-size: 0.8rem;
    color: var(--text-secondary);
    margin: 0;
    line-height: 1.4;
  }

  .session-type-badge {
    flex-shrink: 0;
    padding: 0.18rem 0.55rem;
    border-radius: 999px;
    background: #D1FAE5;
    color: #065F46;
    font-size: 0.7rem;
    font-weight: 700;
    letter-spacing: 0.03em;
    white-space: nowrap;
  }

  .badge-cancelled {
    background: #FEE2E2;
    color: #991B1B;
  }

  .badge-completed {
    background: #E2E8F0;
    color: #475569;
  }

  .session-card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 1rem;
    flex-wrap: wrap;
    padding-top: 0.45rem;
    border-top: 1px solid var(--border-light);
    margin-top: 0.55rem;
  }

  .session-card-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 0.4rem;
  }

  .meta-chip {
    display: inline-flex;
    align-items: center;
    padding: 0.2rem 0.55rem;
    border-radius: var(--radius-sm);
    border: 1px solid var(--border-light);
    font-size: 0.75rem;
    color: var(--text-secondary);
    font-weight: 500;
    white-space: nowrap;
  }

  .meta-chip-muted { background: var(--bg-main); }

  .alert { padding: 0.8rem 1rem; border-radius: var(--radius-sm); margin-bottom: 0; }
  .alert-error { background: var(--danger-light); color: #991b1b; }
  .empty-state { color: var(--text-muted); font-size: 0.875rem; }
  .small { font-size: 0.82rem; }
  .btn-sm { padding: 0.375rem 0.75rem; font-size: 0.8rem; }

  @media (max-width: 700px) {
    .hero-card { flex-direction: column; }
    .hero-meta { text-align: left; }
    .session-card-footer { flex-direction: column; align-items: flex-start; }
    .mentor-header { flex-direction: column; }
  }
</style>
