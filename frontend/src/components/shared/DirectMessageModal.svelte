<script>
  import { onMount, afterUpdate, tick } from 'svelte';
  import { api, getCurrentUser, emitMessagesUpdated } from '../../lib/api.js';

  export let contact = null;
  export let onClose = () => {};

  const currentUser = getCurrentUser();
  let loading = true;
  let sending = false;
  let error = '';
  let messages = [];
  let draft = '';
  let usingLocalFallback = false;
  let messagesContainer;

  function getContactId() {
    return contact?.id || contact?.userId || null;
  }

  function getStorageKey() {
    const currentUserId = currentUser?.id || currentUser?.userId;
    const otherUserId = getContactId();
    if (!currentUserId || !otherUserId) return null;
    const [first, second] = [currentUserId, otherUserId].sort((a, b) => Number(a) - Number(b));
    return `direct-messages:${first}:${second}`;
  }

  function loadLocalMessages() {
    const key = getStorageKey();
    if (!key || typeof window === 'undefined') return [];
    try {
      return JSON.parse(localStorage.getItem(key) || '[]');
    } catch {
      return [];
    }
  }

  function saveLocalMessages(nextMessages) {
    const key = getStorageKey();
    if (!key || typeof window === 'undefined') return;
    localStorage.setItem(key, JSON.stringify(nextMessages));
  }

  function scrollToBottom() {
    if (messagesContainer) {
      messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }
  }

  onMount(() => {
    if (getContactId()) {
      loadConversation();
    }
  });

  afterUpdate(() => {
    scrollToBottom();
  });

  async function loadConversation() {
    loading = true;
    error = '';
    try {
      messages = await api.get(`/messages/with/${getContactId()}`, { cache: false });
      usingLocalFallback = false;
      emitMessagesUpdated();
      await tick();
      scrollToBottom();
    } catch (e) {
      if (e?.status === 404) {
        usingLocalFallback = true;
        messages = loadLocalMessages();
        error = '';
      } else {
        error = e?.data?.message || 'Failed to load messages.';
        messages = [];
      }
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
        recipientId: getContactId(),
        content
      });
      messages = [...messages, created];
      usingLocalFallback = false;
      draft = '';
      emitMessagesUpdated();
    } catch (e) {
      if (e?.status === 404) {
        usingLocalFallback = true;
        const created = {
          id: `local-${Date.now()}`,
          senderId: currentUser?.id || currentUser?.userId,
          senderName: currentUser?.fullName || 'You',
          recipientId: getContactId(),
          recipientName: contact?.fullName || 'Contact',
          content,
          createdAt: new Date().toISOString()
        };
        messages = [...loadLocalMessages(), created];
        saveLocalMessages(messages);
        draft = '';
        error = '';
        emitMessagesUpdated();
      } else {
        error = e?.data?.message || 'Failed to send message.';
      }
    } finally {
      sending = false;
    }
  }

  function handleKeydown(e) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
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
    <div class="chat-header">
      <div class="chat-header-info">
        <div class="chat-avatar">{contact.fullName?.charAt(0) || '?'}</div>
        <div>
          <h3 class="chat-name">{contact.fullName}</h3>
          <p class="chat-email">{contact.email || ''}</p>
        </div>
      </div>
      <button class="close-btn" on:click={onClose} aria-label="Close message dialog">✕</button>
    </div>

    {#if usingLocalFallback}
      <p class="fallback-note">Using local chat storage until the backend message route is available.</p>
    {/if}

    {#if error}
      <div class="alert alert-error">{error}</div>
    {/if}

    <div class="messages-panel" bind:this={messagesContainer}>
      {#if loading}
        <p class="empty-state">Loading conversation...</p>
      {:else if messages.length === 0}
        <p class="empty-state">No messages yet. Start the conversation.</p>
      {:else}
        {#each messages as message}
          <div class="message-bubble" class:is-own={message.senderId === currentUser?.id}>
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
        rows="2"
        bind:value={draft}
        placeholder={`Message ${contact.fullName}...`}
        on:keydown={handleKeydown}
      ></textarea>
      <div class="composer-actions">
        <button class="btn btn-outline btn-sm" on:click={onClose}>Close</button>
        <button class="btn btn-primary btn-sm" on:click={sendMessage} disabled={sending || !draft.trim()}>
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
    height: min(80vh, 640px);
    display: flex;
    flex-direction: column;
    gap: 0;
    padding: 0;
    overflow: hidden;
  }

  .chat-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 1rem;
    padding: 1.25rem 1.5rem;
    border-bottom: 1px solid var(--gray-200, #e5e7eb);
    flex-shrink: 0;
  }

  .chat-header-info {
    display: flex;
    align-items: center;
    gap: 0.75rem;
  }

  .chat-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: var(--gray-900, #111);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    font-size: 1rem;
    flex-shrink: 0;
  }

  .chat-name {
    font-size: 1rem;
    font-weight: 600;
    margin: 0;
  }

  .chat-email {
    color: var(--gray-500, #6b7280);
    font-size: 0.8rem;
    margin: 0;
  }

  .fallback-note {
    margin: 0;
    padding: 0.5rem 1.5rem;
    color: #92400e;
    font-size: 0.78rem;
    background: #fffbeb;
    flex-shrink: 0;
  }

  .alert {
    margin: 0;
    padding: 0.7rem 1.5rem;
    flex-shrink: 0;
  }

  .alert-error {
    background: #fee2e2;
    color: #991b1b;
  }

  .messages-panel {
    flex: 1;
    min-height: 0;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 0.6rem;
    padding: 1rem 1.5rem;
  }

  .message-bubble {
    max-width: 75%;
    padding: 0.75rem 1rem;
    border-radius: 16px 16px 16px 4px;
    background: var(--gray-100, #f3f4f6);
    align-self: flex-start;
  }

  .message-bubble.is-own {
    align-self: flex-end;
    background: #dbeafe;
    border-radius: 16px 16px 4px 16px;
  }

  .message-meta {
    display: flex;
    justify-content: space-between;
    gap: 1rem;
    margin-bottom: 0.25rem;
    font-size: 0.7rem;
    color: var(--gray-500, #6b7280);
  }

  .message-bubble p {
    white-space: pre-wrap;
    color: var(--gray-800, #1f2937);
    margin: 0;
    font-size: 0.9rem;
    line-height: 1.5;
  }

  .composer {
    display: grid;
    gap: 0.6rem;
    flex-shrink: 0;
    padding: 1rem 1.5rem;
    border-top: 1px solid var(--gray-200, #e5e7eb);
    background: var(--gray-50, #f9fafb);
  }

  .composer textarea {
    resize: none;
  }

  .composer-actions {
    display: flex;
    justify-content: flex-end;
    gap: 0.5rem;
  }

  .empty-state {
    color: var(--gray-500, #6b7280);
    padding: 2rem 0;
    text-align: center;
    margin: auto 0;
  }

  .close-btn {
    background: transparent;
    color: var(--gray-500, #6b7280);
    font-size: 1.2rem;
    padding: 0.25rem;
    cursor: pointer;
    border: none;
  }

  .close-btn:hover {
    color: var(--gray-800, #1f2937);
  }

  @media (max-width: 640px) {
    .chat-modal {
      width: 100vw;
      height: 100vh;
      max-width: 100vw;
      border-radius: 0;
    }
  }
</style>
