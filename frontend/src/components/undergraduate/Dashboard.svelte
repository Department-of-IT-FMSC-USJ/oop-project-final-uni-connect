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
  let notePoolCount = 0;
  let recentNotes = [];
  let myProofs = [];
  let proofError = '';
  let proofSuccess = '';
  let showProofUpload = false;
  let proofForm = {
    title: '',
    description: '',
    eventDate: '',
    proofData: '',
    category: 'ACTIVITY'
  };

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
      const profile = await api.get('/users/profile', { cache: false });
      points = profile.cumulativePoints || 0;
      const lvl = computeLevel(points);
      level = lvl.level;
      nextLevelPoints = lvl.next;
      user = { ...user, ...profile };
    } catch (e) { console.error('Failed to load profile', e); }

    try {
      const mentorData = await api.get(`/mentor/connections/${user.userId || user.id}`, { cache: false });
      const connections = mentorData.data || [];
      for (const conn of connections) {
        if (conn.mentorType === 'Academic' && conn.connectionStatus === 'Approved') {
          try {
            const mUser = await api.get(`/users/${conn.mentorId}`, { cache: false });
            mentors.academic = mUser.data;
          } catch {}
        }
        if (conn.mentorType === 'Industry' && conn.connectionStatus === 'Approved') {
          try {
            const mUser = await api.get(`/users/${conn.mentorId}`, { cache: false });
            mentors.industry = mUser.data;
          } catch {}
        }
      }
      mentors = mentors;
    } catch (e) { console.error('Failed to load mentors', e); }

    try {
      const materialsRes = await api.get('/materials', { cache: false });
      const allMaterials = materialsRes.data || [];
      notePoolCount = allMaterials.length;
      recentNotes = allMaterials.slice(0, 3);
    } catch (e) {
      console.error('Failed to load note pool summary', e);
    }

    try {
      myProofs = await api.get('/proofs/my', { cache: false });
    } catch (e) {
      console.error('Failed to load proof submissions', e);
      myProofs = [];
    }
  });

  async function submitProof() {
    proofError = '';
    proofSuccess = '';

    if (!proofForm.title.trim() || !proofForm.eventDate || !proofForm.proofData.trim()) {
      proofError = 'Title, event date, and evidence link are required.';
      return;
    }

    const eventDate = new Date(`${proofForm.eventDate}T00:00:00`);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const oldestAllowed = new Date();
    oldestAllowed.setFullYear(oldestAllowed.getFullYear() - 2);
    oldestAllowed.setHours(0, 0, 0, 0);
    if (eventDate > today) {
      proofError = 'Event date cannot be in the future.';
      return;
    }
    if (eventDate < oldestAllowed) {
      proofError = 'Event date must be within the past two years.';
      return;
    }

    if (!isGoogleDriveLink(proofForm.proofData)) {
      proofError = 'Only Google Drive links are allowed for evidence.';
      return;
    }

    try {
      await api.post('/proofs', {
        title: proofForm.title.trim(),
        description: proofForm.description.trim(),
        eventDate: proofForm.eventDate,
        proofData: proofForm.proofData.trim(),
        category: proofForm.category
      });
      proofForm = { title: '', description: '', eventDate: '', proofData: '', category: 'ACTIVITY' };
      proofSuccess = 'Evidence submitted successfully.';
      showProofUpload = false;
      myProofs = await api.get('/proofs/my', { cache: false });
    } catch (e) {
      proofError = e?.data?.message || 'Failed to submit evidence.';
    }
  }

  function triggerFeedback(sessionId) {
    feedbackSessionId = sessionId;
    showFeedback = true;
  }

  function maxEvidenceDate() {
    return formatLocalDate(new Date());
  }

  function minEvidenceDate() {
    const date = new Date();
    date.setFullYear(date.getFullYear() - 2);
    return formatLocalDate(date);
  }

  function formatLocalDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  function isGoogleDriveLink(value) {
    const trimmed = value?.trim();
    if (!trimmed) return false;

    try {
      const url = new URL(trimmed.startsWith('http') ? trimmed : `https://${trimmed}`);
      return url.hostname.toLowerCase() === 'drive.google.com';
    } catch {
      return false;
    }
  }
