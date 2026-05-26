import { useLocation, useNavigate, useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import { getInterview, submitAnswer } from "../api/interviewApi";
import Alert from "../components/Alert.jsx";
import Icon from "../components/Icon.jsx";
import LoadingButton from "../components/LoadingButton.jsx";

export default function MockInterview() {
  const { interviewId } = useParams();
  const location = useLocation();
  const navigate = useNavigate();
  const [session, setSession] = useState(location.state?.session || null);
  const [answer, setAnswer] = useState("");
  const [previousFeedback, setPreviousFeedback] = useState(location.state?.session?.previousAnswerFeedback || "");
  const [previousScore, setPreviousScore] = useState(location.state?.session?.previousAnswerScore || null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (session) return;
    getInterview(interviewId)
      .then(({ data }) => {
        const unanswered = data.questions?.find((question) => !question.answer);
        setSession({
          interviewId: data.id,
          currentQuestion: unanswered
            ? {
                id: unanswered.id,
                questionText: unanswered.questionText,
                questionType: unanswered.questionType,
                difficulty: unanswered.difficulty,
                topic: unanswered.topic,
                orderNumber: unanswered.orderNumber,
              }
            : null,
          totalQuestions: data.totalQuestions,
          answeredQuestions: data.answeredQuestions,
          status: data.status,
          finalScore: data.score,
        });
      })
      .catch(() => setError("Could not load interview session."));
  }, [interviewId, session]);

  const submit = async () => {
    if (!answer.trim()) {
      setError("Type your answer before submitting.");
      return;
    }
    setLoading(true);
    setError("");
    try {
      const { data } = await submitAnswer(interviewId, {
        questionId: session.currentQuestion.id,
        userAnswer: answer,
      });
      setPreviousFeedback(data.previousAnswerFeedback || "Answer submitted.");
      setPreviousScore(data.previousAnswerScore);
      setSession(data);
      setAnswer("");
    } catch (err) {
      setError(err.response?.data?.message || "Could not submit answer.");
    } finally {
      setLoading(false);
    }
  };

  if (!session) {
    return <div className="empty-state page-loading">Loading interview...</div>;
  }

  const isComplete = session.status === "COMPLETED";
  const progress = session.totalQuestions
    ? Math.round(((session.answeredQuestions || 0) / session.totalQuestions) * 100)
    : 0;

  return (
    <div className="interview-focus">
      <header className="focus-header">
        <div>
          <strong>Active Session</strong>
          <span>AI Mock Interview</span>
        </div>
        <div className="progress-pill">
          <span>
            Question {Math.min((session.answeredQuestions || 0) + 1, session.totalQuestions || 1)} of {session.totalQuestions}
          </span>
          <div className="mini-progress">
            <div style={{ width: `${progress}%` }} />
          </div>
        </div>
        <button className="danger-button" onClick={() => navigate("/history")}>
          <Icon name="close" />
          End Session
        </button>
      </header>

      <Alert type="error">{error}</Alert>

      {isComplete ? (
        <section className="complete-card">
          <Icon name="workspace_premium" />
          <h1>Interview Completed</h1>
          <p>Your final AI evaluation score is ready.</p>
          <strong>{session.finalScore ?? 0}/100</strong>
          <button className="primary-gradient" onClick={() => navigate("/history")}>
            View History
          </button>
        </section>
      ) : (
        <div className="interview-grid">
          <section className="question-card">
            <div className="tag-row">
              <span>{session.currentQuestion?.questionType || "Technical"}</span>
              <span>{session.currentQuestion?.difficulty || "Medium"}</span>
            </div>
            <h1>{session.currentQuestion?.questionText}</h1>
            <p>
              Structure your answer clearly. Define the concept, explain implementation, and add one project-level example when possible.
            </p>
            <div className="target-time">
              <Icon name="timer" />
              Target Time: 2-3 mins
            </div>
          </section>

          <section className="answer-panel">
            <div className="answer-toolbar">
              <span>Your Answer</span>
              <div>
                <Icon name="code" />
                <Icon name="mic" />
              </div>
            </div>
            <textarea
              value={answer}
              onChange={(event) => setAnswer(event.target.value)}
              placeholder="Type your response here..."
            />
            <div className="answer-actions">
              <LoadingButton type="button" loading={loading} className="primary-gradient" onClick={submit}>
                Submit Answer
                <Icon name="send" />
              </LoadingButton>
            </div>
          </section>

          <aside className="insights-panel">
            <h2>
              <Icon name="bolt" />
              Live AI Insights
            </h2>
            {previousFeedback ? (
              <div className="feedback-card">
                <div>
                  <strong>Previous Feedback</strong>
                  {previousScore !== null ? <span>{previousScore}/100</span> : null}
                </div>
                <p>{previousFeedback}</p>
              </div>
            ) : (
              <div className="feedback-card muted">
                <strong>Awaiting first answer</strong>
                <p>Feedback appears here after each submitted answer.</p>
              </div>
            )}
            <div className="analyzing-card">
              <span />
              Analyzing current input after submission...
            </div>
          </aside>
        </div>
      )}
    </div>
  );
}
