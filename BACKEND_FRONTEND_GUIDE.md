# AI-Powered Interview Preparation Platform - Backend Guide for React Frontend

This document explains the current Spring Boot backend so the React frontend can be built against it.

## Backend Base URL

The backend runs on:

```text
http://localhost:9090
```

The port is configured in:

```text
interviewPrep/src/main/resources/application.properties
```

```properties
server.port=9090
```

## Tech Stack

- Java 17+
- Spring Boot 4
- Spring Security
- JWT authentication
- Spring Data JPA
- Hibernate
- MySQL
- PDFBox for resume PDF text extraction
- OpenAI-compatible AI service integration
- Maven wrapper

## Local Run Command

From the backend folder:

```powershell
cd interviewPrep
.\mvnw.cmd spring-boot:run
```

If the app fails because the port is already in use, either stop the old Java process or change `server.port`.

## Database Configuration

Current datasource:

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/interview_prep_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:root}
```

Hibernate will update tables automatically:

```properties
spring.jpa.hibernate.ddl-auto=update
```

## Authentication Model

The backend uses JWT Bearer authentication.

Unauthenticated routes:

```text
POST /api/auth/register
POST /api/auth/login
```

All other `/api/**` routes require:

```http
Authorization: Bearer <jwt-token>
```

React should store the token after login/register. For development, localStorage is acceptable:

```js
localStorage.setItem("token", response.token);
```

Then attach it to API requests:

```js
headers: {
  Authorization: `Bearer ${localStorage.getItem("token")}`
}
```

## API Endpoints

### Register

```http
POST /api/auth/register
Content-Type: application/json
```

Request:

```json
{
  "name": "Rahul Sharma",
  "email": "rahul@example.com",
  "password": "password123"
}
```

Response:

```json
{
  "token": "jwt-token",
  "userId": 1,
  "name": "Rahul Sharma",
  "email": "rahul@example.com",
  "role": "USER"
}
```

Frontend page:

```text
/register
```

### Login

```http
POST /api/auth/login
Content-Type: application/json
```

Request:

```json
{
  "email": "rahul@example.com",
  "password": "password123"
}
```

Response:

```json
{
  "token": "jwt-token",
  "userId": 1,
  "name": "Rahul Sharma",
  "email": "rahul@example.com",
  "role": "USER"
}
```

Frontend page:

```text
/login
```

### Upload Resume

```http
POST /api/resumes/upload
Authorization: Bearer <jwt-token>
Content-Type: multipart/form-data
```

Form field:

```text
file = resume.pdf
```

Response:

```json
{
  "resumeId": 1,
  "fileName": "resume.pdf",
  "extractedSkills": ["Java", "Spring Boot", "MySQL"],
  "experienceLevel": "Entry-Level",
  "aiFeedback": "Resume Strengths:\n- ...\n\nAreas to Improve:\n- ...",
  "atsScore": 70
}
```

Frontend page:

```text
/resume
```

UI ideas:

- PDF upload input
- Upload progress/loading state
- Extracted skills chips
- ATS score card
- AI feedback panel

### Get Latest Resume

```http
GET /api/resumes/latest
Authorization: Bearer <jwt-token>
```

Response:

```json
{
  "resumeId": 1,
  "fileName": "resume.pdf",
  "extractedSkills": ["Java", "Spring Boot", "MySQL"],
  "experienceLevel": "Entry-Level",
  "aiFeedback": "Resume Strengths:\n- ...",
  "atsScore": 70
}
```

### Start Mock Interview

```http
POST /api/interviews/start
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

Request:

```json
{
  "companyName": "Amazon",
  "interviewType": "TECHNICAL",
  "difficultyLevel": "MEDIUM",
  "questionCount": 5
}
```

Response:

```json
{
  "interviewId": 1,
  "currentQuestion": {
    "id": 1,
    "questionText": "Explain dependency injection in Spring Boot.",
    "questionType": "TECHNICAL",
    "difficulty": "MEDIUM",
    "topic": null,
    "orderNumber": 1
  },
  "previousAnswerFeedback": null,
  "previousAnswerScore": null,
  "totalQuestions": 5,
  "answeredQuestions": 0,
  "status": "IN_PROGRESS",
  "finalScore": null
}
```

Frontend page:

```text
/interview/start
```

UI fields:

- Company name
- Interview type
- Difficulty level
- Number of questions

### Submit Interview Answer

```http
POST /api/interviews/{interviewId}/answer
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

Request:

```json
{
  "questionId": 1,
  "userAnswer": "Dependency injection means Spring provides required dependencies..."
}
```

Response while interview continues:

```json
{
  "interviewId": 1,
  "currentQuestion": {
    "id": 2,
    "questionText": "What is JWT authentication?",
    "questionType": "TECHNICAL",
    "difficulty": "MEDIUM",
    "topic": null,
    "orderNumber": 2
  },
  "previousAnswerFeedback": "Good start. Improve by adding examples.",
  "previousAnswerScore": 70,
  "totalQuestions": 5,
  "answeredQuestions": 1,
  "status": "IN_PROGRESS",
  "finalScore": null
}
```

Response when completed:

```json
{
  "interviewId": 1,
  "currentQuestion": null,
  "previousAnswerFeedback": null,
  "previousAnswerScore": null,
  "totalQuestions": 5,
  "answeredQuestions": 5,
  "status": "COMPLETED",
  "finalScore": 75.50
}
```

Frontend page:

```text
/interview/:interviewId
```

UI ideas:

- Current question card
- Answer textarea
- Submit button
- Previous feedback panel
- Score display
- Progress indicator: answered / total

### Get Interview Details

```http
GET /api/interviews/{interviewId}
Authorization: Bearer <jwt-token>
```

Response is the full `Interview` entity with nested questions and answers.

Use carefully in React because entity responses may contain nested objects.

### Get Interview History

```http
GET /api/interviews/history
Authorization: Bearer <jwt-token>
```

Response:

```json
[
  {
    "id": 1,
    "companyName": "Amazon",
    "interviewType": "TECHNICAL",
    "difficultyLevel": "MEDIUM",
    "totalQuestions": 5,
    "answeredQuestions": 5,
    "score": 75.50,
    "technicalScore": 72.00,
    "communicationScore": 80.00,
    "confidenceScore": 75.50,
    "status": "COMPLETED",
    "createdAt": "2026-05-26T12:30:00",
    "completedAt": "2026-05-26T12:45:00"
  }
]
```

Frontend page:

```text
/history
```

### Dashboard

```http
GET /api/dashboard
Authorization: Bearer <jwt-token>
```

Response:

```json
{
  "totalInterviews": 3,
  "averageScore": 74.33,
  "latestResumeFeedback": "Resume Strengths:\n- ...",
  "latestAtsScore": 70
}
```

Frontend page:

```text
/dashboard
```

UI cards:

- Total interviews
- Average score
- Latest ATS score
- Latest resume feedback

## Recommended React Routes

```text
/login
/register
/dashboard
/resume
/interview/start
/interview/:interviewId
/history
/profile
```

## Recommended React Folder Structure

```text
frontend/
  src/
    api/
      apiClient.js
      authApi.js
      resumeApi.js
      interviewApi.js
      dashboardApi.js
    components/
      Navbar.jsx
      ProtectedRoute.jsx
      ScoreCard.jsx
      SkillChip.jsx
      LoadingButton.jsx
    context/
      AuthContext.jsx
    pages/
      Login.jsx
      Register.jsx
      Dashboard.jsx
      ResumeUpload.jsx
      InterviewStart.jsx
      MockInterview.jsx
      InterviewHistory.jsx
    App.jsx
    main.jsx
```

## Axios API Client Example

```js
import axios from "axios";

const apiClient = axios.create({
  baseURL: "http://localhost:9090/api",
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default apiClient;
```

## Auth API Example

```js
import apiClient from "./apiClient";

export const register = (payload) => {
  return apiClient.post("/auth/register", payload);
};

export const login = (payload) => {
  return apiClient.post("/auth/login", payload);
};
```

## Resume Upload Example

```js
import apiClient from "./apiClient";

export const uploadResume = (file) => {
  const formData = new FormData();
  formData.append("file", file);

  return apiClient.post("/resumes/upload", formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
};

export const getLatestResume = () => {
  return apiClient.get("/resumes/latest");
};
```

## Interview API Example

```js
import apiClient from "./apiClient";

export const startInterview = (payload) => {
  return apiClient.post("/interviews/start", payload);
};

export const submitAnswer = (interviewId, payload) => {
  return apiClient.post(`/interviews/${interviewId}/answer`, payload);
};

export const getInterviewHistory = () => {
  return apiClient.get("/interviews/history");
};

export const getInterview = (interviewId) => {
  return apiClient.get(`/interviews/${interviewId}`);
};
```

## Dashboard API Example

```js
import apiClient from "./apiClient";

export const getDashboard = () => {
  return apiClient.get("/dashboard");
};
```

## CORS

The backend CORS configuration allows common local React dev servers:

```text
http://localhost:3000
http://localhost:5173
```

If React runs on another port, add that origin in:

```text
interviewPrep/src/main/java/com/interviewPrep/config/CorsConfig.java
```

## Error Handling

Validation errors are returned as JSON with field details:

```json
{
  "timestamp": "2026-05-26T12:30:00",
  "status": 400,
  "error": "Validation failed",
  "fields": {
    "email": "must be a well-formed email address",
    "password": "size must be between 6 and ..."
  }
}
```

General errors return:

```json
{
  "timestamp": "2026-05-26T12:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "..."
}
```

Frontend should show user-friendly messages instead of raw backend stack details.

## Frontend Build Order

1. Create React app with Vite.
2. Add routing with `react-router-dom`.
3. Add Axios client with JWT interceptor.
4. Build login/register pages.
5. Add protected route wrapper.
6. Build dashboard page.
7. Build resume upload and analysis page.
8. Build interview start page.
9. Build mock interview answer flow.
10. Build interview history page.

## Suggested Install Commands

From the **project root** or the `frontend` folder:

```powershell
# First time only
cd frontend
npm install

# Start the dev server (use one of these — NOT plain "npm run")
npm run dev

# Or from project root:
cd ..
npm start
```

`npm run` by itself only **lists** scripts; it does not start the app. Use `npm run dev` or `npm start`.

Open in the browser:

```text
http://localhost:5173
```

## Important Notes

- Upload only PDF resumes for now.
- User must upload a resume before starting an AI interview because questions are generated from extracted resume skills.
- If `OPENAI_API_KEY` is empty, the backend AI service may use fallback/sample responses depending on the current implementation.
- Store JWT token after login/register.
- Send JWT token on every protected request.
- Backend is currently API-only; the React frontend should be a separate app.

