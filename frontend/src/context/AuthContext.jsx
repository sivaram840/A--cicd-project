import React, { createContext, useState, useEffect } from "react";
import api from "../api/axios";

// Create context
export const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  // Check localStorage on mount
  useEffect(() => {
    const token = localStorage.getItem("token");
    if (token) {
      api.defaults.headers.common["Authorization"] = `Bearer ${token}`;
      fetchProfile();
    } else {
      setLoading(false);
    }
  }, []);

  // Fetch current user profile from backend
  const fetchProfile = async () => {
    try {
      const resp = await api.get("/auth/me"); // calls /api/auth/me
      setUser(resp.data);
      setIsAuthenticated(true);
    } catch (err) {
      console.error("Failed to fetch profile:", err);
      logout();
    } finally {
      setLoading(false);
    }
  };

  // Login: save token, fetch profile
  const login = (token) => {
    localStorage.setItem("token", token);
    api.defaults.headers.common["Authorization"] = `Bearer ${token}`;
    fetchProfile();
  };

  // Register
  const register = async (name, email, password) => {
    return api.post("/auth/register", { name, email, password });
  };

  // Logout
  const logout = () => {
    localStorage.removeItem("token");
    delete api.defaults.headers.common["Authorization"];
    setUser(null);
    setIsAuthenticated(false);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated,
        loading,
        login,
        logout,
        register,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}
