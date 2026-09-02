import { createContext, useState, useEffect } from "react";
import api from "../services/api";

export const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedUser = localStorage.getItem("user");
    const token = localStorage.getItem("token");
    if (storedUser && token) {
      try {
        setUser(JSON.parse(storedUser));
      } catch (err) {
        localStorage.removeItem("token");
        localStorage.removeItem("user");
        setUser(null);
      }
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    const response = await api.post("/api/auth/login", { email, password });
    const data = response.data;

    localStorage.setItem("token", data.token);
    localStorage.setItem(
      "user",
      JSON.stringify({
        email: data.email,
        role: data.role,
        fullName: data.fullName,
      })
    );
    setUser({
      email: data.email,
      role: data.role,
      fullName: data.fullName,
    });

    return data;
  };

  const register = async (fullName, email, password, phone, role) => {
    const response = await api.post("/api/auth/register", {
      fullName,
      email,
      password,
      phone,
      role,
    });
    const data = response.data;

    localStorage.setItem("token", data.token);
    localStorage.setItem(
      "user",
      JSON.stringify({
        email: data.email,
        role: data.role,
        fullName: data.fullName,
      })
    );
    setUser({
      email: data.email,
      role: data.role,
      fullName: data.fullName,
    });

    return data;
  };

  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, register, logout, loading }}>
      {children}
    </AuthContext.Provider>
  );
}