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
        {#if contact.profilePicture}
          <img src={contact.profilePicture} alt={contact.fullName} class="chat-avatar chat-avatar-img" />
        {:else}
          <div class="chat-avatar">{contact.fullName?.charAt(0) || '?'}</div>
        {/if}
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
          {@const isOwn = String(message.senderId) === String(currentUser?.id || currentUser?.userId)}
          <div class="message-row" class:is-own={isOwn}>
              {#if !isOwn && contact.profilePicture}
                <img src={contact.profilePicture} alt={contact.fullName} class="msg-avatar msg-avatar-img" />
              {:else if !isOwn}
                <div class="msg-avatar">{contact.fullName?.charAt(0) || '?'}</div>
              {/if}
            <div class="message-bubble" class:is-own={isOwn}>
              {#if !isOwn}
                <span class="msg-sender">{message.senderName || contact.fullName}</span>
              {/if}
              <p>{message.content}</p>
              <span class="msg-time">{formatDate(message.createdAt)}</span>
            </div>
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
    border-bottom: 1px solid var(--border-light);
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
    background: var(--primary);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    font-size: 1rem;
    flex-shrink: 0;
  }

  .chat-avatar-img {
    object-fit: cover;
  }

  .chat-name {
    font-size: 1rem;
    font-weight: 600;
    color: var(--text-main);
    margin: 0;
  }

  .chat-email {
    color: var(--text-secondary);
    font-size: 0.8rem;
    margin: 0;
  }

  .fallback-note {
    margin: 0;
    padding: 0.5rem 1.5rem;
    color: #92400e;
    font-size: 0.78rem;
    background: var(--warning-light);
    flex-shrink: 0;
  }

  .alert {
    margin: 0;
    padding: 0.7rem 1.5rem;
    flex-shrink: 0;
  }

  .alert-error {
    background: var(--danger-light);
    color: #991b1b;
  }

  .messages-panel {
    flex: 1;
    min-height: 0;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    padding: 1rem 1.25rem;
    background: var(--bg-alt);
  }

  .message-row {
    display: flex;
    align-items: flex-end;
    gap: 0.5rem;
    max-width: 78%;
  }

  .message-row.is-own {
    align-self: flex-end;
    flex-direction: row-reverse;
  }

  .msg-avatar {
    width: 30px;
    height: 30px;
    border-radius: 50%;
    background: var(--primary);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 0.78rem;
    flex-shrink: 0;
    align-self: flex-end;
  }

  .msg-avatar-img {
    object-fit: cover;
  }

  .message-bubble {
    padding: 0.6rem 0.9rem;
    border-radius: 16px 16px 16px 4px;
    background: var(--bg-main);
    border: 1px solid var(--border-light);
    max-width: 100%;
    word-break: break-word;
  }

  .message-bubble.is-own {
    background: var(--primary);
    border-color: transparent;
    border-radius: 16px 16px 4px 16px;
  }

  .msg-sender {
    display: block;
    font-size: 0.68rem;
    font-weight: 700;
    color: var(--primary);
    margin-bottom: 0.2rem;
    letter-spacing: 0.03em;
  }

  .message-bubble p {
    white-space: pre-wrap;
    color: var(--text-main);
    margin: 0;
    font-size: 0.875rem;
    line-height: 1.5;
  }

  .message-bubble.is-own p {
    color: #fff;
  }

  .msg-time {
    display: block;
    font-size: 0.62rem;
    color: var(--text-muted);
    margin-top: 0.2rem;
    text-align: right;
  }

  .message-bubble.is-own .msg-time {
    color: rgba(255, 255, 255, 0.7);
  }

  .composer {
    display: grid;
    gap: 0.6rem;
    flex-shrink: 0;
    padding: 1rem 1.5rem;
    border-top: 1px solid var(--border-light);
    background: var(--bg-alt);
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
    color: var(--text-muted);
    padding: 2rem 0;
    text-align: center;
    margin: auto 0;
  }

  .close-btn {
    background: transparent;
    color: var(--text-muted);
    font-size: 1.2rem;
    padding: 0.25rem;
    cursor: pointer;
    border: none;
  }

  .close-btn:hover {
    color: var(--text-main);
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
