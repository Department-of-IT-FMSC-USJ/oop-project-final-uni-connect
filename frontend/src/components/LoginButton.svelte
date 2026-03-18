<script>
    const handleLoginClick = () => {
        if (typeof window !== 'undefined' && window.openLoginModal) {
            localStorage.setItem('authRedirect', '/profile');
            window.openLoginModal();
        }
    };
</script>

<button class="btn btn-primary" on:click={handleLoginClick}>
    Login / Sign Up
</button>
