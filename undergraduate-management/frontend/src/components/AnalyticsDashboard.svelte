<script>
  import { onMount } from "svelte";

  let activityFilter = "CONTRIBUTIONS";
  let timeRange = "LAST_30_DAYS";

  let loading = false;
  let error = "";
  let dashboard = null;

  const activityOptions = [
    { value: "CONTRIBUTIONS", label: "Contributions" },
    { value: "MENTORING_SESSIONS", label: "Mentoring Sessions" },
    { value: "STUDY_MATERIAL_USAGE", label: "Study Material Usage" },
  ];

  const timeRangeOptions = [
    { value: "LAST_7_DAYS", label: "Last 7 Days" },
    { value: "LAST_30_DAYS", label: "Last 30 Days" },
    { value: "LAST_6_MONTHS", label: "Last 6 Months" },
  ];

  async function loadDashboard() {
    loading = true;
    error = "";

    try {
      const params = new URLSearchParams({
        activityFilter: activityFilter,
        timeRange: timeRange,
      });

      const response = await fetch(
        `http://localhost:8080/api/analytics/dashboard?${params.toString()}`
      );
      const data = await response.json();

      if (data.success) {
        dashboard = data.data;
      } else {
        error = data.message || "Failed to load dashboard data.";
      }
    } catch (err) {
      error = "Failed to connect to the server. Please try again.";
    } finally {
      loading = false;
    }
  }

  // Reload dashboard when filters change
  $: if (activityFilter || timeRange) {
    loadDashboard();
  }

  onMount(() => {
    loadDashboard();
  });

  // Simple SVG line chart renderer
  function getLineChartPath(data) {
    if (!data || data.length === 0) return "";

    const width = 700;
    const height = 200;
    const padding = 40;
    const chartWidth = width - padding * 2;
    const chartHeight = height - padding * 2;

    const maxValue = Math.max(...data.map((d) => d.value), 1);
    const step = chartWidth / Math.max(data.length - 1, 1);

    let path = "";
    data.forEach((point, i) => {
      const x = padding + i * step;
      const y = padding + chartHeight - (point.value / maxValue) * chartHeight;
      if (i === 0) {
        path += `M ${x} ${y}`;
      } else {
        path += ` L ${x} ${y}`;
      }
    });

    return path;
  }

  function getLineChartPoints(data) {
    if (!data || data.length === 0) return [];

    const width = 700;
    const height = 200;
    const padding = 40;
    const chartWidth = width - padding * 2;
    const chartHeight = height - padding * 2;

    const maxValue = Math.max(...data.map((d) => d.value), 1);
    const step = chartWidth / Math.max(data.length - 1, 1);

    return data.map((point, i) => ({
      x: padding + i * step,
      y: padding + chartHeight - (point.value / maxValue) * chartHeight,
      label: point.date,
      value: point.value,
    }));
  }

  function getMaxBarValue(data) {
    if (!data || data.length === 0) return 1;
    return Math.max(...data.map((d) => d.value), 1);
  }
</script>

