<script>
  import { onMount } from 'svelte';
  import { api, getCurrentUser, getRoleDashboardPath, invalidateApiCache, isHodWorkspaceRole } from '../../lib/api.js';
  import { hodNavItems } from '../../lib/navigation.js';
  import DashboardLayout from '../shared/DashboardLayout.svelte';
  import ConfirmDialog from '../shared/ConfirmDialog.svelte';
  import CustomSelect from '../shared/CustomSelect.svelte';
  import { toast } from '../../lib/toast.js';

  let user = getCurrentUser();
  const isDepartmentHead = user?.role === 'DEPARTMENT_HEAD';
  let feedbacks = [];
  let topStudents = [];
  let proofs = [];
  let pendingProofCount = 0;
  let mentorCount = 0;
  let studentCount = 0;
  let assistantCount = 0;
  let assistants = [];
  let showEvidenceReview = false;
  let showAddMentor = false;
  let selectedProof = null;
  let proofReviewAction = 'APPROVE';
  let proofReviewForm = { points: '', note: '' };
  let proofReviewError = '';
  let proofReviewLoading = false;
  let selectedStudent = null;
  let showAwardPoints = false;
  let pointForm = { points: '', category: 'ACTIVITY', note: '' };
  let pointError = '';
  let pointLoading = false;
  let searchQuery = '';
  let searchResults = [];
  let searchLoading = false;
  let dashboardLoading = true;
  let dashboardError = '';
  let mounted = false;
  let searchController = null;
  let searchDebounce = null;
  let pendingDeleteUser = null;
  let deleteLoading = false;
  let dashboardRefreshTimer = null;
  let submissionOpen = false;
  let pendingSuggestionCount = 0;
  let departmentSessions = [];

  function setProofCollection(nextProofs) {
    proofs = nextProofs;
    pendingProofCount = proofs.filter((proof) => !proof.pointStatus || proof.pointStatus === 'PENDING').length;
  }

  // Feedback filters
  let minRating = '';
  let maxRating = '3';
  let sortBy = 'rating_asc';

  // Account creation form
  let mentorFullName = '';
  let mentorEmail = '';
  let mentorPassword = '';
  let mentorRole = 'ACADEMIC_MENTOR';
  let mentorPhone = '';
  let mentorError = '';
  let mentorLoading = false;
  let mentorSuccess = '';

  const lowRatingOptions = [
    { value: '', label: 'All ratings' },
    { value: '1', label: '1 Star' },
    { value: '2', label: 'Up to 2 Stars' },
    { value: '3', label: 'Up to 3 Stars' }
  ];

  const pointCategoryOptions = [
    { value: 'ACTIVITY', label: 'Activity' },
    { value: 'AWARD', label: 'Award' }
  ];

  $: mentorRoleOptions = [
    { value: 'ACADEMIC_MENTOR', label: 'Academic Mentor' },
    { value: 'INDUSTRY_MENTOR', label: 'Industry Mentor' },
    ...(isDepartmentHead
      ? [{
          value: 'DEPARTMENT_ASSISTANT',
          label: `Department Assistant${assistantCount >= 2 ? ' (Limit Reached)' : ''}`,
          disabled: assistantCount >= 2
        }]
      : [])
  ];

  onMount(() => {
    mounted = true;
    if (!user) { window.location.href = '/'; return; }
    if (!isHodWorkspaceRole(user.role)) { window.location.href = getRoleDashboardPath(user.role); return; }

    loadDashboardData();
    dashboardRefreshTimer = window.setInterval(() => {
      loadDashboardData({ force: true, background: true });
    }, 15000);

    return () => {
      mounted = false;
      searchController?.abort();
      if (searchDebounce) clearTimeout(searchDebounce);
      if (dashboardRefreshTimer) clearInterval(dashboardRefreshTimer);
    };
  });

  async function loadDashboardData({ force = false, background = false } = {}) {
    if (!background) {
      dashboardLoading = true;
      dashboardError = '';
    }

    try {
      await Promise.all([
        loadSummaryCounts({ force }),
        loadAssistants({ force }),
        loadFeedbacks({ force }),
        loadTopStudents({ force }),
        loadProofs({ force }),
        loadCurriculumSummary({ force }),
        loadDepartmentSessions({ force })
      ]);
    } catch (e) {
      if (!mounted || background) return;
      dashboardError = 'Failed to load dashboard data';
    } finally {
      if (mounted && !background) {
        dashboardLoading = false;
      }
    }
  }

  async function loadCurriculumSummary({ force = false } = {}) {
    try {
      const [windowRes, suggestionsRes] = await Promise.all([
        api.get('/suggestions/submission-window', { cache: !force }),
        api.get('/suggestions/all', { cache: !force })
      ]);
      if (!mounted) return;
      submissionOpen = !!windowRes?.data?.open;
      const allSuggestions = suggestionsRes?.data || [];
      pendingSuggestionCount = allSuggestions.filter((item) => item.reviewStatus !== 'REVIEWED').length;
    } catch (e) {
      console.error('Failed to load curriculum summary', e);
      submissionOpen = false;
      pendingSuggestionCount = 0;
    }
  }

  async function loadDepartmentSessions({ force = false } = {}) {
    try {
      const res = await api.get('/hod/sessions', { cache: !force });
      if (!mounted) return;
      departmentSessions = Array.isArray(res) ? res : [];
    } catch (e) {
      departmentSessions = [];
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

  async function loadAssistants({ force = false } = {}) {
    try {
      const res = await api.get('/users/hod-assistants', { cache: !force });
      if (!mounted) return;
      assistants = (res.data || []).sort((a, b) => (a.fullName || '').localeCompare(b.fullName || ''));
      assistantCount = assistants.length;
    } catch (e) {
      console.error('Failed to load assistants', e);
      assistants = [];
      assistantCount = 0;
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
      setProofCollection(Array.isArray(res) ? res : (res.data || []));
    } catch (e) { console.error('Failed to load proofs', e); }
  }

  function getPendingProofs() {
    return proofs.filter((proof) => !proof.pointStatus || proof.pointStatus === 'PENDING');
  }

  function getEvidenceProofs() {
    return [...proofs].sort((left, right) => {
      const leftPending = !left?.pointStatus || left.pointStatus === 'PENDING';
      const rightPending = !right?.pointStatus || right.pointStatus === 'PENDING';
      if (leftPending !== rightPending) {
        return leftPending ? -1 : 1;
      }

      const leftDate = left?.createdAt ? new Date(left.createdAt).getTime() : 0;
      const rightDate = right?.createdAt ? new Date(right.createdAt).getTime() : 0;
      return rightDate - leftDate;
    });
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
    const payload = {
      action,
      note: proofReviewForm.note.trim()
    };
    if (action === 'APPROVE') {
      payload.points = Number(proofReviewForm.points);
      payload.category = ((selectedProof?.pointCategory || 'ACTIVITY') + '').toUpperCase();
    }
    try {
      const reviewed = await api.put(`/proofs/${proofId}/review`, payload);
      const reviewedProof = reviewed?.data || reviewed;
      setProofCollection(proofs.map((proof) => (
        proof.id === proofId
          ? { ...proof, ...reviewedProof }
          : proof
      )));
      invalidateApiCache('/proofs');
      await Promise.all([
        loadProofs({ force: true }),
        loadTopStudents({ force: true }),
        loadSummaryCounts({ force: true })
      ]);
      selectedProof = null;
      proofReviewForm = { points: '', note: '' };
      proofReviewError = '';
    } catch (e) {
      proofReviewError = (e?.status === 404 || e?.status === 405)
        ? 'The backend is still running without the latest proof review route. Restart the backend server and try again.'
        : (e?.data?.message || 'Failed to review proof.');
    }
  }

  function openProofReview(proof, action) {
    selectedProof = proof;
    proofReviewAction = action;
    proofReviewError = '';
    proofReviewForm = {
      points: proof?.latestPoints || '',
      note: ''
    };
  }

  async function submitProofReview() {
    if (!selectedProof) return;
    proofReviewError = '';
    if (proofReviewAction === 'APPROVE' && (!proofReviewForm.points || Number(proofReviewForm.points) <= 0)) {
      proofReviewError = 'Enter a valid point value before approving.';
      return;
    }
    proofReviewLoading = true;
    try {
      await reviewProof(selectedProof.id, proofReviewAction);
    } finally {
      proofReviewLoading = false;
    }
  }

  function openAwardPoints(student) {
    selectedStudent = student;
    showAwardPoints = true;
    pointError = '';
    pointForm = { points: '', category: 'ACTIVITY', note: '' };
  }

  async function submitAwardPoints() {
    pointError = '';
    if (!selectedStudent) return;
    if (!pointForm.points || Number(pointForm.points) === 0) {
      pointError = 'Enter a valid point value (positive or negative).';
      return;
    }
    pointLoading = true;
    try {
      const allocated = await api.post('/points/allocate', {
        studentId: selectedStudent.id,
        points: Number(pointForm.points),
        category: pointForm.category,
        note: pointForm.note.trim()
      });
      await api.put(`/points/${allocated.id}/review`, {
        action: 'APPROVE',
        note: pointForm.note.trim()
      });
      showAwardPoints = false;
      selectedStudent = null;
      pointForm = { points: '', category: 'ACTIVITY', note: '' };
      await Promise.all([
        loadTopStudents({ force: true }),
        loadSummaryCounts({ force: true })
      ]);
    } catch (e) {
      pointError = (e?.status === 404 || e?.status === 405)
        ? 'The backend is still running without the latest points routes. Restart the backend server and try again.'
        : (e?.data?.message || 'Failed to award points.');
    } finally {
      pointLoading = false;
    }
  }

  function requestDeleteUser(userId, label) {
    pendingDeleteUser = { userId, label };
  }

  async function deleteUser() {
    if (!pendingDeleteUser) return;
    deleteLoading = true;
    try {
      await api.delete(`/users/${pendingDeleteUser.userId}`);
      invalidateApiCache('/users');
      await Promise.all([
        loadSummaryCounts({ force: true }),
        loadAssistants({ force: true }),
        searchQuery.trim() ? searchStudents() : Promise.resolve()
      ]);
      searchResults = searchResults.filter((entry) => entry.id !== pendingDeleteUser.userId);
      toast.success({
        title: 'Account deleted',
        message: `${pendingDeleteUser.label || 'The selected user'} has been removed from active use.`
      });
      pendingDeleteUser = null;
    } catch (e) {
      dashboardError = (e?.status === 404 || e?.status === 405)
        ? 'The backend is still running without the latest delete account route. Restart the backend server and try again.'
        : (e?.data?.message || 'Failed to delete account.');
      toast.error({
        title: 'Delete failed',
        message: dashboardError
      });
    } finally {
      deleteLoading = false;
    }
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
        phone: mentorPhone
      });
      invalidateApiCache('/users');
      mentorSuccess = mentorRole === 'DEPARTMENT_ASSISTANT'
        ? 'Department assistant account created successfully.'
        : `${mentorRole === 'ACADEMIC_MENTOR' ? 'Academic' : 'Industry'} mentor account created successfully!`;
      mentorFullName = ''; mentorEmail = ''; mentorPassword = ''; mentorPhone = '';
      mentorRole = 'ACADEMIC_MENTOR';
      await Promise.all([
        loadSummaryCounts({ force: true }),
        loadAssistants({ force: true })
      ]);
    } catch (e) {
      mentorError = e.data?.message || 'Failed to create account';
    } finally { mentorLoading = false; }
  }

  function renderStars(rating) {
    return Array.from({length: 5}, (_, i) => i < rating ? '★' : '☆').join('');
  }

  function formatProofCategory(value) {
    return (value || 'ACTIVITY')
      .toLowerCase()
      .split('_')
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
      .join(' ');
  }

  function formatProofStatus(value) {
    return value || 'PENDING';
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
    <div class="card stat-card">
      <span class="stat-label">Department Assistants</span>
      <strong class="stat-value">{assistantCount}/2</strong>
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
      + Create Account
    </button>

    <a class="btn btn-outline curriculum-btn" href="/hod/curriculum-suggestions">
      Curriculum
      <span class="curriculum-status" class:open={submissionOpen}>
        {submissionOpen ? 'Open' : 'Closed'}
      </span>
      {#if pendingSuggestionCount > 0}
        <span class="evidence-badge">{pendingSuggestionCount}</span>
      {/if}
    </a>
  </div>

  <div class="card assistants-card">
    <h2 class="card-title">Department Assistants</h2>
    {#if assistants.length === 0}
      <p class="empty-state">No department assistants created yet.</p>
    {:else}
      <div class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Department</th>
              <th>Phone</th>
              {#if isDepartmentHead}<th>Action</th>{/if}
            </tr>
          </thead>
          <tbody>
            {#each assistants as assistant}
              <tr>
                <td><strong>{assistant.fullName}</strong></td>
                <td>{assistant.email}</td>
                <td>{assistant.department || '-'}</td>
                <td>{assistant.phone || '-'}</td>
                {#if isDepartmentHead}
                  <td>
                    <button class="btn btn-danger btn-sm" on:click={() => requestDeleteUser(assistant.id, assistant.fullName)}>
                      Delete
                    </button>
                  </td>
                {/if}
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    {/if}
  </div>

  <!-- Search Results -->
  {#if searchResults.length > 0}
    <div class="card search-results">
      <h3>Search Results</h3>
      <div class="table-wrapper">
        <table>
          <thead><tr><th>Name</th><th>Reg. No</th><th>Year</th><th>Points</th><th>Department</th><th>Action</th></tr></thead>
          <tbody>
            {#each searchResults as student}
              <tr>
                <td><button class="link-btn" on:click={() => openAwardPoints(student)}><strong>{student.fullName}</strong></button></td>
                <td>{student.registrationNumber || '-'}</td>
                <td>Year {student.yearOfStudy || '-'}</td>
                <td><span class="badge badge-info">{student.cumulativePoints || 0}</span></td>
                <td>{student.department || '-'}</td>
                <td><button class="btn btn-outline btn-sm" on:click={() => openAwardPoints(student)}>Award Points</button></td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    </div>
  {:else if searchQuery.trim() && !searchLoading}
    <div class="card search-results">
      <p class="empty-state">No students found for that search.</p>
    </div>
  {/if}

  <!-- Scheduled Sessions -->
  <div class="card" style="margin-bottom: 1.5rem;">
    <h2 class="card-title">Scheduled Sessions</h2>
    {#if departmentSessions.length === 0}
      <p class="empty-state">No scheduled sessions from department mentors.</p>
    {:else}
      <div class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>Topic</th>
              <th>Mentor</th>
              <th>Type</th>
              <th>Date</th>
              <th>Time</th>
            </tr>
          </thead>
          <tbody>
            {#each departmentSessions.slice(0, 10) as session}
              <tr>
                <td><strong>{session.sessionTitle}</strong></td>
                <td>{session.mentorName}</td>
                <td><span class="badge badge-info">{session.mentorType}</span></td>
                <td>{session.sessionDate || '-'}</td>
                <td>{session.sessionTime || '-'}</td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    {/if}
  </div>

  <!-- Main Grid -->
  <div class="dashboard-grid">
    <!-- Low Rating Feedbacks -->
    <div class="card feedback-card">
      <div class="card-header">
        <h2 class="card-title danger-title">Low Rating Feedback Sessions</h2>
        <div class="filter-row">
          <div class="filter-control">
            <CustomSelect options={lowRatingOptions} bind:value={maxRating} on:change={() => loadFeedbacks({ force: true })} />
          </div>
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
                  <td><button class="link-btn" on:click={() => openAwardPoints(student)}>{student.fullName}</button></td>
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
      <div class="modal-content wide-modal evidence-modal">
        <div class="modal-header">
          <div>
            <p class="eyebrow">Review Submissions</p>
            <h2>Student Evidence Review</h2>
          </div>
          <button class="close-btn" on:click={() => showEvidenceReview = false}>✕</button>
        </div>
        {#if dashboardLoading && proofs.length === 0}
          <p class="empty-state">Loading evidence submissions...</p>
        {:else if proofs.length === 0}
          <p class="empty-state">No evidence submissions yet.</p>
        {:else}
          <div class="evidence-review-list">
            {#each getEvidenceProofs() as proof}
              <article class="evidence-review-card">
                <div class="evidence-review-main">
                  <div class="evidence-review-head">
                    <div>
                      <p class="evidence-student">{proof.studentName || `Student #${proof.studentId}`}</p>
                      <h3>{proof.title}</h3>
                    </div>
                    <span
                      class="badge"
                      class:badge-warning={!proof.pointStatus || proof.pointStatus === 'PENDING'}
                      class:badge-success={proof.pointStatus === 'APPROVED'}
                      class:badge-danger={proof.pointStatus === 'REJECTED'}
                    >
                      {formatProofStatus(proof.pointStatus)}
                    </span>
                  </div>
                  <p class="evidence-description">{proof.description || 'No extra note provided with this evidence submission.'}</p>
                  <div class="evidence-meta">
                    <span class="table-chip">{formatProofCategory(proof.pointCategory)}</span>
                    <span class="table-chip table-chip-muted">{proof.eventDate || 'No date'}</span>
                    {#if proof.latestPoints}
                      <span class="table-chip table-chip-muted">{proof.latestPoints} pts</span>
                    {/if}
                  </div>
                </div>
                <div class="evidence-review-actions">
                  {#if proof.proofData}
                    <a class="btn btn-outline btn-sm" href={proof.proofData} target="_blank" rel="noreferrer">View Evidence</a>
                  {/if}
                  {#if !proof.pointStatus || proof.pointStatus === 'PENDING'}
                    <button class="btn btn-success btn-sm" on:click={() => openProofReview(proof, 'APPROVE')}>Approve</button>
                    <button class="btn btn-danger btn-sm" on:click={() => openProofReview(proof, 'REJECT')}>Reject</button>
                  {:else}
                    <span class="reviewed-label">Already reviewed</span>
                  {/if}
                </div>
              </article>
            {/each}
          </div>
        {/if}
      </div>
    </div>
  {/if}

  {#if selectedProof}
    <div class="modal-overlay" on:click|self={() => selectedProof = null}>
      <div class="modal-content proof-review-modal">
        <div class="modal-header">
          <div>
            <p class="eyebrow">{proofReviewAction === 'APPROVE' ? 'Approve Submission' : 'Reject Submission'}</p>
            <h2>{proofReviewAction === 'APPROVE' ? 'Approve Evidence' : 'Reject Evidence'}</h2>
          </div>
          <button class="close-btn" on:click={() => selectedProof = null}>✕</button>
        </div>
        {#if proofReviewError}<div class="alert alert-error">{proofReviewError}</div>{/if}
        
        <div class="proof-review-content">
          <div class="proof-info-card">
            <div class="proof-info-row">
              <span class="label">Student</span>
              <span class="value">{selectedProof.studentName || `Student #${selectedProof.studentId}`}</span>
            </div>
            <div class="proof-info-row">
              <span class="label">Title</span>
              <span class="value">{selectedProof.title}</span>
            </div>
            <div class="proof-info-row">
              <span class="label">Category</span>
              <span class="value"><span class="badge badge-info">{formatProofCategory(selectedProof.pointCategory)}</span></span>
            </div>
            <div class="proof-info-row">
              <span class="label">Event Date</span>
              <span class="value">{selectedProof.eventDate || '-'}</span>
            </div>
            <div class="proof-info-row">
              <span class="label">Submitted Note</span>
              <span class="value proof-note">{selectedProof.description || 'No note provided.'}</span>
            </div>
            {#if selectedProof.proofData}
              <div class="proof-info-row">
                <span class="label">Evidence Link</span>
                <a href={selectedProof.proofData} target="_blank" rel="noreferrer" class="link-button">Open in new tab</a>
              </div>
            {/if}
          </div>

          <div class="form-group">
            <label for="review-note">Review Note</label>
            <textarea id="review-note" class="input" rows="3" bind:value={proofReviewForm.note} placeholder="Optional note for the student"></textarea>
          </div>

          {#if proofReviewAction === 'APPROVE'}
            <div class="form-group">
              <label for="review-points">Points to Award</label>
              <input id="review-points" type="number" class="input" bind:value={proofReviewForm.points} />
            </div>
          {/if}
        </div>

        <div class="modal-actions">
          <button class="btn btn-outline" on:click={() => selectedProof = null}>Cancel</button>
          <button class="btn {proofReviewAction === 'APPROVE' ? 'btn-success' : 'btn-danger'}" on:click={submitProofReview} disabled={proofReviewLoading}>
            {proofReviewLoading ? 'Saving...' : (proofReviewAction === 'APPROVE' ? 'Approve & Award Points' : 'Reject Evidence')}
          </button>
        </div>
      </div>
    </div>
  {/if}

  {#if showAwardPoints && selectedStudent}
    <div class="modal-overlay" on:click|self={() => showAwardPoints = false}>
      <div class="modal-content">
        <div class="modal-header">
          <h2>{Number(pointForm.points) < 0 ? 'Deduct Points' : 'Award Points'}</h2>
          <button class="close-btn" on:click={() => showAwardPoints = false}>✕</button>
        </div>
        {#if pointError}<div class="alert alert-error">{pointError}</div>{/if}
        <p class="modal-desc">Update the points balance for {selectedStudent.fullName}. Negative values deduct marks from the student.</p>
        <div class="form-row">
          <div class="form-group">
            <label>Points</label>
            <input type="number" class="input" bind:value={pointForm.points} placeholder="Positive or negative values" />
            <p class="helper-text">Use negative values to deduct points from the student.</p>
          </div>
          <div class="form-group">
            <label>Category</label>
            <CustomSelect options={pointCategoryOptions} bind:value={pointForm.category} />
          </div>
        </div>
        <div class="form-group">
          <label>Note</label>
          <textarea class="input" rows="3" bind:value={pointForm.note} placeholder="Reason for this adjustment"></textarea>
        </div>
        <div class="modal-actions">
          <button class="btn btn-outline" on:click={() => showAwardPoints = false}>Cancel</button>
          <button class="btn btn-primary" on:click={submitAwardPoints} disabled={pointLoading}>
            {pointLoading ? 'Saving...' : (Number(pointForm.points) < 0 ? 'Deduct Points' : 'Award Points')}
          </button>
        </div>
      </div>
    </div>
  {/if}

  <!-- Create Account Modal -->
  {#if showAddMentor}
    <div class="modal-overlay" on:click|self={() => showAddMentor = false}>
      <div class="modal-content">
        <div class="modal-header">
          <h2>Create Workspace Account</h2>
          <button class="close-btn" on:click={() => showAddMentor = false}>✕</button>
        </div>
        <p class="modal-desc">
          {#if isDepartmentHead}
            Create academic mentors, industry mentors, or up to two department assistants.
          {:else}
            Create academic or industry mentor accounts for the HOD workspace.
          {/if}
        </p>

        {#if mentorError}<div class="alert alert-error">{mentorError}</div>{/if}
        {#if mentorSuccess}<div class="alert alert-success">{mentorSuccess}</div>{/if}

        <form on:submit|preventDefault={createMentorAccount}>
          <div class="form-group">
            <label>Account Type</label>
            <CustomSelect options={mentorRoleOptions} bind:value={mentorRole} />
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
          <div class="form-group">
            <label>Phone</label>
            <input class="input" bind:value={mentorPhone} placeholder="+94 71 234 5678" />
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

  <ConfirmDialog
    open={!!pendingDeleteUser}
    title="Delete account?"
    message={`Delete ${pendingDeleteUser?.label || 'this account'}? This disables the account and removes it from active use.`}
    confirmLabel="Delete account"
    tone="danger"
    busy={deleteLoading}
    on:cancel={() => pendingDeleteUser = null}
    on:confirm={deleteUser}
  />
</DashboardLayout>

<style>
  .link-btn {
    background: none;
    border: none;
    padding: 0;
    color: var(--primary);
    font-weight: 500;
    cursor: pointer;
    text-decoration: none;
    text-align: left;
  }
  .link-btn:hover {
    text-decoration: none;
    color: var(--primary-light);
  }

  .top-actions {
    display: flex;
    align-items: center;
    gap: 1rem;
    margin-bottom: 1.5rem;
  }

  .stats-strip {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 1rem;
    margin-bottom: 1rem;
  }

  .stat-card {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1.5rem 2rem;
    background: var(--primary-50);
  }

  .stat-label {
    color: var(--text-muted);
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
    background: var(--bg-main);
    border: 1px solid var(--border-medium);
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
  .curriculum-btn {
    position: relative;
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
  }
  .curriculum-status {
    display: inline-flex;
    align-items: center;
    border-radius: 999px;
    padding: 0.15rem 0.55rem;
    font-size: 0.72rem;
    font-weight: 700;
    color: var(--danger);
    background: var(--danger-light);
  }
  .curriculum-status.open {
    color: var(--accent);
    background: var(--success-light);
  }
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

  .assistants-card {
    margin-bottom: 1.5rem;
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
  }
  .danger-title { color: var(--text-main); }
  .success-title { color: var(--text-main); }

  .filter-select {
    padding: 0.6rem 0.9rem;
    border: 1px solid rgba(148, 163, 184, 0.24);
    border-radius: 999px;
    font-size: 0.8125rem;
    background: rgba(255, 255, 255, 0.92);
  }

  .feedback-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem;
    border-bottom: 1px solid var(--bg-secondary);
  }
  .fb-session { font-size: 0.8125rem; font-weight: 500; color: var(--text-main); }
  .fb-mentor { font-size: 0.8125rem; color: var(--text-muted); }
  .fb-stars { color: var(--warning); font-size: 1.125rem; letter-spacing: 2px; }

  .table-wrapper { overflow-x: auto; }
  table { width: 100%; border-collapse: collapse; font-size: 0.8125rem; }
  th { text-align: left; padding: 1rem 0.5rem; font-weight: 500; color: var(--text-muted); background: var(--bg-secondary); border-bottom: 1px solid var(--border-light); }
  td { padding: 1rem 0.5rem; border-bottom: 1px solid var(--border-light); color: var(--text-main); }
  tr:hover td { background: var(--bg-alt); }

  .search-results { margin-bottom: 1.5rem; }
  .search-results h3 { font-size: 0.9375rem; font-weight: 600; margin-bottom: 0.75rem; }

  .empty-state { color: var(--text-muted); font-size: 0.875rem; text-align: center; padding: 2rem; }

  .wide-modal { max-width: 800px; }
  .evidence-modal {
    max-width: 920px;
    width: min(920px, 92vw);
    max-height: 85vh;
    display: flex;
    flex-direction: column;
    border-radius: var(--radius-lg);
  }
  .modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
  .modal-header h2 { font-size: 1.25rem; font-weight: 600; }
  .close-btn { background: none; font-size: 1.25rem; color: var(--text-muted); }
  .close-btn:hover { color: var(--text-secondary); }
  .modal-desc { color: var(--text-muted); font-size: 0.875rem; margin-bottom: 1.25rem; }
  .modal-actions { display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1rem; }
  .detail-summary {
    display: grid;
    gap: 0.5rem;
    margin-bottom: 1rem;
    padding: 0.9rem 1rem;
    border-radius: var(--radius);
    background: var(--bg-alt);
    color: var(--text-secondary);
  }

  .form-group { margin-bottom: 1rem; }
  .form-group label { display: block; font-size: 0.8125rem; font-weight: 500; color: var(--text-secondary); margin-bottom: 0.375rem; }
  .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }

  .btn-sm { padding: 0.375rem 0.75rem; font-size: 0.8125rem; }
  .btn-xs { padding: 0.25rem 0.5rem; font-size: 0.75rem; }

  .alert-error { background: var(--danger-light); color: var(--danger); padding: 0.75rem; border-radius: var(--radius); font-size: 0.8125rem; margin-bottom: 1rem; }
  .alert-success { background: var(--success-light); color: var(--success); padding: 0.75rem; border-radius: var(--radius); font-size: 0.8125rem; margin-bottom: 1rem; }
  .text-muted { color: var(--text-muted); font-size: 0.8125rem; }
  .filter-row { display: flex; gap: 0.5rem; }

  .filter-control {
    width: 11rem;
  }

  .proof-review-modal { max-width: 600px; }
  .proof-review-content { margin-bottom: 1.5rem; }
  .evidence-review-list {
    display: grid;
    gap: 1rem;
    overflow-y: auto;
    flex: 1;
    min-height: 0;
    max-height: 60vh;
    padding-right: 0.5rem;
  }

  .evidence-review-card {
    display: grid;
    grid-template-columns: minmax(0, 1fr) auto;
    gap: 1rem;
    padding: 1.25rem;
    border: 1px solid var(--border-light);
    border-radius: var(--radius);
    background: transparent;
  }

  .evidence-review-main {
    display: grid;
    gap: 0.8rem;
    min-width: 0;
  }

  .evidence-review-head {
    display: flex;
    justify-content: space-between;
    gap: 1rem;
    align-items: flex-start;
  }

  .evidence-student {
    font-size: 0.78rem;
    font-weight: 700;
    letter-spacing: 0.08em;
    text-transform: uppercase;
    color: var(--text-muted);
    margin-bottom: 0.3rem;
  }

  .evidence-review-head h3 {
    font-size: 1.05rem;
    margin: 0;
  }

  .evidence-description {
    color: var(--text-secondary);
    line-height: 1.55;
    max-width: 42rem;
  }

  .evidence-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 0.55rem;
  }

  .evidence-review-actions {
    display: flex;
    flex-direction: column;
    justify-content: flex-start;
    align-items: stretch;
    gap: 0.55rem;
    min-width: 9rem;
  }

  .reviewed-label {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-height: 2.25rem;
    padding: 0.5rem 0.8rem;
    border-radius: var(--radius);
    border: 1px solid var(--border-light);
    color: var(--text-muted);
    font-size: 0.82rem;
    font-weight: 600;
    background: var(--bg-alt);
  }

  .table-chip {
    display: inline-flex;
    align-items: center;
    padding: 0.2rem 0.6rem;
    border-radius: var(--radius-sm);
    border: 1px solid var(--border-light);
    background: transparent;
    color: var(--text-secondary);
    font-size: 0.75rem;
    font-weight: 500;
    white-space: nowrap;
  }

  .table-chip-muted {
    background: var(--bg-main);
    color: var(--text-secondary);
  }

  .proof-info-card {
    background: var(--bg-alt);
    border: 1px solid var(--bg-secondary);
    border-radius: var(--radius);
    padding: 1rem;
    margin-bottom: 1.5rem;
  }

  .proof-info-row {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    padding: 0.75rem 0;
    border-bottom: 1px solid var(--border-light);
  }

  .proof-info-row:last-child {
    border-bottom: none;
  }

  .proof-info-row .label {
    font-size: 0.75rem;
    font-weight: 600;
    text-transform: uppercase;
    color: var(--text-muted);
    letter-spacing: 0.5px;
  }

  .proof-info-row .value {
    color: var(--text-main);
    font-size: 0.9375rem;
    flex: 1;
    text-align: right;
  }

  .proof-note {
    max-width: 20rem;
    white-space: normal;
    line-height: 1.55;
  }

  .link-button {
    display: inline-block;
    padding: 0.375rem 0.875rem;
    background: var(--primary-50);
    border: 1px solid var(--primary-100);
    border-radius: var(--radius-sm);
    color: var(--primary);
    font-size: 0.8125rem;
    font-weight: 500;
    text-decoration: none;
    transition: all 0.15s ease;
  }

  .link-button:hover {
    background: var(--primary-100);
    border-color: var(--primary-light);
    color: var(--primary-dark);
  }

  .action-buttons {
    display: flex;
    gap: 0.5rem;
  }

  .filter-select {
    padding: 0.5rem 0.75rem;
    border: 1px solid var(--border-light);
    border-radius: var(--radius-sm);
    font-size: 0.8125rem;
    background-color: var(--bg-main);
    color: var(--text-secondary);
    cursor: pointer;
  }

  @media (max-width: 900px) {
    .top-actions,
    .dashboard-grid {
      grid-template-columns: 1fr;
    }

    .top-actions {
      display: grid;
    }

    .evidence-review-card {
      grid-template-columns: 1fr;
    }

    .evidence-review-actions {
      min-width: 0;
      flex-direction: row;
      flex-wrap: wrap;
      justify-content: flex-start;
    }

    .form-row {
      grid-template-columns: 1fr;
    }
  }

  @media (max-width: 640px) {
    .proof-info-row {
      flex-direction: column;
      gap: 0.45rem;
    }

    .proof-info-row .value {
      text-align: left;
    }
  }

  .helper-text {
    font-size: 0.75rem;
    color: var(--text-muted);
    margin-top: 0.375rem;
  }

  .eyebrow {
    font-size: 0.7rem;
    font-weight: 700;
    text-transform: uppercase;
    color: var(--text-muted);
    letter-spacing: 1px;
    margin-bottom: 0.25rem;
    display: block;
  }

  @media (max-width: 900px) {
    .stats-strip { grid-template-columns: 1fr; }
    .dashboard-grid { grid-template-columns: 1fr; }
    .top-actions { flex-wrap: wrap; }
  }
</style>
