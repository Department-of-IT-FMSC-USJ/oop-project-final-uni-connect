<script>
    import { onMount } from "svelte";
    
    let isLoggedIn = false;

    onMount(() => {
        // Check if user is logged in by checking for token
        isLoggedIn = !!localStorage.getItem("token");
    });

    const handleLogout = async () => {
        try {
            const response = await fetch(
                "http://localhost:8080/api/auth/logout",
                {
                    method: "POST",
                    headers: {
                        Authorization: `Bearer ${localStorage.getItem("token")}`,
                    },
                },
            );

            // Clear token from localStorage regardless of response
            localStorage.removeItem("token");
            isLoggedIn = false;

            // Redirect to home page
            setTimeout(() => {
                window.location.href = "/";
            }, 300);
        } catch (error) {
            console.error("Error logging out:", error);
            // Still clear token and redirect on error
            localStorage.removeItem("token");
            isLoggedIn = false;
            setTimeout(() => {
                window.location.href = "/";
            }, 300);
        }
    };
</script>

{#if isLoggedIn}
    <button class="btn btn-danger" on:click={handleLogout}>
        Logout
    </button>
{/if}

<style>
    :global(.btn-danger) {
        background-color: #dc2626;
        color: white;
        box-shadow: 0 4px 6px -1px rgba(220, 38, 38, 0.2), 0 2px 4px -2px rgba(220, 38, 38, 0.2);
    }

    :global(.btn-danger:hover) {
        background-color: #b91c1c;
        transform: translateY(-2px);
        box-shadow: 0 6px 8px -1px rgba(220, 38, 38, 0.3), 0 4px 6px -2px rgba(220, 38, 38, 0.2);
    }
</style>
