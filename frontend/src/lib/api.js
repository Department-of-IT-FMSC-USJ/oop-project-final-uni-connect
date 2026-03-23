const API_BASE = 'http://localhost:8080/api';
const DEFAULT_GET_CACHE_TTL = 5000;
const responseCache = new Map();
const inFlightGetRequests = new Map();

function getToken() {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem('token');
}

function getUserRoleHeaderValue() {
  const currentUser = getCurrentUser();
  if (!currentUser?.role) return null;

  switch (currentUser.role) {
    case 'ACADEMIC_MENTOR':
    case 'INDUSTRY_MENTOR':
      return 'MENTOR';
    default:
      return currentUser.role;
  }
}

function getCacheKey(method, path) {
  return `${method}:${path}`;
}

function cloneData(data) {
  if (typeof structuredClone === 'function') {
    return structuredClone(data);
  }
  return JSON.parse(JSON.stringify(data));
}

function readCachedResponse(cacheKey) {
  const cached = responseCache.get(cacheKey);
  if (!cached) return null;
  if (cached.expiresAt <= Date.now()) {
    responseCache.delete(cacheKey);
    return null;
  }
  return cloneData(cached.data);
}

function writeCachedResponse(cacheKey, data, ttl) {
  if (!ttl || ttl <= 0) return;
  responseCache.set(cacheKey, {
    data: cloneData(data),
    expiresAt: Date.now() + ttl
  });
}

export function invalidateApiCache(pathPrefix = '') {
  const normalizedPrefix = pathPrefix.startsWith('/') ? pathPrefix : `/${pathPrefix}`;
  for (const key of responseCache.keys()) {
    const [, path] = key.split(':');
    if (!normalizedPrefix || path.startsWith(normalizedPrefix)) {
      responseCache.delete(key);
    }
  }
}

async function request(method, path, body = null, options = {}) {
  // Never attach Authorization header to auth endpoints.
  const headers = { 'Content-Type': 'application/json' };
  const token = getToken();
  const isAuthEndpoint = path.startsWith('/auth/') || path.startsWith('/auth');
  if (token && !isAuthEndpoint) headers['Authorization'] = `Bearer ${token}`;
  const roleHeaderValue = getUserRoleHeaderValue();
  if (roleHeaderValue) headers['X-User-Role'] = roleHeaderValue;

  const {
    signal,
    cache = method === 'GET',
    cacheTtl = DEFAULT_GET_CACHE_TTL
  } = options;
  const cacheKey = getCacheKey(method, path);

  if (method === 'GET' && cache) {
    const cached = readCachedResponse(cacheKey);
    if (cached !== null) {
      return cached;
    }

    if (inFlightGetRequests.has(cacheKey)) {
      return cloneData(await inFlightGetRequests.get(cacheKey));
    }
  } else if (method !== 'GET') {
    invalidateApiCache();
  }

  const opts = { method, headers, signal };
  if (body) opts.body = JSON.stringify(body);
  const requestPromise = (async () => {
    const res = await fetch(`${API_BASE}${path}`, opts);
    const data = await res.json().catch(() => null);
    if (!res.ok) throw { status: res.status, data };
    if (method === 'GET' && cache) {
      writeCachedResponse(cacheKey, data, cacheTtl);
    }
    return data;
  })();

  if (method === 'GET' && cache) {
    inFlightGetRequests.set(cacheKey, requestPromise);
  }

  try {
    return await requestPromise;
  } finally {
    if (method === 'GET' && cache) {
      inFlightGetRequests.delete(cacheKey);
    }
  }
}

export const api = {
  get: (path, options) => request('GET', path, null, options),
  post: (path, body, options) => request('POST', path, body, options),
  put: (path, body, options) => request('PUT', path, body, options),
  delete: (path, options) => request('DELETE', path, null, options),
};

export async function login(email, password) {
  const res = await api.post('/auth/login', { email, password });
  if (res.success && res.data) {
    localStorage.setItem('token', res.data.token);
    localStorage.setItem('user', JSON.stringify(res.data));
    return res.data;
  }
  throw new Error(res.message || 'Login failed');
}

export async function register(data) {
  const res = await api.post('/auth/register', data);
  if (res.success && res.data) {
    localStorage.setItem('token', res.data.token);
    localStorage.setItem('user', JSON.stringify(res.data));
    return res.data;
  }
  throw new Error(res.message || 'Registration failed');
}

export function logout() {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  window.location.href = '/';
}

export function getCurrentUser() {
  if (typeof window === 'undefined') return null;
  const raw = localStorage.getItem('user');
  if (!raw) return null;
  try { return JSON.parse(raw); }
  catch { return null; }
}

export function isAuthenticated() {
  return !!getToken();
}

export function getRoleDashboardPath(role) {
  switch (role) {
    case 'UNDERGRADUATE': return '/undergraduate/dashboard';
    case 'ACADEMIC_MENTOR': return '/academic-mentor/dashboard';
    case 'INDUSTRY_MENTOR': return '/industry-mentor/dashboard';
    case 'DEPARTMENT_HEAD': return '/hod/dashboard';
    default: return '/';
  }
}
