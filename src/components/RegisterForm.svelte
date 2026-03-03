<script>
    export let role = "student";

    let firstName = "";
    let lastName = "";
    let email = "";
    let phone = "";
    let password = "";
    let profilePicture = "";
    let registrationNumber = "";
    let cpmNumber = "";
    let yearOfStudy = "";

    let errorMessage = "";
    let successMessage = "";
    let isLoading = false;

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
                <label class="form-label" for="email">Email address</label>
                <input
                    class="form-control"
                    type="email"
                    id="email"
                    bind:value={email}
                    required
                />
            </div>

            <div class="form-group">
                <label class="form-label" for="phone">Phone Number</label>
                <input
                    class="form-control"
                    type="tel"
                    id="phone"
                    bind:value={phone}
                />
            </div>

            {#if role === "student"}
                <div class="form-row">
                    <div class="form-group half-width">
                        <label class="form-label" for="registrationNumber"
                            >Registration (MC) Number</label
                        >
                        <input
                            class="form-control"
                            type="text"
                            id="registrationNumber"
                            bind:value={registrationNumber}
                            required
                        />
                    </div>
                    <div class="form-group half-width">
                        <label class="form-label" for="cpmNumber"
                            >CPM Number</label
                        >
                        <input
                            class="form-control"
                            type="text"
                            id="cpmNumber"
                            bind:value={cpmNumber}
                            required
                        />
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
