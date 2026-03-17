<script>
  export let allocations = [];

  $: totalStudents = allocations.length;
  $: departments = [...new Set(allocations.map(a => a.department).filter(Boolean))];
</script>

<div class="summary-section">
  <div class="summary-cards">
    <div class="summary-card">
      <span class="summary-icon">👥</span>
      <div class="summary-info">
        <span class="summary-value">{totalStudents}</span>
        <span class="summary-label">Total Students</span>
      </div>
    </div>

    <div class="summary-card">
      <span class="summary-icon">🏛️</span>
      <div class="summary-info">
        <span class="summary-value">{departments.length}</span>
        <span class="summary-label">Departments</span>
      </div>
    </div>

    <div class="summary-card">
      <span class="summary-icon">📊</span>
      <div class="summary-info">
        <span class="summary-value">{10 - totalStudents}</span>
        <span class="summary-label">Capacity Remaining</span>
      </div>
    </div>
  </div>

  {#if departments.length > 0}
    <div class="dept-tags">
      <span class="dept-label">Departments:</span>
      {#each departments as dept}
        <span class="dept-tag">{dept}</span>
      {/each}
    </div>
  {/if}
</div>

<style>
  .summary-section {
    background: #ffffff;
    border-radius: 14px;
    padding: 24px 32px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    border: 1px solid #e8ecf1;
  }

  .summary-cards {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
  }

  .summary-card {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 16px;
    background: #f8f9fc;
    border-radius: 12px;
    border: 1px solid #e8ecf1;
  }

  .summary-icon {
    font-size: 1.8rem;
  }

  .summary-info {
    display: flex;
    flex-direction: column;
  }

  .summary-value {
    font-size: 1.5rem;
    font-weight: 800;
    color: #1a1a2e;
  }

  .summary-label {
    font-size: 0.78rem;
    color: #888;
    font-weight: 500;
  }

  .dept-tags {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: 20px;
    flex-wrap: wrap;
  }

  .dept-label {
    font-size: 0.85rem;
    color: #666;
    font-weight: 600;
  }

  .dept-tag {
    background: #e8f5e9;
    color: #2e7d32;
    padding: 4px 12px;
    border-radius: 20px;
    font-size: 0.78rem;
    font-weight: 600;
  }

  @media (max-width: 600px) {
    .summary-cards { grid-template-columns: 1fr; }
    .summary-section { padding: 20px 16px; }
  }
</style>
