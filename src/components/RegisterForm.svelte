<script>
    import { onMount } from "svelte";
    
    export let role = "student";

    let firstName = "";
    let lastName = "";
    let email = "";
    let phone = "";
    let password = "";
    let confirmPassword = "";
    let profilePicture = "";
    let registrationNumber = "";
    let cpmNumber = "";
    let yearOfStudy = "";

    let errorMessage = "";
    let passwordMatchError = "";
    let emailError = "";
    let cpmError = "";
    let mcError = "";
    let phoneError = "";
    let successMessage = "";
    let isLoading = false;

    $: {
        // Real-time CPM validation (5 digits only)
        if (role === "student" && cpmNumber && cpmNumber.trim()) {
            const cpmPattern = /^\d{5}$/;
            if (!cpmPattern.test(cpmNumber)) {
                cpmError = "CPM number must be exactly 5 digits";
            } else {
                cpmError = "";
            }
        } else {
            cpmError = "";
        }
    }

    $: {
        // Real-time MC number validation (6 digits only)
        if (role === "student" && registrationNumber) {
            const mcPattern = /^\d{6}$/;
            if (!mcPattern.test(registrationNumber)) {
                mcError = "MC number must be exactly 6 digits";
            } else {
                mcError = "";
            }
        } else {
            mcError = "";
        }
    }

    $: {
        // Real-time phone validation (10-12 digits)
        if (phone && phone.trim()) {
            const phonePattern = /^\d{10,12}$/;
            if (!phonePattern.test(phone.replace(/[-()\s]/g, ""))) {
                phoneError = "Phone number must be 10-12 digits";
            } else {
                phoneError = "";
            }
        } else {
            phoneError = "";
        }
    }

    $: {
        // Real-time password match validation
        if (confirmPassword && password !== confirmPassword) {
            passwordMatchError = "Passwords do not match";
        } else if (confirmPassword && password === confirmPassword) {
            passwordMatchError = "";
        } else if (!confirmPassword) {
            passwordMatchError = "";
        }
    }

    $: {
        // Real-time email format validation for students
        if (role === "student" && email) {
            const emailPattern = /^\d{6}@mgt\.sjp\.ac\.lk$/;
            if (!emailPattern.test(email)) {
                emailError = "Email must be in format: {MC Number}@mgt.sjp.ac.lk (e.g., 112233@mgt.sjp.ac.lk)";
            } else {
                emailError = "";
            }
        } else if (role !== "student") {
            // For non-students, just check if it's a valid email
            const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (email && !emailPattern.test(email)) {
                emailError = "Please enter a valid email address";
            } else {
                emailError = "";
            }
        } else {
            emailError = "";
        }
    }

    const formatRole = (r) => {
        switch (r) {
            case "student":
                return "Student";
            case "academic":
                return "Academic Mentor";
            case "alumni":
                return "Alumni / Industry Mentor";
            case "dept-rep":
                return "Department Representative";
            case "dept-head":
                return "Department Head";
            default:
                return "User";
        }
    };

    // Convert parameter role back to backend enum
    const getBackendRole = (r) => {
        switch (r) {
            case "student":
                return "UNDERGRADUATE";
            case "academic":
                return "ACADEMIC_MENTOR";
            case "alumni":
                return "ALUMNI_MENTOR";
            case "dept-rep":
                return "DEPARTMENT_REP";
            case "dept-head":
                return "DEPARTMENT_HEAD";
            default:
                return "UNDERGRADUATE";
        }
    };

    const handleFileChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onloadend = () => {
                profilePicture = reader.result;
            };
            reader.readAsDataURL(file);
        }
    };

    async function handleRegister() {
        isLoading = true;
        errorMessage = "";
        successMessage = "";

        // Validate email format
        if (role === "student") {
            const emailPattern = /^\d{6}@mgt\.sjp\.ac\.lk$/;
            if (!emailPattern.test(email)) {
                errorMessage = "Please use your university email in the format: {6-digit MC Number}@mgt.sjp.ac.lk";
                isLoading = false;
                return;
            }

            // Validate MC number is 6 digits
            const mcPattern = /^\d{6}$/;
            if (!mcPattern.test(registrationNumber)) {
                errorMessage = "MC number must be exactly 6 digits";
                isLoading = false;
                return;
            }

            // Validate CPM number is 5 digits
            const cpmPattern = /^\d{5}$/;
            if (!cpmPattern.test(cpmNumber)) {
                errorMessage = "CPM number must be exactly 5 digits";
                isLoading = false;
                return;
            }
        }

        // Validate phone number
        if (phone) {
            const phonePattern = /^\d{10,12}$/;
            if (!phonePattern.test(phone.replace(/[-()\s]/g, ""))) {
                errorMessage = "Phone number must be 10-12 digits";
                isLoading = false;
                return;
            }
        }

        // Validate password match
        if (password !== confirmPassword) {
            errorMessage = "Passwords do not match. Please try again.";
            isLoading = false;
            return;
        }

        try {
            const response = await fetch(
                "http://localhost:8080/api/auth/register",
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({
                        fullName: `${firstName} ${lastName}`.trim(),
                        email,
                        password,
                        confirmPassword,
                        role: getBackendRole(role),
                        phone,
                        profilePicture,
                        registrationNumber,
                        cpmNumber,
                        yearOfStudy,
                    }),
                },
            );

            const responseText = await response.text();
            let data = {};
            try {
                data = JSON.parse(responseText);
            } catch (e) {
                data = { message: responseText };
            }

            if (response.ok) {
                // Successful registration
                successMessage = "Registration successful! You may now login.";
                // Clear form
                firstName = "";
                lastName = "";
                email = "";
                phone = "";
                password = "";
                confirmPassword = "";
                profilePicture = "";
                registrationNumber = "";
                cpmNumber = "";
                yearOfStudy = "";

                // Redirect after a short delay
                setTimeout(() => {
                    window.location.href = `/login/${role}`;
                }, 2000);
            } else {
                // Registration failed
                errorMessage =
                    data.message ||
                    "Registration failed. Please check your details.";
            }
        } catch (error) {
            console.error("Registration error:", error);
            errorMessage = "A server error occurred. Please try again later.";
        } finally {
            isLoading = false;
        }
    }
