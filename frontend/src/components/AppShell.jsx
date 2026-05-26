import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import Icon from "./Icon.jsx";

const navItems = [
  { to: "/dashboard", label: "Dashboard", icon: "dashboard" },
  { to: "/resume", label: "Resume Analysis", icon: "description" },
  { to: "/interview/start", label: "Mock Interviews", icon: "keyboard_voice" },
  { to: "/history", label: "Performance", icon: "insights" },
  { to: "/settings", label: "Settings", icon: "settings" },
];

export default function AppShell() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <div className="app-shell">
      <aside className="side-nav">
        <div className="brand">
          <span className="brand-mark">
            <Icon name="bolt" filled />
          </span>
          <span>PrepAI</span>
        </div>

        <nav className="nav-list" aria-label="Primary navigation">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => `nav-item ${isActive ? "active" : ""}`}
            >
              <Icon name={item.icon} />
              <span>{item.label}</span>
            </NavLink>
          ))}
        </nav>

        <div className="side-profile">
          <button className="primary-gradient full-width" onClick={() => navigate("/interview/start")}>
            <Icon name="smart_toy" />
            Start Mock Interview
          </button>
          <div className="profile-row">
            <div className="avatar">{user?.name?.[0]?.toUpperCase() || "U"}</div>
            <div className="profile-copy">
              <strong>{user?.name || "Candidate"}</strong>
              <span>Interview Ready: 85%</span>
            </div>
          </div>
          <button className="ghost-button full-width" onClick={handleLogout}>
            <Icon name="logout" />
            Logout
          </button>
        </div>
      </aside>

      <div className="content-frame">
        <header className="top-bar">
          <div className="top-context">AI interview readiness workspace</div>
          <div className="top-actions">
            <label className="search-box">
              <Icon name="search" />
              <input placeholder="Search..." />
            </label>
            <button className="icon-button" aria-label="Notifications">
              <Icon name="notifications" />
            </button>
            <button className="icon-button" aria-label="Help">
              <Icon name="help_outline" />
            </button>
            <div className="avatar small">{user?.name?.[0]?.toUpperCase() || "U"}</div>
          </div>
        </header>
        <main className="page-canvas">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
