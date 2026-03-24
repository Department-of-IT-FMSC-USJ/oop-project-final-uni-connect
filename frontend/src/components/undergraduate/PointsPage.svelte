<script>
  import { onMount } from 'svelte';
  import { api, getCurrentUser, getRoleDashboardPath } from '../../lib/api.js';
  import { undergraduateNavItems } from '../../lib/navigation.js';
  import DashboardLayout from '../shared/DashboardLayout.svelte';

  let user = getCurrentUser();
  let records = [];
  let loading = true;
  let error = '';

  onMount(async () => {
    if (!user) { window.location.href = '/'; return; }
    if (user.role !== 'UNDERGRADUATE') { window.location.href = getRoleDashboardPath(user.role); return; }

    try {
      records = await api.get('/points/my', { cache: false });
    } catch (e) {
      error = 'Failed to load point history.';
    } finally {
      loading = false;
    }
  });
</script>

<DashboardLayout navItems={undergraduateNavItems} activeItem="points" pageTitle="Points">
  <section class="card hero-card">
    <div>
      <p class="eyebrow">Undergraduate</p>
      <h2>Your Point History</h2>
      <p class="hero-copy">Track allocated points, approval status, category, and review notes in one place.</p>
    </div>
    <div class="hero-meta">
      <span class="hero-count">{records.reduce((sum, record) => sum + (record.points || 0), 0)}</span>
      <span class="hero-label">Total Approved Points</span>
    </div>
  </section>

  <section class="card">
    {#if error}
      <div class="alert alert-error">{error}</div>
    {/if}
    {#if loading}
      <p class="empty-state">Loading point records...</p>
    {:else if records.length === 0}
      <p class="empty-state">No point records available yet.</p>
    {:else}
      <div class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>Category</th>
              <th>Points</th>
              <th>Status</th>
              <th>Allocated</th>
              <th>Review Note</th>
            </tr>
          </thead>
          <tbody>
            {#each records as record}
              <tr>
                <td>{record.category || '-'}</td>
                <td>{record.points || 0}</td>
                <td><span class="badge badge-info">{record.status || '-'}</span></td>
                <td>{record.allocatedAt || '-'}</td>
                <td>{record.reviewNote || record.note || '-'}</td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    {/if}
  </section>
</DashboardLayout>

<style>
  .hero-card { display:flex; justify-content:space-between; gap:1rem; padding:2rem; margin-bottom:1.5rem; background:linear-gradient(135deg, var(--bg-main, #FFFFFF), var(--primary-50, #EEF2FB)); border:1px solid var(--border-light, #E2E8F0); border-radius:var(--radius, 12px); }
  .eyebrow { font-size:0.75rem; text-transform:uppercase; letter-spacing:0.08em; font-weight:700; color:var(--primary, #4F7CDB); margin-bottom:0.5rem; }
  .hero-copy { color:var(--text-secondary, #475569); max-width:40rem; }
  .hero-count { display:block; font-size:2rem; font-weight:700; color:var(--primary, #4F7CDB); }
  .hero-label { color:var(--text-muted, #94A3B8); font-size:0.85rem; }
  .table-wrapper { overflow-x:auto; }
  table { width:100%; border-collapse:collapse; }
  th, td { padding:0.9rem 0.75rem; border-bottom:1px solid var(--border-light, #E2E8F0); text-align:left; }
  th { font-size:0.76rem; text-transform:uppercase; letter-spacing:0.05em; color:var(--text-muted, #94A3B8); background:var(--bg-secondary, #F1F5F9); }
  tbody tr { transition:background 0.18s ease; }
  tbody tr:hover { background:var(--primary-50, #EEF2FB); }
  .empty-state { color:var(--text-muted, #94A3B8); }
  .alert { padding:0.8rem 1rem; border-radius:var(--radius-sm, 8px); margin-bottom:1rem; }
  .alert-error { background:var(--danger-light, #FEE2E2); color:#991b1b; }
</style>
