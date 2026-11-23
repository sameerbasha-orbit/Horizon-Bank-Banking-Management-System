import { useEffect, useState } from 'react'
import {
  ArrowDownLeft,
  ArrowUpRight,
  CreditCard,
  Landmark,
  ShieldCheck,
  Users,
  Wallet,
  RefreshCw,
  Search,
  Plus,
  X,
  Eye,
  Calendar,
  Mail,
  Phone,
  MapPin,
  User,
  ArrowLeftRight,
  Banknote,
  Activity
} from 'lucide-react'

const API = 'http://localhost:8080/api'

const initialCustomerForm = {
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  dateOfBirth: '',
  address: ''
}

const initialAccountForm = {
  customerId: '',
  branchId: '1',
  accountType: 'SAVINGS'
}

const initialTransactionForm = {
  accountId: '',
  fromAccountId: '',
  toAccountId: '',
  amount: '',
  description: ''
}

export default function App() {
  const [page, setPage] = useState('Overview')

  const [token, setToken] = useState(() => localStorage.getItem('horizon_token') || '')
  const [loggedInUser, setLoggedInUser] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem('horizon_user') || 'null')
    } catch {
      return null
    }
  })
  const [loginEmail, setLoginEmail] = useState('')
  const [loginPassword, setLoginPassword] = useState('')
  const [loginError, setLoginError] = useState('')
  const [loginLoading, setLoginLoading] = useState(false)

  const authFetch = (url, options = {}) => {
    return fetch(url, {
      ...options,
      headers: {
        ...(options.headers || {}),
        Authorization: `Bearer ${token}`,
      },
    })
  }

  const handleLogin = async (event) => {
    event.preventDefault()
    setLoginError('')
    setLoginLoading(true)

    try {
      const response = await fetch(`${API}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: loginEmail, password: loginPassword }),
      })
      const data = await response.json().catch(() => ({}))

      if (!response.ok || !data.token) {
        throw new Error(data.message || data.error || 'Login failed. Check your email and password.')
      }

      localStorage.setItem('horizon_token', data.token)
      localStorage.setItem('horizon_user', JSON.stringify({
        userId: data.userId,
        fullName: data.fullName,
        email: data.email,
        role: data.role,
      }))
      setLoggedInUser(data)
      setToken(data.token)
      setLoginPassword('')
    } catch (error) {
      setLoginError(error.message || 'Unable to log in.')
    } finally {
      setLoginLoading(false)
    }
  }

  const handleLogout = () => {
    localStorage.removeItem('horizon_token')
    localStorage.removeItem('horizon_user')
    setToken('')
    setLoggedInUser(null)
    setCustomers([])
    setAccounts([])
    setTransactions([])
    setSelectedCustomer(null)
    setPage('Overview')
    setError('')
    setSuccess('')
  }

  const [apiStatus, setApiStatus] = useState('Checking API...')
  const [customers, setCustomers] = useState([])
  const [accounts, setAccounts] = useState([])
  const [transactions, setTransactions] = useState([])
  const [transactionMode, setTransactionMode] = useState('DEPOSIT')
  const [transactionForm, setTransactionForm] = useState(initialTransactionForm)

  const [search, setSearch] = useState('')
  const [showCustomerForm, setShowCustomerForm] = useState(false)
  const [showAccountForm, setShowAccountForm] = useState(false)

  const [customerForm, setCustomerForm] = useState(initialCustomerForm)
  const [accountForm, setAccountForm] = useState(initialAccountForm)

  const [selectedCustomer, setSelectedCustomer] = useState(null)

  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)

  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  // -----------------------------
  // LOAD CUSTOMERS
  // -----------------------------

  async function loadCustomers() {
    setLoading(true)

    try {
      const response = await authFetch(`${API}/customers`)

      if (!response.ok) {
        throw new Error('Unable to load customers.')
      }

      const data = await response.json()
      setCustomers(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  // -----------------------------
  // LOAD ACCOUNTS
  // -----------------------------

  async function loadAccounts() {
    setLoading(true)

    try {
      const response = await authFetch(`${API}/accounts`)

      if (!response.ok) {
        throw new Error('Unable to load accounts.')
      }

      const data = await response.json()
      setAccounts(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  // -----------------------------
  // LOAD TRANSACTIONS
  // -----------------------------

  async function loadTransactions() {
    try {
      const response = await authFetch(`${API}/transactions`)
      if (!response.ok) throw new Error('Unable to load transactions.')
      const data = await response.json()
      setTransactions(data)
    } catch (err) {
      setError(err.message)
    }
  }

  async function refreshBankingData() {
    await Promise.all([loadAccounts(), loadTransactions()])
  }

  function updateTransactionForm(event) {
    const { name, value } = event.target
    setTransactionForm(previous => ({ ...previous, [name]: value }))
  }

  async function handleTransactionSubmit(event) {
    event.preventDefault()
    setSaving(true)
    setError('')
    setSuccess('')

    let endpoint = ''
    let payload = {}

    if (transactionMode === 'DEPOSIT') {
      endpoint = '/deposit'
      payload = {
        accountId: Number(transactionForm.accountId),
        amount: Number(transactionForm.amount),
        description: transactionForm.description
      }
    } else if (transactionMode === 'WITHDRAWAL') {
      endpoint = '/withdraw'
      payload = {
        accountId: Number(transactionForm.accountId),
        amount: Number(transactionForm.amount),
        description: transactionForm.description
      }
    } else {
      endpoint = '/transfer'
      payload = {
        fromAccountId: Number(transactionForm.fromAccountId),
        toAccountId: Number(transactionForm.toAccountId),
        amount: Number(transactionForm.amount),
        description: transactionForm.description
      }
    }

    try {
      const response = await authFetch(`${API}/transactions${endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      })
      const data = await response.json().catch(() => ({}))
      if (!response.ok) {
        throw new Error(data.message || data.error || 'Transaction failed. Verify account status and balance.')
      }
      setSuccess(`${transactionMode} successful. Reference: ${data.referenceNumber}`)
      setTransactionForm(initialTransactionForm)
      await refreshBankingData()
    } catch (err) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  // -----------------------------
  // INITIAL API CHECK
  // -----------------------------

  useEffect(() => {
    fetch(`${API}/health`)
      .then(response => {
        if (!response.ok) {
          throw new Error('Backend unavailable')
        }

        return response.json()
      })
      .then(data => {
        setApiStatus(`${data.application} API ${data.status}`)
      })
      .catch(() => {
        setApiStatus('Backend not connected')
      })

    if (token) {
      loadCustomers()
      loadAccounts()
      loadTransactions()
    }
  }, [token])

  // -----------------------------
  // CUSTOMER DETAILS
  // -----------------------------

  async function viewCustomer(id) {
    setError('')

    try {
      const response = await authFetch(`${API}/customers/${id}`)

      if (!response.ok) {
        throw new Error('Unable to load customer details.')
      }

      const data = await response.json()
      setSelectedCustomer(data)
    } catch (err) {
      setError(err.message)
    }
  }

  // -----------------------------
  // CUSTOMER FORM
  // -----------------------------

  function updateCustomerForm(event) {
    const { name, value } = event.target

    setCustomerForm(previous => ({
      ...previous,
      [name]: value
    }))
  }

  async function handleCustomerSubmit(event) {
    event.preventDefault()

    setSaving(true)
    setError('')
    setSuccess('')

    try {
      const response = await authFetch(`${API}/customers`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(customerForm)
      })

      const data = await response.json().catch(() => ({}))

      if (!response.ok) {
        throw new Error(
          data.message ||
          'Customer registration failed. Check the entered details.'
        )
      }

      setSuccess(
        `Customer registered successfully. Code: ${data.customerCode}`
      )

      setCustomerForm(initialCustomerForm)
      setShowCustomerForm(false)

      await loadCustomers()
    } catch (err) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  // -----------------------------
  // ACCOUNT FORM
  // -----------------------------

  function updateAccountForm(event) {
    const { name, value } = event.target

    setAccountForm(previous => ({
      ...previous,
      [name]: value
    }))
  }

  async function handleAccountSubmit(event) {
    event.preventDefault()

    setSaving(true)
    setError('')
    setSuccess('')

    try {
      const payload = {
        customerId: Number(accountForm.customerId),
        branchId: Number(accountForm.branchId),
        accountType: accountForm.accountType
      }

      const response = await authFetch(`${API}/accounts`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
      })

      const data = await response.json().catch(() => ({}))

      if (!response.ok) {
        throw new Error(
          data.message || 'Account creation failed.'
        )
      }

      setSuccess(
        `Account created successfully. Account number: ${data.accountNumber}`
      )

      setAccountForm(initialAccountForm)
      setShowAccountForm(false)

      await loadAccounts()
    } catch (err) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  // -----------------------------
  // SEARCH CUSTOMERS
  // -----------------------------

  const filteredCustomers = customers.filter(customer => {
    const term = search.toLowerCase()

    return [
      customer.firstName,
      customer.lastName,
      customer.email,
      customer.phone,
      customer.customerCode
    ].some(value =>
      String(value || '').toLowerCase().includes(term)
    )
  })

  // -----------------------------
  // COMMON STYLES
  // -----------------------------

  const inputStyle = {
    width: '100%',
    padding: '11px 12px',
    border: '1px solid #d8dfdc',
    borderRadius: 7,
    fontSize: 14,
    boxSizing: 'border-box'
  }

  const labelStyle = {
    display: 'block',
    fontSize: 13,
    fontWeight: 600,
    marginBottom: 7,
    color: '#34443e'
  }

  const actionStyle = {
    background: '#153e35',
    color: '#fff',
    border: 0,
    padding: '10px 15px',
    borderRadius: 8,
    cursor: 'pointer',
    display: 'inline-flex',
    alignItems: 'center',
    gap: 8,
    fontWeight: 600
  }

  // -----------------------------
  // CUSTOMER DETAIL COMPONENT
  // -----------------------------

  function Detail({ icon: Icon, label, value }) {
    return (
      <div style={{
        padding: 13,
        background: '#f5f8f6',
        borderRadius: 8
      }}>
        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: 7,
          color: '#718078',
          fontSize: 12,
          marginBottom: 8
        }}>
          <Icon size={15} />
          {label}
        </div>

        <div style={{
          fontWeight: 600,
          fontSize: 14,
          color: '#263a32',
          overflowWrap: 'anywhere'
        }}>
          {value || 'Not provided'}
        </div>
      </div>
    )
  }

  // -----------------------------
  // LOGIN SCREEN
  // -----------------------------

  if (!token) {
    return (
      <div style={{ minHeight: '100vh', display: 'grid', placeItems: 'center', background: '#eef3f0', padding: 20, fontFamily: 'Arial, sans-serif' }}>
        <div style={{ width: '100%', maxWidth: 420, background: '#fff', padding: 32, borderRadius: 16, boxShadow: '0 18px 50px rgba(20, 50, 40, 0.12)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 28 }}>
            <div style={{ width: 46, height: 46, borderRadius: 12, background: '#153e35', color: '#fff', display: 'grid', placeItems: 'center' }}>
              <Landmark size={24} />
            </div>
            <div>
              <div style={{ color: '#153e35', fontWeight: 800, letterSpacing: 2 }}>HORIZON</div>
              <div style={{ color: '#718078', fontSize: 12, letterSpacing: 3 }}>BANK</div>
            </div>
          </div>

          <h2 style={{ color: '#203b32', margin: '0 0 8px' }}>Sign in</h2>
          <p style={{ color: '#718078', margin: '0 0 24px', fontSize: 14 }}>Use your registered Horizon Bank account.</p>

          <form onSubmit={handleLogin}>
            <label style={{ display: 'block', color: '#34443e', fontSize: 13, fontWeight: 600, marginBottom: 7 }}>Email address</label>
            <input type="email" value={loginEmail} onChange={event => setLoginEmail(event.target.value)} required autoComplete="username" placeholder="Enter your email" style={{ width: '100%', boxSizing: 'border-box', padding: 12, border: '1px solid #d8dfdc', borderRadius: 8, marginBottom: 18, fontSize: 14 }} />

            <label style={{ display: 'block', color: '#34443e', fontSize: 13, fontWeight: 600, marginBottom: 7 }}>Password</label>
            <input type="password" value={loginPassword} onChange={event => setLoginPassword(event.target.value)} required autoComplete="current-password" placeholder="Enter your password" style={{ width: '100%', boxSizing: 'border-box', padding: 12, border: '1px solid #d8dfdc', borderRadius: 8, marginBottom: 18, fontSize: 14 }} />

            {loginError && <div role="alert" style={{ padding: 11, background: '#fff0ef', color: '#b42318', borderRadius: 7, marginBottom: 16, fontSize: 13 }}>{loginError}</div>}

            <button type="submit" disabled={loginLoading} style={{ width: '100%', border: 0, borderRadius: 8, padding: 13, background: '#153e35', color: '#fff', fontWeight: 700, cursor: loginLoading ? 'wait' : 'pointer' }}>
              {loginLoading ? 'Signing in...' : 'Sign in securely'}
            </button>
          </form>
          <p style={{ color: '#89958f', fontSize: 11, lineHeight: 1.5, margin: '20px 0 0' }}>Horizon Bank · Educational demonstration environment. Use an account already registered in the backend.</p>
        </div>
      </div>
    )
  }

  // -----------------------------
  // MAIN UI
  // -----------------------------

  return (
    <div className="app-shell">

      {/* SIDEBAR */}

      <aside className="sidebar">

        <div className="brand">
          <div className="brand-mark">
            <Landmark size={22} />
          </div>

          <div>
            <strong>HORIZON</strong>
            <span>BANK</span>
          </div>
        </div>

        <div className="nav-caption">WORKSPACE</div>

        <button
          className={`nav-item ${page === 'Overview' ? 'active' : ''}`}
          onClick={() => setPage('Overview')}
        >
          <span>▦</span>
          Overview
        </button>

        <button
          className={`nav-item ${page === 'Customers' ? 'active' : ''}`}
          onClick={() => {
            setPage('Customers')
            setError('')
            loadCustomers()
          }}
        >
          <Users size={18} />
          Customers
        </button>

        <button
          className={`nav-item ${page === 'Accounts' ? 'active' : ''}`}
          onClick={() => {
            setPage('Accounts')
            setError('')
            loadAccounts()
          }}
        >
          <CreditCard size={18} />
          Accounts
        </button>

        <button
          className={`nav-item ${page === 'Transactions' ? 'active' : ''}`}
          onClick={() => setPage('Transactions')}
        >
          <RefreshCw size={18} />
          Transactions
        </button>

        <button
          className={`nav-item ${page === 'KYC verification' ? 'active' : ''}`}
          onClick={() => setPage('KYC verification')}
        >
          <ShieldCheck size={18} />
          KYC verification
        </button>

        <div className="sidebar-bottom">
          <div className="secure">
            <ShieldCheck size={17} />
            <span>Secure banking workspace</span>
          </div>

          <small>Horizon Bank · Demo environment</small>
        </div>

      </aside>

      {/* MAIN CONTENT */}

      <main className="main">

        <header className="topbar">
          <div>
            <span className="eyebrow">BANKING OPERATIONS</span>
            <h1>{page}</h1>
          </div>

          <div className="profile" style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <div className="avatar">{(loggedInUser?.fullName || loggedInUser?.email || 'U').slice(0, 2).toUpperCase()}</div>

            <div>
              <b>{loggedInUser?.fullName || loggedInUser?.email || 'Signed-in user'}</b>
              <small>{loggedInUser?.role || 'Authenticated user'}</small>
            </div>

            <button type="button" onClick={handleLogout} style={{ marginLeft: 12, border: '1px solid #d8dfdc', background: '#fff', color: '#153e35', borderRadius: 7, padding: '8px 11px', cursor: 'pointer' }}>Logout</button>
          </div>
        </header>

        {/* ERROR MESSAGE */}

        {error && (
          <div style={{
            padding: 13,
            background: '#fff0ef',
            color: '#b42318',
            borderRadius: 7,
            marginTop: 18
          }}>
            {error}

            <button
              onClick={() => setError('')}
              style={{
                float: 'right',
                border: 0,
                background: 'transparent',
                cursor: 'pointer'
              }}
            >
              <X size={16} />
            </button>
          </div>
        )}

        {/* SUCCESS MESSAGE */}

        {success && (
          <div style={{
            padding: 13,
            background: '#eaf7ee',
            color: '#18713a',
            borderRadius: 7,
            marginTop: 18
          }}>
            {success}
          </div>
        )}

        {/* OVERVIEW PAGE */}

        {page === 'Overview' && (
          <>
            <section className="welcome">
              <div>
                <span className="pill">● SYSTEM OVERVIEW</span>

                <h2>Banking, with a clearer horizon.</h2>

                <p>
                  Monitor customers, accounts and banking activity
                  from one workspace.
                </p>
              </div>

              <div className="welcome-icon">
                <Landmark size={58} />
              </div>
            </section>

            <section className="metrics">

              <article className="metric">
                <div className="metric-top">
                  <span>Total customers</span>
                  <Users size={19} />
                </div>

                <strong>{customers.length}</strong>
                <small>Registered customers</small>
              </article>

              <article className="metric">
                <div className="metric-top">
                  <span>Total accounts</span>
                  <CreditCard size={19} />
                </div>

                <strong>{accounts.length}</strong>
                <small>Created accounts</small>
              </article>

              <article className="metric">
                <div className="metric-top">
                  <span>Total deposits</span>
                  <Wallet size={19} />
                </div>

                <strong>{transactions.filter(t => t.transactionType === 'DEPOSIT' && t.status === 'SUCCESS').length}</strong>
                <small>Successful deposits</small>
              </article>

              <article className="metric">
                <div className="metric-top">
                  <span>Transactions today</span>
                  <RefreshCw size={19} />
                </div>

                <strong>{transactions.length}</strong>
                <small>Recorded transactions</small>
              </article>

            </section>

            <section className="lower-grid">

              <article className="panel">
                <div className="panel-heading">
                  <div>
                    <span className="eyebrow">BANKING SERVICES</span>
                    <h3>Core operations</h3>
                  </div>

                  <span className="status">MODULES IN PROGRESS</span>
                </div>

                <div className="service-grid">
                  <div className="service">
                    <ArrowDownLeft />
                    <b>Cash deposit</b>
                    <small>Credit funds to an account</small>
                  </div>

                  <div className="service">
                    <ArrowUpRight />
                    <b>Cash withdrawal</b>
                    <small>Debit funds from an account</small>
                  </div>

                  <div className="service">
                    <RefreshCw />
                    <b>Fund transfer</b>
                    <small>Move funds between accounts</small>
                  </div>

                  <div className="service">
                    <CreditCard />
                    <b>Account services</b>
                    <small>Manage customer accounts</small>
                  </div>
                </div>
              </article>

              <article className="panel api-panel">
                <span className="eyebrow">DEVELOPMENT STATUS</span>

                <h3>System connection</h3>

                <div className="api-indicator">
                  <i />
                  {apiStatus}
                </div>

                <p>
                  Frontend health check for the Spring Boot REST API.
                </p>

                <div className="api-foot">
                  Environment <b>LOCAL DEVELOPMENT</b>
                </div>
              </article>

            </section>
          </>
        )}

        {/* CUSTOMERS PAGE */}

        {page === 'Customers' && (
          <section style={{ marginTop: 25 }}>

            <div style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              flexWrap: 'wrap',
              gap: 12,
              marginBottom: 22
            }}>
              <div>
                <span className="eyebrow">CUSTOMER OPERATIONS</span>

                <h2 style={{ margin: '7px 0' }}>
                  Customer Management
                </h2>

                <p style={{ color: '#718078', margin: 0 }}>
                  Register and manage bank customers.
                </p>
              </div>

              <button
                style={actionStyle}
                onClick={() => {
                  setShowCustomerForm(!showCustomerForm)
                  setError('')
                  setSuccess('')
                }}
              >
                {showCustomerForm ? <X size={17} /> : <Plus size={17} />}

                {showCustomerForm ? 'Close form' : 'Add Customer'}
              </button>
            </div>

            {/* CUSTOMER REGISTRATION FORM */}

            {showCustomerForm && (
              <article className="panel" style={{ marginBottom: 22 }}>

                <h3 style={{ marginTop: 0 }}>
                  New Customer Registration
                </h3>

                <form onSubmit={handleCustomerSubmit}>

                  <div style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
                    gap: 16
                  }}>

                    <div>
                      <label style={labelStyle}>First Name *</label>

                      <input
                        style={inputStyle}
                        name="firstName"
                        value={customerForm.firstName}
                        onChange={updateCustomerForm}
                        required
                        maxLength="80"
                      />
                    </div>

                    <div>
                      <label style={labelStyle}>Last Name *</label>

                      <input
                        style={inputStyle}
                        name="lastName"
                        value={customerForm.lastName}
                        onChange={updateCustomerForm}
                        required
                        maxLength="80"
                      />
                    </div>

                    <div>
                      <label style={labelStyle}>Email Address *</label>

                      <input
                        style={inputStyle}
                        type="email"
                        name="email"
                        value={customerForm.email}
                        onChange={updateCustomerForm}
                        required
                      />
                    </div>

                    <div>
                      <label style={labelStyle}>Phone Number *</label>

                      <input
                        style={inputStyle}
                        name="phone"
                        value={customerForm.phone}
                        onChange={updateCustomerForm}
                        pattern="[0-9]{10,20}"
                        required
                      />
                    </div>

                    <div>
                      <label style={labelStyle}>Date of Birth</label>

                      <input
                        style={inputStyle}
                        type="date"
                        name="dateOfBirth"
                        value={customerForm.dateOfBirth}
                        onChange={updateCustomerForm}
                      />
                    </div>

                    <div>
                      <label style={labelStyle}>Address</label>

                      <input
                        style={inputStyle}
                        name="address"
                        value={customerForm.address}
                        onChange={updateCustomerForm}
                        maxLength="255"
                      />
                    </div>

                  </div>

                  <p style={{ fontSize: 12, color: '#718078' }}>
                    Customer code is generated automatically.
                    KYC starts as PENDING.
                  </p>

                  <button
                    type="submit"
                    style={actionStyle}
                    disabled={saving}
                  >
                    <Plus size={17} />

                    {saving ? 'Registering...' : 'Register Customer'}
                  </button>

                </form>
              </article>
            )}

            {/* CUSTOMER TABLE */}

            <article className="panel">

              <div style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                gap: 12,
                flexWrap: 'wrap',
                marginBottom: 18
              }}>

                <div>
                  <h3 style={{ margin: 0 }}>
                    Registered Customers
                  </h3>

                  <small style={{ color: '#718078' }}>
                    {customers.length} customer(s) found
                  </small>
                </div>

                <div style={{ display: 'flex', gap: 8 }}>

                  <div style={{ position: 'relative' }}>
                    <Search
                      size={16}
                      style={{
                        position: 'absolute',
                        left: 10,
                        top: 12,
                        color: '#718078'
                      }}
                    />

                    <input
                      style={{
                        ...inputStyle,
                        paddingLeft: 34,
                        width: 230
                      }}
                      placeholder="Search customers..."
                      value={search}
                      onChange={event => setSearch(event.target.value)}
                    />
                  </div>

                  <button
                    style={actionStyle}
                    onClick={loadCustomers}
                  >
                    <RefreshCw size={17} />
                  </button>

                </div>
              </div>

              {loading ? (
                <p>Loading customers...</p>
              ) : filteredCustomers.length === 0 ? (
                <p>No customers found.</p>
              ) : (

                <div style={{ overflowX: 'auto' }}>

                  <table style={{
                    width: '100%',
                    borderCollapse: 'collapse',
                    textAlign: 'left',
                    fontSize: 13
                  }}>

                    <thead>
                      <tr style={{ background: '#f4f7f5' }}>
                        {[
                          'Customer',
                          'Customer Code',
                          'Phone',
                          'Email',
                          'KYC Status',
                          'Action'
                        ].map(item => (
                          <th
                            key={item}
                            style={{
                              padding: 13,
                              whiteSpace: 'nowrap'
                            }}
                          >
                            {item}
                          </th>
                        ))}
                      </tr>
                    </thead>

                    <tbody>
                      {filteredCustomers.map(customer => (
                        <tr
                          key={customer.id}
                          style={{
                            borderBottom: '1px solid #edf0ee'
                          }}
                        >

                          <td style={{ padding: 13 }}>
                            <b>
                              {customer.firstName} {customer.lastName}
                            </b>
                          </td>

                          <td style={{ padding: 13 }}>
                            {customer.customerCode}
                          </td>

                          <td style={{ padding: 13 }}>
                            {customer.phone}
                          </td>

                          <td style={{ padding: 13 }}>
                            {customer.email}
                          </td>

                          <td style={{ padding: 13 }}>
                            <span style={{
                              background:
                                customer.kycStatus === 'VERIFIED'
                                  ? '#e5f5e9'
                                  : '#fff4d8',
                              color:
                                customer.kycStatus === 'VERIFIED'
                                  ? '#18713a'
                                  : '#8a6410',
                              padding: '5px 9px',
                              borderRadius: 20,
                              fontSize: 11,
                              fontWeight: 700
                            }}>
                              {customer.kycStatus}
                            </span>
                          </td>

                          <td style={{ padding: 13 }}>
                            <button
                              style={{
                                ...actionStyle,
                                padding: '7px 10px',
                                fontSize: 12
                              }}
                              onClick={() => viewCustomer(customer.id)}
                            >
                              <Eye size={15} />
                              View
                            </button>
                          </td>

                        </tr>
                      ))}
                    </tbody>

                  </table>
                </div>
              )}

            </article>
          </section>
        )}

        {/* ACCOUNTS PAGE */}

        {page === 'Accounts' && (
          <section style={{ marginTop: 25 }}>

            <div style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              flexWrap: 'wrap',
              gap: 12,
              marginBottom: 22
            }}>
              <div>
                <span className="eyebrow">ACCOUNT OPERATIONS</span>

                <h2 style={{ margin: '7px 0' }}>
                  Account Management
                </h2>

                <p style={{ color: '#718078', margin: 0 }}>
                  Create and manage customer bank accounts.
                </p>
              </div>

              <button
                style={actionStyle}
                onClick={() => {
                  setShowAccountForm(!showAccountForm)
                  setError('')
                  setSuccess('')
                }}
              >
                {showAccountForm ? <X size={17} /> : <Plus size={17} />}

                {showAccountForm ? 'Close form' : 'Open New Account'}
              </button>
            </div>

            {/* ACCOUNT CREATION FORM */}

            {showAccountForm && (
              <article className="panel" style={{ marginBottom: 22 }}>

                <h3 style={{ marginTop: 0 }}>
                  New Account Application
                </h3>

                <form onSubmit={handleAccountSubmit}>

                  <div style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
                    gap: 16
                  }}>

                    <div>
                      <label style={labelStyle}>
                        Select Customer *
                      </label>

                      <select
                        style={inputStyle}
                        name="customerId"
                        value={accountForm.customerId}
                        onChange={updateAccountForm}
                        required
                      >
                        <option value="">Select customer</option>

                        {customers.map(customer => (
                          <option
                            key={customer.id}
                            value={customer.id}
                          >
                            {customer.firstName} {customer.lastName}
                            {' - '}
                            {customer.customerCode}
                          </option>
                        ))}
                      </select>
                    </div>

                    <div>
                      <label style={labelStyle}>Branch ID *</label>

                      <input
                        style={inputStyle}
                        type="number"
                        name="branchId"
                        value={accountForm.branchId}
                        onChange={updateAccountForm}
                        min="1"
                        required
                      />
                    </div>

                    <div>
                      <label style={labelStyle}>Account Type *</label>

                      <select
                        style={inputStyle}
                        name="accountType"
                        value={accountForm.accountType}
                        onChange={updateAccountForm}
                        required
                      >
                        <option value="SAVINGS">Savings Account</option>
                        <option value="CURRENT">Current Account</option>
                      </select>
                    </div>

                  </div>

                  <p style={{ fontSize: 12, color: '#718078' }}>
                    Account number is generated automatically.
                    New accounts start with PENDING status and zero balance.
                  </p>

                  <button
                    type="submit"
                    style={actionStyle}
                    disabled={saving || customers.length === 0}
                  >
                    <Plus size={17} />

                    {saving ? 'Creating...' : 'Create Account'}
                  </button>

                </form>
              </article>
            )}

            {/* ACCOUNT TABLE */}

            <article className="panel">

              <div style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                marginBottom: 18
              }}>
                <div>
                  <h3 style={{ margin: 0 }}>
                    Registered Accounts
                  </h3>

                  <small style={{ color: '#718078' }}>
                    {accounts.length} account(s) found
                  </small>
                </div>

                <button
                  style={actionStyle}
                  onClick={loadAccounts}
                >
                  <RefreshCw size={17} />
                  Refresh
                </button>
              </div>

              {loading ? (
                <p>Loading accounts...</p>
              ) : accounts.length === 0 ? (
                <p>No accounts found.</p>
              ) : (

                <div style={{ overflowX: 'auto' }}>

                  <table style={{
                    width: '100%',
                    borderCollapse: 'collapse',
                    textAlign: 'left',
                    fontSize: 13
                  }}>

                    <thead>
                      <tr style={{ background: '#f4f7f5' }}>
                        {[
                          'Account Number',
                          'Customer',
                          'Account Type',
                          'Branch ID',
                          'Balance',
                          'Status'
                        ].map(item => (
                          <th
                            key={item}
                            style={{
                              padding: 13,
                              whiteSpace: 'nowrap'
                            }}
                          >
                            {item}
                          </th>
                        ))}
                      </tr>
                    </thead>

                    <tbody>
                      {accounts.map(account => (
                        <tr
                          key={account.id}
                          style={{
                            borderBottom: '1px solid #edf0ee'
                          }}
                        >

                          <td style={{ padding: 13 }}>
                            <b>{account.accountNumber}</b>
                          </td>

                          <td style={{ padding: 13 }}>
                            {account.customerName}
                          </td>

                          <td style={{ padding: 13 }}>
                            {account.accountType}
                          </td>

                          <td style={{ padding: 13 }}>
                            {account.branchId}
                          </td>

                          <td style={{ padding: 13 }}>
                            {account.currency} {account.availableBalance}
                          </td>

                          <td style={{ padding: 13 }}>
                            <span style={{
                              background: '#fff4d8',
                              color: '#8a6410',
                              padding: '5px 9px',
                              borderRadius: 20,
                              fontSize: 11,
                              fontWeight: 700
                            }}>
                              {account.accountStatus}
                            </span>
                          </td>

                        </tr>
                      ))}
                    </tbody>

                  </table>
                </div>
              )}

            </article>
          </section>
        )}

        {/* TRANSACTIONS PAGE */}
        {page === 'Transactions' && (
          <section style={{ marginTop: 25 }}>
            <div style={{ marginBottom: 22 }}>
              <span className="eyebrow">PAYMENTS & LEDGER</span>
              <h2 style={{ margin: '7px 0' }}>Transaction Management</h2>
              <p style={{ color: '#718078', margin: 0 }}>Process deposits, withdrawals and transfers. Only ACTIVE accounts can transact.</p>
            </div>

            <section className="metrics" style={{ marginBottom: 22 }}>
              {[
                ['Total transactions', transactions.length, Activity],
                ['Deposits', transactions.filter(t => t.transactionType === 'DEPOSIT' && t.status === 'SUCCESS').length, ArrowDownLeft],
                ['Withdrawals', transactions.filter(t => t.transactionType === 'WITHDRAWAL' && t.status === 'SUCCESS').length, ArrowUpRight],
                ['Transfers', transactions.filter(t => t.transactionType === 'TRANSFER' && t.status === 'SUCCESS').length, ArrowLeftRight]
              ].map(([label, value, Icon]) => (
                <article className="metric" key={label}>
                  <div className="metric-top"><span>{label}</span><Icon size={19} /></div>
                  <strong>{value}</strong>
                  <small>From transaction records</small>
                </article>
              ))}
            </section>

            <article className="panel" style={{ marginBottom: 22 }}>
              <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap', marginBottom: 20 }}>
                {['DEPOSIT', 'WITHDRAWAL', 'TRANSFER'].map(mode => (
                  <button key={mode} type="button" onClick={() => { setTransactionMode(mode); setTransactionForm(initialTransactionForm); setError(''); setSuccess('') }} style={{ ...actionStyle, background: transactionMode === mode ? '#153e35' : '#eaf0ed', color: transactionMode === mode ? '#fff' : '#244238' }}>
                    {mode === 'DEPOSIT' ? <ArrowDownLeft size={16} /> : mode === 'WITHDRAWAL' ? <ArrowUpRight size={16} /> : <ArrowLeftRight size={16} />}
                    {mode}
                  </button>
                ))}
              </div>

              <h3 style={{ marginTop: 0 }}>{transactionMode === 'DEPOSIT' ? 'Cash Deposit' : transactionMode === 'WITHDRAWAL' ? 'Cash Withdrawal' : 'Fund Transfer'}</h3>
              <form onSubmit={handleTransactionSubmit}>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 16 }}>
                  {transactionMode !== 'TRANSFER' ? (
                    <div>
                      <label style={labelStyle}>Select account *</label>
                      <select style={inputStyle} name="accountId" value={transactionForm.accountId} onChange={updateTransactionForm} required>
                        <option value="">Choose ACTIVE account</option>
                        {accounts.filter(a => a.accountStatus === 'ACTIVE').map(a => <option key={a.id} value={a.id}>{a.accountNumber} — {a.customerName} — {a.currency} {a.availableBalance}</option>)}
                      </select>
                    </div>
                  ) : <>
                    <div>
                      <label style={labelStyle}>From account *</label>
                      <select style={inputStyle} name="fromAccountId" value={transactionForm.fromAccountId} onChange={updateTransactionForm} required>
                        <option value="">Select sender</option>
                        {accounts.filter(a => a.accountStatus === 'ACTIVE').map(a => <option key={a.id} value={a.id}>{a.accountNumber} — {a.customerName} — {a.currency} {a.availableBalance}</option>)}
                      </select>
                    </div>
                    <div>
                      <label style={labelStyle}>To account *</label>
                      <select style={inputStyle} name="toAccountId" value={transactionForm.toAccountId} onChange={updateTransactionForm} required>
                        <option value="">Select receiver</option>
                        {accounts.filter(a => a.accountStatus === 'ACTIVE').map(a => <option key={a.id} value={a.id}>{a.accountNumber} — {a.customerName}</option>)}
                      </select>
                    </div>
                  </>}
                  <div>
                    <label style={labelStyle}>Amount (INR) *</label>
                    <input style={inputStyle} type="number" name="amount" value={transactionForm.amount} onChange={updateTransactionForm} min="0.01" step="0.01" max="99999999999999999" placeholder="Enter amount" required />
                  </div>
                  <div>
                    <label style={labelStyle}>Description</label>
                    <input style={inputStyle} name="description" value={transactionForm.description} onChange={updateTransactionForm} maxLength="255" placeholder="Transaction description" />
                  </div>
                </div>
                <p style={{ color: '#718078', fontSize: 12 }}>Transactions are recorded in the demo ledger. Ensure the account is ACTIVE and has sufficient balance for withdrawals/transfers.</p>
                <button type="submit" style={actionStyle} disabled={saving || accounts.filter(a => a.accountStatus === 'ACTIVE').length === 0}>
                  <Banknote size={17} /> {saving ? 'Processing...' : `Process ${transactionMode}`}
                </button>
              </form>
            </article>

            <article className="panel">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12, marginBottom: 18 }}>
                <div><h3 style={{ margin: 0 }}>Transaction History</h3><small style={{ color: '#718078' }}>{transactions.length} transaction(s) recorded</small></div>
                <button style={actionStyle} onClick={refreshBankingData}><RefreshCw size={16} /> Refresh</button>
              </div>
              {transactions.length === 0 ? <p>No transactions recorded yet.</p> : (
                <div style={{ overflowX: 'auto' }}>
                  <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: 13 }}>
                    <thead><tr style={{ background: '#f4f7f5' }}>{['Reference Number', 'Type', 'Amount', 'Status', 'Description', 'Date & Time'].map(h => <th key={h} style={{ padding: 13, whiteSpace: 'nowrap' }}>{h}</th>)}</tr></thead>
                    <tbody>{[...transactions].sort((a,b) => new Date(b.createdAt) - new Date(a.createdAt)).map(t => (
                      <tr key={t.id} style={{ borderBottom: '1px solid #edf0ee' }}>
                        <td style={{ padding: 13, fontWeight: 600, whiteSpace: 'nowrap' }}>{t.referenceNumber}</td>
                        <td style={{ padding: 13 }}>{t.transactionType}</td>
                        <td style={{ padding: 13, whiteSpace: 'nowrap' }}>₹{Number(t.amount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</td>
                        <td style={{ padding: 13 }}><span style={{ background: t.status === 'SUCCESS' ? '#e5f5e9' : '#fff4d8', color: t.status === 'SUCCESS' ? '#18713a' : '#8a6410', padding: '5px 9px', borderRadius: 20, fontSize: 11, fontWeight: 700 }}>{t.status}</span></td>
                        <td style={{ padding: 13 }}>{t.description || '—'}</td>
                        <td style={{ padding: 13, whiteSpace: 'nowrap' }}>{t.createdAt ? new Date(t.createdAt).toLocaleString() : '—'}</td>
                      </tr>
                    ))}</tbody>
                  </table>
                </div>
              )}
            </article>
          </section>
        )}

        {/* OTHER MODULES */}
        {page !== 'Overview' && page !== 'Customers' && page !== 'Accounts' && page !== 'Transactions' && (
          <section className="panel" style={{ marginTop: 25 }}>
            <h2>{page}</h2>
            <p>This module is planned for the next development phase.</p>
          </section>
        )}

        {/* CUSTOMER DETAILS POPUP */}

        {selectedCustomer && (
          <div
            style={overlayStyle}
            onClick={() => setSelectedCustomer(null)}
          >
            <div
              style={modalStyle}
              onClick={event => event.stopPropagation()}
            >

              <div style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                marginBottom: 20
              }}>
                <div>
                  <span className="eyebrow">CUSTOMER PROFILE</span>

                  <h2 style={{ margin: '6px 0 0' }}>
                    Customer Details
                  </h2>
                </div>

                <button
                  onClick={() => setSelectedCustomer(null)}
                  style={{
                    border: 0,
                    background: '#edf2ef',
                    borderRadius: 8,
                    padding: 9,
                    cursor: 'pointer'
                  }}
                >
                  <X size={20} />
                </button>
              </div>

              <div style={{
                display: 'flex',
                alignItems: 'center',
                gap: 14,
                padding: 16,
                background: '#f0f5f2',
                borderRadius: 10,
                marginBottom: 20
              }}>
                <div style={{
                  width: 52,
                  height: 52,
                  borderRadius: '50%',
                  background: '#153e35',
                  color: 'white',
                  display: 'grid',
                  placeItems: 'center'
                }}>
                  <User size={25} />
                </div>

                <div>
                  <h3 style={{ margin: 0 }}>
                    {selectedCustomer.firstName}
                    {' '}
                    {selectedCustomer.lastName}
                  </h3>

                  <small style={{ color: '#718078' }}>
                    {selectedCustomer.customerCode}
                  </small>
                </div>

                <span style={{
                  marginLeft: 'auto',
                  padding: '6px 10px',
                  borderRadius: 20,
                  background:
                    selectedCustomer.kycStatus === 'VERIFIED'
                      ? '#e5f5e9'
                      : '#fff4d8',
                  color:
                    selectedCustomer.kycStatus === 'VERIFIED'
                      ? '#18713a'
                      : '#8a6410',
                  fontSize: 11,
                  fontWeight: 700
                }}>
                  {selectedCustomer.kycStatus}
                </span>
              </div>

              <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(190px, 1fr))',
                gap: 12
              }}>
                <Detail
                  icon={User}
                  label="Customer ID"
                  value={selectedCustomer.id}
                />

                <Detail
                  icon={Mail}
                  label="Email Address"
                  value={selectedCustomer.email}
                />

                <Detail
                  icon={Phone}
                  label="Phone Number"
                  value={selectedCustomer.phone}
                />

                <Detail
                  icon={Calendar}
                  label="Date of Birth"
                  value={selectedCustomer.dateOfBirth}
                />

                <Detail
                  icon={MapPin}
                  label="Address"
                  value={selectedCustomer.address}
                />

                <Detail
                  icon={Calendar}
                  label="Registration Date"
                  value={
                    selectedCustomer.createdAt
                      ? new Date(
                          selectedCustomer.createdAt
                        ).toLocaleString()
                      : 'Not available'
                  }
                />
              </div>

              <div style={{
                textAlign: 'right',
                marginTop: 22
              }}>
                <button
                  style={actionStyle}
                  onClick={() => setSelectedCustomer(null)}
                >
                  Close
                </button>
              </div>

            </div>
          </div>
        )}

        <footer>
          © 2026 Horizon Bank · Educational demonstration project
          · No real financial transactions
        </footer>

      </main>
    </div>
  )
}

// -----------------------------
// POPUP STYLES
// -----------------------------

const overlayStyle = {
  position: 'fixed',
  inset: 0,
  background: 'rgba(10, 25, 20, 0.58)',
  display: 'flex',
  justifyContent: 'center',
  alignItems: 'center',
  padding: 18,
  zIndex: 1000
}

const modalStyle = {
  background: '#fff',
  width: '100%',
  maxWidth: 650,
  maxHeight: '90vh',
  overflowY: 'auto',
  padding: 25,
  borderRadius: 14,
  boxShadow: '0 20px 60px rgba(0,0,0,0.2)'
}