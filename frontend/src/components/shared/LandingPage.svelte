<script>
  import { login, register, getCurrentUser, getRoleDashboardPath } from '../../lib/api.js';
  import { onMount } from 'svelte';
  import CustomSelect from './CustomSelect.svelte';

  export let initialMode = 'home';

  let mode = initialMode;
  let error = '';
  let loading = false;

  let loginEmail    = '';
  let loginPassword = '';

  let regFullName            = '';
  let regPassword            = '';
  let regConfirmPassword     = '';
  let regRegistrationNumber  = '';
  let regCpmNumber           = '';
  let regPhone               = '';
  let regDepartment          = 'Department of Information Technology';
  let regYearOfStudy         = '1';
  let regProfilePicture      = '';
  let regProfilePicturePreview = '';
  let regEmail               = '';

  const departmentOptions = [
    { value: 'Department of Information Technology',  label: 'Department of Information Technology' },
    { value: 'Department of Computer Science',        label: 'Department of Computer Science' },
    { value: 'Department of Software Engineering',    label: 'Department of Software Engineering' },
    { value: 'Department of Data Science',            label: 'Department of Data Science' }
  ];
  const yearOfStudyOptions = [
    { value: '1', label: '1st Year' },
    { value: '2', label: '2nd Year' },
    { value: '3', label: '3rd Year' },
    { value: '4', label: '4th Year' }
  ];

  $: regEmail = regRegistrationNumber ? `${regRegistrationNumber}@mgt.sjp.ac.lk` : '';

  const problems = [
    { id: 0, text: 'Mentor allocation managed via spreadsheets and email chains' },
    { id: 1, text: 'Session scheduling handled via paper forms and phone calls' },
    { id: 2, text: 'Study notes scattered across WhatsApp groups and personal drives' },
    { id: 3, text: 'No visibility into student engagement or academic progress' },
    { id: 4, text: 'Feedback collected on paper forms, rarely acted upon' }
  ];

  const stations = [
    { id: 0, label: '01', title: 'Connect with Mentors', body: 'Get paired with academic and industry mentors aligned to your department and learning goals.' },
    { id: 1, label: '02', title: 'Join Mentoring Sessions', body: 'Book and attend structured sessions directly from your dashboard.' },
    { id: 2, label: '03', title: 'Access the Note Pool', body: 'Explore shared learning materials and contribute your own notes.' },
    { id: 3, label: '04', title: 'Earn Points & Track Growth', body: 'Build your achievement score and receive feedback on your progress.' }
  ];

  const teamMembers = [
    { initials: 'HH', name: 'Harsha Heshappriya',  color: '#2E4B8F' },
    { initials: 'TM', name: 'Team Member 2',        color: '#3b6b8c' },
    { initials: 'TM', name: 'Team Member 3',        color: '#4a7c8e' },
    { initials: 'TM', name: 'Team Member 4',        color: '#5b6d9e' },
    { initials: 'TM', name: 'Team Member 5',        color: '#3a5a7c' },
    { initials: 'TM', name: 'Team Member 6',        color: '#2d5f8a' },
    { initials: 'TM', name: 'Team Member 7',        color: '#496b9d' },
    { initials: 'TM', name: 'Team Member 8',        color: '#3e5c85' },
    { initials: 'TM', name: 'Team Member 9',        color: '#345283' }
  ];

  /* ── Horizontal winding SVG paths ──────────────────────────────
     Each path meanders across a 1000×350 viewBox.
     Nodes sit at peaks (top) and valleys (bottom) of the curve.
     Cards alternate above/below the path.
  ────────────────────────────────────────────────────────────── */
  const problemPathD = 'M10,200 C70,30 130,30 180,60 C230,90 280,300 360,280 C440,260 480,30 540,60 C600,90 650,300 720,280 C790,260 850,70 900,100 C930,115 960,130 990,130';
  const problemNodes = [
    { x: 18, y: 17, side: 'below' },
    { x: 36, y: 80, side: 'above' },
    { x: 54, y: 17, side: 'below' },
    { x: 72, y: 80, side: 'above' },
    { x: 90, y: 29, side: 'below' }
  ];

  const solutionPathD = 'M10,200 C80,20 140,40 220,70 C300,100 340,300 420,280 C500,260 560,30 640,60 C720,90 780,300 860,270 C910,252 950,200 990,180';
  const solutionNodes = [
    { x: 22, y: 20, side: 'below' },
    { x: 42, y: 80, side: 'above' },
    { x: 64, y: 17, side: 'below' },
    { x: 86, y: 77, side: 'above' }
  ];

  /* Intersection Observer for CTA / team */
  let visibleSections = {};
  function observeSection(node, key) {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          visibleSections = { ...visibleSections, [key]: true };
          observer.unobserve(node);
        }
      },
      { threshold: 0.15 }
    );
    observer.observe(node);
    return { destroy: () => observer.disconnect() };
  }

  /* ── Scroll tracking ──────────────────────────────────────── */
  let problemWrap, solutionWrap;
  let problemFillEl, solutionFillEl;
  let problemLen = 1000, solutionLen = 1000;
  let pProg = 0, sProg = 0;
  let rafPending = false;

  $: problemThresholds = problemNodes.map(n => Math.max(0.06, n.x / 100 - 0.02));
  $: solutionThresholds = solutionNodes.map(n => Math.max(0.06, n.x / 100 - 0.02));

  $: if (problemFillEl) { try { problemLen = problemFillEl.getTotalLength() || 1000; } catch(_) {} }
  $: if (solutionFillEl) { try { solutionLen = solutionFillEl.getTotalLength() || 1000; } catch(_) {} }

  function getProgress(el) {
    if (!el) return 0;
    const rect = el.getBoundingClientRect();
    const travel = el.offsetHeight - window.innerHeight;
    if (travel <= 0) return 0;
    return Math.max(0, Math.min(1, -rect.top / travel));
  }

  function onScroll() {
    if (rafPending) return;
    rafPending = true;
    requestAnimationFrame(() => {
      pProg = getProgress(problemWrap);
      sProg = getProgress(solutionWrap);
      rafPending = false;
    });
  }

  onMount(() => {
    const user = getCurrentUser();
    if (user) window.location.href = getRoleDashboardPath(user.role);
    window.addEventListener('scroll', onScroll, { passive: true });
    return () => window.removeEventListener('scroll', onScroll);
  });

  async function handleLogin() {
    error = ''; loading = true;
    try {
      const data = await login(loginEmail, loginPassword);
      window.location.href = getRoleDashboardPath(data.role);
    } catch (e) {
      error = e.data?.message || e.message || 'Login failed';
    } finally { loading = false; }
  }

  async function handleRegister() {
    error = '';
    if (regPassword !== regConfirmPassword) { error = 'Passwords do not match'; return; }
    const mc = (regRegistrationNumber || '').trim();
    const cpm = (regCpmNumber || '').trim();
    if (!/^\d{6}$/.test(mc))  { error = 'MC number must be exactly 6 digits.'; return; }
    if (!/^\d{5}$/.test(cpm)) { error = 'CPM number must be exactly 5 digits.'; return; }
    if (!regProfilePicture)   { error = 'Profile picture is required.'; return; }
    if (!regEmail.toLowerCase().endsWith('@mgt.sjp.ac.lk')) {
      error = 'Email must be in the format <MC>@mgt.sjp.ac.lk'; return;
    }
    loading = true;
    try {
      const data = await register({
        fullName: regFullName, email: regEmail,
        password: regPassword, confirmPassword: regConfirmPassword,
        role: 'UNDERGRADUATE',
        registrationNumber: mc, cpmNumber: cpm,
        phone: regPhone, department: regDepartment,
        yearOfStudy: regYearOfStudy, profilePicture: regProfilePicture
      });
      window.location.href = getRoleDashboardPath(data.role);
    } catch (e) {
      error = e.data?.message || e.message || 'Registration failed';
    } finally { loading = false; }
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

  <nav class="top-nav">
    <div class="nav-inner">
      <a href="/" class="nav-brand" aria-label="UniConnect home">
        <img src="/logo.jpg" alt="UniConnect" class="nav-logo" />
      </a>
      <div class="nav-links">
        <a href="/" class="nav-link" class:active={mode === 'home'}>Home</a>
        <a href="/login" class="nav-link btn-login">Login</a>
      </div>
    </div>
  </nav>

  {#if mode === 'home'}

    <!-- ═══ HERO ═══════════════════════════════════════════════════ -->
    <section class="hero">
      <div class="hero-bg" aria-hidden="true"></div>
      <div class="hero-content">
        <span class="eyebrow">Faculty of Management Studies</span>
        <h1 class="hero-heading">Your Academic<br/>Journey Starts Here.</h1>
        <p class="hero-tagline">Connect &nbsp;&middot;&nbsp; Grow &nbsp;&middot;&nbsp; Lead</p>
        <a href="/register" class="btn-cta">
          Get Started
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none"
               stroke="currentColor" stroke-width="2.5" aria-hidden="true">
            <line x1="5" y1="12" x2="19" y2="12"/>
            <polyline points="12 5 19 12 12 19"/>
          </svg>
        </a>
      </div>
    </section>

    <!-- ═══ PROBLEM — Horizontal Winding Roadmap ═══════════════════ -->
    <div class="scroll-wrap" bind:this={problemWrap}>
      <div class="scroll-sticky">
        <div class="scroll-content">
          <span class="section-eyebrow">The Problem</span>
          <h2 class="section-heading">How it works today</h2>

          <div class="hroad">
            <svg class="hroad-svg" viewBox="0 0 1000 350" preserveAspectRatio="xMidYMid meet" aria-hidden="true">
              <path class="hroad-track" d={problemPathD} />
              <path class="hroad-fill" d={problemPathD} bind:this={problemFillEl}
                    stroke-dasharray={problemLen}
                    stroke-dashoffset={problemLen * (1 - pProg)} />
            </svg>

            {#each problems as p, i (p.id)}
              {@const n = problemNodes[i]}
              {@const active = pProg >= problemThresholds[i]}
              <div class="hroad-node" class:active
                   style="left:{n.x}%; top:{n.y}%">
                <div class="hroad-dot"></div>
                <div class="hroad-card" class:above={n.side === 'above'}>
                  <p>{p.text}</p>
                </div>
              </div>
            {/each}
          </div>
        </div>
      </div>
    </div>

    <!-- ═══ SOLUTION — Horizontal Winding Roadmap ══════════════════ -->
    <div class="scroll-wrap" bind:this={solutionWrap}>
      <div class="scroll-sticky scroll-sticky--alt">
        <div class="scroll-content">
          <span class="solution-chip">UniConnect eliminates all of this</span>
          <h2 class="section-heading">One platform. Every mentoring touchpoint.</h2>

          <div class="hroad">
            <svg class="hroad-svg" viewBox="0 0 1000 350" preserveAspectRatio="xMidYMid meet" aria-hidden="true">
              <path class="hroad-track" d={solutionPathD} />
              <path class="hroad-fill hroad-fill--accent" d={solutionPathD} bind:this={solutionFillEl}
                    stroke-dasharray={solutionLen}
                    stroke-dashoffset={solutionLen * (1 - sProg)} />
            </svg>

            {#each stations as s, i (s.id)}
              {@const n = solutionNodes[i]}
              {@const active = sProg >= solutionThresholds[i]}
              <div class="hroad-node" class:active
                   style="left:{n.x}%; top:{n.y}%">
                <div class="hroad-dot hroad-dot--num">
                  <span>{i + 1}</span>
                </div>
                <div class="hroad-card hroad-card--rich" class:above={n.side === 'above'}>
                  <span class="hcard-label">{s.label}</span>
                  <h3 class="hcard-title">{s.title}</h3>
                  <p class="hcard-body">{s.body}</p>
                </div>
              </div>
            {/each}
          </div>
        </div>
      </div>
    </div>

    <!-- ═══ CTA ════════════════════════════════════════════════════ -->
    <section class="cta-section" use:observeSection={'cta'} class:visible={visibleSections['cta']}>
      <span class="eyebrow">Faculty of Management Studies</span>
      <h2 class="cta-heading">Your Academic<br/>Journey Starts Here.</h2>
      <p class="cta-sub">Everything you need to connect, learn, grow, and lead — all in one place.</p>
      <a href="/register" class="btn-cta">
        Get Started
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none"
             stroke="currentColor" stroke-width="2.5" aria-hidden="true">
          <line x1="5" y1="12" x2="19" y2="12"/>
          <polyline points="12 5 19 12 12 19"/>
        </svg>
      </a>
    </section>

    <!-- ═══ TEAM FOOTER ════════════════════════════════════════════ -->
    <footer class="team-footer" use:observeSection={'team'} class:visible={visibleSections['team']}>
      <p class="team-label">Built by</p>
      <div class="avatar-group" role="list" aria-label="9 team members">
        {#each teamMembers as m, i (i)}
          <div class="avatar-wrap" role="listitem" style="z-index:{teamMembers.length - i}">
            <div class="avatar" style="background:{m.color}">{m.initials}</div>
            <span class="avatar-tooltip" role="tooltip">{m.name}</span>
          </div>
        {/each}
      </div>
    </footer>

    <!-- ═══ GIANT BRAND FOOTER ═════════════════════════════════════ -->
    <div class="brand-footer">
      <span class="brand-giant">Uni Connect</span>
    </div>

  {:else if mode === 'login'}
    <section class="auth-section">
      <div class="auth-card">
        <h2>Welcome Back</h2>
        <p class="auth-subtitle">Sign in to your account</p>
        {#if error}<div class="alert alert-error">{error}</div>{/if}
        <form on:submit|preventDefault={handleLogin}>
          <div class="form-group">
            <label for="login-email">Email</label>
            <input id="login-email" type="email" class="input"
                   placeholder="you@university.edu" bind:value={loginEmail} required />
          </div>
          <div class="form-group">
            <label for="login-password">Password</label>
            <input id="login-password" type="password" class="input"
                   placeholder="Enter your password" bind:value={loginPassword} required />
          </div>
          <button type="submit" class="btn btn-primary btn-full" disabled={loading}>
            {loading ? 'Signing in…' : 'Sign In'}
          </button>
        </form>
        <p class="auth-switch">
          New undergraduate student?
          <a href="/register" class="link-btn">Create an account</a>
        </p>
      </div>
    </section>

  {:else if mode === 'register'}
    <section class="auth-section">
      <div class="auth-card register-card">
        <h2>Create Your Account</h2>
        <p class="auth-subtitle">Undergraduate registration</p>
        {#if error}<div class="alert alert-error">{error}</div>{/if}
        <form on:submit|preventDefault={handleRegister}>
          <div class="form-row">
            <div class="form-group">
              <label for="reg-name">Full Name</label>
              <input id="reg-name" type="text" class="input" placeholder="John Doe"
                     bind:value={regFullName} required />
            </div>
            <div class="form-group">
              <label for="reg-phone">Phone</label>
              <input id="reg-phone" type="tel" class="input" placeholder="+94 71 234 5678"
                     bind:value={regPhone} />
            </div>
          </div>
          <div class="form-group">
            <label for="reg-mc">Registration Number (MC Number)</label>
            <input id="reg-mc" type="text" class="input" placeholder="123456"
                   bind:value={regRegistrationNumber} required maxlength="6" inputmode="numeric" />
          </div>
          <div class="form-group">
            <label for="reg-email">University Email</label>
            <input id="reg-email" type="input" class="input" bind:value={regEmail} readonly required />
            <small class="hint">Format: &lt;MC&gt;@mgt.sjp.ac.lk</small>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label for="reg-dept">Department</label>
              <CustomSelect id="reg-dept" options={departmentOptions} bind:value={regDepartment} />
            </div>
            <div class="form-group">
              <label for="reg-year">Year of Study</label>
              <CustomSelect id="reg-year" options={yearOfStudyOptions} bind:value={regYearOfStudy} />
            </div>
          </div>
          <div class="form-group">
            <label for="reg-cpm">CPM Number (5 digits)</label>
            <input id="reg-cpm" type="text" class="input" placeholder="12345"
                   bind:value={regCpmNumber} required maxlength="5" inputmode="numeric" />
          </div>
          <div class="form-group">
            <label for="reg-profile">Profile Picture</label>
            <input id="reg-profile" type="file" class="input" accept="image/*"
                   on:change={handleProfilePictureChange} required />
            {#if regProfilePicturePreview}
              <div style="margin-top:0.75rem">
                <img src={regProfilePicturePreview} alt="Profile preview"
                     style="width:72px;height:72px;border-radius:12px;object-fit:cover" />
              </div>
            {/if}
          </div>
          <div class="form-row">
            <div class="form-group">
              <label for="reg-pass">Password</label>
              <input id="reg-pass" type="password" class="input" placeholder="Min 6 characters"
                     bind:value={regPassword} required minlength="6" />
            </div>
            <div class="form-group">
              <label for="reg-confirm">Confirm Password</label>
              <input id="reg-confirm" type="password" class="input" placeholder="Re-enter password"
                     bind:value={regConfirmPassword} required />
            </div>
          </div>
          <button type="submit" class="btn btn-primary btn-full" disabled={loading}>
            {loading ? 'Creating account…' : 'Create Account'}
          </button>
        </form>
        <p class="auth-switch">
          Already have an account? <a href="/login" class="link-btn">Sign in</a>
        </p>
      </div>
    </section>
  {/if}

</div>

<style>
  .landing { min-height: 100vh; background: var(--bg-main); }

  /* ── Navigation ─────────────────────────────────────────────── */
  .top-nav {
    display: flex; align-items: center;
    background: var(--bg-main);
    border-bottom: 1px solid var(--border-light);
    position: sticky; top: 0; z-index: 50;
    width: 100%; height: 56px;
  }
  .nav-inner {
    display: flex; justify-content: space-between; align-items: center;
    width: 100%; max-width: 1000px; margin: 0 auto; padding: 0 1.5rem;
  }
  .nav-brand { display: flex; align-items: center; text-decoration: none; transition: opacity var(--transition-fast); }
  .nav-brand:hover { opacity: 0.8; }
  .nav-logo { height: 38px; width: auto; display: block; object-fit: contain; }
  .nav-links { display: flex; align-items: center; gap: 0.4rem; }
  .nav-link {
    color: var(--text-secondary); font-size: 0.8rem; font-weight: 500;
    padding: 0.45rem 0.85rem; border-radius: var(--radius-sm);
    border: 1px solid transparent; transition: all var(--transition-fast);
    text-decoration: none; background: none;
  }
  .nav-link:hover  { color: var(--primary); background: var(--primary-50); text-decoration: none; }
  .nav-link.active { color: var(--primary); font-weight: 600; }
  .btn-login { background: var(--primary); color: white !important; border: 1px solid var(--primary); }
  .btn-login:hover { background: var(--primary-light); border-color: var(--primary-light); text-decoration: none; }

  /* ── Shared tokens ──────────────────────────────────────────── */
  .eyebrow {
    display: block; font-size: 0.68rem; font-weight: 600;
    letter-spacing: 0.14em; text-transform: uppercase;
    color: var(--primary); opacity: 0.7; margin-bottom: 1rem;
  }
  .section-eyebrow {
    display: inline-block; font-size: 0.65rem; font-weight: 700;
    letter-spacing: 0.16em; text-transform: uppercase;
    color: var(--primary); opacity: 0.7; margin-bottom: 0.7rem;
  }
  .section-heading {
    font-family: var(--font-heading);
    font-size: clamp(1.7rem, 4vw, 2.6rem); font-weight: 700;
    letter-spacing: -0.03em; line-height: 1.1;
    color: var(--text-main); margin-bottom: 1.5rem;
  }
  .btn-cta {
    display: inline-flex; align-items: center; gap: 0.5rem;
    padding: 0.8rem 2.2rem; font-size: 0.9rem; font-weight: 600;
    border-radius: var(--radius); box-shadow: var(--shadow-md);
    text-decoration: none; color: #fff; background: var(--primary);
    transition: box-shadow 0.2s ease, transform 0.2s ease, background 0.2s ease;
  }
  .btn-cta:hover { text-decoration: none; background: var(--primary-light); box-shadow: var(--shadow-lg); transform: translateY(-2px); }

  .solution-chip {
    display: inline-block; margin-bottom: 1.75rem;
    font-size: 0.64rem; font-weight: 700;
    letter-spacing: 0.13em; text-transform: uppercase;
    color: var(--primary); background: var(--primary-50);
    border: 1px solid rgba(46, 75, 143, 0.14);
    border-radius: 99px; padding: 0.3rem 0.9rem;
  }

  /* ═══════════════════════════════════════════════════════════════
     HERO
     ═══════════════════════════════════════════════════════════════ */
  .hero {
    position: relative; overflow: hidden;
    display: flex; align-items: center; justify-content: center;
    min-height: calc(100vh - 56px); padding: 4rem 1.5rem;
  }
  .hero-bg {
    position: absolute; inset: -6% 0;
    background-image: radial-gradient(circle, var(--border-light) 1.2px, transparent 1.2px);
    background-size: 30px 30px; pointer-events: none;
  }
  .hero-bg::after {
    content: ''; position: absolute; inset: 0;
    background: radial-gradient(ellipse 70% 60% at 50% 50%, transparent 30%, var(--bg-main) 100%);
    pointer-events: none;
  }
  .hero-content {
    position: relative; z-index: 5;
    text-align: center; max-width: 680px;
    animation: heroFadeIn 0.8s ease-out;
  }
  .hero-heading {
    font-family: var(--font-heading);
    font-size: clamp(2.6rem, 6vw, 5.2rem); font-weight: 700;
    letter-spacing: -0.04em; line-height: 1.05;
    color: var(--text-main); margin-bottom: 1rem;
  }
  .hero-tagline {
    font-size: 0.82rem; color: var(--text-muted);
    letter-spacing: 0.28em; text-transform: uppercase;
    font-weight: 500; margin-bottom: 2.5rem;
  }
  @keyframes heroFadeIn {
    from { opacity: 0; transform: translateY(20px); }
    to   { opacity: 1; transform: translateY(0); }
  }

  /* ═══════════════════════════════════════════════════════════════
     SCROLL-DRIVEN SECTIONS (shared)
     ═══════════════════════════════════════════════════════════════ */
  .scroll-wrap {
    position: relative;
    height: 350vh;
  }
  .scroll-sticky {
    position: sticky;
    top: 56px;
    height: calc(100vh - 56px);
    display: flex; align-items: center; justify-content: center;
    background: var(--bg-main);
    overflow: hidden;
  }
  .scroll-sticky--alt {
    background: var(--bg-alt, var(--bg-main));
  }
  .scroll-content {
    width: 94%; max-width: 960px;
    text-align: center;
  }

  /* ═══════════════════════════════════════════════════════════════
     HORIZONTAL ROADMAP (shared by problem + solution)
     ═══════════════════════════════════════════════════════════════ */
  .hroad {
    position: relative;
    width: 100%;
    margin-top: 0.5rem;
  }
  .hroad-svg {
    width: 100%; height: auto; display: block;
  }

  /* Dashed track */
  .hroad-track {
    fill: none;
    stroke: var(--border-light);
    stroke-width: 3.5;
    stroke-dasharray: 14 12;
    stroke-linecap: round;
  }

  /* Solid fill that draws in */
  .hroad-fill {
    fill: none;
    stroke: #6b7280;
    stroke-width: 3.5;
    stroke-linecap: round;
    will-change: stroke-dashoffset;
    filter: drop-shadow(0 0 3px rgba(107,114,128,0.3));
  }
  .hroad-fill--accent {
    stroke: var(--primary);
    filter: drop-shadow(0 0 3px rgba(46,75,143,0.35));
  }

  /* ── Node (dot + card) ─────────────────────────────────────── */
  .hroad-node {
    position: absolute;
    transform: translate(-50%, -50%);
    z-index: 10;
  }

  .hroad-dot {
    width: 16px; height: 16px;
    border-radius: 50%;
    background: var(--bg-main);
    border: 2.5px solid var(--border-light);
    transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
    position: relative; z-index: 2;
  }
  .hroad-node.active .hroad-dot {
    border-color: var(--primary);
    background: var(--primary);
    box-shadow: 0 0 0 6px var(--primary-50);
    transform: scale(1.2);
  }

  .hroad-dot--num {
    width: 28px; height: 28px;
    display: flex; align-items: center; justify-content: center;
  }
  .hroad-dot--num span {
    font-size: 0.6rem; font-weight: 700;
    color: transparent; transition: color 0.3s ease;
  }
  .hroad-node.active .hroad-dot--num span { color: white; }

  /* ── Card ───────────────────────────────────────────────────── */
  .hroad-card {
    position: absolute;
    left: 50%;
    top: calc(100% + 14px);
    transform: translateX(-50%) translateY(12px);
    width: clamp(125px, 13vw, 170px);
    background: var(--bg-main);
    border: 1px solid var(--border-light);
    border-radius: 10px;
    padding: 0.65rem 0.8rem;
    box-shadow: 0 2px 12px rgba(0,0,0,0.06);
    text-align: left;
    opacity: 0;
    pointer-events: none;
    transition: opacity 0.4s ease, transform 0.4s ease;
  }
  .hroad-node.active .hroad-card {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
    pointer-events: auto;
  }

  /* Card above the dot */
  .hroad-card.above {
    top: auto;
    bottom: calc(100% + 14px);
    transform: translateX(-50%) translateY(-12px);
  }
  .hroad-node.active .hroad-card.above {
    transform: translateX(-50%) translateY(0);
  }

  /* Problem card text */
  .hroad-card p {
    font-size: 0.7rem; color: var(--text-secondary);
    line-height: 1.5; margin: 0;
  }

  /* Solution card (richer) */
  .hroad-card--rich { width: clamp(140px, 15vw, 190px); }

  .hcard-label {
    font-size: 0.52rem; font-weight: 700;
    letter-spacing: 0.12em; text-transform: uppercase;
    color: var(--primary); opacity: 0.6;
    display: block; margin-bottom: 0.1rem;
  }
  .hcard-title {
    font-size: 0.78rem; font-weight: 700;
    color: var(--text-main); line-height: 1.3;
    margin-bottom: 0.2rem;
  }
  .hcard-body {
    font-size: 0.65rem; color: var(--text-secondary);
    line-height: 1.5; margin: 0;
  }

  /* ═══════════════════════════════════════════════════════════════
     CTA SECTION
     ═══════════════════════════════════════════════════════════════ */
  .cta-section {
    padding: 6rem 1.5rem;
    text-align: center;
    border-top: 1px solid var(--border-light);
    background: var(--bg-main);
    opacity: 0; transform: translateY(20px);
    transition: opacity 0.7s ease, transform 0.7s ease;
  }
  .cta-section.visible { opacity: 1; transform: translateY(0); }

  .cta-heading {
    font-family: var(--font-heading);
    font-size: clamp(2.2rem, 5.5vw, 4.2rem); font-weight: 700;
    letter-spacing: -0.04em; line-height: 1.05;
    color: var(--text-main); margin-bottom: 1rem;
  }
  .cta-sub {
    font-size: 0.88rem; color: var(--text-secondary); line-height: 1.6;
    max-width: 420px; margin: 0 auto 2rem;
  }

  /* ═══════════════════════════════════════════════════════════════
     TEAM FOOTER
     ═══════════════════════════════════════════════════════════════ */
  .team-footer {
    display: flex; flex-direction: column;
    align-items: center; justify-content: center;
    padding: 3.5rem 1.5rem;
    border-top: 1px solid var(--border-light);
    background: var(--bg-main);
    gap: 0.9rem;
    opacity: 0; transform: translateY(12px);
    transition: opacity 0.6s ease, transform 0.6s ease;
  }
  .team-footer.visible { opacity: 1; transform: translateY(0); }

  .team-label {
    font-size: 0.68rem; font-weight: 600;
    letter-spacing: 0.12em; text-transform: uppercase;
    color: var(--text-muted);
  }
  .avatar-group { display: flex; align-items: center; }
  .avatar-wrap {
    position: relative; margin-left: -10px; cursor: default;
  }
  .avatar-wrap:first-child { margin-left: 0; }
  .avatar {
    width: 44px; height: 44px; border-radius: 50%;
    border: 2.5px solid var(--bg-main);
    display: flex; align-items: center; justify-content: center;
    font-size: 0.62rem; font-weight: 700; color: white;
    letter-spacing: 0.03em;
    box-shadow: 0 1px 3px rgba(0,0,0,0.15);
    transition: transform 0.18s ease;
  }
  .avatar-wrap:hover .avatar { transform: translateY(-5px) scale(1.08); }
  .avatar-tooltip {
    position: absolute; bottom: calc(100% + 8px);
    left: 50%; transform: translateX(-50%);
    background: #111827; color: white;
    font-size: 0.63rem; font-weight: 500;
    padding: 0.28rem 0.6rem; border-radius: 5px;
    white-space: nowrap; opacity: 0; pointer-events: none;
    transition: opacity 0.15s ease;
  }
  .avatar-tooltip::after {
    content: ''; position: absolute;
    top: 100%; left: 50%; transform: translateX(-50%);
    border: 4px solid transparent; border-top-color: #111827;
  }
  .avatar-wrap:hover .avatar-tooltip { opacity: 1; }

  /* ═══════════════════════════════════════════════════════════════
     GIANT BRAND FOOTER
     ═══════════════════════════════════════════════════════════════ */
  .brand-footer {
    padding: 3rem 1.5rem 4rem;
    text-align: center; background: var(--bg-main); overflow: hidden;
  }
  .brand-giant {
    font-family: var(--font-heading);
    font-size: clamp(3.5rem, 12vw, 10rem);
    font-weight: 900; letter-spacing: -0.04em; line-height: 1;
    color: var(--text-main); opacity: 0.08; user-select: none;
  }

  /* ═══════════════════════════════════════════════════════════════
     AUTH FORMS
     ═══════════════════════════════════════════════════════════════ */
  .auth-section {
    display: flex; justify-content: center;
    padding: 2.5rem 1.5rem;
    min-height: calc(100vh - 56px);
    align-items: flex-start; background: var(--bg-alt);
    animation: fadeIn 0.4s ease-out;
  }
  .auth-card {
    background: var(--bg-main); border-radius: var(--radius-lg);
    padding: 2.5rem; box-shadow: var(--shadow-md);
    border: 1px solid var(--border-light);
    width: 100%; max-width: 400px;
    animation: slideIn 0.5s ease-out;
  }
  .register-card { max-width: 520px; }
  .auth-card h2 { font-size: 1.3rem; font-weight: 700; color: var(--text-main); margin-bottom: 0.2rem; font-family: var(--font-heading); }
  .auth-subtitle { color: var(--text-muted); font-size: 0.8rem; margin-bottom: 1.25rem; }
  .form-group { margin-bottom: 0.85rem; }
  .form-group label { display: block; font-size: 0.78rem; font-weight: 600; color: var(--text-main); margin-bottom: 0.35rem; }
  .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 0.85rem; }
  .hint { font-size: 0.7rem; color: var(--text-muted); margin-top: 0.25rem; display: block; }
  .btn-full {
    width: 100%; padding: 0.6rem; margin-top: 0.4rem;
    font-size: 0.85rem; background: var(--primary); color: #FFFFFF;
    border: none; border-radius: var(--radius-sm); font-weight: 600;
    cursor: pointer; transition: all var(--transition-fast);
  }
  .btn-full:hover    { background: var(--primary-dark); }
  .btn-full:disabled { opacity: 0.6; cursor: not-allowed; }
  .alert { padding: 0.6rem 0.85rem; border-radius: var(--radius-sm); font-size: 0.8rem; margin-bottom: 0.85rem; animation: slideIn 0.3s ease-out; }
  .alert-error { background: var(--danger-light); color: var(--danger); border: 1px solid rgba(239, 68, 68, 0.2); }
  .auth-switch { text-align: center; margin-top: 1.25rem; font-size: 0.8rem; color: var(--text-muted); }
  .link-btn { color: var(--primary); font-weight: 600; font-size: 0.8rem; text-decoration: none; transition: color var(--transition-fast); }
  .link-btn:hover { color: var(--primary-dark); text-decoration: none; }

  /* ═══════════════════════════════════════════════════════════════
     RESPONSIVE
     ═══════════════════════════════════════════════════════════════ */
  @media (max-width: 768px) {
    .hero { min-height: auto; padding: 5rem 1.5rem; }
    .hero-heading { font-size: clamp(2rem, 7vw, 3rem); }
    .cta-heading { font-size: clamp(1.9rem, 6.5vw, 2.8rem); }
    .nav-inner { padding: 0 0.75rem; }
    .nav-logo { height: 32px; }
    .form-row { grid-template-columns: 1fr; }
    .auth-card { padding: 1.75rem; }
    .brand-giant { font-size: clamp(2.5rem, 14vw, 6rem); }
    .scroll-wrap { height: 300vh; }
    .hroad-card { width: clamp(105px, 18vw, 145px); padding: 0.55rem 0.65rem; }
    .hroad-card--rich { width: clamp(115px, 20vw, 155px); }
    .hroad-card p, .hcard-body { font-size: 0.62rem; }
  }

  @media (max-width: 580px) {
    .hero { padding: 3rem 1rem; }
    .cta-section { padding: 3rem 1rem; }
    .scroll-wrap { height: auto; }
    .scroll-sticky {
      position: static; height: auto;
      padding: 3rem 1rem;
    }
    .hroad-svg { display: none; }
    .hroad { display: flex; flex-direction: column; gap: 1rem; }
    .hroad-node {
      position: static; transform: none;
      display: flex; align-items: flex-start; gap: 0.8rem;
    }
    .hroad-dot { flex-shrink: 0; }
    .hroad-node.active .hroad-dot { transform: none; }
    .hroad-card, .hroad-card.above {
      position: static; transform: none;
      width: 100%; opacity: 1; pointer-events: auto;
    }
    .hroad-node.active .hroad-card,
    .hroad-node.active .hroad-card.above { transform: none; }
    .avatar { width: 38px; height: 38px; font-size: 0.58rem; }
    .avatar-wrap { margin-left: -8px; }
    .auth-section { padding: 1.5rem 1rem; }
    .auth-card { padding: 1.25rem; }
  }
</style>
