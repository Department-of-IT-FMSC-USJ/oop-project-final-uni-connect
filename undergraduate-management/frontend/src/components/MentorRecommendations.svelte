<script>
  import { onMount } from "svelte";

  let studentId = 1;
  let recommendations = [];
  let loading = false;
  let generating = false;
  let error = "";
  let successMessage = "";

  async function loadRecommendations() {
    loading = true;
    error = "";

    try {
      const response = await fetch(
        `http://localhost:8080/api/recommendations/${studentId}`
      );
      const data = await response.json();

      if (data.success) {
        recommendations = data.data;
      } else {
        error = data.message || "Failed to load recommendations.";
      }
    } catch (err) {
      error = "Failed to connect to the server. Please try again.";
    } finally {
      loading = false;
    }
  }

  async function generateRecommendations() {
    generating = true;
    error = "";
    successMessage = "";

    try {
      const response = await fetch(
        `http://localhost:8080/api/recommendations/generate/${studentId}`,
        { method: "POST" }
      );
      const data = await response.json();

      if (data.success) {
        recommendations = data.data;
        successMessage = data.message;
      } else {
        error = data.message || "Failed to generate recommendations.";
      }
    } catch (err) {
      error = "Failed to connect to the server. Please try again.";
    } finally {
      generating = false;
    }
  }

  function formatDate(dateStr) {
    if (!dateStr) return "N/A";
    return new Date(dateStr).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  }

  onMount(() => {
    loadRecommendations();
  });
</script>

