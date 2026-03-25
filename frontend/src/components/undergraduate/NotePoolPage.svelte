<script>
  import { onMount } from 'svelte';
  import { api, downloadWithAuth, getCurrentUser, getRoleDashboardPath } from '../../lib/api.js';
  import { undergraduateNavItems } from '../../lib/navigation.js';
  import DashboardLayout from '../shared/DashboardLayout.svelte';
  import CustomSelect from '../shared/CustomSelect.svelte';
  import { toast } from '../../lib/toast.js';

  let user = getCurrentUser();
  let materials = [];
  let usersById = {};
  let loading = true;
  let error = '';
  let uploadError = '';
  let uploadSuccess = '';
  let showUpload = false;
  let query = '';
  let yearFilter = '';
  let typeFilter = '';
  let refreshTimer = null;
  let form = {
    title: '',
    description: '',
    materialType: 'NOTES',
    targetYearOfStudy: '',
    file: null
  };

  onMount(() => {
    if (!user) { window.location.href = '/'; return; }
    if (user.role !== 'UNDERGRADUATE') { window.location.href = getRoleDashboardPath(user.role); return; }
    showUpload = new URLSearchParams(window.location.search).get('upload') === '1';
    loadMaterials();
    refreshTimer = window.setInterval(() => loadMaterials(), 15000);
    return () => {
      if (refreshTimer) {
        window.clearInterval(refreshTimer);
      }
    };
  });

  async function loadMaterials() {
    loading = true;
    error = '';
    try {
      const res = await api.get('/materials', { cache: false });
      const payload = Array.isArray(res) ? res : (Array.isArray(res?.data) ? res.data : []);
      materials = payload
        .filter(Boolean)
        .sort((left, right) => {
          const leftDate = left?.uploadDate ? new Date(left.uploadDate).getTime() : 0;
          const rightDate = right?.uploadDate ? new Date(right.uploadDate).getTime() : 0;
          return rightDate - leftDate;
        });
      const uniqueUserIds = [...new Set(materials.map((item) => item.uploadedBy).filter(Boolean))];
      const userEntries = await Promise.all(uniqueUserIds.map(async (id) => {
        try {
          const profile = await api.get(`/users/${id}`, { cache: false });
          return [id, profile.data || null];
        } catch {
          return [id, null];
        }
      }));
      usersById = Object.fromEntries(userEntries);
    } catch (e) {
      error = e?.data?.message || 'Failed to load note pool.';
      materials = [];
    } finally {
      loading = false;
    }
  }

  function visibleMaterials() {
    return materials.filter((item) => {
      const typeOk = !typeFilter || item.materialType === typeFilter;
      const yearOk = !yearFilter || String(item.targetYearOfStudy || '') === yearFilter;
      const normalized = query.trim().toLowerCase();
      const queryOk = !normalized || [item.title, item.description, uploaderName(item), item.materialType]
        .filter(Boolean)
        .some((value) => value.toLowerCase().includes(normalized));
      return typeOk && yearOk && queryOk;
    });
  }

  function uploaderName(item) {
    return usersById[item.uploadedBy]?.fullName || `Student #${item.uploadedBy}`;
  }

  async function uploadNote() {
    uploadError = '';
    uploadSuccess = '';

    const trimmedTitle = form.title.trim();
    if (!trimmedTitle || trimmedTitle.length < 3) {
      uploadError = 'Title must be at least 3 characters long.';
      return;
    }
    if (!/[a-zA-Z]/.test(trimmedTitle)) {
      uploadError = 'Title must contain at least one letter.';
      return;
    }
    if (trimmedTitle.length > 100) {
      uploadError = 'Title cannot exceed 100 characters.';
      return;
    }
    if (form.description.trim().length > 500) {
      uploadError = 'Description cannot exceed 500 characters.';
      return;
    }
    if (!form.file) {
      uploadError = 'Select a PDF file to upload.';
      return;
    }
    if (!form.file.name.toLowerCase().endsWith('.pdf') || form.file.type !== 'application/pdf') {
      uploadError = 'Only PDF files are allowed in the note pool.';
      return;
    }
    if (!form.targetYearOfStudy) {
      uploadError = 'Select a target year for this note.';
      return;
    }

    try {
      const payload = new FormData();
      payload.append('material', new Blob([JSON.stringify({
        title: form.title.trim(),
        description: form.description.trim(),
        materialType: form.materialType,
        targetYearOfStudy: form.targetYearOfStudy
      })], { type: 'application/json' }));
      payload.append('file', form.file);
      await api.upload('/materials/upload', payload);
      form = { title: '', description: '', materialType: 'NOTES', targetYearOfStudy: '', file: null };
      showUpload = false;
      uploadSuccess = 'Note uploaded to the pool successfully.';
      toast.success({ title: 'Upload complete', message: 'Your note is now visible in the shared note pool.' });
      await loadMaterials();
    } catch (e) {
      uploadError = e?.data?.message || 'Failed to upload note.';
    }
  }

  async function downloadNote(materialId, title) {
    try {
      const blob = await downloadWithAuth(`/materials/download/${materialId}`);
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `${title || 'note'}.pdf`;
      document.body.appendChild(link);
      link.click();
      link.remove();
      URL.revokeObjectURL(url);
    } catch (e) {
      error = e?.data?.message || 'Failed to download note.';
      toast.error({ title: 'Download failed', message: error });
    }
  }

  async function viewNote(materialId) {
    try {
      const blob = await downloadWithAuth(`/materials/download/${materialId}`);
      const url = URL.createObjectURL(blob);
      const popup = window.open(url, '_blank', 'noopener,noreferrer');
      if (!popup) {
        const link = document.createElement('a');
        link.href = url;
        link.target = '_blank';
        link.rel = 'noopener noreferrer';
        document.body.appendChild(link);
        link.click();
        link.remove();
      }
      window.setTimeout(() => URL.revokeObjectURL(url), 60000);
    } catch (e) {
      error = e?.data?.message || 'Failed to open note.';
      toast.error({ title: 'Open failed', message: error });
    }
  }

  function formatMaterialType(value) {
    return (value || 'OTHER')
      .toLowerCase()
      .split('_')
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
      .join(' ');
  }

  const yearOptions = ['1', '2', '3', '4'];

  const materialTypeOptions = [
    { value: 'NOTES', label: 'Notes' },
    { value: 'PAST_PAPERS', label: 'Past Papers' },
    { value: 'LECTURE_SLIDES', label: 'Lecture Slides' },
    { value: 'OTHER', label: 'Other' }
  ];

  const typeFilterOptions = [{ value: '', label: 'All types' }, ...materialTypeOptions];
  const yearFilterOptions = [{ value: '', label: 'All years' }, ...yearOptions.map((year) => ({ value: year, label: `Year ${year}` }))];
  const uploadYearOptions = [{ value: '', label: 'Select year' }, ...yearOptions.map((year) => ({ value: year, label: `Year ${year}` }))];
