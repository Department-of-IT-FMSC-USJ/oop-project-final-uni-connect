<script>
  import { login, register, getCurrentUser, getRoleDashboardPath } from '../../lib/api.js';
  import { onMount } from 'svelte';

  let mode = 'home'; // 'home' | 'login' | 'register'
  let error = '';
  let loading = false;

  // Login fields
  let loginEmail = '';
  let loginPassword = '';

  // Register fields
  let regFullName = '';
  let regPassword = '';
  let regConfirmPassword = '';
  let regRegistrationNumber = ''; // MC number: 6 digits
  let regCpmNumber = ''; // CPM number: 5 digits
  let regPhone = '';
  let regDepartment = 'Department of Information Technology';
  let regYearOfStudy = '1';
  let regProfilePicture = ''; // base64 dataURL
  let regProfilePicturePreview = '';
  let regEmail = '';

  $: regEmail = regRegistrationNumber
    ? `${regRegistrationNumber}@mgt.sjp.ac.lk`
    : '';

  onMount(() => {
    const user = getCurrentUser();
    if (user) {
      window.location.href = getRoleDashboardPath(user.role);
    }
  });

  async function handleLogin() {
    error = '';
    loading = true;
    try {
      const data = await login(loginEmail, loginPassword);
      window.location.href = getRoleDashboardPath(data.role);
    } catch (e) {
      error = e.data?.message || e.message || 'Login failed';
    } finally {
      loading = false;
    }
  }

  async function handleRegister() {
    error = '';
    if (regPassword !== regConfirmPassword) {
      error = 'Passwords do not match';
      return;
    }

    const mc = (regRegistrationNumber || '').trim();
    const cpm = (regCpmNumber || '').trim();

    if (!/^\d{6}$/.test(mc)) {
      error = 'MC number must be exactly 6 digits (numeric only).';
      return;
    }
    if (!/^\d{5}$/.test(cpm)) {
      error = 'CPM number must be exactly 5 digits (numeric only).';
      return;
    }
    if (!regProfilePicture) {
      error = 'Profile picture is required.';
      return;
    }
    if (!regEmail.toLowerCase().endsWith('@mgt.sjp.ac.lk')) {
      error = 'Email must be in the format <MC>@mgt.sjp.ac.lk';
      return;
    }

    loading = true;
    try {
      const data = await register({
        fullName: regFullName,
        email: regEmail,
        password: regPassword,
        confirmPassword: regConfirmPassword,
        role: 'UNDERGRADUATE',
        registrationNumber: mc,
        cpmNumber: cpm,
        phone: regPhone,
        department: regDepartment,
        yearOfStudy: regYearOfStudy,
        profilePicture: regProfilePicture
      });
      window.location.href = getRoleDashboardPath(data.role);
    } catch (e) {
      error = e.data?.message || e.message || 'Registration failed';
    } finally {
      loading = false;
    }
  }

  function handleProfilePictureChange(e) {
    const file = e.target?.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = () => {
      regProfilePicture = String(reader.result || '');
      regProfilePicturePreview = regProfilePicture;
    };
    reader.readAsDataURL(file);
  }
</script>

