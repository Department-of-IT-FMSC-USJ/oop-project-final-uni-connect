<script>
  import { onMount } from 'svelte';
  import { api, getCurrentUser, getRoleDashboardPath, invalidateApiCache } from '../../lib/api.js';
  import DashboardLayout from './DashboardLayout.svelte';

  export let navItems = [];
  export let activeItem = 'profile';
  export let pageTitle = 'Profile';

  let user = getCurrentUser();
  let profile = null;
  let editing = false;
  let saving = false;
  let loading = true;
  let error = '';
  let success = '';
  let mounted = false;

  let editForm = {
    fullName: '',
    phone: '',
    department: '',
    yearOfStudy: '',
    registrationNumber: '',
    cpmNumber: ''
  };

  onMount(() => {
    mounted = true;
    if (!user) { window.location.href = '/'; return; }

    loadProfile();

    return () => {
      mounted = false;
    };
  });

  function syncEditForm() {
    editForm = {
      fullName: profile?.fullName || '',
      phone: profile?.phone || '',
      department: profile?.department || '',
      yearOfStudy: profile?.yearOfStudy || '',
      registrationNumber: profile?.registrationNumber || '',
      cpmNumber: profile?.cpmNumber || ''
    };
  }

  async function loadProfile({ force = false } = {}) {
    loading = true;
    error = '';

    try {
      profile = await api.get('/users/profile', { cache: !force });
      if (!mounted) return;
      syncEditForm();
    } catch (e) {
      if (mounted) {
        error = 'Failed to load profile';
      }
    } finally {
      if (mounted) {
        loading = false;
      }
    }
  }

  async function saveProfile() {
    saving = true;
    error = '';
    success = '';
    try {
      profile = await api.put('/users/profile', editForm);
      if (!mounted) return;
      invalidateApiCache('/users/profile');
      syncEditForm();
      success = 'Profile updated successfully';
      editing = false;
      const stored = getCurrentUser();
      if (stored) {
        stored.fullName = profile.fullName;
        localStorage.setItem('user', JSON.stringify(stored));
      }
    } catch (e) {
      error = 'Failed to save profile';
    } finally {
      saving = false;
    }
  }
</script>

