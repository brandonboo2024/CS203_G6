import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { validateForm, sanitizeInput } from "../utils/inputValidation";

export default function Login() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [validationErrors, setValidationErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setValidationErrors({});

    // Validate form data
    const formData = { username, password };
    const validation = validateForm(formData, 'login');
    if (!validation.isValid) {
      setValidationErrors(validation.errors);
      setLoading(false);
      return;
    }

    try {
      const res = await fetch(`${import.meta.env.VITE_API_BASE_URL}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          username: sanitizeInput(username),
          password: sanitizeInput(password)
        }),
      });

      if (!res.ok) {
        // Try to get the error message from the response
        try {
          const errorData = await res.json();
          throw new Error(errorData.error || errorData.message || "Login failed");
        } catch (parseError) {
          throw new Error("Login failed");
        }
      }

      const data = await res.json();

      // Store all useful fields locally
      localStorage.setItem("token", data.token);        // <-- the JWT
      localStorage.setItem("username", data.username);  // optional
      localStorage.setItem("email", data.email);        // optional
      if (data.role) localStorage.setItem("role", data.role); // if backend returns it

      // Redirect based on user role (optional)
      if (data.role === "ADMIN") {
        navigate("/admin/tariffs");
      } else {
        navigate("/dashboard");
      }

    } catch (err) {
      alert("Invalid username or password"); // Show error message
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-wrapper">
      <div className="login-card">
        <h1>Login</h1>
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

          <div className="form-actions">
            <button type="submit" disabled={loading}>
              {loading ? 'Logging in...' : 'Login'}
            </button>
            <p className="register-link">
              Don't have an account? <a href="/register">Register</a>
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
                  {field}:
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
      </div>
    </div>
  );
}
