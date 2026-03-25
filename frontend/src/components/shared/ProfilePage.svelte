<script>
  import { onMount } from 'svelte';
  import { api, getCurrentUser, getRoleDashboardPath, invalidateApiCache } from '../../lib/api.js';
  import DashboardLayout from './DashboardLayout.svelte';
  import CustomSelect from './CustomSelect.svelte';

  export let navItems = [];
  export let activeItem = 'profile';
  export let pageTitle = 'Profile';

  let user = getCurrentUser();
  let profile = null;
  let mentorProfile = null;
  let editing = false;
  let editingMentorProfile = false;
  let saving = false;
  let mentorSaving = false;
  let loading = true;
  let error = '';
  let success = '';
  let mentorError = '';
  let mentorSuccess = '';
  let mounted = false;

  let editForm = {
    fullName: '',
    phone: '',
    department: '',
    yearOfStudy: '',
    registrationNumber: '',
    cpmNumber: ''
  };

  let mentorEditForm = {
    expertiseTags: '',
    company: ''
  };

  const yearOfStudyOptions = [
    { value: '1', label: '1st Year' },
    { value: '2', label: '2nd Year' },
    { value: '3', label: '3rd Year' },
    { value: '4', label: '4th Year' }
  ];

  onMount(() => {
    mounted = true;
    if (!user) { window.location.href = '/'; return; }

    loadProfile();
    if (user.role === 'INDUSTRY_MENTOR') {
      loadMentorProfile();
    }

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

  function syncMentorEditForm() {
    mentorEditForm = {
      expertiseTags: mentorProfile?.expertiseTags || '',
      company: mentorProfile?.company || ''
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

  async function loadMentorProfile({ force = false } = {}) {
    try {
      const mentorId = user?.userId || user?.id;
      const res = await api.get(`/recommendations/mentor-profile/${mentorId}`, { cache: !force });
      if (!mounted) return;
      mentorProfile = res?.data || {};
      syncMentorEditForm();
    } catch (e) {
      if (mounted) {
        mentorProfile = { expertiseTags: '', company: '' };
        syncMentorEditForm();
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

  async function saveMentorProfile() {
    mentorSaving = true;
    mentorError = '';
    mentorSuccess = '';
    try {
      const mentorId = user?.userId || user?.id;
      await api.post('/recommendations/mentor-profile', {
        mentorId,
        mentorName: profile?.fullName || user?.fullName,
        mentorCategory: 'INDUSTRY_MENTOR',
        expertiseTags: mentorEditForm.expertiseTags.trim(),
        department: profile?.department,
        company: mentorEditForm.company.trim()
      });
      if (!mounted) return;
      mentorProfile = { 
        expertiseTags: mentorEditForm.expertiseTags,
        company: mentorEditForm.company 
      };
      syncMentorEditForm();
      mentorSuccess = 'Interests and company updated successfully';
      editingMentorProfile = false;
      invalidateApiCache('/recommendations/mentor-profile');
    } catch (e) {
      mentorError = e?.data?.message || 'Failed to save interests and company';
    } finally {
      mentorSaving = false;
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
            <p class="email"><a href="mailto:{profile.email}" class="email-link">{profile.email}</a></p>
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
                  <CustomSelect options={yearOfStudyOptions} bind:value={editForm.yearOfStudy} />
                </div>
                <div class="form-group">
                  <label>Registration Number</label>
                  <input class="input" bind:value={editForm.registrationNumber} readonly />
                </div>
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
                <span class="detail-value"><a href="mailto:{profile.email}" class="email-link">{profile.email}</a></span>
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
                <div class="detail-item">
                  <span class="detail-label">CPM Number</span>
                  <span class="detail-value">{profile.cpmNumber || '-'}</span>
                </div>
              {/if}
            </div>
          </div>
        {/if}
      </div>

      {#if user.role === 'INDUSTRY_MENTOR'}
        <div class="card mentor-profile-card">
          <div class="section-header">
            <div>
              <p class="section-eyebrow">Mentor Matching</p>
              <h3>Professional Profile & Interests</h3>
            </div>
            <button class="btn btn-outline" on:click={() => editingMentorProfile = !editingMentorProfile}>
              {editingMentorProfile ? 'Cancel' : 'Set Interests'}
            </button>
          </div>

          {#if mentorError}
            <div class="alert alert-error">{mentorError}</div>
          {/if}
          {#if mentorSuccess}
            <div class="alert alert-success">{mentorSuccess}</div>
          {/if}

          {#if editingMentorProfile}
            <form class="mentor-edit-form" on:submit|preventDefault={saveMentorProfile}>
              <div class="form-group">
                <label for="expertise-tags">Areas of Expertise</label>
                <input id="expertise-tags" class="input" bind:value={mentorEditForm.expertiseTags} placeholder="e.g. Cloud Architecture, DevOps, Microservices, Kubernetes" required />
                <p class="helper-text">Enter your areas of expertise separated by commas. This helps match you with students.</p>
              </div>
              <div class="form-group">
                <label for="company">Company / Organization</label>
                <input id="company" class="input" bind:value={mentorEditForm.company} placeholder="e.g. Google, Amazon, Accenture" />
                <p class="helper-text">Your current company or organization (optional).</p>
              </div>
              <div class="form-actions">
                <button type="button" class="btn btn-outline" on:click={() => editingMentorProfile = false}>Cancel</button>
                <button type="submit" class="btn btn-primary" disabled={mentorSaving}>
                  {mentorSaving ? 'Saving...' : 'Save Interests'}
                </button>
              </div>
            </form>
          {:else}
            <div class="mentor-details">
              <div class="detail-item">
                <span class="detail-label">Areas of Expertise</span>
                <span class="detail-value">{mentorProfile?.expertiseTags || 'Not set yet'}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Company</span>
                <span class="detail-value">{mentorProfile?.company || 'Not set yet'}</span>
              </div>
            </div>
          {/if}
        </div>
      {/if}
    {:else}
      <div class="card loading-card">
        <p>Profile data is unavailable.</p>
      </div>
    {/if}
  </div>
</DashboardLayout>

<style>
  .email-link { color: var(--primary); text-decoration: none; }
  .email-link:hover { text-decoration: none; color: var(--primary-dark); }
  .profile-container {
    max-width: 800px;
  }

  .alert {
    padding: 0.75rem 1rem;
    border-radius: var(--radius);
    font-size: 0.8125rem;
    margin-bottom: 1rem;
  }
  .alert-error { background: var(--danger-light); color: #991b1b; }
  .alert-success { background: var(--success-light); color: #065f46; }

  .profile-card {
    padding: 2rem;
  }

  .profile-header {
    display: flex;
    align-items: center;
    gap: 1.5rem;
    margin-bottom: 2rem;
    padding-bottom: 1.5rem;
    border-bottom: 1px solid var(--border-light);
  }

  .avatar-lg {
    width: 72px;
    height: 72px;
    border-radius: 50%;
    background: var(--primary-50);
    color: var(--primary);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 2rem;
    font-weight: 600;
    flex-shrink: 0;
  }

  .profile-meta { flex: 1; }
  .profile-meta h2 { font-size: 1.375rem; font-weight: 600; color: var(--text-main); }
  .role-badge {
    display: inline-block;
    background: var(--primary-50);
    color: var(--primary);
    padding: 0.125rem 0.5rem;
    border-radius: var(--radius-sm);
    font-size: 0.75rem;
    font-weight: 500;
    text-transform: uppercase;
    margin-top: 0.25rem;
  }
  .email { font-size: 0.875rem; color: var(--text-secondary); margin-top: 0.25rem; }

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
  .detail-label { font-size: 0.75rem; font-weight: 500; color: var(--text-muted); text-transform: uppercase; }
  .detail-value { font-size: 0.9375rem; color: var(--text-main); }
  .detail-value.points { color: var(--primary); font-weight: 700; font-size: 1.25rem; }

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
    color: var(--text-secondary);
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
    color: var(--text-muted);
  }

  .mentor-profile-card {
    padding: 2rem;
    margin-top: 1.5rem;
    background: linear-gradient(135deg, rgba(79, 124, 219, 0.04) 0%, rgba(107, 147, 228, 0.04) 100%);
  }

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 1.5rem;
    padding-bottom: 1rem;
    border-bottom: 1px solid var(--border-light);
  }

  .section-eyebrow {
    font-size: 0.7rem;
    font-weight: 700;
    text-transform: uppercase;
    color: var(--text-muted);
    letter-spacing: 1px;
    margin-bottom: 0.25rem;
  }

  .section-header h3 {
    font-size: 1.25rem;
    font-weight: 600;
    color: var(--text-main);
    margin: 0;
  }

  .mentor-edit-form {
    margin-top: 1rem;
  }

  .mentor-details {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1.5rem;
  }

  .helper-text {
    font-size: 0.75rem;
    color: var(--text-muted);
    margin-top: 0.375rem;
  }

  .input {
    width: 100%;
    padding: 0.5rem 0.75rem;
    border: 1px solid var(--border-medium);
    border-radius: var(--radius-sm);
    font-size: 0.875rem;
    background: var(--bg-main);
  }

  .input:focus {
    outline: none;
    border-color: var(--primary);
    box-shadow: 0 0 0 3px rgba(79, 124, 219, 0.12);
  }
</style>