<div class="landing">
  <!-- Navigation -->
  <nav class="top-nav">
    <div class="nav-brand">
      <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M12 14l9-5-9-5-9 5 9 5z"/>
        <path d="M12 14l6.16-3.422a12.083 12.083 0 01.665 6.479A11.952 11.952 0 0012 20.055a11.952 11.952 0 00-6.824-2.998 12.078 12.078 0 01.665-6.479L12 14z"/>
      </svg>
      <span>UniConnect</span>
    </div>
    <div class="nav-links">
      <button class="nav-link" class:active={mode === 'home'} on:click={() => mode = 'home'}>Home</button>
      <button class="nav-link btn-login" on:click={() => mode = 'login'}>Login</button>
    </div>
  </nav>

  {#if mode === 'home'}
    <!-- Hero Section -->
    <section class="hero">
      <div class="hero-content">
        <div class="hero-text">
          <h1>Empower Your Academic Journey.</h1>
          <p>Connect with mentors, manage department goals, and access resources to succeed in your academic career.</p>
          <button class="btn btn-primary hero-btn" on:click={() => mode = 'register'}>
            Get Started
          </button>
        </div>
        <div class="hero-image">
          <div class="hero-illustration">
            <svg viewBox="0 0 400 300" fill="none" stroke="var(--border-medium)" stroke-width="1.5">
              <circle cx="200" cy="150" r="100" />
              <circle cx="200" cy="150" r="140" stroke-dasharray="4 8" opacity="0.6"/>
              <line x1="200" y1="10" x2="200" y2="290" opacity="0.3"/>
              <line x1="60" y1="150" x2="340" y2="150" opacity="0.3"/>
            </svg>
          </div>
        </div>
      </div>
    </section>

    <!-- Features -->
    <section class="features">
      <div class="feature-card">
        <div class="feature-icon student-icon">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
        </div>
        <h3>Student Mentorship</h3>
        <p>Connect with academic and industry mentors, track your progress, and earn points for engagement.</p>
      </div>

      <div class="feature-card">
        <div class="feature-icon dept-icon">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="2" y="7" width="20" height="14" rx="2" ry="2"/>
            <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/>
          </svg>
        </div>
        <h3>Department Management</h3>
        <p>Manage student allocations, verify mentors, and oversee academic progress at the department level.</p>
      </div>

      <div class="feature-card">
        <div class="feature-icon curric-icon">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/>
            <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/>
          </svg>
        </div>
        <h3>Curriculum Insights</h3>
        <p>Industry mentors suggest curriculum improvements, ensuring students learn the most relevant skills.</p>
      </div>
    </section>

  {:else if mode === 'login'}
    <!-- Login Form -->
    <section class="auth-section">
      <div class="auth-card">
        <h2>Welcome Back</h2>
        <p class="auth-subtitle">Sign in to your account</p>

        {#if error}
          <div class="alert alert-error">{error}</div>
        {/if}

        <form on:submit|preventDefault={handleLogin}>
          <div class="form-group">
            <label for="login-email">Email</label>
            <input id="login-email" type="email" class="input" placeholder="you@university.edu" bind:value={loginEmail} required />
          </div>
          <div class="form-group">
            <label for="login-password">Password</label>
            <input id="login-password" type="password" class="input" placeholder="Enter your password" bind:value={loginPassword} required />
          </div>
          <button type="submit" class="btn btn-primary btn-full" disabled={loading}>
            {loading ? 'Signing in...' : 'Sign In'}
          </button>
        </form>

        <p class="auth-switch">
          New undergraduate student?
          <button class="link-btn" on:click={() => { mode = 'register'; error = ''; }}>Create an account</button>
        </p>
      </div>
    </section>

  {:else if mode === 'register'}
    <!-- Register Form -->
    <section class="auth-section">
      <div class="auth-card register-card">
        <h2>Create Your Account</h2>
        <p class="auth-subtitle">Undergraduate registration</p>

        {#if error}
          <div class="alert alert-error">{error}</div>
        {/if}

        <form on:submit|preventDefault={handleRegister}>
          <div class="form-row">
            <div class="form-group">
              <label for="reg-name">Full Name</label>
              <input id="reg-name" type="text" class="input" placeholder="John Doe" bind:value={regFullName} required />
            </div>
            <div class="form-group">
              <label for="reg-phone">Phone</label>
              <input id="reg-phone" type="tel" class="input" placeholder="+94 71 234 5678" bind:value={regPhone} />
            </div>
          </div>

          <div class="form-group">
            <label for="reg-mc">Registration Number (MC Number)</label>
            <input
              id="reg-mc"
              type="text"
              class="input"
              placeholder="123456"
              bind:value={regRegistrationNumber}
              required
              maxlength="6"
              inputmode="numeric"
            />
          </div>

          <div class="form-group">
            <label for="reg-email">University Email</label>
            <input id="reg-email" type="email" class="input" bind:value={regEmail} readonly required />
            <small class="hint">Format: &lt;MC&gt;@mgt.sjp.ac.lk</small>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="reg-dept">Department</label>
              <select id="reg-dept" class="input" bind:value={regDepartment}>
                <option>Department of Information Technology</option>
                <option>Department of Computer Science</option>
                <option>Department of Software Engineering</option>
                <option>Department of Data Science</option>
              </select>
            </div>
            <div class="form-group">
              <label for="reg-year">Year of Study</label>
              <select id="reg-year" class="input" bind:value={regYearOfStudy}>
                <option value="1">1st Year</option>
                <option value="2">2nd Year</option>
                <option value="3">3rd Year</option>
                <option value="4">4th Year</option>
              </select>
            </div>
          </div>

          <div class="form-group">
            <label for="reg-cpm">CPM Number (5 digits)</label>
            <input
              id="reg-cpm"
              type="text"
              class="input"
              placeholder="12345"
              bind:value={regCpmNumber}
              required
              maxlength="5"
              inputmode="numeric"
            />
          </div>

          <div class="form-group">
            <label for="reg-profile">Profile Picture</label>
            <input id="reg-profile" type="file" class="input" accept="image/*" on:change={handleProfilePictureChange} required />
            {#if regProfilePicturePreview}
              <div style="margin-top: 0.75rem;">
                <img src={regProfilePicturePreview} alt="Profile preview" style="width: 72px; height: 72px; border-radius: 12px; object-fit: cover;" />
              </div>
            {/if}
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="reg-pass">Password</label>
              <input id="reg-pass" type="password" class="input" placeholder="Min 6 characters" bind:value={regPassword} required minlength="6" />
            </div>
            <div class="form-group">
              <label for="reg-confirm">Confirm Password</label>
              <input id="reg-confirm" type="password" class="input" placeholder="Re-enter password" bind:value={regConfirmPassword} required />
            </div>
          </div>

          <button type="submit" class="btn btn-primary btn-full" disabled={loading}>
            {loading ? 'Creating account...' : 'Create Account'}
          </button>
        </form>

        <p class="auth-switch">
          Already have an account?
          <button class="link-btn" on:click={() => { mode = 'login'; error = ''; }}>Sign in</button>
        </p>
      </div>
    </section>
  {/if}
</div>

<style>
  .landing {
    min-height: 100vh;
    background: var(--bg-main);
  }

  /* ── Top Navigation ── */
  .top-nav {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem 2rem;
    background: var(--bg-main);
    border-bottom: 1px solid var(--border-light);
    position: sticky;
    top: 0;
    z-index: 50;
    transition: all var(--transition-fast);
  }

  .nav-brand {
    display: flex;
    align-items: center;
    gap: 0.6rem;
    font-size: 1.2rem;
    font-weight: 700;
    color: var(--primary);
    font-family: var(--font-heading);
  }

  .nav-icon {
    width: 28px;
    height: 28px;
    color: var(--primary);
  }

  .nav-links {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }

  .nav-link {
    background: none;
    color: var(--text-secondary);
    font-size: 0.9rem;
    font-weight: 500;
    padding: 0.6rem 1rem;
    border-radius: var(--radius-sm);
    border: 1px solid transparent;
    transition: all var(--transition-fast);
    cursor: pointer;
  }

  .nav-link:hover {
    color: var(--primary);
    background: var(--primary-50);
  }

  .nav-link.active {
    color: var(--primary);
    font-weight: 600;
  }

  .btn-login {
    background: var(--primary);
    color: white !important;
    border: 1px solid var(--primary);
    border-radius: var(--radius-sm);
  }

  .btn-login:hover {
    background: var(--primary-light);
    border-color: var(--primary-light);
  }

  /* ── Hero Section ── */
  .hero {
    margin: 0;
    padding: 4rem 2rem;
    background: linear-gradient(135deg, var(--primary-50), #FFFFFF);
    animation: slideIn 0.6s ease-out;
  }

  .hero-content {
    display: flex;
    align-items: center;
    gap: 6rem;
    padding: 6rem 2rem;
    max-width: 1000px;
    margin: 0 auto;
  }

  .hero-text {
    flex: 1;
  }

  .hero-text h1 {
    font-size: 3.5rem;
    font-weight: 700;
    letter-spacing: -0.03em;
    color: var(--text-main);
    line-height: 1.1;
    margin-bottom: 1.5rem;
    font-family: var(--font-heading);
  }

  .hero-text p {
    font-size: 1.05rem;
    color: var(--text-secondary);
    margin-bottom: 2rem;
    line-height: 1.6;
  }

  .hero-btn {
    padding: 0.8rem 2.2rem;
    font-size: 0.95rem;
    background: var(--primary);
    color: #FFFFFF;
    border: none;
    border-radius: var(--radius);
    font-weight: 600;
    cursor: pointer;
    transition: all var(--transition-fast);
    box-shadow: var(--shadow-sm);
  }

  .hero-btn:hover {
    background: var(--primary-dark);
    transform: translateY(-1px);
    box-shadow: var(--shadow-md);
  }

  .hero-image {
    flex: 1;
    display: flex;
    justify-content: center;
    animation: slideInRight 0.6s ease-out;
  }

  .hero-illustration svg {
    width: 100%;
    max-width: 400px;
  }

  /* Override inline SVG stroke colors via CSS */
  .hero-illustration svg circle {
    stroke: var(--primary-light);
  }

  .hero-illustration svg circle:nth-of-type(2) {
    stroke: var(--accent);
  }

  .hero-illustration svg line {
    stroke: var(--primary-100);
  }

  /* ── Features Section ── */
  .features {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 2rem;
    max-width: 1200px;
    margin: 0 auto;
    padding: 4rem 2rem;
    background: var(--bg-alt);
    /* Full-bleed background trick */
    box-shadow: 0 0 0 100vmax var(--bg-alt);
    clip-path: inset(0 -100vmax);
  }

  .feature-card {
    background: var(--bg-main);
    border-radius: var(--radius);
    padding: 2.5rem 2rem;
    border: 1px solid var(--border-light);
    box-shadow: var(--shadow-sm);
    transition: all var(--transition-smooth);
    animation: slideIn 0.5s ease-out;
  }

  .feature-card:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-md);
    border-color: var(--primary-100);
  }

  .feature-icon {
    width: 56px;
    height: 56px;
    border-radius: var(--radius);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 1.25rem;
  }

  .student-icon {
    background: var(--primary-50);
    color: var(--primary);
  }

  .dept-icon {
    background: var(--success-light);
    color: var(--success);
  }

  .curric-icon {
    background: var(--warning-light);
    color: var(--warning);
  }

  .feature-card h3 {
    font-size: 1.1rem;
    font-weight: 600;
    margin-bottom: 0.5rem;
    color: var(--text-main);
  }

  .feature-card p {
    font-size: 0.9rem;
    color: var(--text-secondary);
    line-height: 1.6;
  }

  /* ── Auth Forms ── */
  .auth-section {
    display: flex;
    justify-content: center;
    padding: 3rem 2rem;
    min-height: calc(100vh - 72px);
    align-items: flex-start;
    background: var(--bg-alt);
    animation: fadeIn 0.4s ease-out;
  }

  .auth-card {
    background: var(--bg-main);
    border-radius: var(--radius-lg);
    padding: 3.5rem;
    box-shadow: var(--shadow-md);
    border: 1px solid var(--border-light);
    width: 100%;
    max-width: 420px;
    animation: slideIn 0.5s ease-out;
  }

  .register-card {
    max-width: 560px;
  }

  .auth-card h2 {
    font-size: 1.5rem;
    font-weight: 700;
    color: var(--text-main);
    margin-bottom: 0.25rem;
    font-family: var(--font-heading);
  }

  .auth-subtitle {
    color: var(--text-muted);
    font-size: 0.9rem;
    margin-bottom: 1.5rem;
  }

  .form-group {
    margin-bottom: 1rem;
  }

  .form-group label {
    display: block;
    font-size: 0.85rem;
    font-weight: 600;
    color: var(--text-main);
    margin-bottom: 0.5rem;
  }

  .form-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1rem;
  }

  .hint {
    font-size: 0.75rem;
    color: var(--text-muted);
    margin-top: 0.3rem;
    display: block;
  }

  .btn-full {
    width: 100%;
    padding: 0.75rem;
    margin-top: 0.5rem;
    font-size: 0.95rem;
    background: var(--primary);
    color: #FFFFFF;
    border: none;
    border-radius: var(--radius-sm);
    font-weight: 600;
    cursor: pointer;
    transition: all var(--transition-fast);
  }

  .btn-full:hover {
    background: var(--primary-dark);
  }

  .btn-full:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }

  .alert {
    padding: 0.75rem 1rem;
    border-radius: var(--radius-sm);
    font-size: 0.85rem;
    margin-bottom: 1rem;
    animation: slideIn 0.3s ease-out;
  }

  .alert-error {
    background: var(--danger-light);
    color: var(--danger);
    border: 1px solid rgba(239, 68, 68, 0.2);
  }

  .auth-switch {
    text-align: center;
    margin-top: 1.5rem;
    font-size: 0.9rem;
    color: var(--text-muted);
  }

  .link-btn {
    background: none;
    color: var(--primary);
    font-weight: 600;
    font-size: 0.9rem;
    border: none;
    cursor: pointer;
    transition: color var(--transition-fast);
  }

  .link-btn:hover {
    color: var(--primary-dark);
    text-decoration: underline;
  }

  /* ── Responsive ── */
  @media (max-width: 768px) {
    .hero-content { flex-direction: column; gap: 2rem; padding: 2rem; }
    .hero-text h1 { font-size: 2rem; }
    .features { grid-template-columns: 1fr; }
    .form-row { grid-template-columns: 1fr; }
    .top-nav { padding: 1rem; }
    .nav-brand { font-size: 1rem; }
    .nav-icon { width: 24px; height: 24px; }
    .auth-card { padding: 2rem; }
  }
</style>
