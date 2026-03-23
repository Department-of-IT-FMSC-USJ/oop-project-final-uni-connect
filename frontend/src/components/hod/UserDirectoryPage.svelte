<script>
  import { onMount } from 'svelte';
  import { api, getCurrentUser, getRoleDashboardPath } from '../../lib/api.js';
  import DashboardLayout from '../shared/DashboardLayout.svelte';

  export let navItems = [];
  export let activeItem = '';
  export let pageTitle = '';
  export let heading = '';
  export let description = '';
  export let mode = 'students';

  const user = getCurrentUser();
  const isMentorMode = mode === 'mentors';

  let loading = true;
  let error = '';
  let query = '';
  let users = [];
  let selectedUser = null;
  let detailLoading = false;
  let detailError = '';

  onMount(() => {
    if (!user) {
      window.location.href = '/';
      return;
    }

    if (user.role !== 'DEPARTMENT_HEAD') {
      window.location.href = getRoleDashboardPath(user.role);
      return;
    }

    loadUsers();
  });

  async function loadUsers() {
    loading = true;
    error = '';

    try {
      if (isMentorMode) {
        const [academic, industry] = await Promise.all([
          api.get('/users/by-role/academic_mentor', { cache: false }),
          api.get('/users/by-role/industry_mentor', { cache: false })
        ]);

        users = [...(academic.data || []), ...(industry.data || [])];
      } else {
        const res = await api.get('/users/students/search', { cache: false });
        users = res.data || [];
      }

      users = users
        .filter(Boolean)
        .sort((a, b) => (a.fullName || '').localeCompare(b.fullName || ''));
    } catch (e) {
      error = `Failed to load ${isMentorMode ? 'mentors' : 'students'}.`;
      users = [];
    } finally {
      loading = false;
    }
  }

  function getRoleLabel(role) {
    return (role || '').replaceAll('_', ' ');
  }

  function getFilteredUsers() {
    const normalized = query.trim().toLowerCase();
    if (!normalized) return users;

    return users.filter((entry) =>
      [entry.fullName, entry.email, entry.department, entry.registrationNumber, entry.cpmNumber]
        .filter(Boolean)
        .some((value) => value.toLowerCase().includes(normalized))
    );
  }

  async function viewUserProfile(userId) {
    detailLoading = true;
    detailError = '';
    selectedUser = null;

    try {
      const res = await api.get(`/users/${userId}`, { cache: false });
      selectedUser = res.data || null;
    } catch (e) {
      detailError = 'Failed to load the selected profile.';
    } finally {
      detailLoading = false;
    }
  }

  function closeModal() {
    selectedUser = null;
    detailError = '';
    detailLoading = false;
  }

  function getInitial(name) {
    return name?.charAt(0)?.toUpperCase() || 'U';
  }

  const filteredUsers = () => getFilteredUsers();
</script>

