<script>
  import { onMount } from 'svelte';
  import { api, getCurrentUser, getRoleDashboardPath, isHodWorkspaceRole } from '../../lib/api.js';
  import { hodNavItems } from '../../lib/navigation.js';
  import DashboardLayout from '../shared/DashboardLayout.svelte';
  import { toast } from '../../lib/toast.js';

  let user = getCurrentUser();
  let loading = true;
  let error = '';
  let suggestions = [];
  let mentorNames = {};
  let submissionOpen = false;
  let windowBusy = false;
  let reviewBusyId = null;
  let refreshTimer = null;

  const isDepartmentHead = user?.role === 'DEPARTMENT_HEAD';

  onMount(() => {
    if (!user) {
      window.location.href = '/';
      return;
    }
    if (!isHodWorkspaceRole(user.role)) {
      window.location.href = getRoleDashboardPath(user.role);
      return;
    }

    loadAll();
    refreshTimer = window.setInterval(() => loadAll({ background: true }), 15000);
    return () => {
      if (refreshTimer) {
        window.clearInterval(refreshTimer);
      }
    };
  });

  async function loadAll({ background = false } = {}) {
    if (!background) {
      loading = true;
    }
    error = '';
    try {
      await Promise.all([loadSubmissionWindow(), loadSuggestions()]);
    } catch (e) {
      error = e?.data?.message || 'Failed to load curriculum suggestion data.';
    } finally {
      if (!background) {
        loading = false;
      }
    }
  }

  async function loadSubmissionWindow() {
    const res = await api.get('/suggestions/submission-window', { cache: false });
    submissionOpen = !!res?.data?.open;
  }

  async function loadSuggestions() {
    const res = await api.get('/suggestions/all', { cache: false });
    suggestions = res?.data || [];
    await loadMentorNames(suggestions);
  }

  async function loadMentorNames(items) {
    const mentorIds = [...new Set(items.map((item) => item.mentorId).filter(Boolean))];
    const unknownIds = mentorIds.filter((id) => !mentorNames[id]);
    if (unknownIds.length === 0) {
      return;
    }

    const entries = await Promise.all(unknownIds.map(async (id) => {
      try {
        const profile = await api.get(`/users/${id}`, { cache: false });
        return [id, profile?.data?.fullName || `Mentor #${id}`];
      } catch {
        return [id, `Mentor #${id}`];
      }
    }));

    mentorNames = { ...mentorNames, ...Object.fromEntries(entries) };
  }

  async function setSubmissionWindow(open) {
    if (!isDepartmentHead) {
      return;
    }
    windowBusy = true;
    try {
      const res = await api.put('/suggestions/submission-window', { open });
      submissionOpen = !!res?.data?.open;
      toast.success({
        title: submissionOpen ? 'Submission opened' : 'Submission closed',
        message: submissionOpen
          ? 'Industry mentors can submit curriculum suggestions now.'
          : 'Industry mentor submissions are now closed.'
      });
    } catch (e) {
      const message = e?.data?.message || 'Failed to update submission window.';
      error = message;
      toast.error({ title: 'Update failed', message });
    } finally {
      windowBusy = false;
    }
  }

  async function setReviewStatus(suggestion, reviewStatus) {
    reviewBusyId = suggestion.suggestionId;
    error = '';
    try {
      await api.put(`/suggestions/${suggestion.suggestionId}/review`, { reviewStatus });
      suggestions = suggestions.map((item) => (
        item.suggestionId === suggestion.suggestionId
          ? { ...item, reviewStatus }
          : item
      ));
      toast.success({
        title: 'Review updated',
        message: `"${suggestion.suggestionTitle}" is now ${reviewStatus}.`
      });
    } catch (e) {
      const message = e?.data?.message || 'Failed to update review status.';
      error = message;
      toast.error({ title: 'Update failed', message });
    } finally {
      reviewBusyId = null;
    }
  }

  function formatDate(value) {
    if (!value) return '-';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return '-';
    return date.toLocaleString();
  }

  function reviewClass(reviewStatus) {
    if (reviewStatus === 'REVIEWED') return 'badge-success';
    return 'badge-warning';
  }
</script>

