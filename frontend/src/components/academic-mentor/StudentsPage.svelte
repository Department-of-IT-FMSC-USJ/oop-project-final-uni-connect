<script>
  import { onMount } from 'svelte';
  import { api, getCurrentUser, getRoleDashboardPath } from '../../lib/api.js';
  import { academicMentorNavItems } from '../../lib/navigation.js';
  import DashboardLayout from '../shared/DashboardLayout.svelte';
  import DirectMessageModal from '../shared/DirectMessageModal.svelte';

  let user = getCurrentUser();
  let students = [];
  let loading = true;
  let error = '';
  let query = '';
  let selectedContact = null;
  let selectedProfile = null;
  let initialContactId = null;

  onMount(async () => {
    if (!user) { window.location.href = '/'; return; }
    if (user.role !== 'ACADEMIC_MENTOR') { window.location.href = getRoleDashboardPath(user.role); return; }
    initialContactId = new URLSearchParams(window.location.search).get('contact');

    try {
      const mentorId = user.userId || user.id;
      const allocData = await api.get(`/allocations/mentor/${mentorId}`, { cache: false });
      const allocations = allocData.data || [];
      const detailedStudents = await Promise.all(
        allocations.map(async (alloc) => {
          try {
            const res = await api.get(`/users/${alloc.studentId}`, { cache: false });
            return { ...alloc, ...(res.data || {}) };
          } catch {
            return alloc;
          }
        })
      );
      students = detailedStudents;
      if (initialContactId) {
        selectedContact = students.find((s) => String(s.id || s.studentId) === initialContactId) || null;
      }
    } catch (e) {
      error = 'Failed to load assigned students.';
    } finally {
      loading = false;
    }
  });

  const filteredStudents = () => {
    const normalized = query.trim().toLowerCase();
    if (!normalized) return students;
    return students.filter((student) =>
      [student.fullName, student.email, student.department, student.registrationNumber]
        .filter(Boolean)
        .some((value) => value.toLowerCase().includes(normalized))
    );
  };

  function getInitial(name) {
    return name?.charAt(0)?.toUpperCase() || 'S';
  }
</script>

