import { Link, useLocation, useNavigate } from "react-router-dom";
import { useState } from "react";
import { useAuth } from "../context/AuthContext.jsx";
import Alert from "../components/Alert.jsx";
import LoadingButton from "../components/LoadingButton.jsx";
import Icon from "../components/Icon.jsx";

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ email: "", password: "" });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const submit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError("");
    try {
      await login(form);
      navigate(location.state?.from?.pathname || "/dashboard", { replace: true });
    } catch (err) {
      const apiMessage = err.response?.data?.message;
      const networkMessage =
        !err.response && err.message ? "Cannot reach server. Is the backend running on port 9090?" : null;
      setError(apiMessage || networkMessage || "Login failed. Check your email and password.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="auth-page">
      <section className="auth-card">
        <div className="brand auth-brand">
          <span className="brand-mark">
            <Icon name="bolt" filled />
          </span>
          <span>PrepAI</span>
        </div>
        <h1>Welcome back</h1>
        <p>Sign in to continue your AI-powered interview preparation.</p>
        <Alert type="error">{error}</Alert>
        <form className="form-stack" onSubmit={submit}>
          <label>
            Email
            <input
              type="email"
              value={form.email}
              onChange={(event) => setForm({ ...form, email: event.target.value })}
              placeholder="you@example.com"
              required
            />
          </label>
          <label>
            Password
            <input
              type="password"
              value={form.password}
              onChange={(event) => setForm({ ...form, password: event.target.value })}
              placeholder="Enter your password"
              required
            />
          </label>
          <LoadingButton type="submit" loading={loading} className="primary-gradient full-width">
            Login
          </LoadingButton>
        </form>
        <p className="auth-switch">
          New to PrepAI? <Link to="/register">Create an account</Link>
        </p>
      </section>
    </main>
  );
}
