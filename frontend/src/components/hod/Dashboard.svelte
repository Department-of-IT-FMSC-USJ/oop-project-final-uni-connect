<script>
  import { onMount } from 'svelte';
  import { api, getCurrentUser, getRoleDashboardPath, invalidateApiCache } from '../../lib/api.js';
  import { hodNavItems } from '../../lib/navigation.js';
  import DashboardLayout from '../shared/DashboardLayout.svelte';

  let user = getCurrentUser();
  let feedbacks = [];
  let topStudents = [];
  let proofs = [];
  let pendingProofCount = 0;
  let mentorCount = 0;
  let studentCount = 0;
  let showEvidenceReview = false;
  let showAddMentor = false;
  let searchQuery = '';
  let searchResults = [];
  let searchLoading = false;
  let dashboardLoading = true;
  let dashboardError = '';
  let mounted = false;
  let searchController = null;
  let searchDebounce = null;

  // Feedback filters
  let minRating = '';
  let maxRating = '3';
  let sortBy = 'rating_asc';

  // Add mentor form
  let mentorFullName = '';
  let mentorEmail = '';
  let mentorPassword = '';
  let mentorRole = 'ACADEMIC_MENTOR';
  let mentorDepartment = 'Department of Information Technology';
  let mentorPhone = '';
  let mentorError = '';
  let mentorLoading = false;
  let mentorSuccess = '';

  onMount(() => {
    mounted = true;
    if (!user) { window.location.href = '/'; return; }
    if (user.role !== 'DEPARTMENT_HEAD') { window.location.href = getRoleDashboardPath(user.role); return; }

    loadDashboardData();

    return () => {
      mounted = false;
      searchController?.abort();
      if (searchDebounce) clearTimeout(searchDebounce);
    };
  });

  async function loadDashboardData({ force = false } = {}) {
    dashboardLoading = true;
    dashboardError = '';

    try {
      await Promise.all([
        loadSummaryCounts({ force }),
        loadFeedbacks({ force }),
        loadTopStudents({ force }),
        loadProofs({ force })
      ]);
    } catch (e) {
      if (!mounted) return;
      dashboardError = 'Failed to load dashboard data';
    } finally {
      if (mounted) {
        dashboardLoading = false;
      }
    }
  }

  async function loadFeedbacks({ force = false } = {}) {
    try {
      const params = new URLSearchParams();
      if (maxRating) params.set('maxRating', maxRating);
      if (minRating) params.set('minRating', minRating);
      const query = params.toString();
      const path = `/feedback-reviews${query ? `?${query}` : ''}`;
      const res = await api.get(path, { cache: !force });
      if (!mounted) return;
      feedbacks = res.data || [];
    } catch (e) { console.error('Failed to load feedbacks', e); }
  }

  async function loadSummaryCounts({ force = false } = {}) {
    try {
      const [academicMentorsRes, industryMentorsRes, studentsRes] = await Promise.all([
        api.get('/users/by-role/ACADEMIC_MENTOR', { cache: !force }),
        api.get('/users/by-role/INDUSTRY_MENTOR', { cache: !force }),
        api.get('/users/students/search', { cache: !force })
      ]);
      if (!mounted) return;
      const academicMentors = academicMentorsRes.data || [];
      const industryMentors = industryMentorsRes.data || [];
      const students = studentsRes.data || [];
      mentorCount = academicMentors.length + industryMentors.length;
      studentCount = students.length;
    } catch (e) {
      console.error('Failed to load summary counts', e);
    }
  }

  async function loadTopStudents({ force = false } = {}) {
    try {
      const res = await api.get('/users/students/top-points?limit=8', { cache: !force });
      if (!mounted) return;
      topStudents = res.data || [];
    } catch (e) { console.error('Failed to load top students', e); }
  }

  async function loadProofs({ force = false } = {}) {
    try {
      const res = await api.get('/proofs', { cache: !force });
      if (!mounted) return;
      proofs = Array.isArray(res) ? res : (res.data || []);
      pendingProofCount = proofs.filter(p => p.latestStatus === 'PENDING' || !p.latestStatus).length;
    } catch (e) { console.error('Failed to load proofs', e); }
  }

  async function searchStudents() {
    const query = searchQuery.trim();
    searchController?.abort();
    if (searchDebounce) clearTimeout(searchDebounce);

    if (!query) {
      searchResults = [];
      searchLoading = false;
      return;
    }

    searchLoading = true;
    searchDebounce = setTimeout(async () => {
      searchController = new AbortController();
      try {
        const res = await api.get(`/users/students/search?query=${encodeURIComponent(query)}`, {
          cache: false,
          signal: searchController.signal
        });
        if (!mounted || searchQuery.trim() !== query) return;
        searchResults = res.data || [];
      } catch (e) {
        if (e?.name !== 'AbortError' && mounted) {
          searchResults = [];
        }
      } finally {
        if (mounted && searchQuery.trim() === query) {
          searchLoading = false;
        }
      }
    }, 250);
  }

  async function reviewProof(proofId, action) {
    try {
      await api.put(`/proofs/${proofId}/review`, { action });
      invalidateApiCache('/proofs');
      loadProofs({ force: true });
    } catch (e) { console.error('Failed to review proof', e); }
  }

  async function createMentorAccount() {
    mentorError = '';
    mentorSuccess = '';
    mentorLoading = true;
    try {
      await api.post('/users/create-account', {
        fullName: mentorFullName,
        email: mentorEmail,
        password: mentorPassword,
        role: mentorRole,
        department: mentorDepartment,
        phone: mentorPhone
      });
      invalidateApiCache('/users');
      mentorSuccess = `${mentorRole === 'ACADEMIC_MENTOR' ? 'Academic' : 'Industry'} mentor account created successfully!`;
      mentorFullName = ''; mentorEmail = ''; mentorPassword = ''; mentorPhone = '';
    } catch (e) {
      mentorError = e.data?.message || 'Failed to create account';
    } finally { mentorLoading = false; }
  }

  function renderStars(rating) {
    return Array.from({length: 5}, (_, i) => i < rating ? '★' : '☆').join('');
  }
