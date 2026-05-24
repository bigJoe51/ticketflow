export function roleToDashboard(role) {
  const normalized = String(role || '').toUpperCase()
  if (normalized === 'ADMIN') return 'admin'
  if (normalized === 'STAFF') return 'staff'
  return 'client'
}

export function displayRole(role) {
  const normalized = String(role || 'USER').toUpperCase()
  return normalized === 'USER' ? 'CLIENT' : normalized
}

export function fullName(user) {
  return [user?.firstName, user?.lastName].filter(Boolean).join(' ') || user?.username || 'User'
}

export function initials(user) {
  return fullName(user)
    .split(' ')
    .map((part) => part[0] || '')
    .join('')
    .slice(0, 2)
    .toUpperCase() || 'US'
}

export function formatDateTime(value) {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  return date.toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' })
}

export function countByStatus(items, status) {
  return (items || []).filter((item) => String(item.status).toUpperCase() === status).length
}

export function priorityClass(priority) {
  const normalized = String(priority || '').toLowerCase()
  if (normalized === 'high') return 'priority-high'
  if (normalized === 'medium') return 'priority-medium'
  return 'priority-low'
}

export function normalizeSearch(value) {
  return String(value || '').trim().toLowerCase()
}

export function getErrorMessage(error, fallback = 'Something went wrong') {
  return error?.message || fallback
}
