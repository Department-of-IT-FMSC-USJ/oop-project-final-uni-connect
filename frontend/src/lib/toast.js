import { writable } from 'svelte/store';

export const toasts = writable([]);

let nextToastId = 1;
const toastTimers = new Map();

function normalizePayload(variant, payload, options = {}) {
  if (typeof payload === 'string') {
    return {
      variant,
      title: '',
      message: payload,
      ...options
    };
  }

  return {
    variant,
    title: '',
    message: '',
    ...payload
  };
}

export function dismissToast(id) {
  const timer = toastTimers.get(id);
  if (timer && typeof window !== 'undefined') {
    window.clearTimeout(timer);
  }
  toastTimers.delete(id);
  toasts.update((items) => items.filter((item) => item.id !== id));
}

export function showToast(payload) {
  const id = nextToastId++;
  const toast = {
    id,
    title: '',
    message: '',
    variant: 'info',
    duration: 4200,
    ...payload
  };

  toasts.update((items) => [...items, toast]);

  if (toast.duration > 0 && typeof window !== 'undefined') {
    const timer = window.setTimeout(() => dismissToast(id), toast.duration);
    toastTimers.set(id, timer);
  }

  return id;
}

export const toast = {
  info: (payload, options) => showToast(normalizePayload('info', payload, options)),
  success: (payload, options) => showToast(normalizePayload('success', payload, options)),
  warning: (payload, options) => showToast(normalizePayload('warning', payload, options)),
  error: (payload, options) => showToast(normalizePayload('error', payload, options))
};