<DashboardLayout navItems={academicMentorNavItems} activeItem="students" pageTitle="Students">
  <div class="page-grid">
    <section class="card hero-card">
      <div>
        <p class="eyebrow">Academic Mentor</p>
        <h2>Your Assigned Students</h2>
        <p class="hero-copy">View student profiles, allocation details, and start direct mentor conversations.</p>
      </div>
      <div class="hero-meta">
        <span class="hero-count">{students.length}</span>
        <span class="hero-label">Students</span>
      </div>
    </section>

    <section class="card list-card">
      <div class="toolbar">
        <div class="search-box">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          <input class="search-input" bind:value={query} placeholder="Search by name, email, department, or registration..." />
        </div>
      </div>

      {#if error}
        <div class="alert alert-error">{error}</div>
      {/if}

      {#if loading}
        <p class="empty-state">Loading students...</p>
      {:else if filteredStudents().length === 0}
        <p class="empty-state">No students found.</p>
      {:else}
        <div class="student-list">
          {#each filteredStudents() as student}
            <article class="student-card">
              <div class="student-main">
                <div class="student-head">
                  <div class="student-identity">
                    <div class="student-avatar">{getInitial(student.fullName || student.studentName)}</div>
                    <div class="student-title-block">
                      <button
                        class="student-name-btn"
                        on:click={() => selectedProfile = student}
                        title="View student profile"
                      >{student.fullName || student.studentName || 'Unknown'}</button>
                      <span class="student-reg">{student.registrationNumber || 'No Reg. No.'}</span>
                    </div>
                  </div>
                  <span class="year-chip">Year {student.yearOfStudy || student.academicYear || '-'}</span>
                </div>
                <div class="student-footer">
                  <div class="student-meta">
                    {#if student.email}
                      <span class="meta-chip">{student.email}</span>
                    {/if}
                    {#if student.department}
                      <span class="meta-chip meta-chip-muted">{student.department}</span>
                    {/if}
                  </div>
                  <div class="student-actions">
                    <button class="btn btn-outline btn-sm" on:click={() => selectedContact = student}>Message</button>
                  </div>
                </div>
              </div>
            </article>
          {/each}
        </div>
      {/if}
    </section>
  </div>

  {#if selectedProfile}
    <div class="modal-overlay" on:click|self={() => selectedProfile = null}>
      <div class="modal-content profile-modal">
        <div class="modal-header">
          <div>
            <p class="eyebrow">Student Profile</p>
            <h3>{selectedProfile.fullName || selectedProfile.studentName || 'Student'}</h3>
          </div>
          <button class="close-btn" on:click={() => selectedProfile = null}>✕</button>
        </div>
        <div class="profile-body">
          <div class="profile-avatar-row">
            <div class="profile-avatar-lg">{getInitial(selectedProfile.fullName || selectedProfile.studentName)}</div>
            <div>
              <p class="profile-role">Undergraduate Student</p>
              <p class="profile-email">{selectedProfile.email || '-'}</p>
            </div>
          </div>
          <div class="detail-rows">
            <div class="detail-row">
              <span class="detail-label">Registration No.</span>
              <span class="detail-value">{selectedProfile.registrationNumber || '-'}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">Year of Study</span>
              <span class="detail-value">{selectedProfile.yearOfStudy ? `Year ${selectedProfile.yearOfStudy}` : '-'}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">Department</span>
              <span class="detail-value">{selectedProfile.department || '-'}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">Phone</span>
              <span class="detail-value">{selectedProfile.phone || '-'}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">Cumulative Points</span>
              <span class="detail-value points-highlight">{selectedProfile.cumulativePoints ?? '-'}</span>
            </div>
          </div>
          <div class="modal-actions">
            <button class="btn btn-outline" on:click={() => selectedProfile = null}>Close</button>
            <button class="btn btn-primary" on:click={() => { selectedContact = selectedProfile; selectedProfile = null; }}>Send Message</button>
          </div>
        </div>
      </div>
    </div>
  {/if}

  {#if selectedContact}
    <DirectMessageModal contact={selectedContact} onClose={() => selectedContact = null} />
  {/if}
</DashboardLayout>

<style>
  .page-grid { display: grid; gap: 1.5rem; }

  /* Hero card */
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
  .hero-copy { color: var(--text-secondary); font-size: 0.875rem; }
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

  /* List card */
  .list-card { padding: 1.5rem; }

  .toolbar { margin-bottom: 1.25rem; }
  .search-box {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    max-width: 480px;
    border: 1px solid var(--border-medium);
    border-radius: var(--radius);
    padding: 0.55rem 0.85rem;
    background: var(--bg-main);
    color: var(--text-muted);
  }
  .search-input {
    border: none;
    outline: none;
    width: 100%;
    font-size: 0.875rem;
    background: transparent;
    color: var(--text-main);
  }
  .search-input::placeholder { color: var(--text-muted); }

  /* Student list + card */
  .student-list { display: grid; gap: 0.75rem; }

  .student-card {
    padding: 1.1rem 1.25rem;
    border: 1px solid var(--border-light);
    border-radius: var(--radius);
    background: transparent;
    transition: border-color 0.15s ease;
  }
  .student-card:hover { border-color: var(--border-medium); }

  .student-main { display: grid; gap: 0.6rem; }

  .student-head {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 1rem;
  }

  .student-identity {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    min-width: 0;
  }

  .student-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: var(--primary-50);
    color: var(--primary);
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 1rem;
    flex-shrink: 0;
  }

  .student-title-block {
    display: flex;
    flex-direction: column;
    gap: 0.15rem;
    min-width: 0;
  }

  .student-name-btn {
    background: none;
    border: none;
    padding: 0;
    font-size: 0.95rem;
    font-weight: 600;
    color: var(--text-main);
    cursor: pointer;
    text-align: left;
    line-height: 1.3;
    transition: color 0.15s ease;
  }
  .student-name-btn:hover { color: var(--primary); text-decoration: underline; }

  .student-reg {
    font-size: 0.72rem;
    font-weight: 600;
    letter-spacing: 0.06em;
    text-transform: uppercase;
    color: var(--text-muted);
  }

  .year-chip {
    flex-shrink: 0;
    padding: 0.18rem 0.6rem;
    border-radius: 999px;
    background: var(--primary-50);
    color: var(--primary);
    font-size: 0.72rem;
    font-weight: 700;
    letter-spacing: 0.03em;
  }

  .student-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 1rem;
    flex-wrap: wrap;
    padding-top: 0.4rem;
    border-top: 1px solid var(--border-light);
    margin-top: 0.15rem;
  }

  .student-meta {
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
  .meta-chip-muted { background: var(--bg-main); }

  .student-actions { flex-shrink: 0; }

  /* Profile modal */
  .profile-modal { max-width: 480px; }
  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 1rem;
    margin-bottom: 1rem;
  }
  h3 {
    font-family: var(--font-heading);
    font-size: 1.15rem;
    font-weight: 700;
    letter-spacing: -0.02em;
    color: var(--text-main);
    margin: 0;
  }
  .close-btn { background: none; border: none; color: var(--text-muted); font-size: 1.1rem; cursor: pointer; }
  .close-btn:hover { color: var(--text-main); }

  .profile-body { display: grid; gap: 1.25rem; }
  .profile-avatar-row {
    display: flex;
    align-items: center;
    gap: 1rem;
    padding-bottom: 1rem;
    border-bottom: 1px solid var(--border-light);
  }
  .profile-avatar-lg {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    background: var(--primary-50);
    color: var(--primary);
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 1.5rem;
    flex-shrink: 0;
  }
  .profile-role { font-size: 0.75rem; font-weight: 700; text-transform: uppercase; letter-spacing: 0.08em; color: var(--primary); margin: 0 0 0.2rem; }
  .profile-email { font-size: 0.85rem; color: var(--text-muted); margin: 0; }

  .detail-rows { display: grid; gap: 0; }
  .detail-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0.65rem 0;
    border-bottom: 1px solid var(--border-light);
  }
  .detail-row:last-child { border-bottom: none; }
  .detail-label { font-size: 0.72rem; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); }
  .detail-value { font-size: 0.875rem; font-weight: 500; color: var(--text-main); text-align: right; }
  .points-highlight {
    font-family: var(--font-heading);
    font-size: 1.25rem;
    font-weight: 700;
    letter-spacing: -0.02em;
    color: var(--primary);
  }

  .modal-actions { display: flex; justify-content: flex-end; gap: 0.75rem; }
  .btn-sm { padding: 0.375rem 0.75rem; font-size: 0.8125rem; }

  .alert { padding: 0.8rem 1rem; border-radius: var(--radius-sm); margin-bottom: 1rem; }
  .alert-error { background: var(--danger-light); color: var(--danger); }
  .empty-state { color: var(--text-muted); font-size: 0.875rem; text-align: center; padding: 2rem; }

  @media (max-width: 700px) {
    .student-footer { flex-direction: column; align-items: flex-start; }
    .hero-card { flex-direction: column; }
    .hero-meta { text-align: left; }
  }
</style>
