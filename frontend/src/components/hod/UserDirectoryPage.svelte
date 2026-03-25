<script>
  import { onMount } from 'svelte';
  import { api, getCurrentUser, getRoleDashboardPath, isHodWorkspaceRole } from '../../lib/api.js';
  import DashboardLayout from '../shared/DashboardLayout.svelte';
  import ConfirmDialog from '../shared/ConfirmDialog.svelte';
  import { toast } from '../../lib/toast.js';

  export let navItems = [];
  export let activeItem = '';
  export let pageTitle = '';
  export let heading = '';
  export let description = '';
  export let mode = 'students';

  const user = getCurrentUser();
  const isMentorMode = mode === 'mentors';
  const canDeleteUsers = user?.role === 'DEPARTMENT_HEAD';

  let loading = true;
  let error = '';
  let query = '';
  let users = [];
  let totalCount = 0;
  let selectedUser = null;
  let detailLoading = false;
  let detailError = '';
  let pendingDeleteUser = null;
  let deleteLoading = false;

  onMount(() => {
    if (!user) {
      window.location.href = '/';
      return;
    }

    if (!isHodWorkspaceRole(user.role)) {
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
      totalCount = users.length;
    } catch (e) {
      error = `Failed to load ${isMentorMode ? 'mentors' : 'students'}.`;
      users = [];
      totalCount = 0;
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
      [entry.fullName, entry.email, entry.department, entry.registrationNumber, entry.phone]
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
      detailError = (e?.status === 404 || e?.status === 405)
        ? 'The backend is still running without the latest user profile route. Restart the backend server and try again.'
        : (e?.data?.message || 'Failed to load the selected profile.');
    } finally {
      detailLoading = false;
    }
  }

  function requestDeleteAccount(entry) {
    if (!canDeleteUsers) return;
    pendingDeleteUser = entry;
  }

  async function deleteAccount() {
    if (!pendingDeleteUser) return;
    deleteLoading = true;
    try {
      await api.delete(`/users/${pendingDeleteUser.id}`);
      await loadUsers();
      if (selectedUser?.id === pendingDeleteUser.id) {
        closeModal();
      }
      toast.success({
        title: 'Account deleted',
        message: `${pendingDeleteUser.fullName || 'The selected user'} has been removed from active use.`
      });
      pendingDeleteUser = null;
    } catch (e) {
      error = (e?.status === 404 || e?.status === 405)
        ? 'The backend is still running without the latest delete account route. Restart the backend server and try again.'
        : (e?.data?.message || 'Failed to delete the selected account.');
      toast.error({
        title: 'Delete failed',
        message: error
      });
    } finally {
      deleteLoading = false;
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
        <span class="hero-count">{totalCount}</span>
        <span class="hero-label">{isMentorMode ? 'Mentors' : 'Students'}</span>
      </div>
    </section>

    <section class="card directory-card">
      <div class="toolbar">
        <input
          class="input search-input"
          bind:value={query}
          placeholder={isMentorMode ? 'Search mentors by name, email, department, or phone' : 'Search students by name, email, registration number, or department'}
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
        <div class="user-list">
          {#each filteredUsers() as entry}
            <article class="user-card">
              <div class="user-head">
                <div class="user-identity">
                  <div class="user-avatar">{getInitial(entry.fullName)}</div>
                  <div class="user-title-block">
                    <button
                      class="user-name-btn"
                      on:click={() => viewUserProfile(entry.id)}
                      title="View profile"
                    >{entry.fullName || 'Unknown'}</button>
                    <span class="user-sub">
                      {isMentorMode ? (entry.email || '-') : (entry.registrationNumber || 'No Reg. No.')}
                    </span>
                  </div>
                </div>
                <span class="user-role-badge">
                  {isMentorMode ? getRoleLabel(entry.role) : `Year ${entry.yearOfStudy || '-'}`}
                </span>
              </div>
              <div class="user-footer">
                <div class="user-meta">
                  {#if entry.department}
                    <span class="meta-chip">{entry.department}</span>
                  {/if}
                  {#if isMentorMode}
                    {#if entry.phone}
                      <span class="meta-chip meta-chip-muted">{entry.phone}</span>
                    {/if}
                  {:else}
                    <span class="meta-chip meta-chip-points">{entry.cumulativePoints || 0} pts</span>
                  {/if}
                  {#if !isMentorMode && entry.email}
                    <span class="meta-chip meta-chip-muted">{entry.email}</span>
                  {/if}
                </div>
                <div class="user-actions">
                  <button class="btn btn-outline btn-sm" on:click={() => viewUserProfile(entry.id)}>View Profile</button>
                  {#if canDeleteUsers}
                    <button class="btn btn-danger btn-sm" on:click={() => requestDeleteAccount(entry)}>Delete</button>
                  {/if}
                </div>
              </div>
            </article>
          {/each}
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
                <span class="detail-label">Points</span>
                <span class="detail-value">{selectedUser.cumulativePoints || 0}</span>
              </div>
            </div>
          </div>
        {/if}
      </div>
    </div>
  {/if}

  <ConfirmDialog
    open={!!pendingDeleteUser}
    title="Delete account?"
    message={`Delete ${pendingDeleteUser?.fullName || 'this account'}? This disables the account and removes it from active use.`}
    confirmLabel="Delete account"
    tone="danger"
    busy={deleteLoading}
    on:cancel={() => pendingDeleteUser = null}
    on:confirm={deleteAccount}
  />
</DashboardLayout>

<style>
  .directory-page {
    display: grid;
    gap: 1.5rem;
  }

  .hero-card {
    position: relative;
    overflow: hidden;
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 1.5rem;
    padding: 2rem 2.5rem;
    background: var(--bg-main);
  }
  /* Dot-grid — mirrors landing page hero pattern */
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
    margin-bottom: 0.6rem;
    font-size: 0.68rem;
    font-weight: 600;
    letter-spacing: 0.14em;
    text-transform: uppercase;
    color: var(--primary);
    opacity: 0.7;
  }

  h2 {
    margin-bottom: 0.5rem;
    font-family: var(--font-heading);
    font-size: clamp(1.4rem, 3vw, 2rem);
    font-weight: 700;
    letter-spacing: -0.03em;
    line-height: 1.1;
    color: var(--text-main);
  }

  .hero-copy {
    max-width: 44rem;
    color: var(--text-secondary);
    font-size: 0.875rem;
    line-height: 1.6;
  }

  .hero-meta {
    min-width: 120px;
    text-align: right;
    flex-shrink: 0;
  }

  .hero-count {
    display: block;
    font-family: var(--font-heading);
    font-size: 3rem;
    font-weight: 700;
    letter-spacing: -0.04em;
    color: var(--text-main);
    line-height: 1;
  }

  .hero-label {
    display: block;
    color: var(--text-muted);
    font-size: 0.72rem;
    font-weight: 500;
    letter-spacing: 0.04em;
    margin-top: 0.2rem;
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

  /* User card list */
  .user-list {
    display: grid;
    gap: 0.75rem;
  }

  .user-card {
    padding: 1.1rem 1.25rem;
    border: 1px solid var(--border-light);
    border-radius: var(--radius);
    background: transparent;
    transition: border-color 0.15s ease;
  }

  .user-card:hover {
    border-color: var(--border-medium);
  }

  .user-head {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 1rem;
  }

  .user-identity {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    min-width: 0;
  }

  .user-avatar {
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

  .user-title-block {
    display: flex;
    flex-direction: column;
    gap: 0.15rem;
    min-width: 0;
  }

  .user-name-btn {
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

  .user-name-btn:hover {
    color: var(--primary);
    text-decoration: underline;
  }

  .user-sub {
    font-size: 0.72rem;
    font-weight: 600;
    letter-spacing: 0.06em;
    text-transform: uppercase;
    color: var(--text-muted);
  }

  .user-role-badge {
    flex-shrink: 0;
    padding: 0.18rem 0.6rem;
    border-radius: 999px;
    background: var(--primary-50);
    color: var(--primary);
    font-size: 0.72rem;
    font-weight: 700;
    letter-spacing: 0.03em;
  }

  .user-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 1rem;
    flex-wrap: wrap;
    padding-top: 0.5rem;
    border-top: 1px solid var(--border-light);
    margin-top: 0.6rem;
  }

  .user-meta {
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

  .meta-chip-points {
    background: var(--primary-50);
    border-color: transparent;
    color: var(--primary);
    font-weight: 600;
  }

  .user-actions {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    flex-shrink: 0;
  }

  .avatar-lg {
    width: 72px;
    height: 72px;
    font-size: 1.9rem;
    border-radius: 9999px;
    background: var(--primary);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    flex-shrink: 0;
  }

  .muted {
    color: var(--text-muted);
    font-size: 0.82rem;
  }

  .empty-state {
    color: var(--text-muted);
    padding: 1rem 0;
  }

  .alert {
    padding: 0.8rem 1rem;
    border-radius: var(--radius);
    margin-bottom: 1rem;
  }

  .alert-error {
    background: var(--danger-light);
    color: var(--danger);
  }

  .btn-sm {
    padding: 0.48rem 0.85rem;
    font-size: 0.8rem;
  }

  .profile-modal {
    max-width: 760px;
    width: min(760px, 92vw);
    border-radius: var(--radius-lg);
  }

  .modal-header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 1rem;
    margin-bottom: 1rem;
  }

  h3, h4 {
    font-family: var(--font-heading);
    font-weight: 700;
    letter-spacing: -0.02em;
    color: var(--text-main);
  }

  .close-btn {
    background: transparent;
    color: var(--text-muted);
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
    background: var(--primary-50);
    color: var(--primary-dark);
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
    border: 1px solid var(--border-light);
    border-radius: var(--radius);
    background: var(--bg-alt);
  }

  .detail-label {
    display: block;
    margin-bottom: 0.35rem;
    font-size: 0.72rem;
    font-weight: 700;
    letter-spacing: 0.05em;
    text-transform: uppercase;
    color: var(--text-muted);
  }

  .detail-value {
    color: var(--text-main);
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

    .user-footer {
      flex-direction: column;
      align-items: flex-start;
    }
  }
</style>
