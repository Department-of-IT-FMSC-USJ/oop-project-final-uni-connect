<script>
  import AllocationSummary from './AllocationSummary.svelte';
  import AllocationTable from './AllocationTable.svelte';

  export let mentorId = 1;
</script>

<div class="page-container">
  <div class="page-header">
    <h1>Student Allocation Dashboard</h1>
    <p>View the students automatically allocated to your mentoring care</p>
  </div>

  <AllocationTable {mentorId} />
</div>

<style>
  .page-container {
    display: flex;
    flex-direction: column;
    gap: 24px;
  }

  .page-header {
    margin-bottom: 8px;
  }

  .page-header h1 {
    font-size: 1.8rem;
    font-weight: 800;
    color: #1a1a2e;
    margin-bottom: 4px;
  }

  .page-header p {
    font-size: 0.95rem;
    color: #666;
  }
</style>
