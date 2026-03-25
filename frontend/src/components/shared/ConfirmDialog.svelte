<script>
  import { createEventDispatcher } from 'svelte';

  export let open = false;
  export let title = 'Confirm action';
  export let message = '';
  export let confirmLabel = 'Confirm';
  export let cancelLabel = 'Cancel';
  export let tone = 'danger';
  export let busy = false;

  const dispatch = createEventDispatcher();

  function handleCancel() {
    if (!busy) {
      dispatch('cancel');
    }
  }

  function handleConfirm() {
    if (!busy) {
      dispatch('confirm');
    }
  }
</script>

{#if open}
  <div
    class="modal-overlay confirm-overlay"
    role="button"
    tabindex="0"
    aria-label="Close confirmation dialog"
    on:click|self={handleCancel}
    on:keydown={(event) => (event.key === 'Enter' || event.key === 'Escape') && handleCancel()}
  >
    <div class="modal-content confirm-modal">
      <div class="confirm-icon" class:danger={tone === 'danger'}>
        {#if tone === 'danger'}
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0Z"/>
            <line x1="12" y1="9" x2="12" y2="13"/>
            <line x1="12" y1="17" x2="12.01" y2="17"/>
          </svg>
        {:else}
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <path d="M12 8v4"/>
            <path d="M12 16h.01"/>
          </svg>
        {/if}
      </div>

      <div class="confirm-copy">
        <p class="confirm-eyebrow">{tone === 'danger' ? 'Destructive action' : 'Please confirm'}</p>
        <h3>{title}</h3>
        <p>{message}</p>
      </div>

      <div class="confirm-actions">
        <button class="btn btn-outline" type="button" on:click={handleCancel} disabled={busy}>{cancelLabel}</button>
        <button class={`btn ${tone === 'danger' ? 'btn-danger' : 'btn-primary'}`} type="button" on:click={handleConfirm} disabled={busy}>
          {busy ? 'Working...' : confirmLabel}
        </button>
      </div>
    </div>
  </div>
{/if}

<style>
  .confirm-overlay {
    z-index: 1250;
  }

  .confirm-modal {
    max-width: 480px;
    padding: 2rem;
    display: grid;
    gap: 1.5rem;
    animation: slideIn 0.3s ease-out;
  }

  .confirm-icon {
    width: 48px;
    height: 48px;
    border-radius: var(--radius);
    display: inline-flex;
    align-items: center;
    justify-content: center;
    background: var(--primary-50);
    color: var(--primary);
    transition: all var(--transition-smooth);
  }

  .confirm-icon.danger {
    background: var(--danger-light);
    color: var(--danger);
  }

  .confirm-copy {
    display: grid;
    gap: 0.5rem;
  }

  .confirm-eyebrow {
    font-size: 0.7rem;
    font-weight: 700;
    letter-spacing: 0.1em;
    text-transform: uppercase;
    color: var(--text-muted);
  }

  .confirm-copy h3 {
    font-size: 1.2rem;
    font-weight: 700;
    color: var(--text-main);
  }

  .confirm-copy p:last-child {
    color: var(--text-secondary);
    line-height: 1.6;
    font-size: 0.95rem;
  }

  .confirm-actions {
    display: flex;
    justify-content: flex-end;
    gap: 0.75rem;
    margin-top: 0.5rem;
  }

  @media (max-width: 640px) {
    .confirm-modal {
      padding: 1.5rem;
      gap: 1rem;
    }

    .confirm-actions {
      flex-direction: column-reverse;
    }

    .confirm-actions :global(.btn) {
      width: 100%;
    }
  }
</style>