</script>

<DashboardLayout navItems={undergraduateNavItems} activeItem="dashboard" pageTitle="Student Dashboard">
  <div class="grid">
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

    <div class="card note-pool-card">
      <div class="section-header">
        <h2>Note Pool</h2>
        <span class="pool-count">{notePoolCount}</span>
      </div>
      <p class="note-copy">Upload PDF notes to the shared note pool and browse notes from other students.</p>
      {#if recentNotes.length === 0}
        <p class="empty-state">No notes in the pool yet.</p>
      {:else}
        <div class="recent-note-list">
          {#each recentNotes as item}
            <div class="recent-note-item">
              <strong>{item.title}</strong>
              <span>Year {item.targetYearOfStudy || '-'}</span>
            </div>
          {/each}
        </div>
      {/if}
      <div class="card-actions">
        <a href="/undergraduate/note-pool?upload=1" class="btn btn-primary btn-sm">Upload Notes</a>
        <a href="/undergraduate/note-pool" class="btn btn-outline btn-sm">View Note Pool</a>
      </div>
    </div>
  </div>

  <div class="dashboard-grid">
    <section class="card evidence-card">
      <div class="section-head">
        <div>
          <p class="eyebrow">Evidence</p>
          <h2>Proof Submissions</h2>
        </div>
        <button class="btn btn-primary btn-sm" on:click={() => showProofUpload = true}>Upload Evidence</button>
      </div>

      {#if proofSuccess}
        <div class="alert alert-success">{proofSuccess}</div>
      {/if}
      {#if myProofs.length === 0}
        <p class="empty-state">No evidence submitted yet.</p>
      {:else}
        <div class="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Title</th>
                <th>Category</th>
                <th>Status</th>
                <th>Date</th>
              </tr>
            </thead>
            <tbody>
              {#each myProofs.slice(0, 5) as proof}
                <tr>
                  <td>{proof.title}</td>
                  <td>{proof.pointCategory || '-'}</td>
                  <td>{proof.pointStatus || 'PENDING'}</td>
                  <td>{proof.eventDate || '-'}</td>
                </tr>
              {/each}
            </tbody>
          </table>
        </div>
      {/if}
    </section>

    <div class="mentors-section">
      <h2 class="section-title">Your Mentors</h2>
      <div class="mentor-grid">
        {#if mentors.academic}
          <div class="card mentor-card">
            <div class="mentor-type">Academic Mentor</div>
            <div class="mentor-info">
              <div class="mentor-avatar">{mentors.academic.fullName?.charAt(0) || 'A'}</div>
              <div>
                <h3>{mentors.academic.fullName}</h3>
                <p>{mentors.academic.department || 'Department not set'}</p>
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
              <div class="mentor-avatar industry-avatar">{mentors.industry.fullName?.charAt(0) || 'I'}</div>
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
  </div>

  {#if showProofUpload}
    <div class="modal-overlay" on:click|self={() => showProofUpload = false}>
      <div class="modal-content">
        <h2>Upload Evidence</h2>
        {#if proofError}
          <div class="alert alert-error">{proofError}</div>
        {/if}
        <div class="form-grid">
          <div class="form-group full-width">
            <label for="proof-title">Title</label>
            <input id="proof-title" class="input" bind:value={proofForm.title} placeholder="Hackathon participation" />
          </div>
          <div class="form-group">
            <label for="proof-category">Category</label>
            <select id="proof-category" class="input" bind:value={proofForm.category}>
              <option value="ACTIVITY">Activity</option>
              <option value="AWARD">Award</option>
              <option value="DIRECT">Direct</option>
            </select>
          </div>
          <div class="form-group">
            <label for="proof-date">Event Date</label>
            <input id="proof-date" type="date" class="input" bind:value={proofForm.eventDate} min={minEvidenceDate()} max={maxEvidenceDate()} />
          </div>
          <div class="form-group full-width">
            <label for="proof-description">Description</label>
            <textarea id="proof-description" class="input" rows="4" bind:value={proofForm.description}></textarea>
          </div>
          <div class="form-group full-width">
            <label for="proof-data">Evidence Link</label>
            <textarea id="proof-data" class="input" rows="3" bind:value={proofForm.proofData} placeholder="Paste the Google Drive link here."></textarea>
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn btn-outline" on:click={() => showProofUpload = false}>Cancel</button>
          <button class="btn btn-primary" on:click={submitProof}>Submit Evidence</button>
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
  .grid { display:grid; grid-template-columns:1fr 1fr 1fr; gap:1.5rem; margin-bottom:2rem; }
  .dashboard-grid { display:grid; grid-template-columns:1.2fr 1fr; gap:1.5rem; }
  .section-title { font-size:1rem; font-weight:600; margin-bottom:1rem; color:var(--gray-800); }
  .section-header, .section-head { display:flex; align-items:center; justify-content:space-between; gap:0.75rem; margin-bottom:1rem; color:var(--gray-700); }
  .points-display { text-align:center; margin-bottom:1rem; }
  .points-number { display:block; font-size:3rem; font-weight:700; color:var(--accent); line-height:1; }
  .points-label { font-size:1rem; color:var(--gray-500); font-weight:500; }
  .progress-bar { height:8px; background:var(--gray-200); border-radius:4px; overflow:hidden; margin-bottom:0.5rem; }
  .progress-bar.small { height:6px; margin-top:0.75rem; }
  .progress-fill { height:100%; background:var(--accent); border-radius:4px; transition:width 0.5s ease; }
  .progress-fill.accent { background:var(--accent); }
  .level-text { font-size:0.8125rem; font-weight:500; color:var(--gray-700); }
  .next-text { color:var(--gray-400); font-weight:400; }
  .empty-state { color:var(--gray-500); }
  .note-copy { color:var(--gray-600); margin-bottom:1rem; }
  .pool-count { font-size:1.5rem; font-weight:700; color:var(--primary); }
  .recent-note-list { display:grid; gap:0.65rem; margin-bottom:1rem; }
  .recent-note-item { display:flex; justify-content:space-between; gap:0.75rem; padding:0.7rem 0.85rem; background:var(--gray-50); border-radius:var(--radius); color:var(--gray-700); }
  .card-actions { display:flex; gap:0.75rem; flex-wrap:wrap; }
  .table-wrapper { overflow-x:auto; }
  table { width:100%; border-collapse:collapse; }
  th, td { padding:0.9rem 0.75rem; border-bottom:1px solid var(--gray-200); text-align:left; }
  th { font-size:0.76rem; text-transform:uppercase; letter-spacing:0.05em; color:var(--gray-500); }
  .mentors-section { display:grid; gap:1rem; }
  .mentor-grid { display:grid; gap:1rem; }
  .mentor-card { display:grid; gap:1rem; }
  .mentor-type { font-size:0.75rem; text-transform:uppercase; letter-spacing:0.08em; font-weight:700; color:var(--accent); }
  .mentor-info { display:flex; gap:0.85rem; align-items:center; }
  .mentor-avatar { width:48px; height:48px; border-radius:50%; background:var(--primary); color:#fff; display:flex; align-items:center; justify-content:center; font-weight:700; }
  .industry-avatar { background:var(--accent); }
  .mentor-actions { display:flex; gap:0.75rem; flex-wrap:wrap; }
  .pending-text,.locked-text { color:var(--gray-600); }
  .eyebrow { font-size:0.75rem; text-transform:uppercase; letter-spacing:0.08em; font-weight:700; color:var(--accent); margin-bottom:0.35rem; }
  .alert { padding:0.8rem 1rem; border-radius:var(--radius); margin-bottom:1rem; }
  .alert-error { background:#fee2e2; color:#991b1b; }
  .alert-success { background:#d1fae5; color:#065f46; }
  .form-grid { display:grid; grid-template-columns:repeat(2,minmax(0,1fr)); gap:1rem; margin-top:1rem; }
  .full-width { grid-column:1 / -1; }
  .form-group label { display:block; margin-bottom:0.35rem; font-size:0.82rem; color:var(--gray-600); }
  .modal-actions { display:flex; justify-content:flex-end; gap:0.75rem; margin-top:1rem; }
  @media (max-width: 1100px) { .grid, .dashboard-grid { grid-template-columns:1fr; } }
</style>
