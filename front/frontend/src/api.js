const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
const TOKEN_KEY = 'ticketflow-jwt-token'
const USER_KEY = 'ticketflow-user'

export class ApiError extends Error {
  constructor(message, status = 0, data = null) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.data = data
  }
}

export const session = {
  getToken: () => localStorage.getItem(TOKEN_KEY),
  setToken: (token) => localStorage.setItem(TOKEN_KEY, token),
  getUser: () => {
    try {
      return JSON.parse(localStorage.getItem(USER_KEY) || 'null')
    } catch {
      localStorage.removeItem(USER_KEY)
      return null
    }
  },
  setUser: (user) => localStorage.setItem(USER_KEY, JSON.stringify(user)),
  clear: () => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  },
  isAuthenticated: () => Boolean(localStorage.getItem(TOKEN_KEY)),
}

export async function apiRequest(endpoint, options = {}) {
  const { method = 'GET', body, headers = {}, isFormData = false } = options
  const requestHeaders = { ...headers }
  const token = session.getToken()

  if (token) requestHeaders.Authorization = `Bearer ${token}`
  if (body && !isFormData && !(body instanceof FormData)) {
    requestHeaders['Content-Type'] = 'application/json'
  }

  const response = await fetch(endpoint.startsWith('http') ? endpoint : `${API_BASE_URL}${endpoint}`, {
    method,
    headers: requestHeaders,
    credentials: 'include',
    body: body instanceof FormData || typeof body === 'string' ? body : body ? JSON.stringify(body) : undefined,
  })

  if (response.status === 401 && !endpoint.includes('/auth/sign')) {
    session.clear()
    throw new ApiError('Session expired. Please sign in again.', 401)
  }

  const contentType = response.headers.get('content-type') || ''
  let data = null
  if (contentType.includes('application/json')) data = await response.json()
  else if (contentType.includes('text')) data = await response.text()

  if (!response.ok) {
    const message =
      (data && typeof data === 'object' && (data.message || data.error)) ||
      (typeof data === 'string' && data) ||
      response.statusText ||
      'Request failed'
    throw new ApiError(message, response.status, data)
  }

  return data
}

export const API = {
  get: (endpoint) => apiRequest(endpoint),
  post: (endpoint, body, options) => apiRequest(endpoint, { method: 'POST', body, ...options }),
  put: (endpoint, body, options) => apiRequest(endpoint, { method: 'PUT', body, ...options }),
  delete: (endpoint) => apiRequest(endpoint, { method: 'DELETE' }),
}
