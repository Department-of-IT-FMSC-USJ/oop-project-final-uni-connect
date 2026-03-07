<script>
    export let role = "user";

    let email = "";
    let password = "";
    let errorMessage = "";
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

    async function handleLogin() {
        isLoading = true;
        errorMessage = "";

        try {
            const response = await fetch(
                "http://localhost:8080/api/auth/login",
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({ email, password }),
                },
            );

            let responseText = await response.text();

            if (response.ok) {
                // Successful login
                console.log("Login successful", responseText);

                // Extract mock token: "Login successful token=mock_token_for_email@example.com"
                if (responseText.includes("token=")) {
                    const token = responseText.split("token=")[1];
                    localStorage.setItem("token", token);
                }

                // Redirect exactly like screenshot demo
                window.location.href = "/profile";
            } else {
                // Login failed
                errorMessage = "Invalid email or password.";
            }
        } catch (error) {
            console.error("Login error:", error);
            errorMessage = "A server error occurred. Please try again later.";
        } finally {
            isLoading = false;
        }
    }
</script>

<div class="auth-wrapper">
    <div class="glass-panel auth-card">
        <h2 class="auth-title">Welcome Back</h2>
        <p class="auth-subtitle">Login as a {formatRole(role)}</p>

        {#if errorMessage}
            <div class="error-alert">
                {errorMessage}
            </div>
        {/if}

        <form class="auth-form" on:submit|preventDefault={handleLogin}>
            <div class="form-group">
                <label class="form-label" for="email">Email address</label>
                <input
                    class="form-control"
                    type="email"
                    id="email"
                    bind:value={email}
                    required
                    placeholder="Enter your email"
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
                    placeholder="Enter your password"
                />
            </div>

            <div class="form-header-actions mb-1">
                <label class="checkbox-label">
                    <input type="checkbox" /> Remember me
                </label>
                <a href="/forgot-password" class="nav-link">Forgot password?</a>
            </div>

            <button
                type="submit"
                class="btn btn-primary btn-block mt-2"
                disabled={isLoading}
            >
                {isLoading ? "Signing In..." : "Sign In"}
            </button>
        </form>

        <div class="auth-footer mt-2">
            <p>
                Don't have an account? <a
                    href={`/register/${role}`}
                    class="nav-link"
                    style="color: var(--primary-light);">Register here</a
                >
            </p>
        </div>
    </div>
</div>

<style>
    .auth-wrapper {
        width: 100%;
        max-width: 480px;
        margin: 0 auto;
        animation: fadeUp 0.5s ease;
    }

    .auth-card {
        padding: 3rem 2.5rem;
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

    .form-header-actions {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-size: 0.85rem;
    }

    .checkbox-label {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        color: var(--text-muted);
    }

    .btn-block {
        width: 100%;
        padding: 0.8rem;
        font-size: 1rem;
    }

    .mb-1 {
        margin-bottom: 1rem;
    }

    .mt-2 {
        margin-top: 2rem;
    }

    .auth-footer {
        text-align: center;
        font-size: 0.9rem;
        border-top: 1px solid var(--glass-border);
        padding-top: 1.5rem;
    }

    .error-alert {
        background-color: rgba(220, 53, 69, 0.2);
        color: #ff6b6b;
        border: 1px solid rgba(220, 53, 69, 0.4);
        padding: 0.75rem;
        border-radius: 6px;
        margin-bottom: 1.5rem;
        font-size: 0.9rem;
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
