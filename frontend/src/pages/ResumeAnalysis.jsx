import { useEffect, useMemo, useRef, useState } from "react";
import { getLatestResume, uploadResume } from "../api/resumeApi";
import Alert from "../components/Alert.jsx";
import Icon from "../components/Icon.jsx";
import LoadingButton from "../components/LoadingButton.jsx";
import SkillChip from "../components/SkillChip.jsx";

const splitFeedback = (feedback = "") => {
  const lines = feedback.split("\n").map((line) => line.trim()).filter(Boolean);
  const strengths = [];
  const improvements = [];
  let bucket = null;

  lines.forEach((line) => {
    const lower = line.toLowerCase();
    if (lower.includes("strength")) {
      bucket = strengths;
      return;
    }
    if (lower.includes("improve") || lower.includes("area")) {
      bucket = improvements;
      return;
    }
    if (line.startsWith("-") && bucket) bucket.push(line.replace(/^-\s*/, ""));
  });

  return { strengths, improvements };
};

export default function ResumeAnalysis() {
  const inputRef = useRef(null);
  const [resume, setResume] = useState(null);
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    getLatestResume()
      .then(({ data }) => setResume(data))
      .catch(() => {})
      .finally(() => setInitialLoading(false));
  }, []);

  const feedback = useMemo(() => splitFeedback(resume?.aiFeedback), [resume?.aiFeedback]);
  const score = resume?.atsScore || 0;

  const submit = async () => {
    if (!file) {
      setError("Choose a PDF resume first.");
      return;
    }
    setLoading(true);
    setError("");
    try {
      const { data } = await uploadResume(file);
      setResume(data);
      setFile(null);
      if (inputRef.current) inputRef.current.value = "";
    } catch (err) {
      setError(err.response?.data?.message || "Could not upload resume. Make sure it is a PDF file.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-stack">
      <div className="page-heading split">
        <div>
          <h1>Resume Analysis</h1>
          <p>Upload your latest resume to receive AI-driven insights, ATS scoring, and targeted improvement suggestions.</p>
        </div>
        <div className="pill">
          <Icon name="school" />
          Experience Level: <strong>{resume?.experienceLevel || "Pending"}</strong>
        </div>
      </div>

      <Alert type="error">{error}</Alert>

      <div className="resume-layout">
        <section className="upload-column">
          <div className="upload-zone" onClick={() => inputRef.current?.click()}>
            <div className="upload-icon">
              <Icon name="cloud_upload" />
            </div>
            <h2>{file ? file.name : "Drag & Drop Resume"}</h2>
            <p>Supports PDF up to 10MB</p>
            <input
              ref={inputRef}
              type="file"
              accept="application/pdf"
              hidden
              onChange={(event) => setFile(event.target.files?.[0] || null)}
            />
            <button className="secondary-button" type="button">
              Browse Files
            </button>
          </div>
          <LoadingButton type="button" loading={loading} className="primary-gradient" onClick={submit}>
            Analyze Resume
          </LoadingButton>

          <section className="panel">
            <div className="panel-title-row">
              <Icon name="psychology" />
              <h2>Extracted Skills Map</h2>
            </div>
            <div className="chip-list">
              {(resume?.extractedSkills || []).length ? (
                resume.extractedSkills.map((skill) => <SkillChip key={skill}>{skill}</SkillChip>)
              ) : (
                <>
                  <SkillChip muted>Upload resume</SkillChip>
                  <SkillChip muted>AI extracts skills here</SkillChip>
                </>
              )}
            </div>
          </section>
        </section>

        <aside className="analysis-column">
          <section className="ats-card">
            <div className="score-line" />
            <div className="ats-gauge" style={{ "--score": score }}>
              <svg viewBox="0 0 100 100">
                <circle cx="50" cy="50" r="44" />
                <circle cx="50" cy="50" r="44" />
              </svg>
              <div>
                <strong>{initialLoading ? "..." : score}</strong>
                <span>/100</span>
              </div>
            </div>
            <h2>ATS Compatibility</h2>
            <p>{score >= 80 ? "Strong Fit" : score ? "Needs Improvement" : "Awaiting Resume"}</p>
          </section>

          <section className="panel feedback-panel">
            <div className="panel-title-row">
              <Icon name="auto_awesome" />
              <h2>AI Feedback</h2>
            </div>
            <FeedbackList title="Strengths" icon="check_circle" tone="success" items={feedback.strengths} />
            <FeedbackList title="Areas for Improvement" icon="info" tone="primary" items={feedback.improvements} />
            {!resume?.aiFeedback ? <p className="muted-copy">AI feedback appears after resume analysis.</p> : null}
          </section>
        </aside>
      </div>
    </div>
  );
}

function FeedbackList({ title, icon, tone, items }) {
  if (!items.length) return null;
  return (
    <div className="feedback-block">
      <h3 className={tone}>
        <Icon name={icon} />
        {title}
      </h3>
      <ul>
        {items.map((item) => (
          <li key={item}>{item}</li>
        ))}
      </ul>
    </div>
  );
}
