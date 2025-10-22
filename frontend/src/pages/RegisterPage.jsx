import { useState } from "react";
import "../App.css";
import { validateForm, sanitizeInput } from "../utils/inputValidation";

export default function RegisterPage() {
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [validationErrors, setValidationErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [serverError, setServerError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setValidationErrors({});
    setServerError(null);

    // Validate form data
    const formData = { username, email, password, confirmPassword };
    const validation = validateForm(formData, 'register');
    if (!validation.isValid) {
      setValidationErrors(validation.errors);
      setLoading(false);
      return;
    }

    if (password !== confirmPassword) {
      setValidationErrors({ confirmPassword: ['Passwords do not match!'] });
      setLoading(false);
      return;
    }

    try {
      const res = await fetch(`${import.meta.env.VITE_API_BASE_URL}/auth/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ 
          username: sanitizeInput(username), 
          email: sanitizeInput(email), 
          password: sanitizeInput(password) 
        }),
      });

      if (res.ok) {
        alert("Registration successful! Please log in.");
        window.location.href = "/login";
      } else {
        // Try to get the error message from the response
        try {
          const errorData = await res.json();
          setServerError(errorData.error || errorData.message || 'Unknown error');
        } catch {
          // If response is not JSON, show generic message
          setServerError(`Registration failed: ${res.status} ${res.statusText}`);
        }
      }
    } catch (err) {
      console.error("Register error:", err);
      setServerError("Something went wrong. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-wrapper">
      <div className="login-card">
        <h1>Register</h1>
        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <label htmlFor="username">Username:</label>
            <input
              type="text"
              id="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Enter your username"
              required
            />
          </div>

          <div className="form-row">
            <label htmlFor="email">Email:</label>
            <input
              type="email"
              id="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Enter your email"
              required
            />
          </div>

          <div className="form-row">
            <label htmlFor="password">Password:</label>
            <input
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter your password"
              required
            />
          </div>

          <div className="form-row">
            <label htmlFor="confirmPassword">Confirm:</label>
            <input
              type="password"
              id="confirmPassword"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="Re-enter password"
              required
            />
          </div>

          <div className="form-actions">
            <button type="submit" disabled={loading}>
              {loading ? 'Registering...' : 'Register'}
            </button>
            <p className="register-link">
              Already have an account? <a href="/login">Login</a>
            </p>
          </div>
        </form>

        {/* Display validation errors */}
        {Object.keys(validationErrors).length > 0 && (
          <div className="validation-errors" style={{ 
            marginTop: '1rem', 
            padding: '1rem', 
            backgroundColor: '#ffebee', 
            border: '1px solid #f44336', 
            borderRadius: '4px' 
          }}>
            <h4 style={{ color: '#d32f2f', margin: '0 0 0.5rem 0' }}>Please fix the following errors:</h4>
            {Object.entries(validationErrors).map(([field, errors]) => (
              <div key={field} style={{ marginBottom: '0.5rem' }}>
                <strong style={{ textTransform: 'capitalize', color: '#d32f2f' }}>
                  {field.replace(/([A-Z])/g, ' $1').trim()}:
                </strong>
                <ul style={{ margin: '0.25rem 0 0 1rem', padding: 0 }}>
                  {errors.map((error, index) => (
                    <li key={index} style={{ color: '#d32f2f', fontSize: '0.9rem' }}>
                      {error}
                    </li>
                  ))}
                </ul>
              </div>
            ))}
          </div>
        )}

        {/* Display server errors */}
        {serverError && (
          <div className="server-error" style={{ 
            marginTop: '1rem', 
            padding: '1rem', 
            backgroundColor: '#ffebee', 
            border: '1px solid #f44336', 
            borderRadius: '4px' 
          }}>
            <h4 style={{ color: '#d32f2f', margin: '0 0 0.5rem 0' }}>Registration Error:</h4>
            <p style={{ color: '#d32f2f', margin: 0, fontSize: '0.9rem' }}>
              {serverError}
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
