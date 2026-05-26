import { Link, useNavigate } from "react-router-dom";
import { useState } from "react";
import { useAuth } from "../context/AuthContext.jsx";
import Alert from "../components/Alert.jsx";
import LoadingButton from "../components/LoadingButton.jsx";
import Icon from "../components/Icon.jsx";

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: "", email: "", password: "" });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const submit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError("");
    try {
      await register(form);
      navigate("/dashboard", { replace: true });
    } catch (err) {
      const fields = err.response?.data?.fields;
      const networkMessage =
        !err.response && err.message ? "Cannot reach server. Is the backend running on port 9090?" : null;
      setError(
        fields
          ? Object.values(fields).join(" ")
          : err.response?.data?.message || networkMessage || "Registration failed."
      );
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
        <h1>Create your account</h1>
        <p>Upload your resume, practice interviews, and track readiness.</p>
        <Alert type="error">{error}</Alert>
        <form className="form-stack" onSubmit={submit}>
          <label>
            Name
            <input
              value={form.name}
              onChange={(event) => setForm({ ...form, name: event.target.value })}
              placeholder="Your name"
              required
            />
          </label>
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
              minLength={6}
              value={form.password}
              onChange={(event) => setForm({ ...form, password: event.target.value })}
              placeholder="At least 6 characters"
              required
            />
          </label>
          <LoadingButton type="submit" loading={loading} className="primary-gradient full-width">
            Register
          </LoadingButton>
        </form>
        <p className="auth-switch">
          Already have an account? <Link to="/login">Login</Link>
        </p>
      </section>
    </main>
  );
}