<DashboardLayout navItems={hodNavItems} activeItem="curriculum" pageTitle="Curriculum Suggestions">
  <section class="card hero-card">
    <div>
      <p class="eyebrow">Curriculum Workflow</p>
      <h2>Suggestion Request Window</h2>
      <p class="window-copy">
        {submissionOpen
          ? 'Submission window is open. Industry mentors can submit suggestions.'
          : 'Submission window is closed. Open it when you want mentors to submit.'}
      </p>
    </div>
    {#if isDepartmentHead}
      <button
        class="btn {submissionOpen ? 'btn-danger' : 'btn-primary'}"
        disabled={windowBusy}
        on:click={() => setSubmissionWindow(!submissionOpen)}
      >
        {windowBusy ? 'Saving...' : (submissionOpen ? 'Close Submission' : 'Request Suggestions')}
      </button>
    {:else}
      <p class="assistant-note">Only department heads can open or close submissions.</p>
    {/if}
  </section>

  <section class="card">
    <div class="section-head">
      <h2>Submitted Suggestions</h2>
      <span class="count-pill">{suggestions.length}</span>
    </div>

    {#if error}
      <div class="alert alert-error">{error}</div>
    {/if}

    {#if loading}
      <p class="empty-state">Loading curriculum suggestions...</p>
    {:else if suggestions.length === 0}
      <p class="empty-state">No curriculum suggestions submitted yet.</p>
    {:else}
      <div class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>Title</th>
              <th>Mentor</th>
              <th>Category</th>
              <th>Suggested Course</th>
              <th>Submitted</th>
              <th>Status</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {#each suggestions as item}
              <tr>
                <td>
                  <strong>{item.suggestionTitle}</strong>
                  <div class="muted">{item.description || 'No description.'}</div>
                </td>
                <td>{mentorNames[item.mentorId] || `Mentor #${item.mentorId}`}</td>
                <td>{item.category || '-'}</td>
                <td>{item.suggestedCourse || '-'}</td>
                <td>{formatDate(item.submissionDate)}</td>
                <td>
                  <span class="badge {reviewClass(item.reviewStatus)}">{item.reviewStatus || 'PENDING'}</span>
                </td>
                <td>
                  {#if item.reviewStatus === 'REVIEWED'}
                    <button
                      class="btn btn-outline btn-sm"
                      disabled={reviewBusyId === item.suggestionId}
                      on:click={() => setReviewStatus(item, 'PENDING')}
                    >
                      {reviewBusyId === item.suggestionId ? 'Saving...' : 'Mark Pending'}
                    </button>
                  {:else}
                    <button
                      class="btn btn-success btn-sm"
                      disabled={reviewBusyId === item.suggestionId}
                      on:click={() => setReviewStatus(item, 'REVIEWED')}
                    >
                      {reviewBusyId === item.suggestionId ? 'Saving...' : 'Mark Reviewed'}
                    </button>
                  {/if}
                </td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    {/if}
  </section>
</DashboardLayout>

<style>
  .hero-card {
    position: relative;
    overflow: hidden;
    display: flex;
    justify-content: space-between;
    align-items: center;
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
    font-size: 0.68rem;
    text-transform: uppercase;
    letter-spacing: 0.14em;
    font-weight: 600;
    color: var(--primary);
    opacity: 0.7;
    margin-bottom: 0.5rem;
  }
  h2 {
    font-family: var(--font-heading);
    font-size: clamp(1.3rem, 2.5vw, 1.8rem);
    font-weight: 700;
    letter-spacing: -0.03em;
    line-height: 1.1;
    color: var(--text-main);
    margin-bottom: 0.5rem;
  }
  .window-copy,
  .assistant-note,
  .muted,
  .empty-state {
    color: var(--text-secondary);
  }
  .section-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 1rem;
    margin-bottom: 0.9rem;
  }
  .count-pill {
    min-width: 2rem;
    padding: 0.25rem 0.65rem;
    border-radius: 999px;
    background: var(--primary-50);
    font-weight: 700;
    color: var(--primary-dark);
    text-align: center;
  }
  .table-wrapper {
    overflow-x: auto;
  }
  table {
    width: 100%;
    border-collapse: collapse;
  }
  th,
  td {
    padding: 0.9rem 0.75rem;
    border-bottom: 1px solid var(--border-light);
    text-align: left;
    vertical-align: top;
  }
  th {
    font-size: 0.76rem;
    text-transform: uppercase;
    letter-spacing: 0.05em;
    color: var(--text-muted);
    background: var(--bg-secondary);
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
  @media (max-width: 920px) {
    .hero-card {
      flex-direction: column;
      align-items: flex-start;
    }
  }
</style>
