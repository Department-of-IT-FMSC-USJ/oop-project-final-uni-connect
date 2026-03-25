<script>
  import { onMount } from 'svelte';
  import { api, getCurrentUser, getRoleDashboardPath } from '../../lib/api.js';
  import { academicMentorNavItems } from '../../lib/navigation.js';
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
    if (user.role !== 'ACADEMIC_MENTOR') { window.location.href = getRoleDashboardPath(user.role); return; }
    initialContactId = new URLSearchParams(window.location.search).get('contact');

    try {
      const mentorId = user.userId || user.id;
      const allocData = await api.get(`/allocations/mentor/${mentorId}`, { cache: false });
      const allocations = allocData.data || [];
      const detailedStudents = await Promise.all(
        allocations.map(async (alloc) => {
          try {
            const res = await api.get(`/users/${alloc.studentId}`, { cache: false });
            return { ...alloc, ...(res.data || {}) };
          } catch {
            return alloc;
          }
        })
      );
      students = detailedStudents;
      if (initialContactId) {
        selectedContact = students.find((student) => String(student.id || student.studentId) === initialContactId) || null;
      }
    } catch (e) {
      error = 'Failed to load assigned students.';
    } finally {
      loading = false;
    }
  });

  const filteredStudents = () => {
    const normalized = query.trim().toLowerCase();
    if (!normalized) return students;
    return students.filter((student) =>
      [student.fullName, student.email, student.department, student.registrationNumber]
        .filter(Boolean)
        .some((value) => value.toLowerCase().includes(normalized))
    );
  };
</script>

<DashboardLayout navItems={academicMentorNavItems} activeItem="students" pageTitle="Students">
  <div class="page-grid">
    <section class="card hero-card">
      <div>
        <p class="eyebrow">Academic Mentor</p>
        <h2>Your Assigned Students</h2>
        <p class="hero-copy">View allocation details, student contact information, and start a direct mentor conversation.</p>
      </div>
      <div class="hero-meta">
        <span class="hero-count">{students.length}</span>
        <span class="hero-label">Students</span>
      </div>
    </section>

    <section class="card">
      <div class="toolbar">
        <input class="input search-input" bind:value={query} placeholder="Search by name, email, department, or registration number" />
      </div>

      {#if error}
        <div class="alert alert-error">{error}</div>
      {/if}

      {#if loading}
        <p class="empty-state">Loading students...</p>
      {:else if filteredStudents().length === 0}
        <p class="empty-state">No students found.</p>
      {:else}
        <div class="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Registration</th>
                <th>Year</th>
                <th>Email</th>
                <th>Department</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {#each filteredStudents() as student}
                <tr>
                  <td><button class="link-btn" on:click={() => selectedContact = student}><strong>{student.fullName || student.studentName}</strong></button></td>
                  <td>{student.registrationNumber || '-'}</td>
                  <td>Year {student.yearOfStudy || student.academicYear || '-'}</td>
                  <td>{student.email || '-'}</td>
                  <td>{student.department || '-'}</td>
                  <td>
                    <button class="btn btn-outline btn-sm" on:click={() => selectedContact = student}>Message</button>
                  </td>
                </tr>
              {/each}
            </tbody>
          </table>
        </div>
      {/if}
    </section>
  </div>

  {#if selectedContact}
    <DirectMessageModal contact={selectedContact} onClose={() => selectedContact = null} />
  {/if}
</DashboardLayout>

<style>
  /* -- Link button -- */
  .link-btn {
    background: none;
    border: none;
    padding: 0;
    color: var(--primary, #4F7CDB);
    font-weight: 600;
    cursor: pointer;
    text-decoration: none;
    text-align: left;
  }
  .link-btn:hover {
    text-decoration: none;
    color: var(--primary-dark, #3A62B5);
  }

  /* -- Page layout -- */
  .page-grid {
    display: grid;
    gap: 1.5rem;
  }

  /* -- Hero card -- */
  .hero-card {
    display: flex;
    justify-content: space-between;
    gap: 1rem;
    padding: 2rem;
    background: linear-gradient(135deg, var(--bg-main, #FFFFFF), var(--primary-50, #EEF2FB));
    border-radius: var(--radius, 12px);
  }
  .eyebrow {
    font-size: 0.75rem;
    text-transform: uppercase;
    letter-spacing: 0.08em;
    font-weight: 700;
    color: var(--accent, #2BA89C);
    margin-bottom: 0.5rem;
  }
  .hero-copy {
    color: var(--text-secondary, #475569);
    max-width: 40rem;
  }
  .hero-count {
    display: block;
    font-size: 2rem;
    font-weight: 700;
    color: var(--primary, #4F7CDB);
  }
  .hero-label {
    color: var(--text-muted, #94A3B8);
    font-size: 0.85rem;
  }

  /* -- Toolbar & search -- */
  .toolbar {
    margin-bottom: 1rem;
  }
  .search-input {
    max-width: 480px;
  }
  .search-input:focus {
    border-color: var(--primary, #4F7CDB);
    box-shadow: 0 0 0 3px var(--primary-100, #D4DFFA);
    outline: none;
  }

  /* -- Student table -- */
  .table-wrapper {
    overflow-x: auto;
    border: 1px solid var(--border-light, #E2E8F0);
    border-radius: var(--radius, 12px);
  }
  table {
    width: 100%;
    border-collapse: collapse;
  }
  th, td {
    padding: 0.9rem 0.75rem;
    border-bottom: 1px solid var(--border-light, #E2E8F0);
    text-align: left;
  }
  th {
    font-size: 0.76rem;
    text-transform: uppercase;
    letter-spacing: 0.05em;
    color: var(--text-muted, #94A3B8);
    background: var(--bg-secondary, #F1F5F9);
    font-weight: 600;
  }
  th:first-child {
    border-radius: var(--radius, 12px) 0 0 0;
  }
  th:last-child {
    border-radius: 0 var(--radius, 12px) 0 0;
  }
  td {
    color: var(--text-secondary, #475569);
  }
  tr:last-child td {
    border-bottom: none;
  }
  tr:hover td {
    background: var(--bg-alt, #F8FAFC);
  }

  /* -- Empty state -- */
  .empty-state {
    color: var(--text-muted, #94A3B8);
    padding: 1rem 0;
  }

  /* -- Alert -- */
  .alert {
    padding: 0.8rem 1rem;
    border-radius: var(--radius-sm, 8px);
    margin-bottom: 1rem;
  }
  .alert-error {
    background: var(--danger-light, #FEE2E2);
    color: var(--danger, #EF4444);
  }

  /* -- Message button -- */
  .btn-sm {
    padding: 0.4rem 0.8rem;
    font-size: 0.8rem;
    border-radius: var(--radius-sm, 8px);
  }
</style>
