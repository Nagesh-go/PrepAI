import apiClient from "./apiClient";

export const uploadResume = (file) => {
  const formData = new FormData();
  formData.append("file", file);

  // Let axios set multipart boundary automatically.
  return apiClient.post("/resumes/upload", formData);
};

export const getLatestResume = () => apiClient.get("/resumes/latest");
