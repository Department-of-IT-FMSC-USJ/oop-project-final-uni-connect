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
    max-width: 460px;
    padding: 1.6rem;
    display: grid;
    gap: 1rem;
  }

  .confirm-icon {
    width: 44px;
    height: 44px;
    border-radius: 14px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, rgba(37, 99, 235, 0.14), rgba(56, 189, 248, 0.2));
    color: #2563eb;
  }

  .confirm-icon.danger {
    background: linear-gradient(135deg, rgba(239, 68, 68, 0.14), rgba(249, 115, 22, 0.18));
    color: #dc2626;
  }

  .confirm-copy {
    display: grid;
    gap: 0.4rem;
  }

  .confirm-eyebrow {
    font-size: 0.72rem;
    font-weight: 700;
    letter-spacing: 0.12em;
    text-transform: uppercase;
    color: var(--gray-500);
  }

  .confirm-copy h3 {
    font-size: 1.2rem;
    color: var(--gray-900);
  }

  .confirm-copy p:last-child {
    color: var(--gray-600);
    line-height: 1.6;
  }

  .confirm-actions {
    display: flex;
    justify-content: flex-end;
    gap: 0.75rem;
  }

  @media (max-width: 640px) {
    .confirm-actions {
      flex-direction: column-reverse;
    }

    .confirm-actions :global(.btn) {
      width: 100%;
    }
  }
</style>
