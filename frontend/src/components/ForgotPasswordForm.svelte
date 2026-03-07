<script>
    import { onMount } from "svelte";

    let email = "";
    let isLoading = false;
    let message = "";
    let isSuccess = false;

    const handleResetRequest = async () => {
        isLoading = true;
        message = "";

        // Simulate API call
        await new Promise((resolve) => setTimeout(resolve, 1500));

        if (email.includes("@")) {
            isSuccess = true;
            message = "A reset link has been sent to your email address.";
        } else {
            message = "Please enter a valid email address.";
        }
        isLoading = false;
    };
</script>

<div class="glass-panel auth-card">
    <div class="auth-header">
        <h2>Reset Password</h2>
        <p class="muted-text">
            Enter your email to receive a password reset link.
        </p>
    </div>

    {#if message}
        <div class="alert {isSuccess ? 'success' : 'error'}">
            {message}
        </div>
    {/if}

    {#if !isSuccess}
        <form on:submit|preventDefault={handleResetRequest}>
            <div class="form-group">
                <label class="form-label" for="email">Email address</label>
                <input
                    class="form-control"
                    type="email"
                    id="email"
                    placeholder="Enter your email"
                    bind:value={email}
                    required
                />
            </div>

            <button
                type="submit"
                class="btn btn-primary w-100 mt-2"
                disabled={isLoading}
            >
                {isLoading ? "Sending..." : "Send Reset Link"}
            </button>
        </form>
    {:else}
        <div class="success-actions mt-3">
            <a href="/login/student" class="btn btn-outline w-100"
                >Back to Login</a
            >
        </div>
    {/if}

    <div class="auth-footer mt-8">
        <p>Remember your password? <a href="/login/student">Sign In</a></p>
    </div>
</div>

<style>
    .auth-card {
        width: 100%;
        max-width: 450px;
        padding: 3rem;
    }

    .auth-header {
        text-align: center;
        margin-bottom: 2.5rem;
    }

    .auth-header h2 {
        font-size: 2rem;
        font-weight: 700;
        color: var(--text-main);
        margin-bottom: 0.5rem;
    }

    .w-100 {
        width: 100%;
    }

    .mt-2 {
        margin-top: 1.5rem;
    }

    .mt-3 {
        margin-top: 2rem;
    }

    .mt-4 {
        margin-top: 2.5rem;
    }

    .mt-8 {
        margin-top: 4rem;
    }

    .auth-footer {
        text-align: center;
        font-size: 0.9rem;
    }

    .auth-footer a {
        color: var(--primary-light);
        text-decoration: none;
        font-weight: 600;
    }

    .auth-footer a:hover {
        text-decoration: underline;
    }

    .alert {
        padding: 1rem;
        border-radius: 8px;
        margin-bottom: 1.5rem;
        font-size: 0.9rem;
    }

    .alert.success {
        background-color: #f0fdf4;
        color: #166534;
        border: 1px solid #bbf7d0;
    }

    .alert.error {
        background-color: #fef2f2;
        color: #991b1b;
        border: 1px solid #fecaca;
    }
</style>
