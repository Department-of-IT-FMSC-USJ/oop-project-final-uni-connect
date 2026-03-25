<script>
  import { onMount, createEventDispatcher } from 'svelte';

  export let options = []; // Array of { value, label, disabled? }
  export let value = '';
  export let placeholder = 'Select...';
  export let id = '';
  export let disabled = false;
  export let compact = false;

  const dispatch = createEventDispatcher();

  let open = false;
  let root;
  let listEl;
  let focusedIndex = -1;

  $: selectedLabel = options.find(o => String(o.value) === String(value))?.label || '';

  function toggle() {
    if (disabled) return;
    open = !open;
    if (open) {
      focusedIndex = options.findIndex(o => String(o.value) === String(value));
      requestAnimationFrame(() => {
        if (listEl) {
          const active = listEl.querySelector('.option.active');
          if (active) active.scrollIntoView({ block: 'nearest' });
        }
      });
    }
  }

  function select(option) {
    if (option?.disabled) return;
    value = option.value;
    open = false;
    dispatch('change', { value: option.value });
  }

  function handleKeydown(e) {
    if (!open) {
      if (e.key === 'Enter' || e.key === ' ' || e.key === 'ArrowDown') {
        e.preventDefault();
        open = true;
        focusedIndex = options.findIndex(o => String(o.value) === String(value));
      }
      return;
    }

    switch (e.key) {
      case 'ArrowDown':
        e.preventDefault();
        focusedIndex = findNextEnabledIndex(focusedIndex, 1);
        scrollToFocused();
        break;
      case 'ArrowUp':
        e.preventDefault();
        focusedIndex = findNextEnabledIndex(focusedIndex, -1);
        scrollToFocused();
        break;
      case 'Enter':
      case ' ':
        e.preventDefault();
        if (focusedIndex >= 0 && focusedIndex < options.length) {
          select(options[focusedIndex]);
        }
        break;
      case 'Escape':
        e.preventDefault();
        open = false;
        break;
    }
  }

  function scrollToFocused() {
    requestAnimationFrame(() => {
      if (listEl) {
        const el = listEl.children[focusedIndex];
        if (el) el.scrollIntoView({ block: 'nearest' });
      }
    });
  }

  function findNextEnabledIndex(startIndex, direction) {
    if (!options.length) return -1;

    let index = startIndex;
    for (let i = 0; i < options.length; i += 1) {
      index += direction;
      if (index < 0) index = options.length - 1;
      if (index >= options.length) index = 0;
      if (!options[index]?.disabled) {
        return index;
      }
    }

    return startIndex;
  }

  function handleClickOutside(e) {
    if (root && !root.contains(e.target)) {
      open = false;
    }
  }

  onMount(() => {
    if (typeof document === 'undefined') {
      return;
    }

    document.addEventListener('click', handleClickOutside);

    return () => {
      document.removeEventListener('click', handleClickOutside);
    };
  });
</script>

<div
  class="custom-select"
  class:open
  class:disabled
  class:compact
  bind:this={root}
