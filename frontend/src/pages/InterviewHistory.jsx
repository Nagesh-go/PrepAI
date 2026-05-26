import { useEffect, useState } from "react";
import { getInterviewHistory } from "../api/interviewApi";
import Icon from "../components/Icon.jsx";

export default function InterviewHistory() {
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getInterviewHistory()
      .then(({ data }) => setHistory(data || []))
      .catch(() => setHistory([]))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="page-stack">
      <div className="page-heading">
        <h1>Performance</h1>
        <p>Review mock interview history, scores, and readiness trends.</p>
      </div>

      <section className="panel">
        <div className="panel-header">
          <h2>Interview History</h2>
          <span className="pill">{history.length} sessions</span>
        </div>
        {loading ? (
          <div className="empty-state">Loading history...</div>
        ) : history.length ? (
          <div className="table-list">
            {history.map((item) => (
              <div className="history-row" key={item.id}>
                <div className="activity-left">
                  <span className="activity-icon">
                    <Icon name="keyboard_voice" />
                  </span>
                  <div>
                    <strong>{item.companyName || "Mock Interview"}</strong>
                    <span>{item.interviewType} - {item.difficultyLevel}</span>
                  </div>
                </div>
                <div className="history-meta">
                  <span>{item.status}</span>
                  <strong>{item.score ?? "Pending"}</strong>
                  <small>{item.createdAt ? new Date(item.createdAt).toLocaleString() : ""}</small>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="empty-state">
            <Icon name="insights" />
            <p>No sessions yet. Start your first mock interview to build performance history.</p>
          </div>
        )}
      </section>
    </div>
  );
}
