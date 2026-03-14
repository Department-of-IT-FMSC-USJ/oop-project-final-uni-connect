<script>
  export let lockMentors = false;

  let selectedRole = "student";

  const handleContinue = () => {
    // Redirect to login page for the selected role
    if (lockMentors && (selectedRole === "academic" || selectedRole === "alumni")) {
      return;
    }
    window.location.href = `/login/${selectedRole}`;
  };

  const handleRegister = () => {
    // Redirect to register page for the selected role
    if (lockMentors && (selectedRole === "academic" || selectedRole === "alumni")) {
      return;
    }
    window.location.href = `/register/${selectedRole}`;
  };
</script>

<div class="glass-panel modal-container">
  <div class="modal-header">
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
        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
        <circle cx="12" cy="7" r="4" />
      </svg>
    </div>
    <div class="login-label">LOGIN / SIGN UP</div>
  </div>

  <div class="form-group">
    <label class="form-label" for="role-select">Select your role</label>
    <select id="role-select" class="form-control" bind:value={selectedRole}>
      <option value="student">Student / Undergraduate</option>
      <option value="academic" disabled={lockMentors}>Academic Mentor {lockMentors ? "(Locked)" : ""}</option>
      <option value="alumni" disabled={lockMentors}>Alumni / Industry Mentor {lockMentors ? "(Locked)" : ""}</option>
      <option value="dept-rep">Department Representative</option>
      <option value="dept-head">Department Head</option>
    </select>
  </div>

  <button class="btn btn-primary btn-block" on:click={handleContinue}>
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
    Continue to Login
  </button>

  <button class="btn btn-secondary btn-block" on:click={handleRegister}>
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
      <path d="M16 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
      <circle cx="12" cy="7" r="4" />
      <line x1="12" y1="12" x2="12" y2="18" />
      <line x1="9" y1="15" x2="15" y2="15" />
    </svg>
    Create New Account
  </button>

  <div class="forgot-password">
    <a href="/forgot-password" class="nav-link">Forgot password?</a>
  </div>
</div>

<style>
  .modal-container {
    width: 100%;
    max-width: 450px;
    padding: 3rem 2.5rem;
    position: relative;
    overflow: hidden;
    border-radius: 12px;
  }

  .modal-header {
    margin-bottom: 2.5rem;
    position: relative;
    display: flex;
    justify-content: flex-start;
    align-items: center;
    gap: 1rem;
  }

  .login-label {
    font-size: 1.75rem;
    font-weight: 900;
    color: var(--text-main);
    letter-spacing: 1.5px;
    margin: 0;
  }

  .icon-right {
    position: relative;
    color: var(--primary-light);
    background: rgba(59, 130, 246, 0.15);
    padding: 0.75rem;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .form-group {
    margin-bottom: 1.75rem;
  }

  .form-label {
    display: block;
    margin-bottom: 0.75rem;
    font-size: 0.9rem;
    font-weight: 600;
    color: var(--text-main);
  }

  .form-control {
    width: 100%;
    padding: 0.9rem 1rem;
    background-color: var(--input-bg);
    border: 1.5px solid var(--glass-border);
    border-radius: 8px;
    color: var(--text-main);
    font-family: var(--font-body);
    font-size: 0.95rem;
    transition: all var(--transition-speed) ease;
  }

  .form-control:hover {
    border-color: var(--primary-light);
  }

  .form-control:focus {
    outline: none;
    border-color: var(--primary-light);
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
    background-color: #ffffff;
  }

  .btn-block {
    width: 100%;
    margin-bottom: 1rem;
  }

  .btn-secondary {
    background-color: #f0f4f8;
    color: var(--text-main);
    border: 1.5px solid var(--glass-border);
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  }

  .btn-secondary:hover {
    background-color: #e8ecf1;
    border-color: var(--primary-light);
    color: var(--primary-light);
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  }

  .forgot-password {
    margin-top: 2rem;
    text-align: center;
    font-size: 0.9rem;
    padding-top: 1.5rem;
    border-top: 1px solid var(--glass-border);
  }

  .nav-link {
    color: var(--primary-light);
    text-decoration: none;
    font-weight: 500;
    transition: color var(--transition-speed) ease;
  }

  .nav-link:hover {
    color: var(--primary);
    text-decoration: underline;
  }
</style>
