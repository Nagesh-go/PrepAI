import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { startInterview } from "../api/interviewApi";
import Alert from "../components/Alert.jsx";
import Icon from "../components/Icon.jsx";
import LoadingButton from "../components/LoadingButton.jsx";

const difficultyOptions = [
  { value: "EASY", title: "Foundation", icon: "sentiment_satisfied", copy: "Core competency and confidence-building questions." },
  { value: "MEDIUM", title: "Advanced", icon: "trending_up", copy: "Scenario-based questions requiring structured problem solving." },
  { value: "HARD", title: "Expert", icon: "local_fire_department", copy: "High-pressure questions for FAANG-style practice." },
];

export default function InterviewStart() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    companyName: "",
    interviewType: "TECHNICAL",
    difficultyLevel: "MEDIUM",
    questionCount: 5,
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const submit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError("");
    try {
      const { data } = await startInterview(form);
      navigate(`/interview/${data.interviewId}`, { state: { session: data } });
    } catch (err) {
      setError(err.response?.data?.message || "Could not start interview. Upload a resume first.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="centered-page">
      <div className="page-heading centered">
        <h1>Session Configuration</h1>
        <p>Tailor your AI mock interview parameters to your upcoming role.</p>
      </div>

      <Alert type="error">{error}</Alert>

      <form className="config-card" onSubmit={submit}>
        <div className="accent-bar" />
        <div className="form-grid two">
          <label>
            Target Company
            <span className="input-icon">
              <Icon name="apartment" />
              <input
                value={form.companyName}
                onChange={(event) => setForm({ ...form, companyName: event.target.value })}
                placeholder="e.g., Google, Amazon, Microsoft"
                required
              />
            </span>
          </label>

          <div className="field-group">
            <span className="field-label">Interview Type</span>
            <div className="segmented-control">
              {["TECHNICAL", "BEHAVIORAL", "HR"].map((type) => (
                <button
                  key={type}
                  type="button"
                  className={form.interviewType === type ? "selected" : ""}
                  onClick={() => setForm({ ...form, interviewType: type })}
                >
                  {type === "HR" ? "HR / Fit" : type.charAt(0) + type.slice(1).toLowerCase()}
                </button>
              ))}
            </div>
          </div>
        </div>

        <div className="field-group">
          <span className="field-label">Difficulty Level</span>
          <div className="difficulty-grid">
            {difficultyOptions.map((option) => (
              <button
                key={option.value}
                type="button"
                className={`difficulty-card ${form.difficultyLevel === option.value ? "selected" : ""}`}
                onClick={() => setForm({ ...form, difficultyLevel: option.value })}
              >
                <div>
                  <Icon name={option.icon} />
                  {form.difficultyLevel === option.value ? <Icon name="check_circle" /> : <span className="radio-dot" />}
                </div>
                <strong>{option.title}</strong>
                <p>{option.copy}</p>
              </button>
            ))}
          </div>
        </div>

        <div className="field-group">
          <div className="range-label">
            <span className="field-label">Number of Questions</span>
            <strong>{form.questionCount}</strong>
          </div>
          <input
            className="range"
            type="range"
            min="1"
            max="10"
            value={form.questionCount}
            onChange={(event) => setForm({ ...form, questionCount: Number(event.target.value) })}
          />
          <div className="range-caption">
            <span>1 Quick</span>
            <span>10 Full Loop</span>
          </div>
        </div>

        <div className="context-banner">
          <Icon name="psychology" />
          <div>
            <strong>Contextual Generation Active</strong>
            <p>Questions will be generated from skills and experience in your latest uploaded resume.</p>
          </div>
        </div>

        <div className="form-actions">
          <LoadingButton type="submit" loading={loading} className="primary-gradient">
            <Icon name="play_arrow" />
            Begin Interview
          </LoadingButton>
        </div>
      </form>
    </div>
  );
}
