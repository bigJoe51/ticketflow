import { useEffect } from 'react'
import { Link } from './router.jsx'

function useTheme() {
  useEffect(() => {
    const saved = localStorage.getItem('ticketflow-theme') || 'light'
    document.body.classList.toggle('dark-theme', saved === 'dark')
    document.body.classList.toggle('light-theme', saved !== 'dark')
  }, [])

  return () => {
    const dark = document.body.classList.contains('dark-theme')
    const next = dark ? 'light' : 'dark'
    localStorage.setItem('ticketflow-theme', next)
    document.body.classList.toggle('dark-theme', next === 'dark')
    document.body.classList.toggle('light-theme', next !== 'dark')
  }
}

export function Navbar({ minimal = false }) {
  const toggleTheme = useTheme()

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <Link href="/" className="navbar-brand">
          <span className="brand-icon">TF</span>
          <span>TicketFlow</span>
        </Link>
        {!minimal && (
          <ul className="nav-links">
            <li><a href="#features" className="active">Features</a></li>
            <li><a href="#how-it-works">How It Works</a></li>
            <li><a href="#about">About</a></li>
            <li><a href="#pricing">Pricing</a></li>
          </ul>
        )}
        <div className="nav-actions">
          <button className="theme-toggle" type="button" onClick={toggleTheme} title="Toggle theme">
            <i className="fas fa-moon" />
          </button>
          {!minimal && (
            <>
              <Link href="/login" className="btn btn-outline btn-sm"><i className="fas fa-sign-in-alt" /> Sign In</Link>
              <Link href="/signup" className="btn btn-primary btn-sm"><i className="fas fa-rocket" /> Get Started</Link>
            </>
          )}
        </div>
      </div>
    </nav>
  )
}

export function Alert({ type = 'error', children }) {
  if (!children) return null
  return <div className={`alert alert-${type}`}>{children}</div>
}

export function SpinnerOverlay({ show }) {
  if (!show) return null
  return <div className="spinner-overlay"><div className="spinner" /></div>
}

export function EmptyRow({ cols, text = 'No records found.' }) {
  return <tr><td colSpan={cols}>{text}</td></tr>
}

export function Badge({ value }) {
  const normalized = String(value || 'N/A').toUpperCase()
  return <span className={`status-badge status-${normalized.toLowerCase().replaceAll('_', '-')}`}>{normalized.replaceAll('_', ' ')}</span>
}

export function ToastLayer({ toast, onClose }) {
  if (!toast) return null
  return (
    <div className="app-toast-layer" aria-live="polite" aria-atomic="true">
      <div className={`app-toast app-toast-${toast.type || 'info'} active`}>
        <div className="app-toast-icon"><i className="fas fa-circle-info" /></div>
        <div className="app-toast-body">
          <div className="app-toast-title">{toast.type || 'Info'}</div>
          <div className="app-toast-message">{toast.message}</div>
        </div>
        <button className="app-toast-close" type="button" aria-label="Close message" onClick={onClose}>
          <i className="fas fa-times" />
        </button>
      </div>
    </div>
  )
}
