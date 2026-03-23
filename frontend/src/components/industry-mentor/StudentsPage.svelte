<script>
  import { onMount } from 'svelte';
  import { api, getCurrentUser, getRoleDashboardPath } from '../../lib/api.js';
  import { industryMentorNavItems } from '../../lib/navigation.js';
  import DashboardLayout from '../shared/DashboardLayout.svelte';
  import DirectMessageModal from '../shared/DirectMessageModal.svelte';

  let user = getCurrentUser();
  let students = [];
  let loading = true;
  let error = '';
  let query = '';
  let selectedContact = null;
  let initialContactId = null;

  onMount(async () => {
    if (!user) { window.location.href = '/'; return; }
    if (user.role !== 'INDUSTRY_MENTOR') { window.location.href = getRoleDashboardPath(user.role); return; }
    initialContactId = new URLSearchParams(window.location.search).get('contact');

    try {
      const connectionsRes = await api.get(`/mentor/mentor/${user.userId || user.id}`, { cache: false });
      const connections = (connectionsRes.data || []).filter((entry) => entry.connectionStatus === 'Approved');
      students = await Promise.all(connections.map(async (entry) => {
        const res = await api.get(`/users/${entry.studentId}`, { cache: false });
        return res.data || entry;
      }));
      if (initialContactId) {
        selectedContact = students.find((student) => String(student.id || student.studentId) === initialContactId) || null;
      }
    } catch (e) {
      error = 'Failed to load connected students.';
    } finally {
      loading = false;
    }
  });

  const filteredStudents = () => {
    const normalized = query.trim().toLowerCase();
    if (!normalized) return students;
    return students.filter((student) =>
      [student.fullName, student.registrationNumber, student.email, student.department, student.yearOfStudy]
        .filter(Boolean)
        .some((value) => value.toString().toLowerCase().includes(normalized))
    );
  };
</script>

<DashboardLayout navItems={industryMentorNavItems} activeItem="students" pageTitle="Students">
  <section class="card hero-card">
    <div>
      <p class="eyebrow">Industry Mentor</p>
      <h2>Connected Students</h2>
      <p class="hero-copy">These students can view your mentor-created sessions and exchange direct messages with you.</p>
    </div>
    <div class="hero-meta">
      <span class="hero-count">{students.length}</span>
      <span class="hero-label">Students</span>
    </div>
  </section>

  <section class="card">
    <div class="toolbar">
      <input class="input search-input" bind:value={query} placeholder="Search by name, registration, email, department, or year" />
    </div>
    {#if error}
      <div class="alert alert-error">{error}</div>
    {/if}
    {#if loading}
      <p class="empty-state">Loading students...</p>
    {:else if filteredStudents().length === 0}
      <p class="empty-state">No connected students yet.</p>
    {:else}
      <div class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Registration</th>
              <th>Email</th>
              <th>Department</th>
              <th>Year</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {#each filteredStudents() as student}
              <tr>
                <td><strong>{student.fullName}</strong></td>
                <td>{student.registrationNumber || '-'}</td>
                <td>{student.email || '-'}</td>
                <td>{student.department || '-'}</td>
                <td>Year {student.yearOfStudy || '-'}</td>
                <td><button class="btn btn-outline btn-sm" on:click={() => selectedContact = student}>Message</button></td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    {/if}
  </section>

  {#if selectedContact}
    <DirectMessageModal contact={selectedContact} onClose={() => selectedContact = null} />
  {/if}
</DashboardLayout>

<style>
  .hero-card { display:flex; justify-content:space-between; gap:1rem; padding:2rem; margin-bottom:1.5rem; background:linear-gradient(135deg,#fff,#f9fbff); }
  .eyebrow { font-size:0.75rem; text-transform:uppercase; letter-spacing:0.08em; font-weight:700; color:var(--accent); margin-bottom:0.5rem; }
  .hero-copy { color:var(--gray-600); max-width:40rem; }
  .hero-count { display:block; font-size:2rem; font-weight:700; color:var(--primary); }
  .hero-label { color:var(--gray-500); font-size:0.85rem; }
  .toolbar { margin-bottom:1rem; }
  .search-input { max-width:480px; }
  .table-wrapper { overflow-x:auto; }
  table { width:100%; border-collapse:collapse; }
  th, td { padding:0.9rem 0.75rem; border-bottom:1px solid var(--gray-200); text-align:left; }
  th { font-size:0.76rem; text-transform:uppercase; letter-spacing:0.05em; color:var(--gray-500); }
  .empty-state { color:var(--gray-500); padding:1rem 0; }
  .alert { padding:0.8rem 1rem; border-radius:var(--radius); margin-bottom:1rem; }
  .alert-error { background:#fee2e2; color:#991b1b; }
  .btn-sm { padding:0.4rem 0.8rem; font-size:0.8rem; }
</style>
