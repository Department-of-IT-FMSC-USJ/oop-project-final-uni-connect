<script>
  let selectedRole = "student";

  const handleContinue = () => {
    // Redirect to login page for the selected role
    window.location.href = `/login/${selectedRole}`;
  };

  const handleRegister = () => {
    // Redirect to register page for the selected role
    window.location.href = `/register/${selectedRole}`;
  };
</script>

<div class="glass-panel modal-container">
  <div class="modal-header">
    <div class="login-label">LOGIN</div>
    <h2 class="modal-title">Sign in</h2>
    <div class="icon-right">
      <svg
        xmlns="http://www.w3.org/2000/svg"
        width="24"
        height="24"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
      >
        <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4" />
        <polyline points="10 17 15 12 10 7" />
        <line x1="15" y1="12" x2="3" y2="12" />
      </svg>
    </div>
  </div>

  <div class="form-group">
    <label class="form-label" for="role-select">Select your role</label>
    <select id="role-select" class="form-control" bind:value={selectedRole}>
      <option value="student">Student / Undergraduate</option>
      <option value="academic">Academic Mentor</option>
      <option value="alumni">Alumni / Industry Mentor</option>
      <option value="dept-rep">Department Representative</option>
      <option value="dept-head">Department Head</option>
    </select>
  </div>

  <button class="btn btn-primary w-100" on:click={handleContinue}>
    <svg
      xmlns="http://www.w3.org/2000/svg"
      width="16"
      height="16"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      stroke-width="2"
      stroke-linecap="round"
      stroke-linejoin="round"
    >
      <line x1="5" y1="12" x2="19" y2="12" />
      <polyline points="12 5 19 12 12 19" />
    </svg>
    Continue
  </button>

  <button class="btn btn-outline w-100 mt-1rem" on:click={handleRegister}>
    Need an account? Register
  </button>

  <div class="forgot-password">
    <a href="/forgot-password" class="nav-link">Forgot password?</a>
  </div>
</div>

<style>
  .modal-container {
    width: 100%;
    max-width: 450px;
    padding: 2.5rem;
    position: relative;
    overflow: hidden;
  }

  .modal-header {
    margin-bottom: 2rem;
    position: relative;
  }

  .login-label {
    font-size: 0.75rem;
    font-weight: 700;
    color: var(--text-muted);
    letter-spacing: 1px;
    margin-bottom: 0.25rem;
  }

  .modal-title {
    font-size: 2rem;
    font-weight: 700;
    color: var(--text-main);
  }

  .icon-right {
    position: absolute;
    top: 0;
    right: 0;
    color: var(--primary);
    background: rgba(59, 130, 246, 0.1);
    padding: 0.5rem;
    border-radius: 8px;
  }

  .w-100 {
    width: 100%;
  }

  .mt-1rem {
    margin-top: 1rem;
  }

  .forgot-password {
    margin-top: 1.5rem;
    text-align: center;
    font-size: 0.85rem;
  }
</style>