<div class="recommendations-page">
  <div class="page-header">
    <h1>Mentor Recommendations</h1>
    <p>
      Mentors recommended for you based on your academic interests and skills.
    </p>
  </div>

  {#if error}
    <div class="alert alert-error">{error}</div>
  {/if}

  {#if successMessage}
    <div class="alert alert-success">{successMessage}</div>
  {/if}

  <div class="actions-bar">
    <button
      class="btn-generate"
      on:click={generateRecommendations}
      disabled={generating}
    >
      {#if generating}
        Generating...
      {:else}
        Generate Recommendations
      {/if}
    </button>
    <span class="hint-text"
      >Click to refresh recommendations based on your latest profile.</span
    >
  </div>

  {#if loading}
    <div class="loading-container">
      <p>Loading recommendations...</p>
    </div>
  {:else if recommendations.length === 0}
    <div class="empty-container">
      <div class="empty-icon">🔍</div>
      <h3>No Recommendations Yet</h3>
      <p>
        Make sure your profile has interest tags, then click "Generate
        Recommendations".
      </p>
    </div>
  {:else}
    <div class="recommendations-list">
      {#each recommendations as rec, index}
        <div class="recommendation-card">
          <div class="card-header">
            <div class="rank-badge">#{index + 1}</div>
            <div class="mentor-info">
              <h3>{rec.mentorName}</h3>
              <span class="mentor-type-tag">{rec.mentorCategory}</span>
            </div>
            <div class="score-circle">
              <span class="score-value">{rec.matchScore}%</span>
              <span class="score-label">Match</span>
            </div>
          </div>

          <div class="card-body">
            <div class="detail-section">
              <h4>Expertise Area</h4>
              <p class="expertise-text">{rec.expertiseArea}</p>
            </div>

            {#if rec.matchingTags && rec.matchingTags.length > 0}
              <div class="detail-section">
                <h4>Matching Skills / Interests</h4>
                <div class="tags-container">
                  {#each rec.matchingTags as tag}
                    <span class="matching-tag">{tag}</span>
                  {/each}
                </div>
              </div>
            {/if}

            <div class="card-footer">
              <span class="rec-date"
                >Recommended: {formatDate(rec.recommendationDate)}</span
              >
              <button class="btn-view-profile">View Mentor Profile</button>
            </div>
          </div>
        </div>
      {/each}
    </div>
  {/if}
</div>

<style>
  .recommendations-page {
    max-width: 900px;
    margin: 0 auto;
  }

  .page-header {
    margin-bottom: 24px;
  }

  .page-header h1 {
    font-size: 1.8rem;
    font-weight: 800;
    color: #1a1a2e;
    margin-bottom: 8px;
  }

  .page-header p {
    color: #666;
    font-size: 0.95rem;
  }

  .alert {
    padding: 12px 16px;
    border-radius: 8px;
    margin-bottom: 20px;
    font-size: 0.9rem;
    font-weight: 500;
  }

  .alert-error {
    background: #fef2f2;
    color: #dc2626;
    border: 1px solid #fecaca;
  }

  .alert-success {
    background: #f0fdf4;
    color: #16a34a;
    border: 1px solid #bbf7d0;
  }

  .actions-bar {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 28px;
  }

  .btn-generate {
    padding: 10px 24px;
    background: #4361ee;
    color: #fff;
    border: none;
    border-radius: 8px;
    font-size: 0.9rem;
    font-weight: 600;
    font-family: inherit;
    cursor: pointer;
    transition: background 0.2s;
  }

  .btn-generate:hover:not(:disabled) {
    background: #3651d4;
  }

  .btn-generate:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }

  .hint-text {
    font-size: 0.82rem;
    color: #9ca3af;
  }

  .loading-container {
    text-align: center;
    padding: 48px 0;
    color: #6b7280;
  }

  .empty-container {
    text-align: center;
    padding: 60px 0;
    color: #6b7280;
  }

  .empty-icon {
    font-size: 3rem;
    margin-bottom: 12px;
  }

  .empty-container h3 {
    font-size: 1.2rem;
    font-weight: 700;
    color: #374151;
    margin-bottom: 8px;
  }

  .empty-container p {
    font-size: 0.9rem;
    color: #6b7280;
  }

  .recommendations-list {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .recommendation-card {
    background: #ffffff;
    border: 1.5px solid #e8ecf1;
    border-radius: 14px;
    overflow: hidden;
    transition: box-shadow 0.2s;
  }

  .recommendation-card:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  }

  .card-header {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px 24px;
    border-bottom: 1px solid #f3f4f6;
  }

  .rank-badge {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: #4361ee;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 0.85rem;
    font-weight: 700;
    flex-shrink: 0;
  }

  .mentor-info {
    flex: 1;
  }

  .mentor-info h3 {
    font-size: 1.1rem;
    font-weight: 700;
    color: #1a1a2e;
    margin-bottom: 4px;
  }

  .mentor-type-tag {
    display: inline-block;
    padding: 2px 10px;
    border-radius: 20px;
    font-size: 0.75rem;
    font-weight: 600;
    background: #f0f4ff;
    color: #4361ee;
  }

  .score-circle {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 64px;
    height: 64px;
    border-radius: 50%;
    background: #f0fdf4;
    border: 2px solid #bbf7d0;
    flex-shrink: 0;
  }

  .score-value {
    font-size: 1rem;
    font-weight: 800;
    color: #16a34a;
    line-height: 1;
  }

  .score-label {
    font-size: 0.65rem;
    font-weight: 500;
    color: #6b7280;
    margin-top: 2px;
  }

  .card-body {
    padding: 20px 24px;
  }

  .detail-section {
    margin-bottom: 16px;
  }

  .detail-section h4 {
    font-size: 0.8rem;
    font-weight: 600;
    color: #6b7280;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    margin-bottom: 6px;
  }

  .expertise-text {
    font-size: 0.9rem;
    color: #374151;
    line-height: 1.5;
  }

  .tags-container {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .matching-tag {
    display: inline-block;
    padding: 4px 12px;
    border-radius: 20px;
    font-size: 0.8rem;
    font-weight: 500;
    background: #ecfdf5;
    color: #059669;
    border: 1px solid #a7f3d0;
  }

  .card-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-top: 16px;
    border-top: 1px solid #f3f4f6;
  }

  .rec-date {
    font-size: 0.8rem;
    color: #9ca3af;
  }

  .btn-view-profile {
    padding: 8px 18px;
    background: transparent;
    color: #4361ee;
    border: 1.5px solid #4361ee;
    border-radius: 8px;
    font-size: 0.85rem;
    font-weight: 600;
    font-family: inherit;
    cursor: pointer;
    transition: all 0.2s;
  }

  .btn-view-profile:hover {
    background: #4361ee;
    color: #fff;
  }

  @media (max-width: 768px) {
    .card-header {
      flex-wrap: wrap;
    }

    .card-footer {
      flex-direction: column;
      gap: 12px;
      align-items: flex-start;
    }

    .actions-bar {
      flex-direction: column;
      align-items: flex-start;
    }
  }
</style>
