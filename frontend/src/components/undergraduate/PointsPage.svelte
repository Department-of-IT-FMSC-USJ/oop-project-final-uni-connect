<script>
  import { onMount } from 'svelte';
  import { api, getCurrentUser, getRoleDashboardPath } from '../../lib/api.js';
  import { undergraduateNavItems } from '../../lib/navigation.js';
  import DashboardLayout from '../shared/DashboardLayout.svelte';
  import CustomSelect from '../shared/CustomSelect.svelte';

  let user = getCurrentUser();
  let records = [];
  let loading = true;
  let error = '';

  // Proof/Evidence
  let myProofs = [];
  let proofError = '';
  let proofSuccess = '';
  let showProofUpload = false;
  let proofForm = { title: '', description: '', eventDate: '', proofData: '', category: 'ACTIVITY' };

  const proofCategoryOptions = [
    { value: 'ACTIVITY', label: 'Activity' },
    { value: 'AWARD', label: 'Award' }
  ];

  onMount(async () => {
    if (!user) { window.location.href = '/'; return; }
    if (user.role !== 'UNDERGRADUATE') { window.location.href = getRoleDashboardPath(user.role); return; }
    await Promise.all([loadRecords(), loadProofs()]);
  });

  async function loadRecords() {
    loading = true;
    error = '';
    try {
      records = await api.get('/points/my', { cache: false });
    } catch (e) {
      error = 'Failed to load point history.';
    } finally {
      loading = false;
    }
  }

  async function loadProofs() {
    try {
      myProofs = await api.get('/proofs/my', { cache: false });
    } catch (e) {
      console.error('Failed to load proofs', e);
      myProofs = [];
    }
  }

  async function submitProof() {
    proofError = '';
    proofSuccess = '';
    if (!proofForm.title.trim() || !proofForm.eventDate || !proofForm.proofData.trim()) {
      proofError = 'Title, event date, and evidence link are required.';
      return;
    }
    const eventDate = new Date(`${proofForm.eventDate}T00:00:00`);
    const today = new Date(); today.setHours(0, 0, 0, 0);
    const oldestAllowed = new Date(); oldestAllowed.setFullYear(oldestAllowed.getFullYear() - 2); oldestAllowed.setHours(0, 0, 0, 0);
    if (eventDate > today) { proofError = 'Event date cannot be in the future.'; return; }
    if (eventDate < oldestAllowed) { proofError = 'Event date must be within the past two years.'; return; }
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

  function isGoogleDriveLink(value) {
    const trimmed = value?.trim();
    if (!trimmed) return false;
    try {
      const url = new URL(trimmed.startsWith('http') ? trimmed : `https://${trimmed}`);
      return url.hostname.toLowerCase() === 'drive.google.com';
    } catch { return false; }
  }

  function maxEvidenceDate() { return formatLocalDate(new Date()); }
  function minEvidenceDate() {
    const d = new Date(); d.setFullYear(d.getFullYear() - 2); return formatLocalDate(d);
  }
  function formatLocalDate(date) {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  }

  $: totalPoints = records.reduce((sum, r) => sum + (r.points || 0), 0);
</script>

<DashboardLayout navItems={undergraduateNavItems} activeItem="points" pageTitle="Points & Evidence">

  <!-- Hero -->
  <section class="card hero-card">
    <div>
      <p class="eyebrow">Undergraduate</p>
      <h2>Points & Evidence</h2>
      <p class="hero-copy">Track your allocated points and submit evidence of activities and achievements for review.</p>
    </div>
    <div class="hero-meta">
      <span class="hero-count">{totalPoints}</span>
      <span class="hero-label">Total Points</span>
    </div>
  </section>

  <div class="page-grid">

    <!-- Point History -->
    <section class="card">
      <div class="section-head">
        <div>
          <p class="eyebrow">History</p>
          <h3 class="section-title">Point Allocations</h3>
        </div>
      </div>
      {#if error}
        <div class="alert alert-error">{error}</div>
      {/if}
      {#if loading}
        <p class="empty-state">Loading point records...</p>
      {:else if records.length === 0}
        <p class="empty-state">No point records available yet.</p>
      {:else}
        <div class="record-list">
          {#each records as record}
            {@const isNeg = Number(record.points) < 0}
            <div class="record-card">
              <div class="record-head">
                <span class="record-category">{record.category || 'General'}</span>
                <span class="badge" class:badge-success={record.status === 'APPROVED' && !isNeg} class:badge-warning={record.status === 'PENDING'} class:badge-danger={record.status === 'REJECTED' || isNeg}>
                  {isNeg ? 'Punishment' : (record.status || '-')}
                </span>
              </div>
              <div class="record-footer">
                <div class="record-meta">
                  <span class="meta-chip" class:meta-chip-neg={isNeg} class:meta-chip-pos={!isNeg && record.points > 0}>{record.points > 0 ? '+' : ''}{record.points || 0} pts</span>
                  <span class="meta-chip meta-chip-muted">{record.allocatedAt || '-'}</span>
                  {#if record.reviewNote || record.note}
                    <span class="meta-chip meta-chip-muted">{record.reviewNote || record.note}</span>
                  {/if}
                </div>
              </div>
            </div>
          {/each}
        </div>
      {/if}
    </section>

    <!-- Evidence Submissions -->
    <section class="card">
      <div class="section-head">
        <div>
          <p class="eyebrow">Evidence</p>
          <h3 class="section-title">Proof Submissions</h3>
        </div>
        <button class="btn btn-primary btn-sm" on:click={() => showProofUpload = true}>Upload Evidence</button>
      </div>

      {#if proofSuccess}
        <div class="alert alert-success">{proofSuccess}</div>
      {/if}

      {#if myProofs.length === 0}
        <p class="empty-state">No evidence submitted yet.</p>
      {:else}
        <div class="proof-list">
          {#each myProofs as proof}
            {@const isPunishment = proof.pointStatus === 'APPROVED' && Number(proof.latestPoints) < 0}
            <article class="proof-card">
              <div class="proof-head">
                <div class="proof-identity">
                  {#if proof.proofData}
                    <a href={proof.proofData} target="_blank" rel="noreferrer" class="proof-title-link">{proof.title}</a>
                  {:else}
                    <span class="proof-title">{proof.title}</span>
                  {/if}
                </div>
                <span
                  class="status-chip"
                  class:status-pending={!proof.pointStatus || proof.pointStatus === 'PENDING'}
                  class:status-approved={proof.pointStatus === 'APPROVED' && !isPunishment}
                  class:status-rejected={proof.pointStatus === 'REJECTED'}
                  class:status-punishment={isPunishment}
                >{isPunishment ? 'Punishment' : (proof.pointStatus || 'PENDING')}</span>
              </div>
              <div class="proof-footer">
                <div class="proof-meta">
                  <span class="meta-chip">{proof.pointCategory || '-'}</span>
                  <span class="meta-chip meta-chip-muted">{proof.eventDate || '-'}</span>
                  {#if proof.latestPoints}
                    <span class="meta-chip" class:meta-chip-neg={Number(proof.latestPoints) < 0} class:meta-chip-pos={Number(proof.latestPoints) > 0}>{proof.latestPoints} pts</span>
                  {/if}
                </div>
              </div>
            </article>
          {/each}
        </div>
      {/if}
    </section>

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
            <CustomSelect id="proof-category" options={proofCategoryOptions} bind:value={proofForm.category} />
          </div>
          <div class="form-group">
            <label for="proof-date">Event Date</label>
            <input id="proof-date" type="date" class="input" bind:value={proofForm.eventDate} min={minEvidenceDate()} max={maxEvidenceDate()} />
          </div>
          <div class="form-group full-width">
            <label for="proof-description">Description</label>
            <textarea id="proof-description" class="input" rows="3" bind:value={proofForm.description}></textarea>
          </div>
          <div class="form-group full-width">
            <label for="proof-data">Evidence Link <span class="helper-text">(Google Drive only)</span></label>
            <textarea id="proof-data" class="input" rows="2" bind:value={proofForm.proofData} placeholder="https://drive.google.com/..."></textarea>
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn btn-outline" on:click={() => showProofUpload = false}>Cancel</button>
          <button class="btn btn-primary" on:click={submitProof}>Submit Evidence</button>
        </div>
      </div>
    </div>
  {/if}

</DashboardLayout>

<style>
  /* Hero */
  .hero-card {
    position: relative;
    overflow: hidden;
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 1.5rem;
    padding: 2rem 2.5rem;
    margin-bottom: 1.5rem;
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
    font-size: 0.68rem;
    font-weight: 600;
    letter-spacing: 0.14em;
    text-transform: uppercase;
    color: var(--primary);
    opacity: 0.7;
    margin-bottom: 0.4rem;
  }
  h2 {
    font-family: var(--font-heading);
    font-size: clamp(1.3rem, 3vw, 1.8rem);
    font-weight: 700;
    letter-spacing: -0.03em;
    color: var(--text-main);
    margin-bottom: 0.4rem;
  }
  .hero-copy { color: var(--text-secondary); font-size: 0.875rem; max-width: 38rem; }
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

  /* Page grid: two side-by-side cards */
  .page-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1.5rem;
    align-items: start;
  }

  /* Section head */
  .section-head {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 1rem;
    margin-bottom: 1rem;
  }
  .section-title {
    font-family: var(--font-heading);
    font-size: 1rem;
    font-weight: 700;
    letter-spacing: -0.01em;
    color: var(--text-main);
    margin: 0.15rem 0 0;
  }

  /* Record cards */
  .record-list { display: grid; gap: 0.6rem; max-height: 28rem; overflow-y: auto; }

  .record-card {
    padding: 0.85rem 1rem;
    border: 1px solid var(--border-light);
    border-radius: var(--radius);
    transition: border-color 0.15s ease;
  }
  .record-card:hover { border-color: var(--border-medium); }

  .record-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 0.75rem;
    margin-bottom: 0.45rem;
  }
  .record-category {
    font-size: 0.875rem;
    font-weight: 600;
    color: var(--text-main);
  }
  .record-footer { }
  .record-meta { display: flex; flex-wrap: wrap; gap: 0.4rem; }

  /* Proof cards */
  .proof-list { display: grid; gap: 0.6rem; max-height: 28rem; overflow-y: auto; }

  .proof-card {
    padding: 0.85rem 1rem;
    border: 1px solid var(--border-light);
    border-radius: var(--radius);
    transition: border-color 0.15s ease;
  }
  .proof-card:hover { border-color: var(--border-medium); }

  .proof-head {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 0.75rem;
    margin-bottom: 0.45rem;
  }
  .proof-identity { min-width: 0; }
  .proof-title-link {
    font-size: 0.9rem;
    font-weight: 600;
    color: var(--primary);
    text-decoration: none;
    line-height: 1.3;
  }
  .proof-title-link:hover { text-decoration: underline; }
  .proof-title {
    font-size: 0.9rem;
    font-weight: 600;
    color: var(--text-main);
    line-height: 1.3;
  }
  .proof-footer { }
  .proof-meta { display: flex; flex-wrap: wrap; gap: 0.4rem; }

  /* Meta chips */
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
  .meta-chip-pos { background: rgba(16,185,129,0.08); border-color: transparent; color: #065f46; font-weight: 600; }
  .meta-chip-neg { background: rgba(239,68,68,0.08); border-color: transparent; color: #991b1b; font-weight: 600; }

  /* Status chips */
  .status-chip {
    flex-shrink: 0;
    display: inline-flex;
    align-items: center;
    padding: 0.15rem 0.55rem;
    border-radius: 999px;
    font-size: 0.72rem;
    font-weight: 600;
    letter-spacing: 0.03em;
    white-space: nowrap;
  }
  .status-pending { background: rgba(245,158,11,0.12); color: #92400e; }
  .status-approved { background: rgba(16,185,129,0.12); color: #065f46; }
  .status-rejected { background: rgba(239,68,68,0.12); color: #991b1b; }
  .status-punishment { background: rgba(245,158,11,0.15); color: #b45309; border: 1px solid rgba(245,158,11,0.3); }

  /* Badges */
  .badge { display:inline-flex; align-items:center; padding:0.15rem 0.55rem; border-radius:999px; font-size:0.72rem; font-weight:600; white-space:nowrap; }
  .badge-success { background:rgba(16,185,129,0.12); color:#065f46; }
  .badge-warning { background:rgba(245,158,11,0.12); color:#92400e; }
  .badge-danger  { background:rgba(239,68,68,0.12);  color:#991b1b; }

  /* Misc */
  .empty-state { color: var(--text-muted); font-size: 0.875rem; }
  .helper-text { color: var(--text-muted); font-size: 0.8rem; font-weight: 400; margin-left: 0.25rem; }
  .alert { padding: 0.8rem 1rem; border-radius: var(--radius-sm); margin-bottom: 1rem; }
  .alert-error { background: var(--danger-light); color: #991b1b; }
  .alert-success { background: var(--success-light, #DCFCE7); color: #065f46; }
  .form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 1rem; margin-top: 1rem; }
  .full-width { grid-column: 1 / -1; }
  .form-group label { display: block; margin-bottom: 0.35rem; font-size: 0.82rem; color: var(--text-secondary); font-weight: 500; }
  .modal-actions { display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1rem; }
  .btn-sm { padding: 0.375rem 0.8rem; font-size: 0.8rem; }

  @media (max-width: 900px) {
    .hero-card { flex-direction: column; align-items: flex-start; }
    .hero-meta { text-align: left; }
    .page-grid { grid-template-columns: 1fr; }
  }
</style>
