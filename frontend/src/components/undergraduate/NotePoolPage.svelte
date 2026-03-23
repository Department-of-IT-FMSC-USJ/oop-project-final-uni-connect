<script>
  import { onMount } from 'svelte';
  import { api, downloadWithAuth, getCurrentUser, getRoleDashboardPath } from '../../lib/api.js';
  import { undergraduateNavItems } from '../../lib/navigation.js';
  import DashboardLayout from '../shared/DashboardLayout.svelte';
  import ConfirmDialog from '../shared/ConfirmDialog.svelte';
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
  let pendingDeleteMaterial = null;
  let deletingMaterial = false;
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

  function requestDeleteMaterial(item) {
    pendingDeleteMaterial = item;
  }

  async function deleteMaterial() {
    if (!pendingDeleteMaterial) return;
    deletingMaterial = true;
    try {
      await api.delete(`/materials/${pendingDeleteMaterial.materialId}`);
      toast.success({ title: 'Note deleted', message: 'Your note was removed from the archive and note pool.' });
      pendingDeleteMaterial = null;
      await loadMaterials();
    } catch (e) {
      const message = e?.data?.message || 'Failed to delete note.';
      toast.error({ title: 'Delete failed', message });
    } finally {
      deletingMaterial = false;
    }
  }

  function myUploads() {
    const currentId = user?.userId || user?.id;
    return materials.filter((item) => String(item.uploadedBy) === String(currentId));
  }

  const yearOptions = ['1', '2', '3', '4'];
</script>

<DashboardLayout navItems={undergraduateNavItems} activeItem="note-pool" pageTitle="Note Pool">
  <section class="card hero-card">
    <div>
      <p class="eyebrow">Undergraduate</p>
      <h2>Note Pool</h2>
      <p class="hero-copy">Upload PDF notes for a specific year and browse the shared note pool across the department.</p>
    </div>
    <button class="btn btn-primary" on:click={() => showUpload = true}>Upload Notes</button>
  </section>

  <section class="card">
    <div class="toolbar">
      <input class="input search-input" bind:value={query} placeholder="Search by title, description, uploader, or type" />
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
                  <strong>{item.title}</strong>
                  <div class="muted">{item.description || 'No description provided.'}</div>
                </td>
                <td>{item.materialType}</td>
                <td>Year {item.targetYearOfStudy || '-'}</td>
                <td>{uploaderName(item)}</td>
                <td>{item.uploadDate ? new Date(item.uploadDate).toLocaleString() : '-'}</td>
                <td><button class="btn btn-outline btn-sm" on:click={() => downloadNote(item.materialId, item.title)}>Download PDF</button></td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    {/if}
  </section>

  <section class="card">
    <div class="section-head">
      <div>
        <p class="eyebrow">Archive</p>
        <h3>Your Uploaded Notes</h3>
      </div>
      <span class="archive-count">{myUploads().length}</span>
    </div>

    {#if myUploads().length === 0}
      <p class="empty-state">You have not uploaded notes yet.</p>
    {:else}
      <div class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>Title</th>
              <th>Type</th>
              <th>Year</th>
              <th>Uploaded At</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {#each myUploads() as item}
              <tr>
                <td><strong>{item.title}</strong></td>
                <td>{item.materialType}</td>
                <td>Year {item.targetYearOfStudy || '-'}</td>
                <td>{item.uploadDate ? new Date(item.uploadDate).toLocaleString() : '-'}</td>
                <td>
                  <div class="action-row">
                    <button class="btn btn-outline btn-sm" on:click={() => downloadNote(item.materialId, item.title)}>Download</button>
                    <button class="btn btn-danger btn-sm" on:click={() => requestDeleteMaterial(item)}>Delete</button>
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

  <ConfirmDialog
    open={!!pendingDeleteMaterial}
    title="Delete uploaded note?"
    message={`Delete "${pendingDeleteMaterial?.title || 'this note'}" from your archive and the shared note pool?`}
    confirmLabel="Delete note"
    tone="danger"
    busy={deletingMaterial}
    on:cancel={() => pendingDeleteMaterial = null}
    on:confirm={deleteMaterial}
  />
</DashboardLayout>

<style>
  .hero-card { display:flex; justify-content:space-between; gap:1rem; padding:2rem; margin-bottom:1.5rem; background:linear-gradient(135deg,#fff,#f6fbff); }
  .eyebrow { font-size:0.75rem; text-transform:uppercase; letter-spacing:0.08em; font-weight:700; color:var(--accent); margin-bottom:0.5rem; }
  .hero-copy,.muted,.helper-text { color:var(--gray-600); }
  .toolbar { display:flex; gap:0.75rem; flex-wrap:wrap; margin-bottom:1rem; }
  .search-input { min-width:min(100%, 320px); }
  .compact { width: 180px; }
  .table-wrapper { overflow-x:auto; }
  table { width:100%; border-collapse:collapse; }
  th, td { padding:0.9rem 0.75rem; border-bottom:1px solid var(--gray-200); text-align:left; vertical-align:top; }
  th { font-size:0.76rem; text-transform:uppercase; letter-spacing:0.05em; color:var(--gray-500); }
  .empty-state { color:var(--gray-500); }
  .alert { padding:0.8rem 1rem; border-radius:var(--radius); margin-bottom:1rem; }
  .alert-error { background:#fee2e2; color:#991b1b; }
  .alert-success { background:#d1fae5; color:#065f46; }
  .form-grid { display:grid; grid-template-columns:repeat(2,minmax(0,1fr)); gap:1rem; margin-top:1rem; }
  .full-width { grid-column:1 / -1; }
  .form-group label { display:block; margin-bottom:0.35rem; font-size:0.82rem; color:var(--gray-600); }
  .modal-actions { display:flex; justify-content:flex-end; gap:0.75rem; margin-top:1rem; }
  .section-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:0.75rem; }
  .archive-count { font-size:1.4rem; font-weight:700; color:var(--primary); }
  .action-row { display:flex; align-items:center; gap:0.55rem; flex-wrap:wrap; }
</style>
