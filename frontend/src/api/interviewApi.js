import apiClient from "./apiClient";

export const startInterview = (payload) => apiClient.post("/interviews/start", payload);

export const submitAnswer = (interviewId, payload) =>
  apiClient.post(`/interviews/${interviewId}/answer`, payload);

export const getInterview = (interviewId) => apiClient.get(`/interviews/${interviewId}`);

export const getInterviewHistory = () => apiClient.get("/interviews/history");
