<script>
  const API_BASE = 'http://localhost:9091';

  let mentors = [];
  let loading = true;
  let errorMessage = '';
  let successMessage = '';

  loadMentors();

  async function loadMentors() {
    loading = true;
    errorMessage = '';

    try {
      const response = await fetch(`${API_BASE}/api/mentor-verifications`, {
        headers: {
          'X-User-Role': 'HOD'
        }
      });

      const result = await response.json();

      if (response.ok && result.success) {
        mentors = result.data || [];
      } else {
        errorMessage = result.message || 'Failed to load mentor verifications.';
      }
    } catch (err) {
      errorMessage = 'Network error. Could not load mentor verifications.';
    } finally {
      loading = false;
    }
  }

  async function updateStatus(mentorId, newStatus) {
    successMessage = '';
    errorMessage = '';

    try {
      const response = await fetch(`${API_BASE}/api/mentor-verifications/${mentorId}/verify`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'X-User-Id': '1',
          'X-User-Role': 'HOD'
        },
        body: JSON.stringify({ verificationStatus: newStatus })
      });

      const result = await response.json();

      if (response.ok && result.success) {
        successMessage = `Mentor verification status updated to ${newStatus} successfully.`;
        await loadMentors();
      } else {
        errorMessage = result.message || 'Failed to update verification status.';
      }
    } catch (err) {
      errorMessage = 'Network error. Could not update verification status.';
    }
  }

  function handleStatusChange(mentorId, event) {
    const newStatus = event.target.value;
    if (newStatus) {
      updateStatus(mentorId, newStatus);
    }
  }

  function getStatusClass(status) {
    if (status === 'VERIFIED') return 'status-verified';
    if (status === 'REJECTED') return 'status-rejected';
    return 'status-pending';
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
      <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
      <circle cx="8.5" cy="7" r="4"/>
      <polyline points="17 11 19 13 23 9"/>
    </svg>
    <h2>Industry Mentor Verification</h2>
    <span class="badge">{mentors.length}</span>
  </div>

  {#if successMessage}
    <div class="alert alert-success">
      <span>{successMessage}</span>
      <button class="alert-close" on:click={() => successMessage = ''}>✕</button>
    </div>
  {/if}

  {#if loading}
    <div class="loading-state">
      <span class="spinner"></span>
      <p>Loading mentor profiles...</p>
    </div>
  {:else if errorMessage}
    <div class="alert alert-error">
      <span>{errorMessage}</span>
    </div>
  {:else if mentors.length === 0}
    <div class="empty-state">
      <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
        <circle cx="9" cy="7" r="4"/>
        <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
        <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
      </svg>
      <p>No industry mentor profiles found.</p>
    </div>
  {:else}
    <div class="table-wrapper">
      <table>
        <thead>
          <tr>
            <th>Mentor Name</th>
            <th>Organization</th>
            <th>Expertise</th>
            <th>Email</th>
            <th>Status</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {#each mentors as mentor}
            <tr>
              <td class="td-name">{mentor.mentorName}</td>
              <td>{mentor.organization}</td>
              <td class="td-expertise">{mentor.professionalExpertise}</td>
              <td><a href="mailto:{mentor.contactEmail}" class="email-link">{mentor.contactEmail}</a></td>
              <td>
                <span class="status-badge {getStatusClass(mentor.verificationStatus)}">
                  {mentor.verificationStatus}
                </span>
              </td>
              <td>
                <select
                  class="status-select"
                  value={mentor.verificationStatus}
                  on:change={(e) => handleStatusChange(mentor.mentorId, e)}
                >
                  <option value="PENDING">Pending</option>
                  <option value="VERIFIED">Verified</option>
                  <option value="REJECTED">Rejected</option>
                </select>
              </td>
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

  .alert {
    padding: 14px 18px;
    border-radius: 10px;
    font-size: 0.9rem;
    margin-bottom: 16px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .alert-success {
    background: #d4edda; color: #155724; border: 1px solid #c3e6cb;
  }

  .alert-error {
    background: #f8d7da; color: #721c24; border: 1px solid #f5c6cb;
  }

  .alert-close {
    background: none; border: none; cursor: pointer;
    font-size: 1rem; color: inherit; padding: 0 4px;
  }

  .table-wrapper {
    overflow-x: auto;
  }

  table {
    width: 100%;
    border-collapse: collapse;
    font-size: 0.9rem;
  }

  thead th {
    background: #f8f9fb;
    padding: 12px 16px;
    text-align: left;
    font-weight: 600;
    color: #555;
    border-bottom: 2px solid #e8ecf1;
    white-space: nowrap;
  }

  tbody td {
    padding: 14px 16px;
    border-bottom: 1px solid #f0f0f0;
    vertical-align: middle;
  }

  tbody tr:hover {
    background: #f8f9fb;
  }

  .td-name { font-weight: 600; color: #1a1a2e; }
  .td-expertise {
    max-width: 200px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .email-link {
    color: #4361ee;
    text-decoration: none;
  }

  .email-link:hover {
    text-decoration: underline;
  }

  .status-badge {
    padding: 4px 12px;
    border-radius: 20px;
    font-size: 0.78rem;
    font-weight: 600;
    white-space: nowrap;
  }

  .status-pending { background: #fff3cd; color: #856404; }
  .status-verified { background: #d4edda; color: #155724; }
  .status-rejected { background: #f8d7da; color: #721c24; }

  .status-select {
    padding: 6px 12px;
    border: 1.5px solid #e8ecf1;
    border-radius: 8px;
    font-size: 0.85rem;
    background: white;
    cursor: pointer;
    transition: border-color 0.2s;
  }

  .status-select:hover { border-color: #4361ee; }
  .status-select:focus { outline: none; border-color: #4361ee; box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.1); }

  @media (max-width: 768px) {
    .table-section { padding: 20px 16px; }
    thead th, tbody td { padding: 10px 12px; font-size: 0.82rem; }
  }
</style>