<DashboardLayout {navItems} {activeItem} {pageTitle}>
  <div class="directory-page">
    <section class="card hero-card">
      <div>
        <p class="eyebrow">Head of Department</p>
        <h2>{heading}</h2>
        <p class="hero-copy">{description}</p>
      </div>
      <div class="hero-meta">
        <span class="hero-count">{filteredUsers().length}</span>
        <span class="hero-label">{isMentorMode ? 'Mentors' : 'Students'}</span>
      </div>
    </section>

    <section class="card directory-card">
      <div class="toolbar">
        <input
          class="input search-input"
          bind:value={query}
          placeholder={isMentorMode ? 'Search mentors by name, email, department, or CPM number' : 'Search students by name, email, registration number, or department'}
        />
      </div>

      {#if error}
        <div class="alert alert-error">{error}</div>
      {/if}

      {#if loading}
        <p class="empty-state">Loading {isMentorMode ? 'mentors' : 'students'}...</p>
      {:else if filteredUsers().length === 0}
        <p class="empty-state">No {isMentorMode ? 'mentors' : 'students'} found.</p>
      {:else}
        <div class="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>{isMentorMode ? 'Role' : 'Registration'}</th>
                <th>Email</th>
                <th>Department</th>
                <th>{isMentorMode ? 'CPM Number' : 'Points'}</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {#each filteredUsers() as entry}
                <tr>
                  <td>
                    <div class="person-cell">
                      <div class="avatar-sm">{getInitial(entry.fullName)}</div>
                      <div>
                        <strong>{entry.fullName || 'Unknown User'}</strong>
                        {#if !isMentorMode}
                          <div class="muted">Year {entry.yearOfStudy || '-'}</div>
                        {/if}
                      </div>
                    </div>
                  </td>
                  <td>{isMentorMode ? getRoleLabel(entry.role) : (entry.registrationNumber || '-')}</td>
                  <td>{entry.email || '-'}</td>
                  <td>{entry.department || '-'}</td>
                  <td>{isMentorMode ? (entry.cpmNumber || '-') : (entry.cumulativePoints || 0)}</td>
                  <td>
                    <button class="btn btn-outline btn-sm" on:click={() => viewUserProfile(entry.id)}>
                      View Profile
                    </button>
                  </td>
                </tr>
              {/each}
            </tbody>
          </table>
        </div>
      {/if}
    </section>
  </div>

  {#if detailLoading || selectedUser || detailError}
    <div class="modal-overlay" on:click|self={closeModal}>
      <div class="modal-content profile-modal">
        <div class="modal-header">
          <div>
            <p class="eyebrow">Profile View</p>
            <h3>{isMentorMode ? 'Mentor Profile' : 'Student Profile'}</h3>
          </div>
          <button class="close-btn" on:click={closeModal} aria-label="Close profile dialog">✕</button>
        </div>

        {#if detailLoading}
          <p class="empty-state">Loading profile...</p>
        {:else if detailError}
          <div class="alert alert-error">{detailError}</div>
        {:else if selectedUser}
          <div class="profile-panel">
            <div class="profile-summary">
              <div class="avatar-lg">{getInitial(selectedUser.fullName)}</div>
              <div>
                <h4>{selectedUser.fullName}</h4>
                <p class="role-chip">{getRoleLabel(selectedUser.role)}</p>
                <p class="muted">{selectedUser.email || '-'}</p>
              </div>
            </div>

            <div class="detail-grid">
              <div class="detail-item">
                <span class="detail-label">Phone</span>
                <span class="detail-value">{selectedUser.phone || 'Not set'}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Department</span>
                <span class="detail-value">{selectedUser.department || 'Not set'}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Registration Number</span>
                <span class="detail-value">{selectedUser.registrationNumber || '-'}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Year of Study</span>
                <span class="detail-value">{selectedUser.yearOfStudy || '-'}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">CPM Number</span>
                <span class="detail-value">{selectedUser.cpmNumber || '-'}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Points</span>
                <span class="detail-value">{selectedUser.cumulativePoints || 0}</span>
              </div>
            </div>
          </div>
        {/if}
      </div>
    </div>
  {/if}
</DashboardLayout>

<style>
  .directory-page {
    display: grid;
    gap: 1.5rem;
  }

  .hero-card {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 1.5rem;
    padding: 2rem;
    background:
      radial-gradient(circle at top right, rgba(59, 130, 246, 0.16), transparent 28%),
      linear-gradient(135deg, #ffffff, #f6f9ff);
  }

  .eyebrow {
    margin-bottom: 0.65rem;
    font-size: 0.75rem;
    font-weight: 700;
    letter-spacing: 0.08em;
    text-transform: uppercase;
    color: var(--accent);
  }

  h2 {
    margin-bottom: 0.5rem;
    font-size: 1.8rem;
    color: var(--gray-900);
  }

  .hero-copy {
    max-width: 44rem;
    color: var(--gray-600);
  }

  .hero-meta {
    min-width: 120px;
    text-align: right;
  }

  .hero-count {
    display: block;
    font-size: 2rem;
    font-weight: 700;
    color: var(--primary);
  }

  .hero-label {
    color: var(--gray-500);
    font-size: 0.875rem;
  }

  .directory-card {
    padding: 1.5rem;
  }

  .toolbar {
    margin-bottom: 1rem;
  }

  .search-input {
    max-width: 520px;
  }

  .table-wrapper {
    overflow-x: auto;
  }

  table {
    width: 100%;
    border-collapse: collapse;
  }

  th, td {
    padding: 0.95rem 0.8rem;
    text-align: left;
    border-bottom: 1px solid var(--gray-200);
    vertical-align: middle;
  }

  th {
    font-size: 0.76rem;
    text-transform: uppercase;
    letter-spacing: 0.05em;
    color: var(--gray-500);
  }

  .person-cell {
    display: flex;
    align-items: center;
    gap: 0.75rem;
  }

  .avatar-sm, .avatar-lg {
    border-radius: 9999px;
    background: var(--primary);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    flex-shrink: 0;
  }

  .avatar-sm {
    width: 40px;
    height: 40px;
    font-size: 0.95rem;
  }

  .avatar-lg {
    width: 72px;
    height: 72px;
    font-size: 1.9rem;
  }

  .muted {
    color: var(--gray-500);
    font-size: 0.82rem;
  }

  .empty-state {
    color: var(--gray-500);
    padding: 1rem 0;
  }

  .alert {
    padding: 0.8rem 1rem;
    border-radius: var(--radius);
    margin-bottom: 1rem;
  }

  .alert-error {
    background: #fee2e2;
    color: #991b1b;
  }

  .btn-sm {
    padding: 0.4rem 0.8rem;
    font-size: 0.8rem;
  }

  .profile-modal {
    max-width: 760px;
    width: min(760px, 92vw);
  }

  .modal-header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 1rem;
    margin-bottom: 1rem;
  }

  h3, h4 {
    color: var(--gray-900);
  }

  .close-btn {
    background: transparent;
    color: var(--gray-500);
    font-size: 1rem;
  }

  .profile-summary {
    display: flex;
    align-items: center;
    gap: 1rem;
    margin-bottom: 1.5rem;
  }

  .role-chip {
    display: inline-flex;
    padding: 0.2rem 0.55rem;
    border-radius: 9999px;
    background: var(--gray-100);
    color: var(--gray-700);
    font-size: 0.75rem;
    font-weight: 600;
    margin: 0.3rem 0;
  }

  .detail-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 1rem;
  }

  .detail-item {
    padding: 1rem;
    border: 1px solid var(--gray-200);
    border-radius: var(--radius);
    background: var(--gray-50);
  }

  .detail-label {
    display: block;
    margin-bottom: 0.35rem;
    font-size: 0.72rem;
    font-weight: 700;
    letter-spacing: 0.05em;
    text-transform: uppercase;
    color: var(--gray-500);
  }

  .detail-value {
    color: var(--gray-900);
    font-weight: 500;
  }

  @media (max-width: 800px) {
    .hero-card {
      flex-direction: column;
    }

    .hero-meta {
      text-align: left;
    }

    .detail-grid {
      grid-template-columns: 1fr;
    }
  }
</style>
