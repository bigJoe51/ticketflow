import { useEffect, useMemo, useState } from 'react'
import { API, session } from './api.js'
import { Alert, Badge, EmptyRow, Navbar, SpinnerOverlay, ToastLayer } from './components.jsx'
import { Link, useRoute } from './router.jsx'
import {
  countByStatus,
  displayRole,
  formatDateTime,
  fullName,
  getErrorMessage,
  initials,
  normalizeSearch,
  priorityClass,
  roleToDashboard,
} from './utils.js'
import './ticketflow.css'
import './App.css'

function App() {
  const route = useRoute()

  if (route === '/login') return <LoginPage />
  if (route === '/signup') return <SignupPage />
  if (route === '/forgot-password') return <ForgotPasswordPage />
  if (route === '/reset-password') return <ResetPasswordPage />
  if (route === '/dashboard') return <DashboardPage />
  return <LandingPage />
}

function LandingPage() {
  const [mobileOpen, setMobileOpen] = useState(false)

  const smoothScroll = (event, id) => {
    event.preventDefault()
    document.querySelector(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
    setMobileOpen(false)
  }

  return (
    <div className="landing-page">
      <Navbar />
      <div className="mobile-menu" style={{ display: mobileOpen ? 'flex' : 'none' }}>
        {['features', 'how-it-works', 'about', 'pricing'].map((id) => (
          <a key={id} href={`#${id}`} onClick={(event) => smoothScroll(event, `#${id}`)}>{id.replaceAll('-', ' ')}</a>
        ))}
        <Link href="/login">Sign In</Link>
        <Link href="/signup">Get Started</Link>
      </div>
      <button className="mobile-menu-toggle react-mobile-toggle" type="button" onClick={() => setMobileOpen((value) => !value)}>
        <span /><span /><span />
      </button>

      <section className="hero">
        <div className="hero-veil" />
        <div className="hero-content">
          <h1 className="hero-title">Streamline Your Support <span>with Intelligent Ticket Management</span></h1>
          <p className="hero-subtitle">Transform your customer support experience with our modern ticketing system. Track, manage, and resolve tickets faster than ever before.</p>
          <div className="hero-buttons">
            <Link href="/signup" className="btn btn-primary btn-lg"><i className="fas fa-rocket" /> Start Free Trial</Link>
            <a href="#features" className="btn btn-outline btn-lg" onClick={(event) => smoothScroll(event, '#features')}><i className="fas fa-play" /> See How It Works</a>
          </div>
          <div className="hero-stats">
            <div className="hero-stat"><div className="hero-stat-number">10K+</div><div className="hero-stat-label">Active Users</div></div>
            <div className="hero-stat"><div className="hero-stat-number">50K+</div><div className="hero-stat-label">Tickets Resolved</div></div>
            <div className="hero-stat"><div className="hero-stat-number">99.9%</div><div className="hero-stat-label">Uptime</div></div>
          </div>
        </div>
      </section>

      <section id="features" className="features">
        <div className="container">
          <div className="section-header"><h2>Powerful Features</h2><p>Everything you need to manage customer support effectively</p></div>
          <div className="features-grid">
            {[
              ['fa-ticket-alt', 'Smart Ticket Management', 'Create, track, and manage support tickets with intelligent automation and priority routing.'],
              ['fa-users-cog', 'Role-Based Access', 'Granular permissions for Admin, Support Staff, and Clients with custom role configurations.'],
              ['fa-chart-line', 'Real-Time Analytics', 'Comprehensive dashboards with real-time metrics, trends, and performance insights.'],
              ['fa-bell', 'Smart Notifications', 'Multi-channel notifications via email, SMS, and in-app alerts for instant updates.'],
              ['fa-comments', 'Collaboration Tools', 'Internal notes, comments, and file attachments for seamless team collaboration.'],
              ['fa-star', 'Feedback & Ratings', 'Collect customer feedback and ratings to continuously improve service quality.'],
            ].map(([icon, title, body]) => (
              <div className="feature-card" key={title}>
                <div className="feature-icon"><i className={`fas ${icon}`} /></div>
                <h3>{title}</h3>
                <p>{body}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section id="how-it-works" className="how-it-works">
        <div className="container">
          <div className="section-header"><h2>How It Works</h2><p>Get started in minutes with our simple 4-step process</p></div>
          <div className="steps-container">
            {['Create Account', 'Create Tickets', 'Track Progress', 'Close & Rate'].map((title, index) => (
              <div className="step" key={title}>
                <div className="step-number">{index + 1}</div>
                <h3>{title}</h3>
                <p>{['Sign up in seconds with your email and get instant access', 'Log issues and support requests with detailed descriptions', 'Monitor ticket status and get real-time updates', 'Close resolved tickets and share your feedback'][index]}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section id="about" className="features">
        <div className="container">
          <div className="stats-grid">
            {['98% Customer Satisfaction', '2.5x Faster Resolution', '24/7 Support Available', '500+ Enterprise Clients'].map((stat) => {
              const [number, ...label] = stat.split(' ')
              return <div className="stat-card" key={stat}><div className="stat-number">{number}</div><div className="stat-label">{label.join(' ')}</div></div>
            })}
          </div>
        </div>
      </section>

      <section id="pricing" className="cta-section">
        <div className="container">
          <div className="cta-card">
            <h2>Ready to Transform Your Support?</h2>
            <p>Join thousands of organizations that trust TicketFlow for their ticket management needs</p>
            <Link href="/signup" className="btn btn-primary btn-lg"><i className="fas fa-rocket" /> Get Started Today</Link>
          </div>
        </div>
      </section>
      <Footer />
    </div>
  )
}

function Footer() {
  return (
    <footer className="footer">
      <div className="footer-content">
        {['TicketFlow', 'Product', 'Support', 'Legal'].map((heading) => (
          <div className="footer-section" key={heading}>
            <h4>{heading}</h4>
            {heading === 'TicketFlow' ? <p>Modern ticket management system for forward-thinking teams.</p> : (
              <ul>
                {['Features', 'How It Works', 'Documentation', 'Security'].map((item) => <li key={item}><a href="#features"><i className="fas fa-chevron-right" /> {item}</a></li>)}
              </ul>
            )}
          </div>
        ))}
      </div>
      <div className="footer-bottom"><p>&copy; 2026 TicketFlow. All rights reserved.</p></div>
    </footer>
  )
}

function LoginPage() {
  const initialLoginError = new URLSearchParams(window.location.search).get('error')
    ? 'Authentication could not be completed. Please try again.'
    : ''
  const [form, setForm] = useState({ email: '', password: '', rememberMe: false })
  const [error, setError] = useState(initialLoginError)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    const params = new URLSearchParams(window.location.search)
    const token = params.get('token')
    if (params.get('oauth') === '1' && token) {
      const user = Object.fromEntries(params.entries())
      session.setToken(token)
      session.setUser(user)
      navigate('/dashboard')
    }
  }, [])

  const submit = async (event) => {
    event.preventDefault()
    setError('')
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) return setError('Enter a valid email.')
    if (!form.password) return setError('Password is required.')
    setLoading(true)
    try {
      const response = await API.post('/auth/sign', { email: form.email.trim(), password: form.password })
      session.setToken(typeof response === 'string' ? response : response.token)
      session.setUser(response)
      if (form.rememberMe) localStorage.setItem('ticketflow-remember-email', form.email)
      else localStorage.removeItem('ticketflow-remember-email')
      navigate(new URLSearchParams(window.location.search).get('redirect') || '/dashboard')
    } catch (err) {
      setError(err.status === 401 ? 'Invalid email or password.' : getErrorMessage(err, 'Unable to sign in right now.'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <Navbar minimal />
      <SpinnerOverlay show={loading} />
      <div className="auth-container">
        <div className="auth-card">
          <div className="auth-header"><h1>Welcome Back</h1><p>Sign in to your account</p></div>
          <Alert>{error}</Alert>
          <form className="auth-form" onSubmit={submit}>
            <Field icon="fa-envelope" label="Email Address" type="email" value={form.email} onChange={(email) => setForm({ ...form, email })} placeholder="you@example.com" />
            <PasswordField label="Password" value={form.password} onChange={(password) => setForm({ ...form, password })} />
            <div className="form-options">
              <label className="remember-me"><input type="checkbox" checked={form.rememberMe} onChange={(event) => setForm({ ...form, rememberMe: event.target.checked })} /><span>Remember me</span></label>
              <Link href="/forgot-password" className="forgot-password">Forgot password?</Link>
            </div>
            <button type="submit" className="btn btn-primary btn-block" disabled={loading}>{loading ? 'Signing in...' : 'Sign In'} <i className="fas fa-arrow-right" /></button>
          </form>
          <div className="auth-footer"><p>Don't have an account? <Link href="/signup" className="auth-link">Sign up</Link></p></div>
        </div>
      </div>
    </>
  )
}

function SignupPage() {
  const [form, setForm] = useState({ firstName: '', lastName: '', username: '', email: '', phoneNumber: '', roleId: '', departmentId: '', password: '', confirmPassword: '' })
  const [roles, setRoles] = useState([])
  const [departments, setDepartments] = useState([])
  const [message, setMessage] = useState({ type: 'error', text: '' })
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    Promise.allSettled([API.get('/auth/roles'), API.get('/departments')]).then(([roleResult, departmentResult]) => {
      if (roleResult.status === 'fulfilled') setRoles(roleResult.value || [])
      if (departmentResult.status === 'fulfilled') setDepartments(departmentResult.value || [])
    })
  }, [])

  const update = (key, value) => setForm((current) => ({ ...current, [key]: value }))
  const passwordScore = ['(?=.{8,})', '(?=.*[A-Z])', '(?=.*[a-z])', '(?=.*[0-9])', '(?=.*[^A-Za-z0-9])'].filter((rule) => new RegExp(rule).test(form.password)).length

  const submit = async (event) => {
    event.preventDefault()
    setMessage({ type: 'error', text: '' })
    if (!form.firstName || !form.lastName || form.username.length < 3) return setMessage({ type: 'error', text: 'Name and username are required.' })
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) return setMessage({ type: 'error', text: 'Enter a valid email.' })
    if (form.password.length < 8) return setMessage({ type: 'error', text: 'Password must be at least 8 characters.' })
    if (form.password !== form.confirmPassword) return setMessage({ type: 'error', text: 'Passwords do not match.' })
    if (!form.roleId) return setMessage({ type: 'error', text: 'Select a role.' })
    setLoading(true)
    try {
      await API.post('/auth/register', {
        firstName: form.firstName,
        lastName: form.lastName,
        username: form.username,
        email: form.email,
        password: form.password,
        phoneNumber: form.phoneNumber,
        status: 'ACTIVE',
        role: { id: Number(form.roleId) },
        ...(form.departmentId ? { department: { id: Number(form.departmentId) } } : {}),
      })
      setMessage({ type: 'success', text: 'Account created successfully. Redirecting to sign in...' })
      setTimeout(() => navigate('/login'), 900)
    } catch (err) {
      setMessage({ type: 'error', text: getErrorMessage(err, 'Unable to create account.') })
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <Navbar minimal />
      <div className="auth-container">
        <div className="auth-card" style={{ maxWidth: 560 }}>
          <div className="auth-header"><h1>Create Account</h1><p>Register to start managing tickets</p></div>
          <Alert type={message.type}>{message.text}</Alert>
          <form className="auth-form" onSubmit={submit}>
            <div className="form-grid-two">
              <Field icon="fa-user" label="First Name" value={form.firstName} onChange={(value) => update('firstName', value)} placeholder="John" />
              <Field icon="fa-user" label="Last Name" value={form.lastName} onChange={(value) => update('lastName', value)} placeholder="Doe" />
            </div>
            <Field icon="fa-at" label="Username" value={form.username} onChange={(value) => update('username', value)} placeholder="john_doe" />
            <Field icon="fa-envelope" label="Email" type="email" value={form.email} onChange={(value) => update('email', value)} placeholder="john@example.com" />
            <Field icon="fa-phone" label="Phone Number" value={form.phoneNumber} onChange={(value) => update('phoneNumber', value)} placeholder="+254712345678" />
            <div className="form-grid-two">
              <SelectField icon="fa-user-tag" label="Role" value={form.roleId} onChange={(value) => update('roleId', value)} options={roles.map((role) => ({ value: role.id, label: displayRole(role.name) }))} placeholder="Select role" />
              <SelectField icon="fa-building" label="Department" value={form.departmentId} onChange={(value) => update('departmentId', value)} options={departments.map((department) => ({ value: department.id, label: department.name }))} placeholder="No department" />
            </div>
            <PasswordField label="Password" value={form.password} onChange={(value) => update('password', value)} />
            <div className="password-strength">
              <div className="strength-meter">{[0, 1, 2, 3].map((index) => <span key={index} className={`strength-segment ${index < Math.min(4, passwordScore) ? 'active' : ''}`} />)}</div>
              <span className="strength-text">{passwordScore <= 2 ? 'Weak password' : passwordScore <= 4 ? 'Medium password' : 'Strong password'}</span>
            </div>
            <PasswordField label="Confirm Password" value={form.confirmPassword} onChange={(value) => update('confirmPassword', value)} />
            <button type="submit" className="btn btn-primary btn-block" disabled={loading}>{loading ? 'Creating account...' : 'Create Account'}</button>
          </form>
          <div className="auth-footer"><p>Already have an account? <Link href="/login">Sign in</Link></p></div>
        </div>
      </div>
    </>
  )
}

function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [message, setMessage] = useState({})
  const submit = async (event) => {
    event.preventDefault()
    try {
      const response = await API.post('/auth/forgot-password', { email })
      setMessage({ type: 'success', text: response.message || 'Reset link generated.' })
    } catch (err) {
      setMessage({ type: 'error', text: getErrorMessage(err, 'Failed to generate reset link.') })
    }
  }
  return <AuthSimple title="Forgot Password" subtitle="Enter your email to generate a password reset link." message={message} onSubmit={submit} button="Generate Reset Link"><Field label="Email Address" type="email" value={email} onChange={setEmail} placeholder="you@example.com" /></AuthSimple>
}

function ResetPasswordPage() {
  const [form, setForm] = useState({ newPassword: '', confirmPassword: '' })
  const [message, setMessage] = useState({})
  const submit = async (event) => {
    event.preventDefault()
    const token = new URLSearchParams(window.location.search).get('token')
    if (!token) return setMessage({ type: 'error', text: 'Invalid reset link.' })
    if (form.newPassword !== form.confirmPassword) return setMessage({ type: 'error', text: 'Passwords do not match.' })
    try {
      const response = await API.post('/auth/reset-password', { token, newPassword: form.newPassword })
      setMessage({ type: 'success', text: response.message || 'Password changed successfully.' })
      setTimeout(() => navigate('/login'), 1200)
    } catch (err) {
      setMessage({ type: 'error', text: getErrorMessage(err, 'Could not reset password.') })
    }
  }
  return (
    <AuthSimple title="Reset Password" subtitle="Set a new password for your account." message={message} onSubmit={submit} button="Change Password">
      <PasswordField label="New Password" value={form.newPassword} onChange={(value) => setForm({ ...form, newPassword: value })} />
      <PasswordField label="Confirm New Password" value={form.confirmPassword} onChange={(value) => setForm({ ...form, confirmPassword: value })} />
    </AuthSimple>
  )
}

function AuthSimple({ title, subtitle, message, onSubmit, button, children }) {
  return (
    <>
      <Navbar minimal />
      <div className="auth-container">
        <div className="auth-card">
          <div className="auth-header"><h1>{title}</h1><p>{subtitle}</p></div>
          <Alert type={message.type}>{message.text}</Alert>
          <form className="auth-form" onSubmit={onSubmit}>
            {children}
            <button type="submit" className="btn btn-primary btn-block">{button}</button>
          </form>
          <div className="auth-footer"><p><Link href="/login" className="auth-link">Back to sign in</Link></p></div>
        </div>
      </div>
    </>
  )
}

function DashboardPage() {
  const [user] = useState(session.getUser())
  const [section, setSection] = useState('overview')
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [reportsOpen, setReportsOpen] = useState(false)
  const [notificationsOpen, setNotificationsOpen] = useState(false)
  const [escalationHidden, setEscalationHidden] = useState(false)
  const [loading, setLoading] = useState(true)
  const [toast, setToast] = useState(null)
  const [data, setData] = useState({ tickets: [], notifications: [], knowledge: [], users: [], departments: [], categories: [], audit: [], stats: {}, ratings: [], assets: [] })
  const [filters, setFilters] = useState({ category: 'ALL', status: 'ALL', sort: 'NEWEST', search: '' })

  const dashboardRole = roleToDashboard(user?.role)
  const nav = getDashboardNav(dashboardRole)
  const showToast = (message, type = 'info') => {
    setToast({ message, type })
    setTimeout(() => setToast(null), 3200)
  }

  useEffect(() => {
    if (!session.isAuthenticated()) {
      navigate('/login?redirect=/dashboard')
      return
    }
    loadDashboardData(dashboardRole, user)
      .then(setData)
      .catch((err) => showToast(getErrorMessage(err, 'Failed to load dashboard data.'), 'error'))
      .finally(() => setLoading(false))
  }, [dashboardRole, user])

  const filteredTickets = useMemo(() => {
    return (data.tickets || [])
      .filter((ticket) => filters.status === 'ALL' || String(ticket.status).toUpperCase() === filters.status)
      .filter((ticket) => filters.category === 'ALL' || String(ticket.categoryId || ticket.category || '').toUpperCase() === filters.category)
      .sort((a, b) => filters.sort === 'OLDEST' ? new Date(a.createdAt) - new Date(b.createdAt) : new Date(b.createdAt) - new Date(a.createdAt))
  }, [data.tickets, filters])

  const refresh = async () => setData(await loadDashboardData(dashboardRole, user))
  const openSection = (target) => {
    setSection(target)
    setSidebarOpen(false)
  }
  const escalatedTickets = (data.tickets || []).filter(isEscalatedTicket)
  const showEscalationPopup = (dashboardRole === 'admin' || dashboardRole === 'staff') && escalatedTickets.length > 0 && !escalationHidden

  useEffect(() => {
    if (!escalationHidden || !escalatedTickets.length) return undefined
    const timer = window.setTimeout(() => setEscalationHidden(false), 5000)
    return () => window.clearTimeout(timer)
  }, [escalationHidden, escalatedTickets.length])

  return (
    <div className="dashboard-wrapper">
      <aside className={`dashboard-sidebar ${sidebarOpen ? 'active' : ''}`} id="sidebar">
        <div className="sidebar-header"><div className="sidebar-logo"><i className={`fas ${dashboardRole === 'admin' ? 'fa-shield-halved' : dashboardRole === 'staff' ? 'fa-user-gear' : 'fa-user'}`} /></div><div className="sidebar-title">{dashboardRole === 'admin' ? 'Admin Console' : dashboardRole === 'staff' ? 'Staff Workspace' : 'Client Workspace'}</div></div>
        <ul className="sidebar-menu">
          {nav.map((item) => (
            <li key={item.key}>
              {item.children ? (
                <>
                  <button className={`menu-item submenu-toggle ${item.children.some((child) => child.key === section) ? 'active' : ''}`} type="button" data-expanded={reportsOpen} onClick={() => setReportsOpen((open) => !open)}>
                    <i className={`fas ${item.icon}`} /><span>{item.label}</span><i className="fas fa-chevron-down" />
                  </button>
                  <ul className={`submenu ${reportsOpen ? 'active' : ''}`} style={{ display: reportsOpen ? 'block' : 'none' }}>
                    {item.children.map((child) => (
                      <li key={child.key}><button className={`submenu-item ${section === child.key ? 'active' : ''}`} type="button" onClick={() => openSection(child.key)}>{child.label}</button></li>
                    ))}
                  </ul>
                </>
              ) : (
                <button className={`menu-item ${section === item.key ? 'active' : ''}`} type="button" onClick={() => openSection(item.key)}><i className={`fas ${item.icon}`} /><span>{item.label}</span></button>
              )}
            </li>
          ))}
        </ul>
        <div className="sidebar-footer">
          <div className="user-info"><div className="user-avatar">{initials(user)}</div><div className="user-details"><h4>{fullName(user)}</h4><span>{user?.email || 'No email'}</span></div></div>
        </div>
      </aside>

      <main className="dashboard-main">
        <div className="navbar navbar-dashboard" style={{ position: 'static', marginBottom: 'var(--spacing-xl)' }}>
          <div className="navbar-container" style={{ padding: 0 }}>
            <button className="btn toolbar-action menu-toggle-btn" type="button" aria-label="Open navigation menu" onClick={() => setSidebarOpen(true)}>
              <i className="fas fa-bars" /><span className="toolbar-action-label">Menu</span>
            </button>
            <div className="dashboard-header" style={{ marginBottom: 0 }}><h1>{displayRole(dashboardRole)} Dashboard</h1><p className="subtitle">{dashboardSubtitle(dashboardRole)}</p></div>
            <div className="nav-actions">
              <button className="btn toolbar-action notification-trigger" type="button" onClick={() => setNotificationsOpen(true)}><i className="fas fa-bell" /><span className="notification-dot">{data.notifications.length}</span><span className="toolbar-action-label">Notifications</span></button>
              <button className="btn btn-outline btn-sm" type="button" onClick={logoutUser}><i className="fas fa-right-from-bracket" /> Logout</button>
            </div>
          </div>
        </div>
        {loading ? <div className="table-container">Loading dashboard...</div> : (
          <>
            {section === 'overview' && <Overview role={dashboardRole} data={data} tickets={data.tickets} setSection={setSection} />}
            {section === 'tickets' && <TicketsSection role={dashboardRole} data={data} tickets={filteredTickets} filters={filters} setFilters={setFilters} refresh={refresh} showToast={showToast} />}
            {section === 'submit' && <SubmitTicket user={user} departments={data.departments} categories={data.categories} refresh={refresh} showToast={showToast} />}
            {section === 'comments' && <CommentsSection user={user} tickets={data.tickets} showToast={showToast} />}
            {section === 'knowledge' && <KnowledgeSection role={dashboardRole} user={user} articles={data.knowledge} showToast={showToast} refresh={refresh} />}
            {section === 'users' && <UsersSection users={data.users} departments={data.departments} refresh={refresh} showToast={showToast} />}
            {section === 'departments' && <DepartmentsSection departments={data.departments} refresh={refresh} showToast={showToast} />}
            {section === 'assets' && <AssetsSection assets={data.assets} departments={data.departments} refresh={refresh} showToast={showToast} />}
            {section === 'sla' && <SlaSection showToast={showToast} />}
            {section === 'reports' && <ReportsSection data={data} />}
            {section === 'escalations' && <EscalationWatch tickets={data.tickets} setSection={setSection} />}
            {section === 'audit' && <AuditLogSection audit={data.audit} />}
            {section === 'feedback' && <FeedbackSection user={user} tickets={data.tickets} ratings={data.ratings} showToast={showToast} />}
            {section === 'profile' && <ProfileSection user={user} showToast={showToast} />}
          </>
        )}
      </main>
      <NotificationDrawer open={notificationsOpen} notifications={data.notifications} onClose={() => setNotificationsOpen(false)} />
      {showEscalationPopup && <EscalationPopup tickets={escalatedTickets} role={dashboardRole} onClose={() => setEscalationHidden(true)} onOpen={() => { setSection(dashboardRole === 'admin' ? 'escalations' : 'tickets'); setEscalationHidden(true) }} />}
      <ToastLayer toast={toast} onClose={() => setToast(null)} />
    </div>
  )
}

async function loadDashboardData(role, user) {
  const userId = Number(user?.userId || 0)
  const common = await Promise.allSettled([API.get('/departments'), API.get('/ticket-categories')])
  const departments = common[0].status === 'fulfilled' ? common[0].value || [] : []
  const categories = common[1].status === 'fulfilled' ? common[1].value || [] : []

  if (role === 'admin') {
    const results = await settleObject({
      stats: API.get('/dashboard/admin'),
      tickets: API.get('/admin/tickets'),
      users: API.get('/admin/users'),
      audit: API.get('/audit'),
      pending: API.get('/knowledge/pending'),
      approved: API.get('/knowledge/approved'),
      notifications: userId ? API.get(`/notifications/user/${userId}`) : Promise.resolve([]),
      ratings: API.get('/ratings/all'),
      assets: departments[0]?.id ? API.get(`/assets/department/${departments[0].id}`) : Promise.resolve([]),
    })
    return { ...results, departments, categories, knowledge: [...(results.pending || []), ...(results.approved || [])] }
  }

  if (role === 'staff') {
    const results = await settleObject({
      tickets: API.get(`/tickets/staff/${userId}`),
      notifications: API.get(`/notifications/user/${userId}`),
      knowledge: API.get('/knowledge/approved'),
      avgRating: API.get(`/ratings/staff/${userId}`),
      ratings: API.get(`/ratings/staff/${userId}/details`),
    })
    return { ...results, departments, categories }
  }

  const results = await settleObject({
    tickets: API.get(`/tickets/user/${userId}`),
    notifications: API.get(`/notifications/user/${userId}`),
    knowledge: API.get('/knowledge/approved'),
  })
  return { ...results, departments, categories }
}

async function settleObject(requests) {
  const entries = Object.entries(requests)
  const settled = await Promise.allSettled(entries.map(([, request]) => request))
  return settled.reduce((acc, result, index) => {
    const key = entries[index][0]
    acc[key] = result.status === 'fulfilled' ? result.value : []
    return acc
  }, {})
}

function Overview({ role, data, tickets, setSection }) {
  const cards = role === 'admin'
    ? [['tickets', tickets.length, 'Total Tickets', 'fa-ticket'], ['tickets', countByStatus(tickets, 'OPEN'), 'Open Tickets', 'fa-clock'], ['tickets', countByStatus(tickets, 'RESOLVED'), 'Resolved Tickets', 'fa-check-circle'], ['users', data.users.length, 'Users', 'fa-users'], ['knowledge', (data.knowledge || []).filter((a) => String(a.status).toUpperCase() === 'PENDING').length, 'Pending Articles', 'fa-book-open']]
    : role === 'staff'
      ? [['tickets', tickets.length, 'Assigned Tickets', 'fa-ticket'], ['tickets', countByStatus(tickets, 'OPEN'), 'Open Tickets', 'fa-clock'], ['tickets', countByStatus(tickets, 'RESOLVED'), 'Resolved Tickets', 'fa-check-circle'], ['feedback', Number(data.avgRating || 0).toFixed(1), 'Average Rating', 'fa-star'], ['knowledge', data.knowledge.length, 'Approved Articles', 'fa-book-open']]
      : [['tickets', tickets.length, 'My Tickets', 'fa-ticket'], ['tickets', countByStatus(tickets, 'OPEN'), 'Open', 'fa-clock'], ['tickets', countByStatus(tickets, 'RESOLVED'), 'Resolved', 'fa-check-circle'], ['notifications', data.notifications.length, 'Unread Notifications', 'fa-bell'], ['knowledge', data.knowledge.length, 'Knowledge Articles', 'fa-book-open']]
  return <section className="dashboard-section active"><div className="stats-container">{cards.map(([target, number, label, icon]) => <div className="stat-item overview-link" key={label} onClick={() => setSection(target)}><div className="stat-header"><i className={`fas ${icon}`} /></div><div className="stat-number">{number}</div><div className="stat-label">{label}</div></div>)}</div></section>
}

function TicketsSection({ role, data, tickets, filters, setFilters, refresh, showToast }) {
  const [activeTicket, setActiveTicket] = useState(null)
  const [deletingTicket, setDeletingTicket] = useState(null)

  const updateTicket = async (ticket, values) => {
    try {
      await API.put(`/tickets/${ticket.id}`, values)
      showToast('Ticket updated')
      setActiveTicket(null)
      await refresh()
    } catch (err) {
      showToast(getErrorMessage(err, 'Could not update ticket.'), 'error')
    }
  }
  const deleteTicket = async (id) => {
    try {
      await API.delete(`/tickets/${id}`)
      showToast('Ticket deleted')
      setDeletingTicket(null)
      await refresh()
    } catch (err) {
      showToast(getErrorMessage(err, 'Could not delete ticket.'), 'error')
    }
  }

  return (
    <section className="dashboard-section active">
      <div className="table-container">
        <h3><i className="fas fa-ticket" /> {role === 'client' ? 'My Tickets' : 'Ticket Table'}</h3>
        <div className="ticket-filter-bar">
          <SelectBare label="Category" value={filters.category} onChange={(value) => setFilters({ ...filters, category: value })} options={[{ value: 'ALL', label: 'All categories' }, ...(data.categories || []).map((c) => ({ value: String(c.name || c.id).toUpperCase(), label: c.name }))]} />
          <SelectBare label="Status" value={filters.status} onChange={(value) => setFilters({ ...filters, status: value })} options={['ALL', 'OPEN', 'IN_PROGRESS', 'ESCALATED', 'RESOLVED'].map((value) => ({ value, label: value.replaceAll('_', ' ') }))} />
          <SelectBare label="Date" value={filters.sort} onChange={(value) => setFilters({ ...filters, sort: value })} options={[{ value: 'NEWEST', label: 'Newest to oldest' }, { value: 'OLDEST', label: 'Oldest to newest' }]} />
          <button className="btn btn-outline btn-sm" type="button" onClick={() => setFilters({ category: 'ALL', status: 'ALL', sort: 'NEWEST', search: '' })}>Reset</button>
        </div>
        <table>
          <thead><tr><th>ID</th><th>Title</th><th>Status</th><th>Priority</th><th>Assigned</th><th>Created</th><th>Action</th></tr></thead>
          <tbody>
            {!tickets.length && <EmptyRow cols={7} text="No tickets available." />}
            {tickets.map((ticket) => (
              <tr key={ticket.id}>
                <td>#{ticket.id}</td><td>{ticket.title || '-'}</td><td><Badge value={ticket.status} /></td><td><span className={priorityClass(ticket.priority)}>{ticket.priority || '-'}</span></td><td>{ticket.assignedStaffName || ticket.assignedStaff || 'Unassigned'}</td><td>{formatDateTime(ticket.createdAt)}</td>
                <td className="inline-actions">
                  {role !== 'client' && <button className="btn btn-sm btn-outline" type="button" onClick={() => setActiveTicket(ticket)}><i className="fas fa-sliders" /> Manage</button>}
                  {role === 'client' && <button className="btn btn-sm btn-danger" type="button" onClick={() => setDeletingTicket(ticket)}>Delete</button>}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <div className="kanban-board">
        {['OPEN', 'IN_PROGRESS', 'ESCALATED', 'RESOLVED'].map((status) => <div className="kanban-column" key={status}><h4>{status.replaceAll('_', ' ')}</h4>{tickets.filter((ticket) => String(ticket.status).toUpperCase() === status).map((ticket) => <div className="kanban-card" key={ticket.id}><strong>#{ticket.id} {ticket.title}</strong><span>{ticket.priority}</span></div>)}</div>)}
      </div>
      <TicketOperationsModal
        ticket={activeTicket}
        role={role}
        staff={getRegisteredStaff(data.users)}
        onClose={() => setActiveTicket(null)}
        onSave={(values) => updateTicket(activeTicket, values)}
      />
      <ConfirmModal
        open={Boolean(deletingTicket)}
        title="Delete Ticket"
        message={deletingTicket ? `Delete ticket #${deletingTicket.id} ${deletingTicket.title || ''}? This cannot be undone.` : ''}
        tone="danger"
        confirmLabel="Delete Ticket"
        onCancel={() => setDeletingTicket(null)}
        onConfirm={() => deleteTicket(deletingTicket.id)}
      />
    </section>
  )
}

function TicketOperationsModal({ ticket, role, staff, onClose, onSave }) {
  const [form, setForm] = useState({ status: '', priority: 'MEDIUM', assignedToId: '', clearAssignment: false })
  const [error, setError] = useState('')

  useEffect(() => {
    if (!ticket) return
    setForm({
      status: '',
      priority: String(ticket.priority || 'MEDIUM').toUpperCase(),
      assignedToId: ticket.assignedStaffId ? String(ticket.assignedStaffId) : '',
      clearAssignment: false,
    })
    setError('')
  }, [ticket])

  if (!ticket) return null

  const submit = (event) => {
    event.preventDefault()
    const payload = {
      priority: form.priority,
    }
    if (form.status) payload.status = form.status
    if (role === 'admin') {
      payload.assignedToId = form.clearAssignment || !form.assignedToId ? null : Number(form.assignedToId)
      payload.clearAssignment = Boolean(form.clearAssignment || !form.assignedToId)
    }
    if (form.status === 'RESOLVED' && role === 'admin' && !payload.assignedToId && !ticket.assignedStaffId) {
      setError('Assign a staff member before resolving this ticket.')
      return
    }
    onSave(payload)
  }

  return (
    <div className="app-modal active" role="dialog" aria-modal="true" aria-labelledby="ticket-operations-title">
      <div className="app-modal-backdrop" onClick={onClose} />
      <div className="app-modal-dialog ticket-operations-modal">
        <button className="app-modal-close" type="button" aria-label="Close ticket operations" onClick={onClose}><i className="fas fa-times" /></button>
        <div className="modal-kicker">Ticket Operations</div>
        <h3 id="ticket-operations-title">#{ticket.id} {ticket.title || 'Ticket'}</h3>
        <div className="ticket-detail-grid">
          <div className="ticket-detail-chip"><strong>Status</strong><span>{ticket.status || '-'}</span></div>
          <div className="ticket-detail-chip"><strong>Priority</strong><span>{ticket.priority || '-'}</span></div>
          <div className="ticket-detail-chip"><strong>Assigned</strong><span>{ticket.assignedStaffName || ticket.assignedStaff || 'Unassigned'}</span></div>
        </div>
        <form onSubmit={submit}>
          <SelectBare label="Status Action" value={form.status} onChange={(value) => { setError(''); setForm({ ...form, status: value }) }} options={[{ value: '', label: 'No manual change' }, { value: 'OPEN', label: 'Reopen' }, { value: 'RESOLVED', label: 'Resolve' }]} />
          <SelectBare label="Change Priority" value={form.priority} onChange={(value) => setForm({ ...form, priority: value })} options={['LOW', 'MEDIUM', 'HIGH'].map((value) => ({ value, label: value }))} />
          {role === 'admin' && (
            <>
              <div className="form-group ticket-filter-group">
                <label>Assign To Staff</label>
                <select className="form-input" value={form.assignedToId} disabled={form.clearAssignment} onChange={(event) => setForm({ ...form, assignedToId: event.target.value, clearAssignment: false })}>
                  <option value="">Unassigned</option>
                  {staff.map((member) => <option key={member.id} value={member.id}>{fullName(member)} ({member.email})</option>)}
                </select>
              </div>
              <label className="remember-me modal-check">
                <input type="checkbox" checked={form.clearAssignment} onChange={(event) => setForm({ ...form, clearAssignment: event.target.checked, assignedToId: event.target.checked ? '' : form.assignedToId })} />
                <span>Clear current assignment</span>
              </label>
            </>
          )}
          {error && <p className="modal-error">{error}</p>}
          <div className="modal-actions">
            <button className="btn btn-outline" type="button" onClick={onClose}>Cancel</button>
            <button className="btn btn-primary" type="submit"><i className="fas fa-save" /> Save Changes</button>
          </div>
        </form>
      </div>
    </div>
  )
}

function ConfirmModal({ open, title, message, tone = 'primary', confirmLabel = 'Confirm', onCancel, onConfirm }) {
  if (!open) return null
  return (
    <div className="app-modal active" role="dialog" aria-modal="true" aria-labelledby="confirm-modal-title">
      <div className="app-modal-backdrop" onClick={onCancel} />
      <div className="app-modal-dialog confirm-modal">
        <button className="app-modal-close" type="button" aria-label="Close confirmation" onClick={onCancel}><i className="fas fa-times" /></button>
        <div className={`confirm-modal-icon confirm-modal-icon-${tone}`}><i className="fas fa-triangle-exclamation" /></div>
        <h3 id="confirm-modal-title">{title}</h3>
        <p>{message}</p>
        <div className="modal-actions">
          <button className="btn btn-outline" type="button" onClick={onCancel}>Cancel</button>
          <button className={`btn ${tone === 'danger' ? 'btn-danger' : 'btn-primary'}`} type="button" onClick={onConfirm}>{confirmLabel}</button>
        </div>
      </div>
    </div>
  )
}

function SubmitTicket({ user, departments, categories, refresh, showToast }) {
  const [form, setForm] = useState({ title: '', description: '', priority: 'MEDIUM', categoryId: '', departmentId: '', files: [] })
  const submit = async (event) => {
    event.preventDefault()
    if (!form.title || !form.description) return showToast('Title and description are required.', 'error')
    try {
      const ticket = await API.post('/tickets/create', {
        title: form.title,
        description: form.description,
        priority: form.priority,
        status: 'OPEN',
        createdById: Number(user?.userId),
        categoryId: Number(form.categoryId || 0) || null,
        departmentId: Number(form.departmentId || 0) || null,
      })
      for (const file of form.files) {
        const fd = new FormData()
        fd.append('file', file)
        await API.post(`/attachments/ticket/${ticket?.id || ticket?.ticketId}/self`, fd, { isFormData: true })
      }
      showToast('Ticket submitted')
      setForm({ title: '', description: '', priority: 'MEDIUM', categoryId: '', departmentId: '', files: [] })
      refresh()
    } catch (err) {
      showToast(getErrorMessage(err, 'Could not submit ticket.'), 'error')
    }
  }
  return (
    <section className="dashboard-section active"><div className="table-container"><h3><i className="fas fa-plus-circle" /> Create Ticket</h3><form onSubmit={submit}>
      <Field label="Title" value={form.title} onChange={(value) => setForm({ ...form, title: value })} />
      <TextArea label="Description" value={form.description} onChange={(value) => setForm({ ...form, description: value })} />
      <SelectBare label="Priority" value={form.priority} onChange={(value) => setForm({ ...form, priority: value })} options={['LOW', 'MEDIUM', 'HIGH'].map((value) => ({ value, label: value }))} />
      <SelectBare label="Category" value={form.categoryId} onChange={(value) => setForm({ ...form, categoryId: value })} options={categories.map((c) => ({ value: c.id, label: c.name }))} />
      <SelectBare label="Department" value={form.departmentId} onChange={(value) => setForm({ ...form, departmentId: value })} options={departments.map((d) => ({ value: d.id, label: d.name }))} />
      <div className="form-group"><label>Attachments</label><input className="form-input" type="file" multiple onChange={(event) => setForm({ ...form, files: Array.from(event.target.files || []) })} /></div>
      <button className="btn btn-primary btn-block" type="submit">Submit Ticket</button>
    </form></div></section>
  )
}

function CommentsSection({ user, tickets, showToast }) {
  const [ticketId, setTicketId] = useState('')
  const [comment, setComment] = useState('')
  const [comments, setComments] = useState([])
  useEffect(() => { if (ticketId) API.get(`/comments/ticket/${ticketId}`).then(setComments).catch(() => setComments([])) }, [ticketId])
  const submit = async (event) => {
    event.preventDefault()
    if (!ticketId || !comment) return showToast('Ticket and comment required.', 'error')
    try {
      await API.post('/comments/add', { ticketId: Number(ticketId), authorId: Number(user?.userId), comment })
      setComment('')
      setComments(await API.get(`/comments/ticket/${ticketId}`))
      showToast('Comment added')
    } catch (err) {
      showToast(getErrorMessage(err, 'Could not add comment.'), 'error')
    }
  }
  return <section className="dashboard-section active"><div className="dashboard-grid"><div className="table-container"><h3><i className="fas fa-comment" /> Add Comment</h3><form onSubmit={submit}><SelectBare label="Ticket" value={ticketId} onChange={setTicketId} options={tickets.map((t) => ({ value: t.id, label: `#${t.id} ${t.title}` }))} /><TextArea label="Comment" value={comment} onChange={setComment} /><button className="btn btn-primary btn-block">Post Comment</button></form></div><div className="table-container"><h3><i className="fas fa-comments" /> Ticket Comments</h3><div className="activity-feed">{comments.map((item) => <div className="activity-item" key={item.id || item.createdAt}><div className="activity-icon"><i className="fas fa-comment" /></div><div className="activity-content"><p>{item.comment || item.content}</p><span className="activity-time">{formatDateTime(item.createdAt)}</span></div></div>)}</div></div></div></section>
}

function KnowledgeSection({ role, user, articles, showToast, refresh }) {
  const [query, setQuery] = useState('')
  const [form, setForm] = useState({ title: '', content: '' })
  const filtered = articles.filter((article) => `${article.title} ${article.content}`.toLowerCase().includes(normalizeSearch(query)))
  const submit = async (event) => {
    event.preventDefault()
    try {
      await API.post('/knowledge/create', { title: form.title, content: form.content, status: 'PENDING', createdById: Number(user?.userId) })
      setForm({ title: '', content: '' })
      showToast('Article submitted')
      refresh()
    } catch (err) {
      showToast(getErrorMessage(err, 'Could not submit article.'), 'error')
    }
  }
  const review = async (id, status) => {
    await API.put(`/knowledge/${id}/review`, { status })
    showToast(`Article ${status.toLowerCase()}`)
    refresh()
  }
  return <section className="dashboard-section active"><div className="dashboard-grid">{role === 'staff' && <div className="table-container"><h3>Create Article</h3><form onSubmit={submit}><Field label="Title" value={form.title} onChange={(value) => setForm({ ...form, title: value })} /><TextArea label="Content" value={form.content} onChange={(value) => setForm({ ...form, content: value })} /><button className="btn btn-outline btn-block">Submit For Approval</button></form></div>}<div className="table-container"><div className="table-header"><h3><i className="fas fa-book-open" /> Knowledge Base</h3><input className="form-input" placeholder="Search article" value={query} onChange={(event) => setQuery(event.target.value)} /></div><div className="online-help-links"><strong>Online Help Services:</strong><a href="https://support.microsoft.com" target="_blank">Microsoft Support</a><a href="https://support.google.com" target="_blank">Google Help Center</a><a href="https://stackoverflow.com" target="_blank">Stack Overflow</a></div><div className="activity-feed">{filtered.map((article) => <div className="activity-item" key={article.id}><div className="activity-icon"><i className="fas fa-book" /></div><div className="activity-content"><p><strong>{article.title}</strong></p><p>{String(article.content || '').slice(0, 180)}</p><span className="activity-time"><Badge value={article.status || 'APPROVED'} /></span></div>{role === 'admin' && String(article.status).toUpperCase() === 'PENDING' && <div className="inline-actions"><button className="btn btn-sm btn-outline" onClick={() => review(article.id, 'APPROVED')}>Approve</button><button className="btn btn-sm btn-outline" onClick={() => review(article.id, 'REJECTED')}>Reject</button></div>}</div>)}</div></div></div></section>
}

function UsersSection({ users, departments, refresh, showToast }) {
  const [form, setForm] = useState({ firstName: '', lastName: '', username: '', email: '', password: '', roleId: 3, departmentId: departments[0]?.id || '' })
  const submit = async (event) => {
    event.preventDefault()
    try {
      await API.post('/admin/users', { ...form, status: 'ACTIVE', role: { id: Number(form.roleId) }, department: { id: Number(form.departmentId || 1) } })
      showToast('User created')
      refresh()
    } catch (err) { showToast(getErrorMessage(err, 'Could not create user.'), 'error') }
  }
  const updateUser = async (id, key, value) => {
    await API.put(`/admin/users/${id}/${key}`, { [key]: value })
    showToast('User updated')
    refresh()
  }
  return <section className="dashboard-section active"><div className="dashboard-grid"><div className="table-container"><h3>Users</h3><table><thead><tr><th>ID</th><th>Name</th><th>Email</th><th>Role</th><th>Status</th><th>Actions</th></tr></thead><tbody>{users.map((u) => <tr key={u.id}><td>{u.id}</td><td>{fullName(u)}</td><td>{u.email}</td><td><select className="form-input table-action-select" value={displayRole(u.role)} onChange={(event) => updateUser(u.id, 'role', event.target.value)}><option>ADMIN</option><option>STAFF</option><option>CLIENT</option></select></td><td><Badge value={u.status || 'ACTIVE'} /></td><td><button className="btn btn-sm btn-outline" onClick={() => updateUser(u.id, 'status', 'ACTIVE')}>Activate</button><button className="btn btn-sm btn-outline" onClick={() => updateUser(u.id, 'status', 'INACTIVE')}>Deactivate</button></td></tr>)}</tbody></table></div><div className="table-container"><h3>Create User</h3><form onSubmit={submit}>{['firstName', 'lastName', 'username', 'email', 'password'].map((key) => <Field key={key} label={key} type={key === 'password' ? 'password' : 'text'} value={form[key]} onChange={(value) => setForm({ ...form, [key]: value })} />)}<SelectBare label="Role" value={form.roleId} onChange={(value) => setForm({ ...form, roleId: value })} options={[{ value: 2, label: 'STAFF' }, { value: 3, label: 'CLIENT' }]} /><SelectBare label="Department" value={form.departmentId} onChange={(value) => setForm({ ...form, departmentId: value })} options={departments.map((d) => ({ value: d.id, label: d.name }))} /><button className="btn btn-primary btn-block">Create User</button></form></div></div></section>
}

function DepartmentsSection({ departments, refresh, showToast }) {
  const [form, setForm] = useState({ name: '', description: '' })
  const submit = async (event) => {
    event.preventDefault()
    try { await API.post('/departments', form); setForm({ name: '', description: '' }); showToast('Department added'); refresh() }
    catch (err) { showToast(getErrorMessage(err, 'Could not add department.'), 'error') }
  }
  return <section className="dashboard-section active"><div className="table-container"><h3>Department Management</h3><form className="inline-form" onSubmit={submit}><input className="form-input" placeholder="Department name" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} /><input className="form-input" placeholder="Description" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} /><button className="btn btn-primary btn-sm">Add Department</button></form><table><thead><tr><th>ID</th><th>Name</th><th>Description</th></tr></thead><tbody>{departments.map((d) => <tr key={d.id}><td>{d.id}</td><td>{d.name}</td><td>{d.description || '-'}</td></tr>)}</tbody></table></div></section>
}

function AssetsSection({ assets, departments, refresh, showToast }) {
  const [form, setForm] = useState({ name: '', description: '', serialNumber: '', location: '', departmentId: departments[0]?.id || '', assignedToId: '' })
  const [deletingAsset, setDeletingAsset] = useState(null)
  const submit = async (event) => {
    event.preventDefault()
    try { await API.post('/assets/register', { ...form, status: 'ACTIVE', departmentId: Number(form.departmentId), assignedToId: Number(form.assignedToId) || null }); showToast('Asset registered'); refresh() }
    catch (err) { showToast(getErrorMessage(err, 'Could not register asset.'), 'error') }
  }
  const deleteAsset = async () => {
    try {
      await API.delete(`/assets/${deletingAsset.id}`)
      setDeletingAsset(null)
      showToast('Asset deleted')
      refresh()
    } catch (err) {
      showToast(getErrorMessage(err, 'Could not delete asset.'), 'error')
    }
  }
  return <section className="dashboard-section active"><div className="dashboard-grid"><div className="table-container"><h3>Register Asset</h3><form onSubmit={submit}>{['name', 'description', 'serialNumber', 'location', 'assignedToId'].map((key) => <Field key={key} label={key} value={form[key]} onChange={(value) => setForm({ ...form, [key]: value })} />)}<SelectBare label="Department" value={form.departmentId} onChange={(value) => setForm({ ...form, departmentId: value })} options={departments.map((d) => ({ value: d.id, label: d.name }))} /><button className="btn btn-primary btn-block">Register Asset</button></form></div><div className="table-container"><h3>Department Assets</h3><table><thead><tr><th>ID</th><th>Name</th><th>Status</th><th>Department</th><th>Action</th></tr></thead><tbody>{!assets.length && <EmptyRow cols={5} text="No assets registered." />}{assets.map((asset) => <tr key={asset.id}><td>{asset.id}</td><td>{asset.name}</td><td><Badge value={asset.status} /></td><td>{asset.departmentName || asset.department}</td><td><button className="btn btn-sm btn-danger" type="button" onClick={() => setDeletingAsset(asset)}>Delete</button></td></tr>)}</tbody></table></div></div><ConfirmModal open={Boolean(deletingAsset)} title="Delete Asset" message={deletingAsset ? `Delete asset ${deletingAsset.name || `#${deletingAsset.id}`}?` : ''} tone="danger" confirmLabel="Delete Asset" onCancel={() => setDeletingAsset(null)} onConfirm={deleteAsset} /></section>
}

function SlaSection({ showToast }) {
  const [sla, setSla] = useState({ highResponse: '2', highResolve: '8', mediumResponse: '4', mediumResolve: '24', lowResponse: '8', lowResolve: '48' })
  const [esc, setEsc] = useState({ high: '6', medium: '16', low: '32', status: 'ESCALATED' })

  useEffect(() => {
    Promise.allSettled([API.get('/admin/config/sla'), API.get('/admin/config/escalation')]).then(([slaResult, escResult]) => {
      if (slaResult.status === 'fulfilled') {
        const byPriority = Object.fromEntries((slaResult.value || []).map((item) => [String(item.priorityLevel).toUpperCase(), item]))
        setSla({
          highResponse: byPriority.HIGH?.responseTimeHours ?? '2',
          highResolve: byPriority.HIGH?.resolutionTimeHours ?? '8',
          mediumResponse: byPriority.MEDIUM?.responseTimeHours ?? '4',
          mediumResolve: byPriority.MEDIUM?.resolutionTimeHours ?? '24',
          lowResponse: byPriority.LOW?.responseTimeHours ?? '8',
          lowResolve: byPriority.LOW?.resolutionTimeHours ?? '48',
        })
      }
      if (escResult.status === 'fulfilled') {
        const byPriority = Object.fromEntries((escResult.value || []).map((item) => [String(item.priority).toUpperCase(), item]))
        setEsc({
          high: byPriority.HIGH?.thresholdHours ?? '6',
          medium: byPriority.MEDIUM?.thresholdHours ?? '16',
          low: byPriority.LOW?.thresholdHours ?? '32',
          status: byPriority.HIGH?.targetStatus || byPriority.MEDIUM?.targetStatus || byPriority.LOW?.targetStatus || 'ESCALATED',
        })
      }
    })
  }, [])

  const saveSla = async (event) => {
    event.preventDefault()
    await API.put('/admin/config/sla', [
      { priorityLevel: 'HIGH', responseTimeHours: Number(sla.highResponse), resolutionTimeHours: Number(sla.highResolve) },
      { priorityLevel: 'MEDIUM', responseTimeHours: Number(sla.mediumResponse), resolutionTimeHours: Number(sla.mediumResolve) },
      { priorityLevel: 'LOW', responseTimeHours: Number(sla.lowResponse), resolutionTimeHours: Number(sla.lowResolve) },
    ])
    showToast('SLA rules saved')
  }

  const saveEsc = async (event) => {
    event.preventDefault()
    await API.put('/admin/config/escalation', [
      { priority: 'HIGH', thresholdHours: Number(esc.high), escalationLevel: 1, targetStatus: esc.status, active: true },
      { priority: 'MEDIUM', thresholdHours: Number(esc.medium), escalationLevel: 1, targetStatus: esc.status, active: true },
      { priority: 'LOW', thresholdHours: Number(esc.low), escalationLevel: 1, targetStatus: esc.status, active: true },
    ])
    showToast('Escalation rules saved')
  }

  return (
    <section className="dashboard-section active">
      <div className="dashboard-grid" style={{ gridTemplateColumns: '1fr 1fr' }}>
        <div className="table-container">
          <h3><i className="fas fa-stopwatch" /> SLA Rules (Priority)</h3>
          <form onSubmit={saveSla}>
            <Field label="HIGH response (hrs)" value={sla.highResponse} onChange={(value) => setSla({ ...sla, highResponse: value })} />
            <Field label="HIGH resolution (hrs)" value={sla.highResolve} onChange={(value) => setSla({ ...sla, highResolve: value })} />
            <Field label="MEDIUM response (hrs)" value={sla.mediumResponse} onChange={(value) => setSla({ ...sla, mediumResponse: value })} />
            <Field label="MEDIUM resolution (hrs)" value={sla.mediumResolve} onChange={(value) => setSla({ ...sla, mediumResolve: value })} />
            <Field label="LOW response (hrs)" value={sla.lowResponse} onChange={(value) => setSla({ ...sla, lowResponse: value })} />
            <Field label="LOW resolution (hrs)" value={sla.lowResolve} onChange={(value) => setSla({ ...sla, lowResolve: value })} />
            <button className="btn btn-primary btn-block">Save SLA Rules</button>
          </form>
        </div>
        <div className="table-container">
          <h3><i className="fas fa-turn-up" /> Escalation Rules</h3>
          <form onSubmit={saveEsc}>
            <Field label="Escalate HIGH after (hrs)" value={esc.high} onChange={(value) => setEsc({ ...esc, high: value })} />
            <Field label="Escalate MEDIUM after (hrs)" value={esc.medium} onChange={(value) => setEsc({ ...esc, medium: value })} />
            <Field label="Escalate LOW after (hrs)" value={esc.low} onChange={(value) => setEsc({ ...esc, low: value })} />
            <Field label="Escalation status" value={esc.status} onChange={(value) => setEsc({ ...esc, status: value })} />
            <button className="btn btn-primary btn-block">Save Escalation Rules</button>
          </form>
        </div>
      </div>
    </section>
  )
}

function ReportsSection({ data }) {
  const staffRows = buildStaffPerformanceRows(data.tickets || [])
  return (
    <section className="dashboard-section active">
      <div className="table-container" style={{ marginBottom: 'var(--spacing-lg)' }}>
        <div className="table-header">
          <h3><i className="fas fa-file-chart-column" /> Report Controls</h3>
          <div className="inline-actions">
            <button className="btn btn-outline btn-sm" type="button" onClick={() => window.print()}><i className="fas fa-print" /> Print</button>
            <button className="btn btn-outline btn-sm" type="button" onClick={() => window.print()}><i className="fas fa-file-pdf" /> PDF</button>
          </div>
        </div>
        <div className="stats-container">
          <div className="stat-item"><div className="stat-number">{data.tickets.length}</div><div className="stat-label">Total Tickets</div></div>
          <div className="stat-item"><div className="stat-number">{countByStatus(data.tickets, 'OPEN')}</div><div className="stat-label">Open Tickets</div></div>
          <div className="stat-item"><div className="stat-number">{countByStatus(data.tickets, 'RESOLVED')}</div><div className="stat-label">Resolved Tickets</div></div>
          <div className="stat-item"><div className="stat-number">{(data.tickets || []).filter(isEscalatedTicket).length}</div><div className="stat-label">Escalated</div></div>
        </div>
      </div>
      <div className="dashboard-grid admin-report-grid" style={{ gridTemplateColumns: 'repeat(3, minmax(0, 1fr))' }}>
        <MiniChart title="Tickets Per Day" icon="fa-chart-bar" items={groupTicketsByDay(data.tickets)} />
        <MiniChart title="Resolution & SLA" icon="fa-bullseye" items={[['Open', countByStatus(data.tickets, 'OPEN')], ['Resolved', countByStatus(data.tickets, 'RESOLVED')], ['Escalated', (data.tickets || []).filter(isEscalatedTicket).length]]} />
        <StatusMix tickets={data.tickets} />
      </div>
      <div className="table-container" style={{ marginTop: 'var(--spacing-lg)' }}>
        <h3><i className="fas fa-user-check" /> Staff Performance (Derived)</h3>
        <table><thead><tr><th>Staff</th><th>Assigned</th><th>Resolved</th><th>Resolution Rate</th></tr></thead><tbody>{staffRows.map((row) => <tr key={row.name}><td>{row.name}</td><td>{row.assigned}</td><td>{row.resolved}</td><td>{row.rate}%</td></tr>)}</tbody></table>
      </div>
      <div className="table-container" style={{ marginTop: 'var(--spacing-lg)' }}>
        <h3><i className="fas fa-star" /> Staff Feedback & Ratings</h3>
        <table><thead><tr><th>When</th><th>Staff</th><th>Ticket</th><th>Rating</th><th>Feedback</th></tr></thead><tbody>{(data.ratings || []).map((rating) => <tr key={rating.id || `${rating.ticketId}-${rating.createdAt}`}><td>{formatDateTime(rating.createdAt)}</td><td>{rating.staffName || rating.staff || '-'}</td><td>{rating.ticketId || '-'}</td><td>{rating.rating || rating.score || '-'}</td><td>{rating.feedback || '-'}</td></tr>)}</tbody></table>
      </div>
    </section>
  )
}

function EscalationWatch({ tickets, setSection }) {
  const rows = (tickets || []).filter((ticket) => isEscalatedTicket(ticket) || typeof ticket.secondsToEscalation === 'number')
  return (
    <section className="dashboard-section active">
      <div className="table-container">
        <div className="table-header"><h3><i className="fas fa-siren-on" /> Escalation Watch</h3></div>
        <table>
          <thead><tr><th>Ticket</th><th>Status</th><th>Assigned To</th><th>Due</th><th>Clock</th><th>Action</th></tr></thead>
          <tbody>
            {!rows.length && <EmptyRow cols={6} text="No tickets are approaching escalation." />}
            {rows.map((ticket) => <tr key={ticket.id}><td>#{ticket.id} {ticket.title}</td><td><Badge value={ticket.status} /></td><td>{ticket.assignedStaffName || ticket.assignedStaff || 'Unassigned'}</td><td>{formatDateTime(ticket.escalationDueAt)}</td><td>{ticket.timeToEscalationLabel || (isEscalatedTicket(ticket) ? 'Escalated' : '-')}</td><td><button className="btn btn-sm btn-outline" type="button" onClick={() => setSection('tickets')}>Reassign / Reset</button></td></tr>)}
          </tbody>
        </table>
      </div>
    </section>
  )
}

function AuditLogSection({ audit = [] }) {
  return (
    <section className="dashboard-section active">
      <div className="table-container">
        <div className="table-header">
          <h3><i className="fas fa-clipboard-list" /> Audit Log</h3>
        </div>
        <table>
          <thead><tr><th>When</th><th>Actor</th><th>Action</th><th>Entity</th><th>Details</th></tr></thead>
          <tbody>
            {!audit.length && <EmptyRow cols={5} text="No audit entries available." />}
            {audit.map((entry) => (
              <tr key={entry.id || `${entry.entityType}-${entry.entityId}-${entry.timestamp}`}>
                <td>{formatDateTime(entry.timestamp)}</td>
                <td>{entry.performedByUsername || entry.performedBy || '-'}</td>
                <td><Badge value={entry.action} /></td>
                <td>{entry.entityType || '-'}{entry.entityId ? ` #${entry.entityId}` : ''}</td>
                <td>
                  <div className="audit-detail">
                    <strong>{entry.description || '-'}</strong>
                    {(entry.oldValue || entry.newValue) && <span>{entry.oldValue || '-'} {'->'} {entry.newValue || '-'}</span>}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  )
}

function FeedbackSection({ user, tickets, ratings = [], showToast }) {
  const [form, setForm] = useState({ ticketId: '', rating: 5, feedback: '' })
  const resolved = tickets.filter((ticket) => String(ticket.status).toUpperCase() === 'RESOLVED')
  const submit = async (event) => {
    event.preventDefault()
    try { await API.post('/ratings/rate', { ticketId: Number(form.ticketId), userId: Number(user?.userId), rating: Number(form.rating), feedback: form.feedback }); showToast('Rating submitted') }
    catch (err) { showToast(getErrorMessage(err, 'Could not submit rating.'), 'error') }
  }
  return <section className="dashboard-section active"><div className="table-container"><h3>Feedback & Ratings</h3>{resolved.length > 0 && <form onSubmit={submit}><SelectBare label="Ticket" value={form.ticketId} onChange={(value) => setForm({ ...form, ticketId: value })} options={resolved.map((t) => ({ value: t.id, label: `#${t.id} ${t.title}` }))} /><Field label="Score (1-5)" type="number" value={form.rating} onChange={(value) => setForm({ ...form, rating: value })} /><TextArea label="Feedback" value={form.feedback} onChange={(value) => setForm({ ...form, feedback: value })} /><button className="btn btn-outline btn-block">Submit Rating</button></form>}<div className="activity-feed">{ratings.map((r) => <div className="activity-item" key={r.id}><div className="activity-icon"><i className="fas fa-star" /></div><div className="activity-content"><p>{r.feedback || 'No feedback'}</p><span className="activity-time">{r.rating || r.score}/5</span></div></div>)}</div></div></section>
}

function ProfileSection({ user, showToast }) {
  const [form, setForm] = useState({ firstName: user?.firstName || '', lastName: user?.lastName || '', email: user?.email || '', file: null })
  const submit = async (event) => {
    event.preventDefault()
    try {
      let profileImageUrl = user?.profilePicture || user?.profileImageUrl || ''
      if (form.file) {
        const fd = new FormData()
        fd.append('file', form.file)
        const upload = await API.post(`/users/${user.userId}/avatar`, fd, { isFormData: true })
        profileImageUrl = upload.profileImageUrl || profileImageUrl
      }
      const updated = await API.put(`/users/${user.userId}/profile`, { firstName: form.firstName, lastName: form.lastName, email: form.email, profileImageUrl })
      session.setUser({ ...user, ...updated, ...form, profileImageUrl, profilePicture: profileImageUrl })
      showToast('Profile updated')
    } catch (err) { showToast(getErrorMessage(err, 'Could not update profile.'), 'error') }
  }
  return <section className="dashboard-section active"><div className="table-container"><h3>Profile Settings</h3><div className="profile-identity"><div className="user-avatar profile-avatar">{initials({ ...user, ...form })}</div><div className="profile-identity-text"><h4>{fullName({ ...user, ...form })}</h4><span>{form.email}</span></div></div><form onSubmit={submit}><Field label="First Name" value={form.firstName} onChange={(value) => setForm({ ...form, firstName: value })} /><Field label="Last Name" value={form.lastName} onChange={(value) => setForm({ ...form, lastName: value })} /><Field label="Email" value={form.email} onChange={(value) => setForm({ ...form, email: value })} /><div className="form-group"><label>Profile Picture</label><input className="form-input" type="file" accept="image/*" onChange={(e) => setForm({ ...form, file: e.target.files?.[0] })} /></div><button className="btn btn-primary btn-block">Update Profile</button></form></div></section>
}

function NotificationDrawer({ open, notifications, onClose }) {
  return (
    <>
      <div className={`notification-drawer-backdrop ${open ? 'active' : ''}`} onClick={onClose} />
      <aside className={`notification-drawer ${open ? 'active' : ''}`} aria-hidden={!open}>
        <div className="notification-drawer-header">
          <h3><i className="fas fa-bell" /> Notifications</h3>
          <button className="btn btn-icon btn-sm" type="button" aria-label="Close notifications" onClick={onClose}><i className="fas fa-times" /></button>
        </div>
        <div className="notification-drawer-list">
          {notifications.map((item) => <div className="activity-item" key={item.id}><div className="activity-icon"><i className="fas fa-bell" /></div><div className="activity-content"><p>{item.message || item.title || 'Notification'}</p><span className="activity-time">{formatDateTime(item.createdAt)}</span></div></div>)}
          {!notifications.length && <div className="activity-item"><div className="activity-content"><p>No notifications.</p></div></div>}
        </div>
      </aside>
    </>
  )
}

function EscalationPopup({ tickets, role, onClose, onOpen }) {
  return (
    <div className="escalation-popup active">
      <div className="escalation-popup-header">
        <h4>{role === 'admin' ? 'Escalated Tickets Need Reassignment' : 'Escalated Tickets Need Attention'}</h4>
        <button className="btn btn-icon btn-sm" type="button" onClick={onClose}><i className="fas fa-times" /></button>
      </div>
      <div className="escalation-popup-list">
        {tickets.map((ticket) => <div className="escalation-popup-item" key={ticket.id}><div><strong>#{ticket.id} {ticket.title || 'Ticket'}</strong><p>{ticket.assignedStaffName || ticket.assignedStaff || 'Unassigned'} - {ticket.timeToEscalationLabel || 'Escalated'}</p></div><button className="btn btn-sm btn-outline" type="button" onClick={onOpen}>Open</button></div>)}
      </div>
      <p className="escalation-popup-note">This alert will return every 5 seconds until the ticket is reassigned and the escalation timer is reset.</p>
    </div>
  )
}

function Field({ label, value, onChange, type = 'text', placeholder = '', icon }) {
  return <div className="form-group"><label>{label}</label><div className={icon ? 'input-wrapper' : undefined}>{icon && <i className={`fas ${icon}`} />}<input className="form-input" type={type} value={value} placeholder={placeholder} onChange={(event) => onChange(event.target.value)} required /></div></div>
}

function PasswordField({ label, value, onChange }) {
  const [visible, setVisible] = useState(false)
  return <div className="form-group"><label>{label}</label><div className="input-wrapper"><i className="fas fa-lock" /><input className="form-input" type={visible ? 'text' : 'password'} value={value} placeholder="••••••••" onChange={(event) => onChange(event.target.value)} required /><button className="password-button" type="button" onClick={() => setVisible(!visible)}><i className={`fas ${visible ? 'fa-eye-slash' : 'fa-eye'}`} /></button></div></div>
}

function TextArea({ label, value, onChange }) {
  return <div className="form-group"><label>{label}</label><textarea className="form-input" rows="4" value={value} onChange={(event) => onChange(event.target.value)} /></div>
}

function SelectField({ label, value, onChange, options, placeholder, icon }) {
  return <div className="form-group"><label>{label}</label><div className="input-wrapper">{icon && <i className={`fas ${icon}`} />}<select className="form-input" value={value} onChange={(event) => onChange(event.target.value)}><option value="">{placeholder}</option>{options.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}</select></div></div>
}

function SelectBare({ label, value, onChange, options }) {
  const hasEmptyOption = options.some((option) => String(option.value) === '')
  return <div className="form-group ticket-filter-group"><label>{label}</label><select className="form-input" value={value} onChange={(event) => onChange(event.target.value)}>{!hasEmptyOption && <option value="">Select</option>}{options.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}</select></div>
}

function getDashboardNav(role) {
  if (role === 'admin') return [
    ['overview', 'Overview', 'fa-chart-pie'],
    ['users', 'User Management', 'fa-users-gear'],
    ['departments', 'Department Management', 'fa-building'],
    ['assets', 'Asset Management', 'fa-laptop'],
    ['sla', 'SLA & Escalation', 'fa-stopwatch'],
    { key: 'reportsMenu', label: 'Reports', icon: 'fa-chart-line', children: [{ key: 'reports', label: 'Overview' }, { key: 'tickets', label: 'Ticket Operations' }, { key: 'escalations', label: 'Escalation Watch' }] },
    ['knowledge', 'Knowledge Base', 'fa-book'],
    ['audit', 'Audit Log', 'fa-clipboard-list'],
    ['profile', 'Profile', 'fa-id-badge'],
  ].map((item) => Array.isArray(item) ? { key: item[0], label: item[1], icon: item[2] } : item)
  if (role === 'staff') return [['overview', 'Overview', 'fa-chart-line'], ['tickets', 'Assigned Kanban', 'fa-columns'], ['comments', 'Communication', 'fa-comments'], ['knowledge', 'Knowledge Base', 'fa-book'], ['feedback', 'Performance', 'fa-bullseye'], ['profile', 'Profile', 'fa-id-card']].map(([key, label, icon]) => ({ key, label, icon }))
  return [['overview', 'Overview', 'fa-chart-line'], ['submit', 'Submit Ticket', 'fa-plus-circle'], ['tickets', 'My Tickets / Kanban', 'fa-columns'], ['comments', 'Communication', 'fa-comments'], ['feedback', 'Feedback', 'fa-star'], ['knowledge', 'Knowledge Base', 'fa-book-open'], ['profile', 'Profile', 'fa-id-card']].map(([key, label, icon]) => ({ key, label, icon }))
}

function dashboardSubtitle(role) {
  if (role === 'admin') return 'Full control for users, tickets, SLA, analytics, and compliance.'
  if (role === 'staff') return 'Handle assigned tickets, collaborate with clients, and build knowledge.'
  return 'Submit, track, discuss, and rate support interactions.'
}

function navigate(path) {
  window.history.pushState({}, '', path)
  window.dispatchEvent(new PopStateEvent('popstate'))
}

function logoutUser() {
  session.clear()
  navigate('/login')
}

function isEscalatedTicket(ticket) {
  return Boolean(ticket?.escalationTriggered) || String(ticket?.status || '').toUpperCase() === 'ESCALATED'
}

function groupTicketsByDay(tickets = []) {
  const counts = tickets.reduce((acc, ticket) => {
    const key = ticket.createdAt ? new Date(ticket.createdAt).toLocaleDateString() : 'Unknown'
    acc[key] = (acc[key] || 0) + 1
    return acc
  }, {})
  return Object.entries(counts).slice(-7)
}

function buildStaffPerformanceRows(tickets = []) {
  const grouped = tickets.reduce((acc, ticket) => {
    const name = ticket.assignedStaffName || ticket.assignedStaff || 'Unassigned'
    acc[name] ||= { name, assigned: 0, resolved: 0 }
    acc[name].assigned += 1
    if (String(ticket.status || '').toUpperCase() === 'RESOLVED') acc[name].resolved += 1
    return acc
  }, {})
  return Object.values(grouped).map((row) => ({ ...row, rate: row.assigned ? Math.round((row.resolved / row.assigned) * 100) : 0 }))
}

function getRegisteredStaff(users = []) {
  return users.filter((user) => displayRole(user.role) === 'STAFF')
}

function MiniChart({ title, icon, items }) {
  const max = Math.max(1, ...items.map(([, value]) => Number(value) || 0))
  return <div className="table-container"><h3><i className={`fas ${icon}`} /> {title}</h3><div className="mini-chart">{items.map(([label, value]) => <div className="mini-chart-row" key={label}><span>{label}</span><div className="mini-chart-bar"><span style={{ width: `${((Number(value) || 0) / max) * 100}%` }} /></div><strong>{value}</strong></div>)}</div></div>
}

function StatusMix({ tickets }) {
  const items = ['OPEN', 'IN_PROGRESS', 'ESCALATED', 'RESOLVED'].map((status) => [status.replaceAll('_', ' '), countByStatus(tickets, status)])
  return <MiniChart title="Ticket Status Mix" icon="fa-chart-pie" items={items} />
}

export default App
