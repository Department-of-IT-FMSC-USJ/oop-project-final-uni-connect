<script>
  import { onMount } from 'svelte';
  import { api, getCurrentUser } from '../../lib/api.js';

  export let contact = null;
  export let onClose = () => {};

  const currentUser = getCurrentUser();
  let loading = true;
  let sending = false;
  let error = '';
  let messages = [];
  let draft = '';

  onMount(() => {
    if (contact?.id) {
      loadConversation();
    }
  });

  async function loadConversation() {
    loading = true;
    error = '';
    try {
      messages = await api.get(`/messages/with/${contact.id}`, { cache: false });
    } catch (e) {
      error = e?.data?.message || 'Failed to load messages.';
      messages = [];
    } finally {
      loading = false;
    }
  }

  async function sendMessage() {
    const content = draft.trim();
    if (!content) return;

    sending = true;
    error = '';
    try {
      const created = await api.post('/messages', {
        recipientId: contact.id,
        content
      });
      messages = [...messages, created];
      draft = '';
    } catch (e) {
      error = e?.data?.message || 'Failed to send message.';
    } finally {
      sending = false;
    }
  }

  function formatDate(value) {
    if (!value) return '';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return date.toLocaleString();
  }
</script>

<div class="modal-overlay" on:click|self={onClose}>
  <div class="modal-content chat-modal">
    <div class="modal-header">
      <div>
        <p class="eyebrow">Conversation</p>
        <h3>{contact.fullName}</h3>
        <p class="subtitle">{contact.email || ''}</p>
      </div>
      <button class="close-btn" on:click={onClose} aria-label="Close message dialog">✕</button>
    </div>

    {#if error}
      <div class="alert alert-error">{error}</div>
    {/if}

    <div class="messages-panel">
      {#if loading}
        <p class="empty-state">Loading conversation...</p>
      {:else if messages.length === 0}
        <p class="empty-state">No messages yet. Start the conversation.</p>
      {:else}
        {#each messages as message}
          <div class:is-own={message.senderId === currentUser?.id} class="message-bubble">
            <div class="message-meta">
              <strong>{message.senderId === currentUser?.id ? 'You' : message.senderName}</strong>
              <span>{formatDate(message.createdAt)}</span>
            </div>
            <p>{message.content}</p>
          </div>
        {/each}
      {/if}
    </div>

    <div class="composer">
      <textarea
        class="input"
        rows="3"
        bind:value={draft}
        placeholder={`Message ${contact.fullName}...`}
      ></textarea>
      <div class="composer-actions">
        <button class="btn btn-outline" on:click={onClose}>Close</button>
        <button class="btn btn-primary" on:click={sendMessage} disabled={sending || !draft.trim()}>
          {sending ? 'Sending...' : 'Send'}
        </button>
      </div>
    </div>
  </div>
</div>

<style>
  .chat-modal {
    width: min(760px, 92vw);
    max-width: 760px;
    display: grid;
    gap: 1rem;
  }

  .modal-header {
    display: flex;
    justify-content: space-between;
    gap: 1rem;
  }

  .eyebrow {
    margin-bottom: 0.35rem;
    font-size: 0.75rem;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.08em;
    color: var(--accent);
  }

  .subtitle {
    color: var(--gray-500);
    font-size: 0.85rem;
  }

  .messages-panel {
    max-height: 400px;
    overflow-y: auto;
    display: grid;
    gap: 0.75rem;
    padding: 0.25rem;
  }

  .message-bubble {
    max-width: 85%;
    padding: 0.85rem 1rem;
    border-radius: 16px 16px 16px 4px;
    background: var(--gray-100);
  }

  .message-bubble.is-own {
    justify-self: end;
    background: #dbeafe;
    border-radius: 16px 16px 4px 16px;
  }

  .message-meta {
    display: flex;
    justify-content: space-between;
    gap: 1rem;
    margin-bottom: 0.35rem;
    font-size: 0.72rem;
    color: var(--gray-500);
  }

  .message-bubble p {
    white-space: pre-wrap;
    color: var(--gray-800);
  }

  .composer {
    display: grid;
    gap: 0.75rem;
  }

  .composer-actions {
    display: flex;
    justify-content: flex-end;
    gap: 0.75rem;
  }

  .empty-state {
    color: var(--gray-500);
    padding: 1rem 0;
  }

  .alert {
    padding: 0.8rem 1rem;
    border-radius: var(--radius);
  }

  .alert-error {
    background: #fee2e2;
    color: #991b1b;
  }

  .close-btn {
    background: transparent;
    color: var(--gray-500);
  }
</style>
