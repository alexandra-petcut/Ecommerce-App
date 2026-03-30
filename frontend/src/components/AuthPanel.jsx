import { useState } from 'react'
import { loginUser, registerUser } from '../services/api'

function AuthPanel({ onAuthSuccess }) {
  const [mode, setMode] = useState('login') // 'login' or 'register'
  const [form, setForm] = useState({ name: '', email: '', password: '' })
  const [showPassword, setShowPassword] = useState(false)
  const [message, setMessage] = useState({ text: '', type: '' }) // type: 'success' | 'error'
  const [loading, setLoading] = useState(false)

  function handleChange(event) {
    const { name, value } = event.target
    setForm((current) => ({ ...current, [name]: value })) // update only the changed field
  }

  async function handleSubmit(event) {
    event.preventDefault() // prevent page refresh on form submit
    setLoading(true)
    setMessage({ text: '', type: '' }) // clear old messages

    try {
      const payload =
        mode === 'register'
          ? form // if registering, send name + email + password
          : { email: form.email, password: form.password } // if logging in, only email + password

      const response =
        mode === 'register'
          ? await registerUser(payload) // send request to backend
          : await loginUser(payload)

      onAuthSuccess(response) // tell App.jsx the login/register was successful
      setForm({ name: '', email: '', password: '' }) // clear the form
      setMessage({ text: response.message || 'Success!', type: 'success' })
    } catch (error) {
      setMessage({ text: error.message, type: 'error' })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-header">
          <h2>{mode === 'login' ? 'Welcome back' : 'Create account'}</h2>
          <p>
            {mode === 'login'
              ? 'Log in to your account to continue'
              : 'Sign up to start shopping'}
          </p>
        </div>

        {/* Tab toggle between Login and Register */}
        <div className="auth-tabs">
          <button
            className={`auth-tab ${mode === 'login' ? 'active' : ''}`}
            onClick={() => setMode('login')}
          >
            Log in
          </button>
          <button
            className={`auth-tab ${mode === 'register' ? 'active' : ''}`}
            onClick={() => setMode('register')}
          >
            Register
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          {/* Full name field — only shown when registering */}
          {mode === 'register' && (
            <div className="form-group">
              <label className="form-label" htmlFor="name">
                Full name
              </label>
              <input
                className="form-input"
                id="name"
                name="name"
                placeholder="John Doe"
                value={form.name}
                onChange={handleChange}
                autoComplete="name"
                required
              />
            </div>
          )}

          <div className="form-group">
            <label className="form-label" htmlFor="email">
              Email
            </label>
            <input
              className="form-input"
              id="email"
              name="email"
              type="email"
              placeholder="you@example.com"
              value={form.email}
              onChange={handleChange}
              autoComplete="email"
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="password">
              Password
            </label>
            <div className="password-wrapper">
              <input
                className="form-input"
                id="password"
                name="password"
                type={showPassword ? 'text' : 'password'}
                placeholder="Enter your password"
                value={form.password}
                onChange={handleChange}
                autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
                required
              />
              <button
                type="button"
                className="password-toggle"
                onClick={() => setShowPassword(!showPassword)}
                aria-label={showPassword ? 'Hide password' : 'Show password'}
              >
                {showPassword ? 'Hide' : 'Show'}
              </button>
            </div>
          </div>

          <button
            type="submit"
            className="btn-primary auth-submit"
            disabled={loading}
          >
            {loading
              ? 'Please wait...'
              : mode === 'login'
                ? 'Log in'
                : 'Create account'}
          </button>
        </form>

        {/* Show success or error message after submit */}
        {message.text && (
          <p className={`auth-message ${message.type}`}>{message.text}</p>
        )}
      </div>
    </div>
  )
}

export default AuthPanel