</script>

<DashboardLayout navItems={undergraduateNavItems} activeItem="note-pool" pageTitle="Note Pool">
  <section class="card hero-card">
    <div class="hero-content">
      <p class="eyebrow">Undergraduate</p>
      <h2>Note Pool</h2>
      <p class="hero-copy">Upload PDF notes for a specific year and browse the shared note pool across the department.</p>
    </div>
    <button class="btn btn-primary hero-action" on:click={() => showUpload = true}>Upload Notes</button>
  </section>

  <section class="card">
    <div class="toolbar">
      <div class="toolbar-search">
        <input class="input search-input" bind:value={query} placeholder="Search by title, description, uploader, or type" />
      </div>
      <div class="toolbar-filters">
        <div class="toolbar-filter">
          <CustomSelect options={typeFilterOptions} bind:value={typeFilter} compact />
        </div>
        <div class="toolbar-filter">
          <CustomSelect options={yearFilterOptions} bind:value={yearFilter} compact />
        </div>
      </div>
    </div>

    {#if uploadSuccess}
      <div class="alert alert-success">{uploadSuccess}</div>
    {/if}
    {#if error}
      <div class="alert alert-error">{error}</div>
    {/if}

    {#if loading}
      <p class="empty-state">Loading note pool...</p>
    {:else if visibleMaterials().length === 0}
      <p class="empty-state">No notes found for the selected filters.</p>
    {:else}
      <div class="note-list">
        {#each visibleMaterials() as item}
          <article class="note-card">
            <div class="note-head">
              <div class="note-identity">
                <div class="note-icon">PDF</div>
                <div class="note-title-block">
                  <button class="note-title-btn" type="button" on:click={() => viewNote(item.materialId)}>
                    {item.title}
                  </button>
                  {#if item.description}
                    <p class="note-desc">{item.description}</p>
                  {/if}
                </div>
              </div>
              <span class="table-chip">{formatMaterialType(item.materialType)}</span>
            </div>
            <div class="note-footer">
              <div class="note-meta">
                <span class="table-chip table-chip-muted">Year {item.targetYearOfStudy || '-'}</span>
                <span class="meta-uploader">{uploaderName(item)}</span>
                <span class="meta-date">{item.uploadDate ? new Date(item.uploadDate).toLocaleDateString() : '-'}</span>
              </div>
              <div class="note-actions">
                <button class="btn btn-outline btn-sm" on:click={() => viewNote(item.materialId)}>View</button>
                <button class="btn btn-outline btn-sm" on:click={() => downloadNote(item.materialId, item.title)}>Download</button>
              </div>
            </div>
          </article>
        {/each}
      </div>
    {/if}
  </section>

  {#if showUpload}
    <div class="modal-overlay" on:click|self={() => showUpload = false}>
      <div class="modal-content">
        <h2>Upload to Note Pool</h2>
        {#if uploadError}
          <div class="alert alert-error">{uploadError}</div>
        {/if}
        <div class="form-grid">
          <div class="form-group full-width">
            <label for="note-title">Title</label>
            <input id="note-title" class="input" bind:value={form.title} placeholder="Operating Systems revision notes" />
          </div>
          <div class="form-group">
            <label for="note-type">Type</label>
            <CustomSelect id="note-type" options={materialTypeOptions} bind:value={form.materialType} />
          </div>
          <div class="form-group">
            <label for="note-year">Target Year</label>
            <CustomSelect id="note-year" options={uploadYearOptions} bind:value={form.targetYearOfStudy} />
          </div>
          <div class="form-group full-width">
            <label for="note-description">Description</label>
            <textarea id="note-description" class="input" rows="4" bind:value={form.description}></textarea>
          </div>
          <div class="form-group full-width">
            <label for="note-file">PDF File</label>
            <input id="note-file" type="file" accept="application/pdf,.pdf" on:change={(event) => form.file = event.currentTarget.files?.[0] || null} />
            <p class="helper-text">Only PDF files are allowed.</p>
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn btn-outline" on:click={() => showUpload = false}>Cancel</button>
          <button class="btn btn-primary" on:click={uploadNote}>Upload</button>
        </div>
      </div>
    </div>
  {/if}

</DashboardLayout>

<style>
  .hero-card {
    position: relative;
    overflow: hidden;
    display: grid;
    grid-template-columns: minmax(0, 1fr) auto;
    align-items: center;
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
    background: linear-gradient(to right, var(--bg-main) 40%, transparent 90%);
    pointer-events: none;
  }
  .hero-card > * { position: relative; z-index: 1; }
  .hero-content { display: grid; gap: 0.45rem; max-width: 44rem; }
  .hero-action { min-width: 11.5rem; min-height: 3.25rem; }
  .eyebrow {
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
    font-size: clamp(1.3rem, 3vw, 1.8rem);
    font-weight: 700;
    letter-spacing: -0.03em;
    color: var(--text-main);
  }
  .hero-copy { color: var(--text-secondary); max-width: 40rem; line-height: 1.55; font-size: 0.875rem; }

  .toolbar { display: grid; gap: 0.9rem; margin-bottom: 1.25rem; }
  .toolbar-search { width: 100%; }
  .toolbar-filters { display: flex; gap: 0.75rem; flex-wrap: wrap; }
  .search-input { min-width: 0; }
  .toolbar-filter { width: min(100%, 180px); }

  /* Note card list */
  .note-list { display: grid; gap: 0.75rem; }

  .note-card {
    padding: 1.1rem 1.25rem;
    border: 1px solid var(--border-light);
    border-radius: var(--radius);
    background: transparent;
    transition: border-color 0.15s ease;
  }

  .note-card:hover { border-color: var(--border-medium); }

  .note-head {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 1rem;
  }

  .note-identity {
    display: flex;
    align-items: flex-start;
    gap: 0.75rem;
    min-width: 0;
  }

  .note-icon {
    flex-shrink: 0;
    width: 40px;
    height: 40px;
    border-radius: var(--radius-sm);
    background: var(--danger-light, #FEE2E2);
    color: var(--danger, #EF4444);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 0.6rem;
    font-weight: 800;
    letter-spacing: 0.05em;
  }

  .note-title-block {
    display: flex;
    flex-direction: column;
    gap: 0.2rem;
    min-width: 0;
  }

  .note-title-btn {
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

  .note-title-btn:hover { color: var(--primary); text-decoration: underline; }

  .note-desc {
    font-size: 0.82rem;
    color: var(--text-secondary);
    line-height: 1.45;
    margin: 0;
  }

  .note-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 1rem;
    flex-wrap: wrap;
    padding-top: 0.5rem;
    border-top: 1px solid var(--border-light);
    margin-top: 0.65rem;
  }

  .note-meta {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 0.5rem;
  }

  .meta-uploader {
    font-size: 0.75rem;
    color: var(--text-secondary);
    font-weight: 500;
  }

  .meta-date {
    font-size: 0.72rem;
    color: var(--text-muted);
  }

  .note-actions { display: flex; gap: 0.5rem; flex-shrink: 0; }

  .table-chip { display: inline-flex; align-items: center; padding: 0.2rem 0.6rem; border-radius: var(--radius-sm); border: 1px solid var(--border-light); font-size: 0.75rem; color: var(--text-secondary); font-weight: 500; white-space: nowrap; }
  .table-chip-muted { background: var(--bg-main); }

  .empty-state { color: var(--text-muted); font-size: 0.875rem; }
  .alert { padding: 0.8rem 1rem; border-radius: var(--radius-sm); margin-bottom: 1rem; }
  .alert-error { background: var(--danger-light); color: #991b1b; }
  .alert-success { background: var(--success-light, #DCFCE7); color: #065f46; }
  .form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 1rem; margin-top: 1rem; }
  .full-width { grid-column: 1 / -1; }
  .form-group label { display: block; margin-bottom: 0.35rem; font-size: 0.82rem; color: var(--text-secondary); }
  .modal-actions { display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1rem; }
  .helper-text { color: var(--text-secondary); font-size: 0.8rem; margin-top: 0.25rem; }
  .btn-sm { padding: 0.375rem 0.75rem; font-size: 0.8rem; }

  @media (max-width: 900px) {
    .hero-card { grid-template-columns: 1fr; padding: 1.75rem; }
    .hero-action { width: 100%; }
  }

  @media (max-width: 640px) {
    .toolbar-filters,
    .form-grid { grid-template-columns: 1fr; }
    .toolbar-filters { display: grid; }
    .toolbar-filter { width: 100%; }
    .note-footer { flex-direction: column; align-items: flex-start; }
  }
</style>
