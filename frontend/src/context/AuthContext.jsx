import { createContext, useContext, useMemo, useState } from "react";
import { login as loginRequest, register as registerRequest } from "../api/authApi";

const AuthContext = createContext(null);

const readUser = () => {
  try {
    return JSON.parse(localStorage.getItem("prepai_user"));
  } catch {
    return null;
  }
};

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem("prepai_token"));
  const [user, setUser] = useState(readUser);

  const persistAuth = (authResponse) => {
    localStorage.setItem("prepai_token", authResponse.token);
    localStorage.setItem("prepai_user", JSON.stringify(authResponse));
    setToken(authResponse.token);
    setUser(authResponse);
  };

  const login = async (payload) => {
    const { data } = await loginRequest(payload);
    persistAuth(data);
    return data;
  };

  const register = async (payload) => {
    const { data } = await registerRequest(payload);
    persistAuth(data);
    return data;
  };

  const logout = () => {
    localStorage.removeItem("prepai_token");
    localStorage.removeItem("prepai_user");
    setToken(null);
    setUser(null);
  };

  const value = useMemo(
    () => ({
      token,
      user,
      isAuthenticated: Boolean(token),
      login,
      register,
      logout,
    }),
    [token, user]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);
