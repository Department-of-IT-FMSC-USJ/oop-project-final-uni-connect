<script>
  import { onMount } from 'svelte';

  const API_BASE = 'http://localhost:8080';

  export let isLoggedIn = false;
  export let userId = null;
  export let userRole = '';
  export let refreshTrigger = 0;

  let materials = [];
  let loading = true;
  let errorMessage = '';

  async function fetchMaterials() {
    loading = true;
    errorMessage = '';
    try {
      const response = await fetch(`${API_BASE}/api/materials`);
      const result = await response.json();
      if (result.success) {
        materials = result.data || [];
      } else {
        errorMessage = result.message || 'Failed to load materials.';
      }
    } catch (err) {
      errorMessage = 'Unable to connect to server. Please try again later.';
    } finally {
      loading = false;
    }
  }

  onMount(() => {
    fetchMaterials();
  });

  $: if (refreshTrigger > 0) {
    fetchMaterials();
  }

  async function downloadMaterial(materialId, title) {
    try {
      const response = await fetch(`${API_BASE}/api/materials/download/${materialId}`, {
        headers: {
          'X-User-Id': String(userId),
          'X-User-Role': userRole
        }
      });

      if (!response.ok) {
        if (response.status === 401) {
          alert('Please login to download materials.');
          return;
        }
        alert('Download failed. Please try again.');
        return;
      }

      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;

      const disposition = response.headers.get('Content-Disposition');
      let filename = `${title}.pdf`;
      if (disposition) {
        const match = disposition.match(/filename="?(.+?)"?$/);
        if (match) filename = match[1];
      }

      link.download = filename;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      alert('Network error during download. Please try again.');
    }
  }

  function formatDate(dateStr) {
    if (!dateStr) return 'N/A';
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  function getMaterialTypeLabel(type) {
    const labels = {
      'NOTES': 'Notes',
      'PAST_PAPERS': 'Past Papers',
      'LECTURE_SLIDES': 'Lecture Slides',
      'OTHER': 'Other'
    };
    return labels[type] || type;
  }

  function getMaterialTypeIcon(type) {
    switch (type) {
      case 'NOTES': return '📝';
      case 'PAST_PAPERS': return '📄';
      case 'LECTURE_SLIDES': return '📊';
      default: return '📁';
    }
  }

  function getMaterialTypeBadgeClass(type) {
    switch (type) {
      case 'NOTES': return 'badge-notes';
      case 'PAST_PAPERS': return 'badge-papers';
      case 'LECTURE_SLIDES': return 'badge-slides';
      default: return 'badge-other';
    }
  }

  let deletingId = null;

  async function deleteMaterial(materialId, title) {
    if (!confirm(`Are you sure you want to delete "${title}"? This action cannot be undone.`)) {
      return;
    }

    deletingId = materialId;
    try {
      const response = await fetch(`${API_BASE}/api/materials/${materialId}`, {
        method: 'DELETE',
        headers: {
          'X-User-Id': String(userId),
          'X-User-Role': userRole
        }
      });

      const result = await response.json();

      if (response.ok && result.success) {
        materials = materials.filter(m => m.materialId !== materialId);
      } else if (response.status === 401 || response.status === 403) {
        alert('You can only delete materials that you uploaded.');
      } else {
        alert(result.message || 'Failed to delete material. Please try again.');
      }
    } catch (err) {
      alert('Network error. Please try again.');
    } finally {
      deletingId = null;
    }
  }
</script>

<div class="materials-section">
  <div class="section-header">
    <div class="header-left">
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
        <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
      </svg>
      <h2>Study Materials</h2>
    </div>
    <button class="btn-refresh" on:click={fetchMaterials} title="Refresh list">
      <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="23 4 23 10 17 10"/>
        <polyline points="1 20 1 14 7 14"/>
        <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
      </svg>
    </button>
  </div>

  {#if loading}
    <div class="loading-state">
      <div class="loading-spinner"></div>
      <p>Loading study materials...</p>
    </div>
  {:else if errorMessage}
    <div class="error-state">
      <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10"/>
        <line x1="12" y1="8" x2="12" y2="12"/>
        <line x1="12" y1="16" x2="12.01" y2="16"/>
      </svg>
      <p>{errorMessage}</p>
      <button class="btn-retry" on:click={fetchMaterials}>Try Again</button>
    </div>
  {:else if materials.length === 0}
    <div class="empty-state">
      <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
        <path d="M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z"/>
        <polyline points="13 2 13 9 20 9"/>
      </svg>
      <p class="empty-title">No Materials Yet</p>
      <p class="empty-subtitle">Be the first to share study materials with your peers!</p>
    </div>
  {:else}
    <div class="materials-grid">
      {#each materials as material (material.materialId)}
        <div class="material-card">
          <div class="card-header">
            <span class="material-type-badge {getMaterialTypeBadgeClass(material.materialType)}">
              {getMaterialTypeIcon(material.materialType)} {getMaterialTypeLabel(material.materialType)}
            </span>
          </div>
          <div class="card-body">
            <h3 class="material-title">{material.title}</h3>
            {#if material.description}
              <p class="material-description">{material.description}</p>
            {:else}
              <p class="material-description no-desc">No description provided</p>
            {/if}
          </div>
          <div class="card-footer">
            <div class="upload-info">
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                <line x1="16" y1="2" x2="16" y2="6"/>
                <line x1="8" y1="2" x2="8" y2="6"/>
                <line x1="3" y1="10" x2="21" y2="10"/>
              </svg>
              <span>{formatDate(material.uploadDate)}</span>
            </div>
            <div class="card-actions">
              {#if isLoggedIn}
                <button
                  class="btn-download"
                  on:click={() => downloadMaterial(material.materialId, material.title)}
                >
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                    <polyline points="7 10 12 15 17 10"/>
                    <line x1="12" y1="15" x2="12" y2="3"/>
                  </svg>
                  Download
                </button>
                {#if material.uploadedBy === userId}
                  <button
                    class="btn-delete"
                    on:click={() => deleteMaterial(material.materialId, material.title)}
                    disabled={deletingId === material.materialId}
                  >
                    {#if deletingId === material.materialId}
                      <span class="delete-spinner"></span>
                    {:else}
                      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <polyline points="3 6 5 6 21 6"/>
                        <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                      </svg>
                    {/if}
                    Delete
                  </button>
                {/if}
              {:else}
                <span class="login-hint">Login to download</span>
              {/if}
            </div>
          </div>
        </div>
      {/each}
    </div>
  {/if}
</div>

<style>
  .materials-section {
    background: #ffffff;
    border-radius: 14px;
    padding: 32px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    border: 1px solid #e8ecf1;
  }

  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 24px;
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: 10px;
    color: #1a1a2e;
  }

  .header-left h2 {
    margin: 0;
    font-size: 1.4rem;
    font-weight: 700;
  }

  .header-left svg {
    color: #4361ee;
  }

  .btn-refresh {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 38px;
    height: 38px;
    border: 1.5px solid #d1d5db;
    border-radius: 8px;
    background: #fafbfc;
    color: #555;
    cursor: pointer;
    transition: all 0.2s;
  }

  .btn-refresh:hover {
    border-color: #4361ee;
    color: #4361ee;
    background: #f0f4ff;
  }

  .loading-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 40px 20px;
    gap: 12px;
    color: #888;
  }

  .loading-spinner {
    width: 36px;
    height: 36px;
    border: 3px solid #e8ecf1;
    border-top-color: #4361ee;
    border-radius: 50%;
    animation: spin 0.7s linear infinite;
  }

  @keyframes spin {
    to { transform: rotate(360deg); }
  }

  .error-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 40px 20px;
    gap: 12px;
    color: #dc3545;
    text-align: center;
  }

  .btn-retry {
    padding: 8px 20px;
    border: 1.5px solid #dc3545;
    background: transparent;
    color: #dc3545;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
  }

  .btn-retry:hover {
    background: #dc3545;
    color: #fff;
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 48px 20px;
    gap: 8px;
    color: #aaa;
    text-align: center;
  }

  .empty-title {
    font-size: 1.1rem;
    font-weight: 600;
    color: #666;
    margin: 8px 0 0;
  }

  .empty-subtitle {
    font-size: 0.9rem;
    color: #999;
    margin: 0;
  }

  .materials-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 20px;
  }

  .material-card {
    display: flex;
    flex-direction: column;
    border: 1.5px solid #e8ecf1;
    border-radius: 12px;
    overflow: hidden;
    background: #fafbfc;
    transition: transform 0.2s, box-shadow 0.2s;
  }

  .material-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
  }

  .card-header {
    padding: 14px 18px 0;
  }

  .material-type-badge {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px 12px;
    border-radius: 20px;
    font-size: 0.78rem;
    font-weight: 600;
    letter-spacing: 0.01em;
  }

  .badge-notes {
    background: #e0f2fe;
    color: #0369a1;
  }

  .badge-papers {
    background: #fef3c7;
    color: #92400e;
  }

  .badge-slides {
    background: #ede9fe;
    color: #6d28d9;
  }

  .badge-other {
    background: #f1f5f9;
    color: #475569;
  }

  .card-body {
    padding: 14px 18px;
    flex: 1;
  }

  .material-title {
    margin: 0 0 8px;
    font-size: 1.05rem;
    font-weight: 700;
    color: #1a1a2e;
    line-height: 1.3;
  }

  .material-description {
    margin: 0;
    font-size: 0.88rem;
    color: #555;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .no-desc {
    color: #bbb;
    font-style: italic;
  }

  .card-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 18px;
    border-top: 1px solid #e8ecf1;
    background: #fff;
    flex-wrap: wrap;
    gap: 8px;
  }

  .card-actions {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .upload-info {
    display: flex;
    align-items: center;
    gap: 5px;
    font-size: 0.8rem;
    color: #888;
  }

  .btn-download {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 7px 16px;
    background: #4361ee;
    color: #fff;
    border: none;
    border-radius: 8px;
    font-size: 0.82rem;
    font-weight: 600;
    cursor: pointer;
    transition: background 0.2s, transform 0.1s;
  }

  .btn-download:hover {
    background: #3651d4;
    transform: translateY(-1px);
  }

  .btn-delete {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 7px 14px;
    background: transparent;
    color: #dc3545;
    border: 1.5px solid #dc3545;
    border-radius: 8px;
    font-size: 0.82rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
  }

  .btn-delete:hover:not(:disabled) {
    background: #dc3545;
    color: #fff;
    transform: translateY(-1px);
  }

  .btn-delete:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }

  .delete-spinner {
    display: inline-block;
    width: 14px;
    height: 14px;
    border: 2px solid rgba(220, 53, 69, 0.3);
    border-top-color: #dc3545;
    border-radius: 50%;
    animation: spin 0.6s linear infinite;
  }

  .login-hint {
    font-size: 0.8rem;
    color: #aaa;
    font-style: italic;
  }

  @media (max-width: 600px) {
    .materials-section {
      padding: 20px 16px;
    }

    .materials-grid {
      grid-template-columns: 1fr;
    }

    .section-header h2 {
      font-size: 1.15rem;
    }
  }
</style>
