<script>
  const API_BASE = 'http://localhost:9090';

  export let mentorId = 1;

  let contactInfo = null;
  let loading = true;
  let errorMessage = '';

  import { onMount } from 'svelte';

  onMount(() => {
    loadContactInfo();
  });

  async function loadContactInfo() {
    loading = true;
    errorMessage = '';

    try {
      const response = await fetch(`${API_BASE}/api/contact-info/mentor/${mentorId}`, {
        headers: {
          'X-User-Id': String(mentorId),
          'X-User-Role': 'STUDENT'
        }
      });

      const result = await response.json();

      if (response.ok && result.success) {
        contactInfo = result.data;
      } else if (response.status === 404) {
        contactInfo = null;
      } else {
        errorMessage = result.message || 'Failed to load contact information.';
      }
    } catch (err) {
      errorMessage = 'Network error. Could not load contact information.';
    } finally {
      loading = false;
    }
  }
</script>

<div class="contact-card">
  <div class="section-header">
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
      <circle cx="12" cy="7" r="4"/>
    </svg>
    <h2>Mentor Contact Information</h2>
  </div>

  {#if loading}
    <div class="loading-state">
      <span class="spinner"></span>
      <p>Loading contact information...</p>
    </div>
  {:else if errorMessage}
    <div class="alert alert-error">
      <span>{errorMessage}</span>
    </div>
  {:else if !contactInfo}
    <div class="empty-state">
      <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
        <circle cx="12" cy="7" r="4"/>
      </svg>
      <p>No contact information available for this mentor.</p>
    </div>
  {:else}
    <div class="contact-details">
      <div class="mentor-avatar">
        <span>{contactInfo.mentorName ? contactInfo.mentorName.charAt(0).toUpperCase() : 'M'}</span>
      </div>

      <h3 class="mentor-name">{contactInfo.mentorName}</h3>

      <div class="details-grid">
        <div class="detail-item">
          <div class="detail-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
              <polyline points="22,6 12,13 2,6"/>
            </svg>
          </div>
          <div class="detail-content">
            <span class="detail-label">Email Address</span>
            <span class="detail-value">{contactInfo.email}</span>
          </div>
        </div>

        <div class="detail-item">
          <div class="detail-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/>
            </svg>
          </div>
          <div class="detail-content">
            <span class="detail-label">Phone Number</span>
            <span class="detail-value">{contactInfo.phone || 'Not provided'}</span>
          </div>
        </div>

        <div class="detail-item">
          <div class="detail-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
              <circle cx="12" cy="10" r="3"/>
            </svg>
          </div>
          <div class="detail-content">
            <span class="detail-label">Office Location</span>
            <span class="detail-value">{contactInfo.officeLocation || 'Not provided'}</span>
          </div>
        </div>

        <div class="detail-item">
          <div class="detail-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12 6 12 12 16 14"/>
            </svg>
          </div>
          <div class="detail-content">
            <span class="detail-label">Office Hours</span>
            <span class="detail-value">{contactInfo.officeHours || 'Not provided'}</span>
          </div>
        </div>
      </div>
    </div>
  {/if}
</div>

<style>
  .contact-card {
    background: #ffffff;
    border-radius: 14px;
    padding: 32px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    border: 1px solid #e8ecf1;
    max-width: 600px;
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

  .contact-details {
    text-align: center;
  }

  .mentor-avatar {
    width: 72px;
    height: 72px;
    border-radius: 50%;
    background: linear-gradient(135deg, #4361ee, #7c3aed);
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 16px;
  }

  .mentor-avatar span {
    font-size: 1.8rem;
    font-weight: 800;
    color: white;
  }

  .mentor-name {
    font-size: 1.3rem;
    font-weight: 700;
    color: #1a1a2e;
    margin-bottom: 24px;
  }

  .details-grid {
    display: flex;
    flex-direction: column;
    gap: 16px;
    text-align: left;
  }

  .detail-item {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 14px 16px;
    background: #f8f9fc;
    border-radius: 10px;
    border: 1px solid #e8ecf1;
  }

  .detail-icon {
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #eef2ff;
    border-radius: 8px;
    flex-shrink: 0;
  }

  .detail-icon svg { color: #4361ee; }

  .detail-content {
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  .detail-label {
    font-size: 0.75rem;
    color: #999;
    font-weight: 500;
    text-transform: uppercase;
    letter-spacing: 0.3px;
  }

  .detail-value {
    font-size: 0.9rem;
    color: #333;
    font-weight: 500;
  }

  @media (max-width: 600px) {
    .contact-card { padding: 20px 16px; }
  }
</style>
