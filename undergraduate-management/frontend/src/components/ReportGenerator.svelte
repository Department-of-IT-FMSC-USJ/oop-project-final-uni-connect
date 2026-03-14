<script>
  let reportType = "";
  let startDate = "";
  let endDate = "";
  let department = "";
  let reportFormat = "";

  let loading = false;
  let error = "";
  let successMessage = "";
  let generatedReport = null;
  let userReports = [];
  let loadingReports = false;

  const reportTypes = [
    { value: "STUDENT_CONTRIBUTION", label: "Student Contribution Report" },
    { value: "MENTOR_ACTIVITY", label: "Mentor Activity Report" },
    { value: "MENTORING_SESSION", label: "Mentoring Session Report" },
    { value: "ENGAGEMENT_SUMMARY", label: "Engagement Summary" },
  ];

  const departments = [
    { value: "", label: "All Departments" },
    { value: "Computer Science", label: "Computer Science" },
    { value: "Information Technology", label: "Information Technology" },
    { value: "Software Engineering", label: "Software Engineering" },
    { value: "Data Science", label: "Data Science" },
    { value: "Cyber Security", label: "Cyber Security" },
  ];

  const reportFormats = [
    { value: "PDF", label: "PDF" },
    { value: "EXCEL", label: "Excel" },
  ];

  // Mock user ID (in production, this would come from auth context)
  const userId = 1;

  async function generateReport() {
    error = "";
    successMessage = "";
    generatedReport = null;

    // Validate required fields
    if (!reportType) {
      error = "Please select a report type.";
      return;
    }
    if (!startDate) {
      error = "Please select a start date.";
      return;
    }
    if (!endDate) {
      error = "Please select an end date.";
      return;
    }
    if (new Date(endDate) <= new Date(startDate)) {
      error = "End date must be later than start date.";
      return;
    }
    if (!reportFormat) {
      error = "Please select a report format.";
      return;
    }

    loading = true;

    try {
      const response = await fetch("http://localhost:8080/api/reports/generate", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          userId: userId,
          reportType: reportType,
          startDate: startDate,
          endDate: endDate,
          department: department || null,
          reportFormat: reportFormat,
        }),
      });

      const data = await response.json();

      if (data.success) {
        successMessage = data.message;
        generatedReport = data.data;
        await loadUserReports();
      } else {
        error = data.message || "Failed to generate report.";
      }
    } catch (err) {
      error = "Failed to connect to the server. Please try again.";
    } finally {
      loading = false;
    }
  }

  async function loadUserReports() {
    loadingReports = true;
    try {
      const response = await fetch(`http://localhost:8080/api/reports/user/${userId}`);
      const data = await response.json();

      if (data.success) {
        userReports = data.data;
      }
    } catch (err) {
      console.error("Failed to load reports:", err);
    } finally {
      loadingReports = false;
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

  function formatDateTime(dateStr) {
    if (!dateStr) return "N/A";
    return new Date(dateStr).toLocaleString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  }

  // Load existing reports on mount
  import { onMount } from "svelte";
  onMount(() => {
    loadUserReports();
  });
</script>

<div class="report-generator">
  <div class="page-header">
    <h1>System Report Generation</h1>
    <p>Generate reports for student contributions, mentor activities, mentoring sessions, and engagement statistics.</p>
  </div>

  {#if error}
    <div class="alert alert-error">{error}</div>
  {/if}

  {#if successMessage}
    <div class="alert alert-success">{successMessage}</div>
  {/if}

  <div class="form-card">
    <h2>Report Parameters</h2>
    <div class="form-grid">
      <div class="form-group">
        <label for="reportType">Report Type <span class="required">*</span></label>
        <select id="reportType" bind:value={reportType}>
          <option value="">-- Select Report Type --</option>
          {#each reportTypes as type}
            <option value={type.value}>{type.label}</option>
          {/each}
        </select>
      </div>

      <div class="form-group">
        <label for="department">Department Filter</label>
        <select id="department" bind:value={department}>
          {#each departments as dept}
            <option value={dept.value}>{dept.label}</option>
          {/each}
        </select>
      </div>

      <div class="form-group">
        <label for="startDate">Start Date <span class="required">*</span></label>
        <input type="date" id="startDate" bind:value={startDate} />
      </div>

      <div class="form-group">
        <label for="endDate">End Date <span class="required">*</span></label>
        <input type="date" id="endDate" bind:value={endDate} min={startDate || ""} />
      </div>

      <div class="form-group">
        <label for="reportFormat">Report Format <span class="required">*</span></label>
        <select id="reportFormat" bind:value={reportFormat}>
          <option value="">-- Select Format --</option>
          {#each reportFormats as format}
            <option value={format.value}>{format.label}</option>
          {/each}
        </select>
      </div>
    </div>

    <button class="btn-generate" on:click={generateReport} disabled={loading}>
      {#if loading}
        Generating Report...
      {:else}
        Generate Report
      {/if}
    </button>
  </div>

  {#if generatedReport}
    <div class="result-card">
      <h2>Generated Report</h2>
      <div class="report-details">
        <div class="detail-row">
          <span class="detail-label">Report ID:</span>
          <span class="detail-value">#{generatedReport.reportId}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Type:</span>
          <span class="detail-value">{generatedReport.reportType}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Date Range:</span>
          <span class="detail-value">{formatDate(generatedReport.startDate)} - {formatDate(generatedReport.endDate)}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Department:</span>
          <span class="detail-value">{generatedReport.department}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Format:</span>
          <span class="detail-value">{generatedReport.reportFormat}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Generated:</span>
          <span class="detail-value">{formatDateTime(generatedReport.generatedDate)}</span>
        </div>
      </div>
      <a href={generatedReport.filePath} class="btn-download" download>
        Download Report
      </a>
    </div>
  {/if}

  <div class="history-card">
    <h2>Report History</h2>
    {#if loadingReports}
      <p class="loading-text">Loading reports...</p>
    {:else if userReports.length === 0}
      <p class="empty-text">No reports generated yet.</p>
    {:else}
      <div class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Type</th>
              <th>Date Range</th>
              <th>Department</th>
              <th>Format</th>
              <th>Generated</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {#each userReports as report}
              <tr>
                <td>#{report.reportId}</td>
                <td>{report.reportType}</td>
                <td>{formatDate(report.startDate)} - {formatDate(report.endDate)}</td>
                <td>{report.department}</td>
                <td>{report.reportFormat}</td>
                <td>{formatDateTime(report.generatedDate)}</td>
                <td>
                  <a href={report.filePath} class="btn-table-download" download>
                    Download
                  </a>
                </td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    {/if}
  </div>
</div>

<style>
  .report-generator {
    max-width: 900px;
    margin: 0 auto;
  }

  .page-header {
    margin-bottom: 32px;
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

  .form-card,
  .result-card,
  .history-card {
    background: #ffffff;
    border: 1.5px solid #e8ecf1;
    border-radius: 14px;
    padding: 28px;
    margin-bottom: 24px;
  }

  .form-card h2,
  .result-card h2,
  .history-card h2 {
    font-size: 1.2rem;
    font-weight: 700;
    color: #1a1a2e;
    margin-bottom: 20px;
  }

  .form-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
    margin-bottom: 24px;
  }

  .form-group {
    display: flex;
    flex-direction: column;
  }

  .form-group label {
    font-size: 0.85rem;
    font-weight: 600;
    color: #374151;
    margin-bottom: 6px;
  }

  .required {
    color: #dc2626;
  }

  .form-group select,
  .form-group input {
    padding: 10px 12px;
    border: 1.5px solid #d1d5db;
    border-radius: 8px;
    font-size: 0.9rem;
    font-family: inherit;
    color: #1a1a2e;
    background: #fff;
    transition: border-color 0.2s;
  }

  .form-group select:focus,
  .form-group input:focus {
    outline: none;
    border-color: #4361ee;
    box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.1);
  }

  .btn-generate {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: 12px 28px;
    background: #4361ee;
    color: #fff;
    border: none;
    border-radius: 8px;
    font-size: 0.95rem;
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

  .report-details {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    margin-bottom: 20px;
  }

  .detail-row {
    display: flex;
    flex-direction: column;
  }

  .detail-label {
    font-size: 0.8rem;
    font-weight: 600;
    color: #6b7280;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    margin-bottom: 2px;
  }

  .detail-value {
    font-size: 0.95rem;
    color: #1a1a2e;
    font-weight: 500;
  }

  .btn-download {
    display: inline-flex;
    align-items: center;
    padding: 10px 24px;
    background: #16a34a;
    color: #fff;
    border: none;
    border-radius: 8px;
    font-size: 0.9rem;
    font-weight: 600;
    text-decoration: none;
    cursor: pointer;
    transition: background 0.2s;
  }

  .btn-download:hover {
    background: #15803d;
  }

  .table-wrapper {
    overflow-x: auto;
  }

  table {
    width: 100%;
    border-collapse: collapse;
    font-size: 0.88rem;
  }

  thead th {
    background: #f9fafb;
    padding: 10px 12px;
    text-align: left;
    font-weight: 600;
    color: #374151;
    border-bottom: 2px solid #e5e7eb;
  }

  tbody td {
    padding: 10px 12px;
    border-bottom: 1px solid #f3f4f6;
    color: #4b5563;
  }

  tbody tr:hover {
    background: #f9fafb;
  }

  .btn-table-download {
    padding: 4px 12px;
    background: #4361ee;
    color: #fff;
    border-radius: 6px;
    font-size: 0.82rem;
    font-weight: 500;
    text-decoration: none;
    transition: background 0.2s;
  }

  .btn-table-download:hover {
    background: #3651d4;
  }

  .loading-text,
  .empty-text {
    color: #6b7280;
    font-size: 0.9rem;
    text-align: center;
    padding: 20px 0;
  }

  @media (max-width: 768px) {
    .form-grid {
      grid-template-columns: 1fr;
    }

    .report-details {
      grid-template-columns: 1fr;
    }
  }
</style>
