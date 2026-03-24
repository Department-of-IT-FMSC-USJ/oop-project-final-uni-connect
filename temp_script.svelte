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
