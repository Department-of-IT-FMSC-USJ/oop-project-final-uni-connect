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
            <svg viewBox="0 0 400 300" fill="none">
              <rect x="50" y="40" width="300" height="200" rx="12" fill="#e0e7ff" opacity="0.5"/>
              <rect x="80" y="70" width="120" height="80" rx="8" fill="#3b82f6" opacity="0.3"/>
              <rect x="220" y="70" width="100" height="80" rx="8" fill="#10b981" opacity="0.3"/>
              <circle cx="140" cy="200" r="30" fill="#1e3a5f" opacity="0.2"/>
              <circle cx="260" cy="200" r="25" fill="#f59e0b" opacity="0.3"/>
              <path d="M100 180 L200 130 L300 170" stroke="#3b82f6" stroke-width="3" fill="none"/>
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
    background: var(--gray-50);
  }

  /* Top Navigation */
  .top-nav {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem 2rem;
    background: white;
    border-bottom: 1px solid var(--gray-200);
    position: sticky;
    top: 0;
    z-index: 50;
  }

  .nav-brand {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-size: 1.25rem;
    font-weight: 700;
    color: var(--primary);
  }

  .nav-icon {
    width: 28px;
    height: 28px;
    color: var(--primary);
  }

  .nav-links {
    display: flex;
    align-items: center;
    gap: 1rem;
  }

  .nav-link {
    background: none;
    color: var(--gray-600);
    font-size: 0.9rem;
    font-weight: 500;
    padding: 0.5rem 1rem;
    border-radius: var(--radius);
    transition: all 0.15s;
  }
  .nav-link:hover { color: var(--primary); }
  .nav-link.active { color: var(--primary); }

  .btn-login {
    background: var(--primary);
    color: white !important;
  }
  .btn-login:hover { background: var(--primary-light); }

  /* Hero */
  .hero {
    max-width: 1200px;
    margin: 0 auto;
    padding: 4rem 2rem;
  }

  .hero-content {
    display: flex;
    align-items: center;
    gap: 4rem;
    background: white;
    border-radius: 16px;
    padding: 3rem;
    box-shadow: var(--shadow);
  }

  .hero-text {
    flex: 1;
  }

  .hero-text h1 {
    font-size: 2.5rem;
    font-weight: 700;
    color: var(--gray-900);
    line-height: 1.2;
    margin-bottom: 1rem;
  }

  .hero-text p {
    font-size: 1.1rem;
    color: var(--gray-500);
    margin-bottom: 2rem;
    line-height: 1.6;
  }

  .hero-btn {
    padding: 0.75rem 2rem;
    font-size: 1rem;
  }

  .hero-image {
    flex: 1;
    display: flex;
    justify-content: center;
  }

  .hero-illustration svg {
    width: 100%;
    max-width: 400px;
  }

  /* Features */
  .features {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 1.5rem;
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 2rem 4rem;
  }

  .feature-card {
    background: white;
    border-radius: 12px;
    padding: 2rem;
    box-shadow: var(--shadow);
    transition: transform 0.2s, box-shadow 0.2s;
  }
  .feature-card:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
  }

  .feature-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 1rem;
  }

  .student-icon { background: #dbeafe; color: #2563eb; }
  .dept-icon { background: #d1fae5; color: #059669; }
  .curric-icon { background: #fef3c7; color: #d97706; }

  .feature-card h3 {
    font-size: 1.125rem;
    font-weight: 600;
    margin-bottom: 0.5rem;
    color: var(--gray-900);
  }

  .feature-card p {
    font-size: 0.875rem;
    color: var(--gray-500);
    line-height: 1.6;
  }

  /* Auth Forms */
  .auth-section {
    display: flex;
    justify-content: center;
    padding: 3rem 2rem;
    min-height: calc(100vh - 72px);
    align-items: flex-start;
  }

  .auth-card {
    background: white;
    border-radius: 12px;
    padding: 2.5rem;
    box-shadow: var(--shadow-md);
    width: 100%;
    max-width: 420px;
  }

  .register-card {
    max-width: 560px;
  }

  .auth-card h2 {
    font-size: 1.5rem;
    font-weight: 700;
    color: var(--gray-900);
    margin-bottom: 0.25rem;
  }

  .auth-subtitle {
    color: var(--gray-500);
    font-size: 0.875rem;
    margin-bottom: 1.5rem;
  }

  .form-group {
    margin-bottom: 1rem;
  }

  .form-group label {
    display: block;
    font-size: 0.8125rem;
    font-weight: 500;
    color: var(--gray-700);
    margin-bottom: 0.375rem;
  }

  .form-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1rem;
  }

  .hint {
    font-size: 0.75rem;
    color: var(--gray-400);
    margin-top: 0.25rem;
    display: block;
  }

  .btn-full {
    width: 100%;
    padding: 0.75rem;
    margin-top: 0.5rem;
    font-size: 0.9375rem;
  }

  .alert {
    padding: 0.75rem 1rem;
    border-radius: var(--radius);
    font-size: 0.8125rem;
    margin-bottom: 1rem;
  }

  .alert-error {
    background: #fee2e2;
    color: #991b1b;
    border: 1px solid #fecaca;
  }

  .auth-switch {
    text-align: center;
    margin-top: 1.5rem;
    font-size: 0.875rem;
    color: var(--gray-500);
  }

  .link-btn {
    background: none;
    color: var(--accent);
    font-weight: 500;
    font-size: 0.875rem;
  }
  .link-btn:hover { text-decoration: underline; }

  @media (max-width: 768px) {
    .hero-content { flex-direction: column; gap: 2rem; }
    .features { grid-template-columns: 1fr; }
    .form-row { grid-template-columns: 1fr; }
  }
</style>
