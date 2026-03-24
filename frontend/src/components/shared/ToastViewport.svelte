<script>
  import { toasts, dismissToast } from '../../lib/toast.js';

  const variantLabels = {
    info: 'Notice',
    success: 'Success',
    warning: 'Warning',
    error: 'Error'
  };

  const variantIcons = {
    info: 'i',
    success: '✓',
    warning: '!',
    error: '×'
  };

  function getTitle(item) {
    return item.title || variantLabels[item.variant] || 'Notice';
  }
</script>

<div class="toast-viewport" aria-live="polite" aria-atomic="true">
  {#each $toasts as item (item.id)}
    <article class="toast" class:success={item.variant === 'success'} class:warning={item.variant === 'warning'} class:error={item.variant === 'error'} class:info={item.variant === 'info'}>
      <div class="toast-icon">{variantIcons[item.variant] || 'i'}</div>
      <div class="toast-body">
        <div class="toast-head">
          <strong>{getTitle(item)}</strong>
          <button class="toast-close" type="button" on:click={() => dismissToast(item.id)} aria-label="Dismiss notification">✕</button>
        </div>
        <p>{item.message}</p>
      </div>
    </article>
  {/each}
</div>

<style>
  .toast-viewport {
    position: fixed;
    top: calc(var(--header-height) + 1rem);
    right: 1.5rem;
    width: min(380px, calc(100vw - 2rem));
    display: grid;
    gap: 0.75rem;
    z-index: 1300;
    pointer-events: none;
  }

  .toast {
    display: grid;
    grid-template-columns: 32px 1fr;
    align-items: start;
    gap: 0.75rem;
    border-radius: 10px;
    padding: 1rem;
    border: 1px solid var(--border-light);
    background: var(--bg-main);
    box-shadow: var(--shadow-lg);
    pointer-events: auto;
    animation: slideIn 0.3s ease-out;
    transition: all var(--transition-smooth);
  }

  .toast:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-lg);
    border-color: var(--border-medium);
  }

  .toast-icon {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 0.85rem;
    font-weight: 700;
    background: #f0f4f8;
    color: var(--accent);
    border: 1px solid var(--border-light);
    flex-shrink: 0;
  }

  .toast.success .toast-icon {
    background: rgba(16, 185, 129, 0.1);
    color: var(--success);
  }

  .toast.warning .toast-icon {
    background: rgba(245, 158, 11, 0.1);
    color: var(--warning);
  }

  .toast.error .toast-icon {
    background: rgba(239, 68, 68, 0.1);
    color: var(--danger);
  }

  .toast.info .toast-icon {
    background: rgba(37, 99, 235, 0.1);
    color: var(--accent);
  }

  .toast-body {
    padding: 0;
  }

  .toast-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 1rem;
    margin-bottom: 0.4rem;
  }

  .toast-head strong {
    font-size: 0.9rem;
    color: var(--text-main);
    font-weight: 600;
    letter-spacing: 0;
  }

  .toast p {
    color: var(--text-muted);
    font-size: 0.85rem;
    line-height: 1.5;
    margin: 0;
  }

  .toast-close {
    width: 28px;
    height: 28px;
    border-radius: 6px;
    background: transparent;
    color: var(--text-muted);
    font-size: 0.8rem;
    border: 1px solid transparent;
    cursor: pointer;
    transition: all var(--transition-fast);
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .toast-close:hover {
    background: var(--bg-alt);
    color: var(--text-main);
  }

  .toast.error .toast-close:hover {
    background: rgba(239, 68, 68, 0.1);
    color: var(--danger);
  }

  @keyframes toast-enter {
    from {
      opacity: 0;
      transform: translateY(-12px) scale(0.96);
    }
    to {
      opacity: 1;
      transform: translateY(0) scale(1);
    }
  }

  @media (max-width: 720px) {
    .toast-viewport {
      top: auto;
      right: 1rem;
      bottom: 1rem;
      width: calc(100vw - 2rem);
    }
  }
</style>
