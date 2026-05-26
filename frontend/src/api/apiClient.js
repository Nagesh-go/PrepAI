import axios from "axios";

// Use "/api" in dev so Vite proxies to the backend (avoids CORS).
// Override with VITE_API_BASE_URL in .env for production builds.
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "/api",
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("prepai_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const isAuthRequest = error.config?.url?.includes("/auth/");
    if (error.response?.status === 401 && !isAuthRequest) {
      localStorage.removeItem("prepai_token");
      localStorage.removeItem("prepai_user");
      if (!window.location.pathname.includes("/login")) {
        window.location.assign("/login");
      }
    }
    return Promise.reject(error);
  }
);

export default apiClient;
