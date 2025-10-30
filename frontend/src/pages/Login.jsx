import React, { useState, useContext } from "react";
import { useNavigate } from "react-router-dom";
import {
  Container,
  Box,
  Typography,
  TextField,
  Button,
  Alert,
} from "@mui/material";
import api from "../api/axios";
import { AuthContext } from "../context/AuthContext";

/**
 * Login page
 * POST /api/auth/login
 * Body: { email, password }
 * Expects response: { token: "...", tokenType: "Bearer" }
 *
 * On success: call authContext.login(token) (this saves to localStorage)
 * and navigate to /groups
 */
export default function Login() {
  const navigate = useNavigate();
  const { login } = useContext(AuthContext);

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const resp = await api.post("/auth/login", { email, password });
      // backend returns { token: "...", tokenType: "Bearer" }
      const token = resp.data?.token;
      if (!token) {
        setError("Login succeeded but no token returned by server.");
        setLoading(false);
        return;
      }

      // save token in context -> AuthContext will persist to localStorage
      login(token);

      // optionally set the default header immediately (interceptor reads localStorage,
      // but setting defaults helps for immediate subsequent requests)
      api.defaults.headers.common["Authorization"] = `Bearer ${token}`;

      // navigate to groups page (we'll add this route soon)
      navigate("/groups");
    } catch (err) {
      // Show simple error message
      if (err.response && err.response.data) {
        setError(
          typeof err.response.data === "string"
            ? err.response.data
            : err.response.data.message || "Invalid credentials"
        );
      } else {
        setError("Network or server error. Is backend running on :8080?");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container maxWidth="xs">
      <Box sx={{ mt: 10, display: "flex", flexDirection: "column", gap: 2 }}>
        <Typography variant="h5" align="center">
          Login
        </Typography>

        {error && <Alert severity="error">{error}</Alert>}

        <form onSubmit={handleSubmit}>
          <Box sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
            <TextField
              label="Email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              fullWidth
            />

            <TextField
              label="Password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              fullWidth
            />

            <Button
              type="submit"
              variant="contained"
              disabled={loading}
              fullWidth
            >
              {loading ? "Logging in…" : "Login"}
            </Button>

            <Button
              onClick={() => navigate("/register")}
              variant="text"
              fullWidth
            >
              Don't have an account? Register
            </Button>
          </Box>
        </form>
      </Box>
    </Container>
  );
}
