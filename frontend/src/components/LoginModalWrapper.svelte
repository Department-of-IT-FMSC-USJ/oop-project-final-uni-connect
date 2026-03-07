<script>
    import RoleSelectModal from './RoleSelectModal.svelte';
    
    let showModal = false;
    
    // Function to open modal (called from header via window object)
    if (typeof window !== 'undefined') {
        window.openLoginModal = () => {
            showModal = true;
        };
        window.closeLoginModal = () => {
            showModal = false;
        };
    }
    
    const handleBackdropClick = () => {
        showModal = false;
    };
</script>

{#if showModal}
    <div class="modal-backdrop" on:click={handleBackdropClick}>
        <div class="modal-content" on:click|stopPropagation>
            <button class="close-btn" on:click={handleBackdropClick}>
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="18" y1="6" x2="6" y2="18"></line>
                    <line x1="6" y1="6" x2="18" y2="18"></line>
                </svg>
            </button>
            <RoleSelectModal />
        </div>
    </div>
{/if}

<style>
    .modal-backdrop {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.5);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 1000;
        animation: fadeIn 0.3s ease;
    }

    .modal-content {
        position: relative;
        max-width: 450px;
        width: 90%;
        animation: slideUp 0.3s ease;
    }

    .close-btn {
        position: absolute;
        top: 1rem;
        right: 1rem;
        background: none;
        border: none;
        cursor: pointer;
        color: var(--text-muted);
        padding: 0.5rem;
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 1001;
        transition: color 0.3s ease;
    }

    .close-btn:hover {
        color: var(--text-main);
    }

    @keyframes fadeIn {
        from {
            opacity: 0;
        }
        to {
            opacity: 1;
        }
    }

    @keyframes slideUp {
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