<div class="analytics-dashboard">
  <div class="page-header">
    <h1>Engagement Analytics Dashboard</h1>
    <p>
      Monitor student engagement levels, mentor participation, and activity
      trends.
    </p>
  </div>

  {#if error}
    <div class="alert alert-error">{error}</div>
  {/if}

  <!-- Filters -->
  <div class="filters-bar">
    <div class="filter-group">
      <label for="activityFilter">Activity Filter</label>
      <select id="activityFilter" bind:value={activityFilter}>
        {#each activityOptions as option}
          <option value={option.value}>{option.label}</option>
        {/each}
      </select>
    </div>

    <div class="filter-group">
      <label for="timeRange">Time Range</label>
      <select id="timeRange" bind:value={timeRange}>
        {#each timeRangeOptions as option}
          <option value={option.value}>{option.label}</option>
        {/each}
      </select>
    </div>
  </div>

  {#if loading}
    <div class="loading-container">
      <p>Loading dashboard data...</p>
    </div>
  {:else if dashboard}
    <!-- Summary Cards -->
    <div class="summary-grid">
      <div class="summary-card">
        <span class="summary-label">Total Active Students</span>
        <span class="summary-value">{dashboard.totalActiveStudents ?? 0}</span>
      </div>
      <div class="summary-card">
        <span class="summary-label">Total Contributions</span>
        <span class="summary-value">{dashboard.totalContributions ?? 0}</span>
      </div>
      <div class="summary-card">
        <span class="summary-label">Mentoring Sessions</span>
        <span class="summary-value"
          >{dashboard.totalMentoringSessions ?? 0}</span
        >
      </div>
    </div>

    <!-- Engagement Trend Chart (Line Chart) -->
    <div class="chart-card">
      <h2>Engagement Trend</h2>
      {#if dashboard.engagementTrend && dashboard.engagementTrend.length > 0}
        <div class="chart-container">
          <svg viewBox="0 0 700 240" class="line-chart">
            <!-- Grid lines -->
            <line x1="40" y1="40" x2="40" y2="200" stroke="#e5e7eb" stroke-width="1" />
            <line x1="40" y1="200" x2="660" y2="200" stroke="#e5e7eb" stroke-width="1" />
            <line x1="40" y1="120" x2="660" y2="120" stroke="#f3f4f6" stroke-width="1" stroke-dasharray="4" />
            <line x1="40" y1="40" x2="660" y2="40" stroke="#f3f4f6" stroke-width="1" stroke-dasharray="4" />

            <!-- Line path -->
            <path
              d={getLineChartPath(dashboard.engagementTrend)}
              fill="none"
              stroke="#4361ee"
              stroke-width="2.5"
              stroke-linejoin="round"
              stroke-linecap="round"
            />

            <!-- Data points -->
            {#each getLineChartPoints(dashboard.engagementTrend) as point}
              <circle cx={point.x} cy={point.y} r="4" fill="#4361ee" />
              <title>{point.label}: {point.value}</title>
            {/each}
          </svg>
        </div>
        <div class="chart-labels">
          {#each dashboard.engagementTrend as point}
            <span class="chart-label-item">{point.date}</span>
          {/each}
        </div>
      {:else}
        <p class="empty-text">No trend data available for the selected filters.</p>
      {/if}
    </div>

    <!-- Department Engagement Comparison (Bar Chart) -->
    <div class="chart-card">
      <h2>Department Engagement Comparison</h2>
      {#if dashboard.departmentComparison && dashboard.departmentComparison.length > 0}
        <div class="bar-chart">
          {#each dashboard.departmentComparison as dept}
            <div class="bar-row">
              <span class="bar-label">{dept.department}</span>
              <div class="bar-track">
                <div
                  class="bar-fill"
                  style="width: {(dept.value / getMaxBarValue(dashboard.departmentComparison)) * 100}%"
                >
                  <span class="bar-value">{dept.value}</span>
                </div>
              </div>
            </div>
          {/each}
        </div>
      {:else}
        <p class="empty-text">No department data available for the selected filters.</p>
      {/if}
    </div>
  {:else}
    <div class="empty-container">
      <p>No dashboard data available.</p>
    </div>
  {/if}
</div>

<style>
  .analytics-dashboard {
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

  .filters-bar {
    display: flex;
    gap: 20px;
    margin-bottom: 28px;
    background: #ffffff;
    border: 1.5px solid #e8ecf1;
    border-radius: 14px;
    padding: 20px 24px;
  }

  .filter-group {
    display: flex;
    flex-direction: column;
    flex: 1;
  }

  .filter-group label {
    font-size: 0.85rem;
    font-weight: 600;
    color: #374151;
    margin-bottom: 6px;
  }

  .filter-group select {
    padding: 10px 12px;
    border: 1.5px solid #d1d5db;
    border-radius: 8px;
    font-size: 0.9rem;
    font-family: inherit;
    color: #1a1a2e;
    background: #fff;
    transition: border-color 0.2s;
  }

  .filter-group select:focus {
    outline: none;
    border-color: #4361ee;
    box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.1);
  }

  .summary-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
    margin-bottom: 28px;
  }

  .summary-card {
    background: #ffffff;
    border: 1.5px solid #e8ecf1;
    border-radius: 14px;
    padding: 24px;
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .summary-label {
    font-size: 0.82rem;
    font-weight: 600;
    color: #6b7280;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    margin-bottom: 8px;
  }

  .summary-value {
    font-size: 2.2rem;
    font-weight: 800;
    color: #4361ee;
  }

  .chart-card {
    background: #ffffff;
    border: 1.5px solid #e8ecf1;
    border-radius: 14px;
    padding: 28px;
    margin-bottom: 24px;
  }

  .chart-card h2 {
    font-size: 1.2rem;
    font-weight: 700;
    color: #1a1a2e;
    margin-bottom: 20px;
  }

  .chart-container {
    width: 100%;
    overflow-x: auto;
  }

  .line-chart {
    width: 100%;
    height: auto;
    max-height: 240px;
  }

  .chart-labels {
    display: flex;
    justify-content: space-between;
    padding: 8px 40px 0;
    overflow-x: auto;
  }

  .chart-label-item {
    font-size: 0.72rem;
    color: #9ca3af;
    white-space: nowrap;
  }

  .bar-chart {
    display: flex;
    flex-direction: column;
    gap: 14px;
  }

  .bar-row {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .bar-label {
    font-size: 0.85rem;
    font-weight: 500;
    color: #374151;
    min-width: 160px;
    text-align: right;
  }

  .bar-track {
    flex: 1;
    background: #f3f4f6;
    border-radius: 6px;
    height: 32px;
    overflow: hidden;
  }

  .bar-fill {
    height: 100%;
    background: linear-gradient(90deg, #4361ee, #6b8cff);
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    padding-right: 10px;
    min-width: 40px;
    transition: width 0.4s ease;
  }

  .bar-value {
    font-size: 0.8rem;
    font-weight: 700;
    color: #ffffff;
  }

  .loading-container,
  .empty-container {
    text-align: center;
    padding: 48px 0;
    color: #6b7280;
    font-size: 0.95rem;
  }

  .empty-text {
    color: #6b7280;
    font-size: 0.9rem;
    text-align: center;
    padding: 20px 0;
  }

  @media (max-width: 768px) {
    .filters-bar {
      flex-direction: column;
    }

    .summary-grid {
      grid-template-columns: 1fr;
    }

    .bar-row {
      flex-direction: column;
      align-items: flex-start;
    }

    .bar-label {
      text-align: left;
      min-width: auto;
    }
  }
</style>