</script>

<div class="auth-wrapper">
    <div class="glass-panel auth-card">
        <h2 class="auth-title">Create an Account</h2>
        <p class="auth-subtitle">Registering as a {formatRole(role)}</p>

        {#if errorMessage}
            <div class="alert error-alert">
                {errorMessage}
            </div>
        {/if}

        {#if successMessage}
            <div class="alert success-alert">
                {successMessage}
            </div>
        {/if}

        <form class="auth-form" on:submit|preventDefault={handleRegister}>
            <div class="form-row">
                <div class="form-group half-width">
                    <label class="form-label" for="firstName">First Name</label>
                    <input
                        class="form-control"
                        type="text"
                        id="firstName"
                        bind:value={firstName}
                        required
                    />
                </div>
                <div class="form-group half-width">
                    <label class="form-label" for="lastName">Last Name</label>
                    <input
                        class="form-control"
                        type="text"
                        id="lastName"
                        bind:value={lastName}
                        required
                    />
                </div>
            </div>

            <div class="form-group">
                <label class="form-label" for="email">
                    {#if role === "student"}
                        University Email Address

                    {:else}
                        Email address
                    {/if}
                </label>
                <input
                    class="form-control {emailError ? 'input-error' : ''}"
                    type="email"
                    id="email"
                    placeholder={role === "student" ? "e.g., 112233@mgt.sjp.ac.lk" : "your.email@example.com"}
                    bind:value={email}
                    required
                />
                {#if emailError}
                    <div class="email-error">
                        ✗ {emailError}
                    </div>
                {:else if role === "student" && email && email.match(/^\d{6}@mgt\.sjp\.ac\.lk$/)}
                    <div class="email-success">
                        ✓ Valid university email
                    </div>
                {/if}
                {#if role === "student"}
                    <div class="email-info">
                        💡 Use your MC number (6 digits) followed by @mgt.sjp.ac.lk. This email will be used for all important communications.
                    </div>
                {/if}
            </div>

            <div class="form-group">
                <label class="form-label" for="phone">Phone Number</label>
                <input
                    class="form-control {phoneError ? 'input-error' : ''}"
                    type="tel"
                    id="phone"
                    bind:value={phone}
                    required
                />
                {#if phoneError}
                    <div class="phone-error">
                        ✗ {phoneError}
                    </div>
                {:else if phone && !phoneError}
                    <div class="phone-success">
                        ✓ Valid phone number
                    </div>
                {/if}
            </div>

            {#if role === "student"}
                <div class="form-row">
                    <div class="form-group half-width">
                        <label class="form-label" for="registrationNumber"
                            >Registration (MC) Number</label
                        >
                        <input
                            class="form-control {mcError ? 'input-error' : ''}"
                            type="text"
                            id="registrationNumber"
                            placeholder="e.g., 112233"
                            bind:value={registrationNumber}
                            maxlength="6"
                            required
                        />
                        {#if mcError}
                            <div class="mc-error">
                                ✗ {mcError}
                            </div>
                        {:else if registrationNumber && !mcError}
                            <div class="mc-success">
                                ✓ Valid MC number
                            </div>
                        {/if}
                    </div>
                    <div class="form-group half-width">
                        <label class="form-label" for="cpmNumber"
                            >CPM Number</label
                        >
                        <input
                            class="form-control {cpmError ? 'input-error' : ''}"
                            type="text"
                            id="cpmNumber"
                            placeholder="e.g., 54321"
                            bind:value={cpmNumber}
                            maxlength="5"
                            required
                        />
                        {#if cpmError}
                            <div class="cpm-error">
                                ✗ {cpmError}
                            </div>
                        {:else if cpmNumber && !cpmError}
                            <div class="cpm-success">
                                ✓ Valid CPM number
                            </div>
                        {/if}
                    </div>
                </div>

                <div class="form-group">
                    <label class="form-label" for="yearOfStudy"
                        >Year of Study</label
                    >
                    <select
                        class="form-control"
                        id="yearOfStudy"
                        bind:value={yearOfStudy}
                        required
                    >
                        <option value="">Select Year</option>
                        <option value="1">Year 1</option>
                        <option value="2">Year 2</option>
                        <option value="3">Year 3</option>
                        <option value="4">Year 4</option>
                    </select>
                </div>
            {/if}

            <div class="form-group">
                <label class="form-label" for="profilePicture"
                    >Profile Picture</label
                >
                <input
                    class="form-control"
                    type="file"
                    id="profilePicture"
                    accept="image/*"
                    on:change={handleFileChange}
                    required
                />
            </div>

            <div class="form-group">
                <label class="form-label" for="password">Password</label>
                <input
                    class="form-control"
                    type="password"
                    id="password"
                    bind:value={password}
                    required
                />
            </div>

            <div class="form-group">
                <label class="form-label" for="confirmPassword">Confirm Password</label>
                <input
                    class="form-control {passwordMatchError ? 'input-error' : ''}"
                    type="password"
                    id="confirmPassword"
                    bind:value={confirmPassword}
                    required
                />
                {#if passwordMatchError}
                    <div class="password-error">
                        ✗ {passwordMatchError}
                    </div>
                {:else if confirmPassword && password === confirmPassword}
                    <div class="password-success">
                        ✓ Passwords match
                    </div>
                {/if}
            </div>

            <button
                type="submit"
                class="btn btn-primary btn-block mt-2"
                disabled={isLoading}
            >
                {isLoading ? "Registering..." : "Register Now"}
            </button>
        </form>

        <div class="auth-footer mt-2">
            <p>
                Already have an account? <a
                    href={`/login/${role}`}
                    class="nav-link"
                    style="color: var(--primary-light);">Log in</a
                >
            </p>
        </div>
    </div>
</div>

<style>
    .auth-wrapper {
        width: 100%;
        max-width: 550px;
        margin: 0 auto;
        animation: fadeUp 0.5s ease;
    }

    .auth-card {
        padding: 3rem 2.5rem;
    }

    .form-row {
        display: flex;
        gap: 1rem;
        flex-wrap: wrap;
    }

    .half-width {
        flex: 1;
        min-width: 200px;
    }

    .auth-title {
        font-size: 1.75rem;
        margin-bottom: 0.5rem;
    }

    .auth-subtitle {
        color: var(--text-muted);
        margin-bottom: 2rem;
        font-size: 0.95rem;
    }

    .btn-block {
        width: 100%;
        padding: 0.8rem;
        font-size: 1rem;
    }

    .mt-2 {
        margin-top: 1.5rem;
    }

    .auth-footer {
        text-align: center;
        font-size: 0.9rem;
        border-top: 1px solid var(--glass-border);
        padding-top: 1.5rem;
    }

    .alert {
        padding: 0.75rem;
        border-radius: 6px;
        margin-bottom: 1.5rem;
        font-size: 0.9rem;
    }

    .error-alert {
        background-color: rgba(220, 53, 69, 0.2);
        color: #ff6b6b;
        border: 1px solid rgba(220, 53, 69, 0.4);
    }

    .success-alert {
        background-color: rgba(40, 167, 69, 0.2);
        color: #4cd137;
        border: 1px solid rgba(40, 167, 69, 0.4);
    }

    .password-error {
        color: #ff6b6b;
        font-size: 0.85rem;
        margin-top: 0.5rem;
    }

    .password-success {
        color: #4cd137;
        font-size: 0.85rem;
        margin-top: 0.5rem;
    }

    .input-error {
        border-color: #ff6b6b !important;
        background-color: rgba(255, 107, 107, 0.05);
    }

    .email-error {
        color: #ff6b6b;
        font-size: 0.85rem;
        margin-top: 0.5rem;
    }

    .email-success {
        color: #4cd137;
        font-size: 0.85rem;
        margin-top: 0.5rem;
    }

    .email-info {
        background-color: rgba(66, 153, 225, 0.1);
        color: #4299e1;
        border-left: 3px solid #4299e1;
        padding: 0.75rem;
        margin-top: 0.75rem;
        border-radius: 4px;
        font-size: 0.85rem;
        line-height: 1.4;
    }

    .required-note {
        color: var(--text-muted);
        font-size: 0.85rem;
        font-weight: normal;
    }

    .mc-error {
        color: #ff6b6b;
        font-size: 0.85rem;
        margin-top: 0.5rem;
    }

    .mc-success {
        color: #4cd137;
        font-size: 0.85rem;
        margin-top: 0.5rem;
    }

    .cpm-error {
        color: #ff6b6b;
        font-size: 0.85rem;
        margin-top: 0.5rem;
    }

    .cpm-success {
        color: #4cd137;
        font-size: 0.85rem;
        margin-top: 0.5rem;
    }

    .phone-error {
        color: #ff6b6b;
        font-size: 0.85rem;
        margin-top: 0.5rem;
    }

    .phone-success {
        color: #4cd137;
        font-size: 0.85rem;
        margin-top: 0.5rem;
    }

    @keyframes fadeUp {
        from {
            opacity: 0;
            transform: translateY(20px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }
</style>
