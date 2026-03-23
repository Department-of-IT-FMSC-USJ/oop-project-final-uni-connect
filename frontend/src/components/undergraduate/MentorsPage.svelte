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
  let selectedSession = null;
  let initialContactId = null;

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

      sessions = await api.get(`/student-sessions/${user.userId || user.id}`, { cache: false });
    } catch (e) {
      error = e?.data?.message || 'Failed to load mentors.';
    } finally {
      loading = false;
    }
  });

  function sessionsForMentor(mentorId) {
    return sessions.filter((session) => session.mentorId === mentorId);
  }
</script>

<DashboardLayout navItems={undergraduateNavItems} activeItem="mentors" pageTitle="Mentors">
  <div class="page-grid">
    <section class="card hero-card">
      <div>
        <p class="eyebrow">Undergraduate</p>
        <h2>Your Mentors and Their Sessions</h2>
        <p class="hero-copy">You cannot create sessions. You can only view and join sessions created by your approved mentors, and message them directly.</p>
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
          <div class="mentor-header">
            <div>
              <p class="type-chip">{mentor.mentorType} Mentor</p>
              <h3>{mentor.fullName}</h3>
              <p class="muted">{mentor.department || 'Department not set'}</p>
              <p class="muted">{mentor.email || 'Email not set'}</p>
            </div>
            <button class="btn btn-outline" on:click={() => selectedContact = mentor}>Message Mentor</button>
          </div>

          <div class="session-list">
            <h4>Mentor-Created Sessions</h4>
            {#if sessionsForMentor(mentor.id).length === 0}
              <p class="empty-state small">No sessions created by this mentor yet.</p>
            {:else}
              {#each sessionsForMentor(mentor.id) as session}
                <article class="session-item">
                  <div>
                    <strong>{session.sessionTitle}</strong>
                    <p class="muted">{session.sessionTopic}</p>
                    <p class="copy">{session.sessionDescription || 'No description provided.'}</p>
                  </div>
                  <div class="session-actions">
                    <span class="badge badge-info">{session.sessionType}</span>
                    <span>{session.sessionDate} at {session.sessionTime}</span>
                    <button class="btn btn-primary btn-sm" on:click={() => selectedSession = session}>Join Session</button>
                  </div>
                </article>
              {/each}
            {/if}
          </div>
        </section>
      {/each}
    {/if}
  </div>

  {#if selectedContact}
    <DirectMessageModal contact={selectedContact} onClose={() => selectedContact = null} />
  {/if}

  {#if selectedSession}
    <div class="modal-overlay" on:click|self={() => selectedSession = null}>
      <div class="modal-content session-modal">
        <div class="modal-header">
          <div>
            <p class="eyebrow">Session Access</p>
            <h3>{selectedSession.sessionTitle}</h3>
          </div>
          <button class="close-btn" on:click={() => selectedSession = null} aria-label="Close session dialog">✕</button>
        </div>
        <p class="muted">{selectedSession.sessionTopic}</p>
        <p class="copy">{selectedSession.sessionDescription || 'No description provided.'}</p>
        <div class="detail-box">
          <strong>{selectedSession.sessionDate} at {selectedSession.sessionTime}</strong>
          <p>Use the mentor messaging button on this page to ask your mentor for the meeting link or room details.</p>
        </div>
        <div class="modal-actions">
          <button class="btn btn-outline" on:click={() => selectedSession = null}>Close</button>
        </div>
      </div>
    </div>
  {/if}
</DashboardLayout>

<style>
  .page-grid { display:grid; gap:1.5rem; }
  .hero-card { display:flex; justify-content:space-between; gap:1rem; padding:2rem; background:linear-gradient(135deg,#fff,#f7fbff); }
  .eyebrow { font-size:0.75rem; text-transform:uppercase; letter-spacing:0.08em; font-weight:700; color:var(--accent); margin-bottom:0.5rem; }
  .hero-copy,.muted { color:var(--gray-600); }
  .hero-count { display:block; font-size:2rem; font-weight:700; color:var(--primary); }
  .hero-label { color:var(--gray-500); font-size:0.85rem; }
  .mentor-card { display:grid; gap:1rem; }
  .mentor-header { display:flex; justify-content:space-between; gap:1rem; align-items:start; }
  .type-chip { display:inline-flex; padding:0.2rem 0.55rem; border-radius:9999px; background:var(--gray-100); color:var(--gray-700); font-size:0.75rem; font-weight:600; margin-bottom:0.45rem; }
  .session-list { display:grid; gap:0.75rem; }
  .session-item { display:flex; justify-content:space-between; gap:1rem; border:1px solid var(--gray-200); border-radius:var(--radius); padding:1rem; }
  .copy { margin-top:0.35rem; color:var(--gray-700); }
  .session-actions { min-width:220px; display:grid; justify-items:end; gap:0.6rem; text-align:right; }
  .empty-state { color:var(--gray-500); }
  .small { font-size:0.9rem; }
  .alert { padding:0.8rem 1rem; border-radius:var(--radius); }
  .alert-error { background:#fee2e2; color:#991b1b; }
  .btn-sm { padding:0.4rem 0.8rem; font-size:0.8rem; }
  .session-modal { width:min(640px,92vw); max-width:640px; }
  .modal-header { display:flex; justify-content:space-between; gap:1rem; margin-bottom:0.75rem; }
  .close-btn { background:transparent; color:var(--gray-500); }
  .detail-box { margin-top:1rem; padding:1rem; border:1px solid var(--gray-200); border-radius:var(--radius); background:var(--gray-50); }
  .modal-actions { display:flex; justify-content:flex-end; margin-top:1rem; }
</style>
