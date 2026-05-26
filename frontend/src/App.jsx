import { Navigate, Route, Routes } from "react-router-dom";
import ProtectedRoute from "./components/ProtectedRoute.jsx";
import AppShell from "./components/AppShell.jsx";
import Login from "./pages/Login.jsx";
import Register from "./pages/Register.jsx";
import Dashboard from "./pages/Dashboard.jsx";
import ResumeAnalysis from "./pages/ResumeAnalysis.jsx";
import InterviewStart from "./pages/InterviewStart.jsx";
import MockInterview from "./pages/MockInterview.jsx";
import InterviewHistory from "./pages/InterviewHistory.jsx";

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route
        element={
          <ProtectedRoute>
            <AppShell />
          </ProtectedRoute>
        }
      >
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/resume" element={<ResumeAnalysis />} />
        <Route path="/interview/start" element={<InterviewStart />} />
        <Route path="/interview/:interviewId" element={<MockInterview />} />
        <Route path="/history" element={<InterviewHistory />} />
        <Route path="/performance" element={<InterviewHistory />} />
        <Route path="/settings" element={<Dashboard />} />
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}
