<script>
  import { toasts, dismissToast } from '../../lib/toast.js';

  const variantLabels = {
    info: 'Notice',
    success: 'Success',
    warning: 'Warning',
    error: 'Error'
  };

  function getTitle(item) {
    return item.title || variantLabels[item.variant] || 'Notice';
  }
</script>

<div class="toast-viewport" aria-live="polite" aria-atomic="true">
  {#each $toasts as item (item.id)}
    <article class="toast" class:success={item.variant === 'success'} class:warning={item.variant === 'warning'} class:error={item.variant === 'error'}>
      <div class="toast-accent"></div>
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
    width: min(360px, calc(100vw - 2rem));
    display: grid;
    gap: 0.85rem;
    z-index: 1300;
    pointer-events: none;
  }

  .toast {
    position: relative;
    display: grid;
    grid-template-columns: 4px 1fr;
    border-radius: 20px;
    overflow: hidden;
    border: 1px solid rgba(148, 163, 184, 0.22);
    background: rgba(255, 255, 255, 0.95);
    box-shadow: 0 24px 48px rgba(15, 23, 42, 0.14);
    backdrop-filter: blur(18px);
    pointer-events: auto;
  }

  .toast-accent {
    background: linear-gradient(180deg, #2563eb, #38bdf8);
  }

  .toast.success .toast-accent {
    background: linear-gradient(180deg, #10b981, #34d399);
  }

  .toast.warning .toast-accent {
    background: linear-gradient(180deg, #f59e0b, #fbbf24);
  }

  .toast.error .toast-accent {
    background: linear-gradient(180deg, #ef4444, #f97316);
  }

  .toast-body {
    padding: 0.95rem 1rem 1rem;
  }

  .toast-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 1rem;
    margin-bottom: 0.35rem;
  }

  .toast-head strong {
    font-size: 0.84rem;
    color: var(--gray-900);
    letter-spacing: 0.01em;
  }

  .toast p {
    color: var(--gray-600);
    font-size: 0.84rem;
    line-height: 1.55;
  }

  .toast-close {
    width: 28px;
    height: 28px;
    border-radius: 999px;
    background: rgba(241, 245, 249, 0.9);
    color: var(--gray-500);
    font-size: 0.8rem;
    transition: background 0.18s ease, color 0.18s ease;
  }

  .toast-close:hover {
    background: rgba(226, 232, 240, 0.95);
    color: var(--gray-800);
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