</script>

<DashboardLayout navItems={hodNavItems} activeItem="dashboard" pageTitle="Head of Department Dashboard">
  {#if dashboardError}
    <div class="card error-card">
      <p>{dashboardError}</p>
    </div>
  {/if}

  <!-- Top Actions -->
  <div class="stats-strip">
    <div class="card stat-card">
      <span class="stat-label">Registered Mentors</span>
      <strong class="stat-value">{mentorCount}</strong>
    </div>
    <div class="card stat-card">
      <span class="stat-label">Registered Students</span>
      <strong class="stat-value">{studentCount}</strong>
    </div>
  </div>

  <div class="top-actions">
    <div class="search-box">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
      </svg>
      <input class="search-input" placeholder="Search student by name or ID..." bind:value={searchQuery} on:input={searchStudents} />
    </div>

    <button class="btn btn-primary evidence-btn" on:click={() => showEvidenceReview = true}>
      Evidence Review
      {#if pendingProofCount > 0}
        <span class="evidence-badge">{pendingProofCount}</span>
      {/if}
    </button>

    <button class="btn btn-outline" on:click={() => showAddMentor = true}>
      + Add Mentors
    </button>
  </div>

  <!-- Search Results -->
  {#if searchResults.length > 0}
    <div class="card search-results">
      <h3>Search Results</h3>
      <div class="table-wrapper">
        <table>
          <thead><tr><th>Name</th><th>Reg. No</th><th>Year</th><th>Points</th><th>Department</th></tr></thead>
          <tbody>
            {#each searchResults as student}
              <tr>
                <td><strong>{student.fullName}</strong></td>
                <td>{student.registrationNumber || '-'}</td>
                <td>Year {student.yearOfStudy || '-'}</td>
                <td><span class="badge badge-info">{student.cumulativePoints || 0}</span></td>
                <td>{student.department || '-'}</td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    </div>
  {/if}

  <!-- Main Grid -->
  <div class="dashboard-grid">
    <!-- Low Rating Feedbacks -->
    <div class="card feedback-card">
      <div class="card-header">
        <h2 class="card-title danger-title">Low Rating Feedback Sessions</h2>
        <div class="filter-row">
          <select class="filter-select" bind:value={maxRating} on:change={() => loadFeedbacks({ force: true })}>
            <option value="">All Ratings</option>
            <option value="1">1 Star</option>
            <option value="2">Up to 2 Stars</option>
            <option value="3">Up to 3 Stars</option>
          </select>
        </div>
      </div>
      {#if dashboardLoading && feedbacks.length === 0}
        <p class="empty-state">Loading feedback sessions...</p>
      {:else if feedbacks.length === 0}
        <p class="empty-state">No feedback sessions found</p>
      {:else}
        {#each feedbacks as fb}
          <div class="feedback-item">
            <div class="fb-info">
              <p class="fb-session">Session: {fb.sessionId ? `Session #${fb.sessionId}` : 'Unknown'}</p>
              <p class="fb-mentor">Mentor: {fb.mentorName || 'Unknown'}</p>
              <div class="fb-stars">{renderStars(fb.rating)}</div>
            </div>
            <button class="btn btn-outline btn-sm">View Feedback</button>
          </div>
        {/each}
      {/if}
    </div>

    <!-- Highest Point Students -->
    <div class="card points-card">
      <h2 class="card-title success-title">Highest Point Students</h2>
      {#if dashboardLoading && topStudents.length === 0}
        <p class="empty-state">Loading student rankings...</p>
      {:else if topStudents.length === 0}
        <p class="empty-state">No student data available</p>
      {:else}
        <div class="table-wrapper">
          <table>
            <thead><tr><th>Rank</th><th>Student Name</th><th>Points</th><th>Year</th></tr></thead>
            <tbody>
              {#each topStudents as student, i}
                <tr>
                  <td><strong>Rank {i + 1}</strong></td>
                  <td>{student.fullName}</td>
                  <td><strong>{student.cumulativePoints || 0}</strong></td>
                  <td>Year {student.yearOfStudy || '-'}</td>
                </tr>
              {/each}
            </tbody>
          </table>
        </div>
      {/if}
    </div>
  </div>

  <!-- Evidence Review Modal -->
  {#if showEvidenceReview}
    <div class="modal-overlay" on:click|self={() => showEvidenceReview = false}>
      <div class="modal-content wide-modal">
        <div class="modal-header">
          <h2>Evidence Review</h2>
          <button class="close-btn" on:click={() => showEvidenceReview = false}>✕</button>
        </div>
        {#if dashboardLoading && proofs.length === 0}
          <p class="empty-state">Loading evidence submissions...</p>
        {:else if proofs.length === 0}
          <p class="empty-state">No evidence submissions</p>
        {:else}
          <div class="table-wrapper">
            <table>
              <thead><tr><th>Student</th><th>Title</th><th>Type</th><th>Date</th><th>Status</th><th>Actions</th></tr></thead>
              <tbody>
                {#each proofs as proof}
                  <tr>
                    <td>{proof.studentName || `Student #${proof.studentId}`}</td>
                    <td>{proof.title}</td>
                    <td>{proof.proofType || '-'}</td>
                    <td>{proof.eventDate || '-'}</td>
                    <td>
                      <span class="badge" class:badge-warning={proof.latestStatus === 'PENDING' || !proof.latestStatus}
                        class:badge-success={proof.latestStatus === 'APPROVED'}
                        class:badge-danger={proof.latestStatus === 'REJECTED'}>
                        {proof.latestStatus || 'PENDING'}
                      </span>
                    </td>
                    <td>
                      {#if !proof.latestStatus || proof.latestStatus === 'PENDING'}
                        <button class="btn btn-success btn-xs" on:click={() => reviewProof(proof.id, 'APPROVE')}>Approve</button>
                        <button class="btn btn-danger btn-xs" on:click={() => reviewProof(proof.id, 'REJECT')}>Reject</button>
                      {:else}
                        <span class="text-muted">Reviewed</span>
                      {/if}
                    </td>
                  </tr>
                {/each}
              </tbody>
            </table>
          </div>
        {/if}
      </div>
    </div>
  {/if}

  <!-- Add Mentor Modal -->
  {#if showAddMentor}
    <div class="modal-overlay" on:click|self={() => showAddMentor = false}>
      <div class="modal-content">
        <div class="modal-header">
          <h2>Add Mentor</h2>
          <button class="close-btn" on:click={() => showAddMentor = false}>✕</button>
        </div>
        <p class="modal-desc">Create an account for an academic or industry mentor.</p>

        {#if mentorError}<div class="alert-error">{mentorError}</div>{/if}
        {#if mentorSuccess}<div class="alert-success">{mentorSuccess}</div>{/if}

        <form on:submit|preventDefault={createMentorAccount}>
          <div class="form-group">
            <label>Mentor Type</label>
            <select class="input" bind:value={mentorRole}>
              <option value="ACADEMIC_MENTOR">Academic Mentor</option>
              <option value="INDUSTRY_MENTOR">Industry Mentor</option>
            </select>
          </div>
          <div class="form-group">
            <label>Full Name</label>
            <input class="input" bind:value={mentorFullName} required placeholder="Dr. John Smith" />
          </div>
          <div class="form-group">
            <label>Email</label>
            <input type="email" class="input" bind:value={mentorEmail} required placeholder="mentor@university.edu" />
          </div>
          <div class="form-group">
            <label>Initial Password</label>
            <input type="text" class="input" bind:value={mentorPassword} required placeholder="Temporary password" />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Department</label>
              <select class="input" bind:value={mentorDepartment}>
                <option>Department of Information Technology</option>
                <option>Department of Computer Science</option>
                <option>Department of Software Engineering</option>
                <option>Department of Data Science</option>
              </select>
            </div>
            <div class="form-group">
              <label>Phone</label>
              <input class="input" bind:value={mentorPhone} placeholder="+94 71 234 5678" />
            </div>
          </div>
          <div class="modal-actions">
            <button type="button" class="btn btn-outline" on:click={() => showAddMentor = false}>Cancel</button>
            <button type="submit" class="btn btn-primary" disabled={mentorLoading}>
              {mentorLoading ? 'Creating...' : 'Create Account'}
            </button>
          </div>
        </form>
      </div>
    </div>
  {/if}
</DashboardLayout>

<style>
  .top-actions {
    display: flex;
    align-items: center;
    gap: 1rem;
    margin-bottom: 1.5rem;
  }

  .stats-strip {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 1rem;
    margin-bottom: 1rem;
  }

  .stat-card {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1.1rem 1.25rem;
  }

  .stat-label {
    color: var(--gray-500);
    font-size: 0.875rem;
  }

  .stat-value {
    font-size: 1.9rem;
    color: var(--primary);
  }

  .search-box {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 0.5rem;
    background: white;
    border: 1px solid var(--gray-300);
    border-radius: var(--radius);
    padding: 0.5rem 0.75rem;
  }
  .search-input {
    border: none;
    outline: none;
    width: 100%;
    font-size: 0.875rem;
    background: transparent;
  }

  .evidence-btn { position: relative; }
  .evidence-badge {
    position: absolute;
    top: -6px;
    right: -6px;
    background: var(--danger);
    color: white;
    font-size: 0.65rem;
    width: 18px;
    height: 18px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
  }

  .dashboard-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1.5rem;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
  }

  .card-title {
    font-size: 1rem;
    font-weight: 600;
    padding: 0.5rem 1rem;
    border-radius: var(--radius);
  }
  .danger-title { background: #fee2e2; color: #991b1b; }
  .success-title { background: #d1fae5; color: #065f46; }

  .filter-select {
    padding: 0.375rem 0.75rem;
    border: 1px solid var(--gray-300);
    border-radius: var(--radius);
    font-size: 0.8125rem;
    background: white;
  }

  .feedback-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem;
    border-bottom: 1px solid var(--gray-100);
  }
  .fb-session { font-size: 0.8125rem; font-weight: 500; color: var(--gray-800); }
  .fb-mentor { font-size: 0.8125rem; color: var(--gray-500); }
  .fb-stars { color: #f59e0b; font-size: 1.125rem; letter-spacing: 2px; }

  .table-wrapper { overflow-x: auto; }
  table { width: 100%; border-collapse: collapse; font-size: 0.8125rem; }
  th { text-align: left; padding: 0.75rem; font-weight: 600; color: var(--gray-600); border-bottom: 2px solid var(--gray-200); }
  td { padding: 0.75rem; border-bottom: 1px solid var(--gray-100); color: var(--gray-700); }
  tr:hover td { background: var(--gray-50); }

  .search-results { margin-bottom: 1.5rem; }
  .search-results h3 { font-size: 0.9375rem; font-weight: 600; margin-bottom: 0.75rem; }

  .empty-state { color: var(--gray-400); font-size: 0.875rem; text-align: center; padding: 2rem; }

  .wide-modal { max-width: 800px; }
  .modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
  .modal-header h2 { font-size: 1.25rem; font-weight: 600; }
  .close-btn { background: none; font-size: 1.25rem; color: var(--gray-400); }
  .close-btn:hover { color: var(--gray-600); }
  .modal-desc { color: var(--gray-500); font-size: 0.875rem; margin-bottom: 1.25rem; }
  .modal-actions { display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1rem; }

  .form-group { margin-bottom: 1rem; }
  .form-group label { display: block; font-size: 0.8125rem; font-weight: 500; color: var(--gray-700); margin-bottom: 0.375rem; }
  .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }

  .btn-sm { padding: 0.375rem 0.75rem; font-size: 0.8125rem; }
  .btn-xs { padding: 0.25rem 0.5rem; font-size: 0.75rem; }

  .alert-error { background: #fee2e2; color: #991b1b; padding: 0.75rem; border-radius: var(--radius); font-size: 0.8125rem; margin-bottom: 1rem; }
  .alert-success { background: #d1fae5; color: #065f46; padding: 0.75rem; border-radius: var(--radius); font-size: 0.8125rem; margin-bottom: 1rem; }
  .text-muted { color: var(--gray-400); font-size: 0.8125rem; }
  .filter-row { display: flex; gap: 0.5rem; }

  @media (max-width: 900px) {
    .stats-strip { grid-template-columns: 1fr; }
    .dashboard-grid { grid-template-columns: 1fr; }
    .top-actions { flex-wrap: wrap; }
  }
</style>