>
  <button
    type="button"
    class="select-trigger"
    id={id}
    on:click={toggle}
    on:keydown={handleKeydown}
    aria-haspopup="listbox"
    aria-expanded={open}
    {disabled}
  >
    <span class="select-value" class:placeholder={!selectedLabel}>
      {#if selectedLabel}
        {#if value !== ''}
          <svg class="check-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <polyline points="20 6 9 17 4 12"/>
          </svg>
        {/if}
        {selectedLabel}
      {:else}
        {placeholder}
      {/if}
    </span>
    <svg class="chevron" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
      <path d="M6 9l6 6 6-6"/>
    </svg>
  </button>

  {#if open}
    <ul class="select-dropdown" role="listbox" bind:this={listEl}>
      {#each options as option, i}
        <li
          class="option"
          class:active={String(option.value) === String(value)}
          class:focused={focusedIndex === i}
          class:option-disabled={option.disabled}
          role="option"
          aria-selected={String(option.value) === String(value)}
          aria-disabled={option.disabled}
          on:click={() => select(option)}
          on:mouseenter={() => focusedIndex = option.disabled ? focusedIndex : i}
        >
          {#if String(option.value) === String(value)}
            <svg class="option-check" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
          {:else}
            <span class="option-check-placeholder"></span>
          {/if}
          {option.label}
        </li>
      {/each}
    </ul>
  {/if}
</div>

<style>
  .custom-select {
    position: relative;
    display: flex;
    width: 100%;
    font-family: var(--font-ui, 'Inter', -apple-system, sans-serif);
  }

  .select-trigger {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 0.5rem;
    width: 100%;
    min-width: 140px;
    padding: 0.5rem 0.75rem;
    font-size: 0.8125rem;
    font-family: inherit;
    color: var(--text-main, #1E293B);
    background: var(--bg-main, #fff);
    border: 1px solid var(--border-light, #E2E8F0);
    border-radius: var(--radius-sm, 8px);
    cursor: pointer;
    transition: all 0.15s ease;
    line-height: 1.4;
  }

  .select-trigger:hover {
    border-color: var(--primary, #4F7CDB);
    background: var(--primary-50, #EEF2FB);
  }

  .open .select-trigger {
    border-color: var(--primary, #4F7CDB);
    box-shadow: 0 0 0 3px rgba(79, 124, 219, 0.12);
  }

  .disabled .select-trigger {
    opacity: 0.5;
    cursor: not-allowed;
    background: var(--bg-secondary, #F1F5F9);
  }

  .compact .select-trigger {
    padding: 0.375rem 0.65rem;
    min-width: 120px;
    font-size: 0.78rem;
  }

  .select-value {
    display: flex;
    align-items: center;
    gap: 0.35rem;
    flex: 1;
    text-align: left;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .select-value.placeholder {
    color: var(--text-muted, #94A3B8);
  }

  .check-icon {
    color: var(--primary, #4F7CDB);
    flex-shrink: 0;
  }

  .chevron {
    color: var(--text-muted, #94A3B8);
    flex-shrink: 0;
    transition: transform 0.2s ease;
  }

  .open .chevron {
    transform: rotate(180deg);
  }

  .select-dropdown {
    position: absolute;
    top: calc(100% + 4px);
    left: 0;
    min-width: 100%;
    max-height: 220px;
    overflow-y: auto;
    padding: 0.3rem;
    margin: 0;
    list-style: none;
    background: var(--bg-main, #fff);
    border: 1px solid var(--border-light, #E2E8F0);
    border-radius: var(--radius, 12px);
    box-shadow: 0 8px 30px rgba(0,0,0,0.12), 0 2px 8px rgba(0,0,0,0.06);
    z-index: 500;
    animation: dropdownIn 0.18s cubic-bezier(0.2, 0, 0.13, 1.5);
  }

  @keyframes dropdownIn {
    from {
      opacity: 0;
      transform: translateY(-6px) scale(0.97);
    }
    to {
      opacity: 1;
      transform: translateY(0) scale(1);
    }
  }

  .option {
    display: flex;
    align-items: center;
    gap: 0.45rem;
    padding: 0.5rem 0.65rem;
    font-size: 0.8125rem;
    color: var(--text-main, #1E293B);
    border-radius: var(--radius-sm, 8px);
    cursor: pointer;
    transition: background 0.1s ease, color 0.1s ease;
    white-space: nowrap;
  }

  .option:hover,
  .option.focused {
    background: var(--primary, #4F7CDB);
    color: #fff;
  }

  .option.active {
    font-weight: 600;
    color: var(--primary, #4F7CDB);
  }

  .option.active.focused,
  .option.active:hover {
    background: var(--primary, #4F7CDB);
    color: #fff;
  }

  .option.option-disabled {
    opacity: 0.45;
    cursor: not-allowed;
  }

  .option.option-disabled:hover,
  .option.option-disabled.focused {
    background: transparent;
    color: var(--text-main, #1E293B);
  }

  .option-check {
    flex-shrink: 0;
    color: inherit;
  }

  .option-check-placeholder {
    width: 14px;
    height: 14px;
    flex-shrink: 0;
  }

  /* Scrollbar styling for the dropdown */
  .select-dropdown::-webkit-scrollbar {
    width: 5px;
  }
  .select-dropdown::-webkit-scrollbar-track {
    background: transparent;
  }
  .select-dropdown::-webkit-scrollbar-thumb {
    background: var(--border-light, #E2E8F0);
    border-radius: 99px;
  }
</style>

