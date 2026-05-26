import apiClient from "./apiClient";

export const register = (payload) => apiClient.post("/auth/register", payload);

export const login = (payload) => apiClient.post("/auth/login", payload);
