<script>
  import { onMount } from 'svelte';
  import { api, downloadWithAuth, getCurrentUser, getRoleDashboardPath } from '../../lib/api.js';
  import { undergraduateNavItems } from '../../lib/navigation.js';
  import DashboardLayout from '../shared/DashboardLayout.svelte';
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
        <select class="input compact" bind:value={typeFilter}>
          <option value="">All types</option>
          <option value="NOTES">Notes</option>
          <option value="PAST_PAPERS">Past Papers</option>
          <option value="LECTURE_SLIDES">Lecture Slides</option>
          <option value="OTHER">Other</option>
        </select>
        <select class="input compact" bind:value={yearFilter}>
          <option value="">All years</option>
          {#each yearOptions as year}
            <option value={year}>Year {year}</option>
          {/each}
        </select>
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
      <div class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>Title</th>
              <th>Type</th>
              <th>Year</th>
              <th>Uploaded By</th>
              <th>Uploaded At</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {#each visibleMaterials() as item}
              <tr>
                <td>
                  <div class="material-title">
                    <button class="note-link" type="button" on:click={() => viewNote(item.materialId)}>
                      {item.title}
                    </button>
                    <div class="muted material-description">{item.description || 'No description provided.'}</div>
                  </div>
                </td>
                <td><span class="table-chip">{formatMaterialType(item.materialType)}</span></td>
                <td><span class="table-chip table-chip-muted">Year {item.targetYearOfStudy || '-'}</span></td>
                <td>{uploaderName(item)}</td>
                <td>{item.uploadDate ? new Date(item.uploadDate).toLocaleString() : '-'}</td>
                <td class="table-action">
                  <div class="action-row table-action-row">
                    <button class="btn btn-outline btn-sm" on:click={() => viewNote(item.materialId)}>View</button>
                    <button class="btn btn-outline btn-sm" on:click={() => downloadNote(item.materialId, item.title)}>Download</button>
                  </div>
                </td>
              </tr>
            {/each}
          </tbody>
        </table>
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
            <select id="note-type" class="input" bind:value={form.materialType}>
              <option value="NOTES">Notes</option>
              <option value="PAST_PAPERS">Past Papers</option>
              <option value="LECTURE_SLIDES">Lecture Slides</option>
              <option value="OTHER">Other</option>
            </select>
          </div>
          <div class="form-group">
            <label for="note-year">Target Year</label>
            <select id="note-year" class="input" bind:value={form.targetYearOfStudy}>
              <option value="">Select year</option>
              {#each yearOptions as year}
                <option value={year}>Year {year}</option>
              {/each}
            </select>
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
  .hero-card { display:grid; grid-template-columns:minmax(0, 1fr) auto; align-items:center; gap:1.5rem; padding:2.25rem 2.5rem; margin-bottom:1.5rem; background:linear-gradient(135deg, var(--bg-main, #FFFFFF), var(--primary-50, #EEF2FB)); border:1px solid var(--border-light, #E2E8F0); border-radius:var(--radius, 12px); }
  .hero-content { display:grid; gap:0.45rem; max-width:44rem; }
  .hero-action { min-width: 11.5rem; min-height: 3.25rem; }
  .eyebrow { font-size:0.75rem; text-transform:uppercase; letter-spacing:0.08em; font-weight:700; color:var(--primary, #4F7CDB); margin-bottom:0.5rem; }
  .hero-copy,.muted,.helper-text { color:var(--text-secondary, #475569); }
  .hero-copy { max-width: 40rem; line-height: 1.55; }
  .toolbar { display:grid; gap:0.9rem; margin-bottom:1.25rem; }
  .toolbar-search { width: 100%; }
  .toolbar-filters { display:flex; gap:0.75rem; flex-wrap:wrap; }
  .search-input { min-width:0; transition:border-color 0.2s ease, box-shadow 0.2s ease; }
  .search-input:focus { outline:none; border-color:var(--primary, #4F7CDB); box-shadow:0 0 0 3px var(--primary-100, #D4DFFA); }
  .compact { width: min(100%, 180px); transition:border-color 0.2s ease, box-shadow 0.2s ease; }
  .compact:focus { outline:none; border-color:var(--primary, #4F7CDB); box-shadow:0 0 0 3px var(--primary-100, #D4DFFA); }
  .table-wrapper { overflow-x:auto; }
  table { width:100%; border-collapse:collapse; }
  th, td { padding:1rem 0.75rem; border-bottom:1px solid var(--border-light, #E2E8F0); text-align:left; vertical-align:top; }
  th { font-size:0.76rem; text-transform:uppercase; letter-spacing:0.05em; color:var(--text-muted, #94A3B8); background:var(--bg-secondary, #F1F5F9); }
  tbody tr { transition: background 0.18s ease; }
  tbody tr:hover { background: var(--primary-50, #EEF2FB); }
  th:last-child, td:last-child { text-align:right; white-space:nowrap; }
  .empty-state { color:var(--text-muted, #94A3B8); }
  .alert { padding:0.8rem 1rem; border-radius:var(--radius-sm, 8px); margin-bottom:1rem; }
  .alert-error { background:var(--danger-light, #FEE2E2); color:#991b1b; }
  .alert-success { background:var(--success-light, #DCFCE7); color:#065f46; }
  .form-grid { display:grid; grid-template-columns:repeat(2,minmax(0,1fr)); gap:1rem; margin-top:1rem; }
  .full-width { grid-column:1 / -1; }
  .form-group label { display:block; margin-bottom:0.35rem; font-size:0.82rem; color:var(--text-secondary, #475569); }
  .modal-actions { display:flex; justify-content:flex-end; gap:0.75rem; margin-top:1rem; }
  .action-row { display:flex; align-items:center; gap:0.55rem; flex-wrap:wrap; }
  .table-action-row { justify-content:flex-end; }
  .material-title { display:grid; gap:0.3rem; max-width:20rem; }
  .material-description { font-size:0.94rem; line-height:1.45; }
  .note-link { padding:0; background:none; border:none; color:var(--text-main, #1E293B); font-size:1rem; font-weight:700; text-align:left; transition: color 0.18s ease; }
  .note-link:hover { color:var(--primary, #4F7CDB); text-decoration:underline; text-underline-offset:0.16rem; }
  .table-chip { display:inline-flex; align-items:center; padding:0.34rem 0.68rem; border-radius:999px; border:1px solid var(--border-light, #E2E8F0); background:var(--bg-secondary, #F1F5F9); color:var(--text-secondary, #475569); font-size:0.78rem; font-weight:600; white-space:nowrap; }
  .table-chip-muted { background:var(--bg-main, #FFFFFF); color:var(--text-secondary, #475569); }
  .table-action { width:1%; }

  @media (max-width: 900px) {
    .hero-card { grid-template-columns:1fr; padding:1.75rem; }
    .hero-action { width:100%; }
  }

  @media (max-width: 640px) {
    .toolbar-filters,
    .form-grid { grid-template-columns:1fr; }
    .toolbar-filters { display:grid; }
    .compact { width:100%; }
    th:last-child, td:last-child { text-align:left; }
    .table-action-row { justify-content:flex-start; }
  }
</style>