<DashboardLayout {navItems} {activeItem} {pageTitle}>
  <div class="profile-container">
    {#if error}
      <div class="alert alert-error">{error}</div>
    {/if}
    {#if success}
      <div class="alert alert-success">{success}</div>
    {/if}

    {#if loading}
      <div class="card loading-card">
        <p>Loading profile...</p>
      </div>
    {:else if profile}
      <div class="card profile-card">
        <div class="profile-header">
          <div class="avatar-lg">
            {profile.fullName?.charAt(0) || 'U'}
          </div>
          <div class="profile-meta">
            <h2>{profile.fullName}</h2>
            <p class="role-badge">{profile.role?.replace('_', ' ')}</p>
            <p class="email">{profile.email}</p>
          </div>
          <button class="btn btn-outline" on:click={() => editing = !editing}>
            {editing ? 'Cancel' : 'Edit Profile'}
          </button>
        </div>

        {#if editing}
          <form class="edit-form" on:submit|preventDefault={saveProfile}>
            <div class="form-grid">
              <div class="form-group">
                <label>Full Name</label>
                <input class="input" bind:value={editForm.fullName} />
              </div>
              <div class="form-group">
                <label>Phone</label>
                <input class="input" bind:value={editForm.phone} />
              </div>
              <div class="form-group">
                <label>Department</label>
                <input class="input" bind:value={editForm.department} />
              </div>
              {#if profile.role === 'UNDERGRADUATE'}
                <div class="form-group">
                  <label>Year of Study</label>
                  <select class="input" bind:value={editForm.yearOfStudy}>
                    <option value="1">1st Year</option>
                    <option value="2">2nd Year</option>
                    <option value="3">3rd Year</option>
                    <option value="4">4th Year</option>
                  </select>
                </div>
                <div class="form-group">
                  <label>Registration Number</label>
                  <input class="input" bind:value={editForm.registrationNumber} readonly />
                </div>
              {/if}
              {#if profile.role === 'ACADEMIC_MENTOR'}
                <div class="form-group">
                  <label>CPM Number</label>
                  <input class="input" bind:value={editForm.cpmNumber} />
                </div>
              {/if}
            </div>
            <div class="form-actions">
              <button type="submit" class="btn btn-primary" disabled={saving}>
                {saving ? 'Saving...' : 'Save Changes'}
              </button>
            </div>
          </form>
        {:else}
          <div class="profile-details">
            <div class="detail-grid">
              <div class="detail-item">
                <span class="detail-label">Email</span>
                <span class="detail-value">{profile.email}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Phone</span>
                <span class="detail-value">{profile.phone || 'Not set'}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Department</span>
                <span class="detail-value">{profile.department || 'Not set'}</span>
              </div>
              {#if profile.role === 'UNDERGRADUATE'}
                <div class="detail-item">
                  <span class="detail-label">Year of Study</span>
                  <span class="detail-value">Year {profile.yearOfStudy || '-'}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">Registration Number</span>
                  <span class="detail-value">{profile.registrationNumber || '-'}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">Points</span>
                  <span class="detail-value points">{profile.cumulativePoints || 0}</span>
                </div>
              {/if}
              {#if profile.role === 'ACADEMIC_MENTOR'}
                <div class="detail-item">
                  <span class="detail-label">CPM Number</span>
                  <span class="detail-value">{profile.cpmNumber || '-'}</span>
                </div>
              {/if}
            </div>
          </div>
        {/if}
      </div>
    {:else}
      <div class="card loading-card">
        <p>Profile data is unavailable.</p>
      </div>
    {/if}
  </div>
</DashboardLayout>

<style>
  .profile-container {
    max-width: 800px;
  }

  .alert {
    padding: 0.75rem 1rem;
    border-radius: var(--radius);
    font-size: 0.8125rem;
    margin-bottom: 1rem;
  }
  .alert-error { background: #fee2e2; color: #991b1b; }
  .alert-success { background: #d1fae5; color: #065f46; }

  .profile-card {
    padding: 2rem;
  }

  .profile-header {
    display: flex;
    align-items: center;
    gap: 1.5rem;
    margin-bottom: 2rem;
    padding-bottom: 1.5rem;
    border-bottom: 1px solid var(--gray-200);
  }

  .avatar-lg {
    width: 72px;
    height: 72px;
    border-radius: 50%;
    background: var(--primary);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 2rem;
    font-weight: 600;
    flex-shrink: 0;
  }

  .profile-meta { flex: 1; }
  .profile-meta h2 { font-size: 1.375rem; font-weight: 600; color: var(--gray-900); }
  .role-badge {
    display: inline-block;
    background: var(--gray-100);
    color: var(--gray-600);
    padding: 0.125rem 0.5rem;
    border-radius: 4px;
    font-size: 0.75rem;
    font-weight: 500;
    text-transform: uppercase;
    margin-top: 0.25rem;
  }
  .email { font-size: 0.875rem; color: var(--gray-500); margin-top: 0.25rem; }

  .detail-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1.25rem;
  }

  .detail-item {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
  }
  .detail-label { font-size: 0.75rem; font-weight: 500; color: var(--gray-500); text-transform: uppercase; }
  .detail-value { font-size: 0.9375rem; color: var(--gray-800); }
  .detail-value.points { color: var(--accent); font-weight: 700; font-size: 1.25rem; }

  .form-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1rem;
  }
  .form-group { margin-bottom: 0; }
  .form-group label {
    display: block;
    font-size: 0.8125rem;
    font-weight: 500;
    color: var(--gray-700);
    margin-bottom: 0.375rem;
  }
  .form-actions {
    margin-top: 1.5rem;
    display: flex;
    justify-content: flex-end;
  }

  .loading-card {
    text-align: center;
    padding: 3rem;
    color: var(--gray-400);
  }
</style>
