import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getDashboard } from "../api/dashboardApi";
import { getInterviewHistory } from "../api/interviewApi";
import { useAuth } from "../context/AuthContext.jsx";
import ScoreCard from "../components/ScoreCard.jsx";
import Icon from "../components/Icon.jsx";

export default function Dashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [dashboard, setDashboard] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let mounted = true;
    Promise.allSettled([getDashboard(), getInterviewHistory()])
      .then(([dashboardResult, historyResult]) => {
        if (!mounted) return;
        if (dashboardResult.status === "fulfilled") setDashboard(dashboardResult.value.data);
        if (historyResult.status === "fulfilled") setHistory(historyResult.value.data || []);
      })
      .finally(() => mounted && setLoading(false));
    return () => {
      mounted = false;
    };
  }, []);

  const averageScore = Number(dashboard?.averageScore || 0).toFixed(0);
  const atsScore = dashboard?.latestAtsScore ?? 0;

  return (
    <div className="page-stack">
      <div className="page-heading split">
        <div>
          <h1>Welcome back, {user?.name || "Candidate"}.</h1>
          <p>Your readiness workspace is ready. Upload, practice, and review your progress.</p>
        </div>
        <button className="primary-gradient" onClick={() => navigate("/interview/start")}>
          <Icon name="play_arrow" />
          Start New Mock Interview
        </button>
      </div>

      <div className="metric-grid">
        <ScoreCard
          label="Total Interviews"
          value={loading ? "..." : dashboard?.totalInterviews ?? history.length}
          icon="record_voice_over"
          detail="Practice sessions completed"
        />
        <section className="metric-card ring-card">
          <div className="metric-header">
            <span>Average Score</span>
            <span className="metric-icon primary">
              <Icon name="analytics" />
            </span>
          </div>
          <div className="ring-row">
            <div className="progress-ring" style={{ "--progress": averageScore }}>
              <span>{averageScore}%</span>
            </div>
            <div>
              <strong>{averageScore >= 80 ? "Solid" : averageScore >= 60 ? "Improving" : "Getting Started"}</strong>
              <p>Interview performance trend</p>
            </div>
          </div>
        </section>
        <ScoreCard
          label="Latest ATS Score"
          value={loading ? "..." : atsScore}
          suffix="/ 100"
          icon="grading"
          tone={atsScore >= 80 ? "success" : "warning"}
          detail={atsScore >= 80 ? "Strong resume fit" : "Needs resume polish"}
        />
      </div>

      <div className="dashboard-grid">
        <section className="panel span-8">
          <div className="panel-header">
            <h2>Recent Activity</h2>
            <button className="link-button" onClick={() => navigate("/history")}>
              View All
            </button>
          </div>
          <div className="activity-list">
            {history.slice(0, 4).length ? (
              history.slice(0, 4).map((item) => (
                <div className="activity-item" key={item.id}>
                  <div className="activity-left">
                    <span className="activity-icon">
                      <Icon name="business" />
                    </span>
                    <div>
                      <strong>{item.companyName || "Mock Interview"}</strong>
                      <span>{item.interviewType || "Technical"} - {item.difficultyLevel || "Medium"}</span>
                    </div>
                  </div>
                  <div className="activity-score">
                    <strong>{item.score ?? "In progress"}</strong>
                    <span>{item.status}</span>
                  </div>
                </div>
              ))
            ) : (
              <div className="empty-state">
                <Icon name="keyboard_voice" />
                <p>No interview history yet. Start a mock interview after uploading your resume.</p>
              </div>
            )}
          </div>
        </section>

        <section className="panel span-4 resume-glimpse">
          <div className="panel-header">
            <h2>Resume Quick View</h2>
            <button className="icon-button" onClick={() => navigate("/resume")} aria-label="Edit resume">
              <Icon name="edit" />
            </button>
          </div>
          <div className="file-tile">
            <Icon name="picture_as_pdf" />
            <div>
              <strong>Latest resume</strong>
              <span>ATS Score: {atsScore || "Not uploaded"}</span>
            </div>
          </div>
          <div className="ai-note">
            <div>
              <Icon name="auto_awesome" />
              <strong>AI Insight</strong>
            </div>
            <p>{dashboard?.latestResumeFeedback || "Upload a PDF resume to unlock tailored AI feedback."}</p>
          </div>
        </section>
      </div>
    </div>
  );
}
