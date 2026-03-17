<script>
  const API_BASE = 'http://localhost:9090';

  export let mentorId = 1;

  let allocations = [];
  let loading = true;
  let errorMessage = '';

  import { onMount } from 'svelte';

  onMount(() => {
    loadAllocations();
  });

  async function loadAllocations() {
    loading = true;
    errorMessage = '';

    try {
      const response = await fetch(`${API_BASE}/api/allocations/mentor/${mentorId}`, {
        headers: {
          'X-User-Id': String(mentorId),
          'X-User-Role': 'MENTOR'
        }
      });

      const result = await response.json();

      if (response.ok && result.success) {
        allocations = result.data || [];
      } else {
        errorMessage = result.message || 'Failed to load allocations.';
      }
    } catch (err) {
      errorMessage = 'Network error. Could not load student allocations.';
    } finally {
      loading = false;
    }
  }

  function formatDate(dateStr) {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('en-US', {
      year: 'numeric', month: 'short', day: 'numeric'
    });
  }
</script>

<div class="table-section">
  <div class="section-header">
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
      <circle cx="9" cy="7" r="4"/>
      <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
      <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
    </svg>
    <h2>Allocated Students</h2>
    <span class="badge">{allocations.length}</span>
  </div>

  {#if loading}
    <div class="loading-state">
      <span class="spinner"></span>
      <p>Loading student allocations...</p>
    </div>
  {:else if errorMessage}
    <div class="alert alert-error">
      <span>{errorMessage}</span>
    </div>
  {:else if allocations.length === 0}
    <div class="empty-state">
      <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
        <circle cx="9" cy="7" r="4"/>
      </svg>
      <p>No students have been allocated yet. Students are assigned automatically by the system.</p>
    </div>
  {:else}
    <div class="table-wrapper">
      <table>
        <thead>
          <tr>
            <th>#</th>
            <th>Student ID</th>
            <th>Student Name</th>
            <th>Department</th>
            <th>Academic Year</th>
            <th>Assigned Date</th>
          </tr>
        </thead>
        <tbody>
          {#each allocations as allocation, i}
            <tr>
              <td class="row-num">{i + 1}</td>
              <td>
                <span class="id-badge">{allocation.studentId}</span>
              </td>
              <td>
                <strong>{allocation.studentName || 'Student ' + allocation.studentId}</strong>
              </td>
              <td>{allocation.department}</td>
              <td>{allocation.academicYear || '—'}</td>
              <td>{formatDate(allocation.allocationDate)}</td>
            </tr>
          {/each}
        </tbody>
      </table>
    </div>
  {/if}
</div>

<style>
  .table-section {
    background: #ffffff;
    border-radius: 14px;
    padding: 32px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    border: 1px solid #e8ecf1;
  }

  .section-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 24px;
    color: #1a1a2e;
  }

  .section-header svg { color: #4361ee; }
  .section-header h2 { margin: 0; font-size: 1.4rem; font-weight: 700; }

  .badge {
    background: #4361ee;
    color: white;
    padding: 2px 10px;
    border-radius: 20px;
    font-size: 0.8rem;
    font-weight: 600;
  }

  .loading-state, .empty-state {
    text-align: center;
    padding: 48px 24px;
    color: #999;
  }

  .empty-state svg { color: #d1d5db; margin-bottom: 12px; }

  .spinner {
    display: inline-block; width: 24px; height: 24px;
    border: 3px solid #e8ecf1; border-top-color: #4361ee;
    border-radius: 50%; animation: spin 0.6s linear infinite;
    margin-bottom: 12px;
  }

  @keyframes spin { to { transform: rotate(360deg); } }

  .alert-error {
    background: #f8d7da; color: #721c24; border: 1px solid #f5c6cb;
    padding: 14px 18px; border-radius: 10px; font-size: 0.9rem;
  }

  .table-wrapper {
    overflow-x: auto;
    border-radius: 10px;
    border: 1px solid #e8ecf1;
  }

  table {
    width: 100%;
    border-collapse: collapse;
    font-size: 0.88rem;
  }

  thead {
    background: #f8f9fc;
  }

  th {
    padding: 14px 16px;
    text-align: left;
    font-weight: 600;
    color: #555;
    font-size: 0.82rem;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    border-bottom: 2px solid #e8ecf1;
  }

  td {
    padding: 14px 16px;
    border-bottom: 1px solid #f0f0f0;
    color: #333;
  }

  tr:last-child td {
    border-bottom: none;
  }

  tr:hover {
    background: #fafbff;
  }

  .row-num {
    color: #999;
    font-weight: 500;
    width: 40px;
  }

  .id-badge {
    background: #e3f2fd;
    color: #1565c0;
    padding: 3px 10px;
    border-radius: 6px;
    font-size: 0.82rem;
    font-weight: 600;
  }

  @media (max-width: 600px) {
    .table-section { padding: 20px 16px; }
  }
</style>
