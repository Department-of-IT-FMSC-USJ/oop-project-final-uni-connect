<script>
  import ContactInfoCard from './ContactInfoCard.svelte';

  export let mentorId = 1;
</script>

<div class="page-container">
  <div class="page-header">
    <h1>Mentor Contact Details</h1>
    <p>View your assigned mentor's contact information for academic guidance</p>
  </div>

  <ContactInfoCard {mentorId} />
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
