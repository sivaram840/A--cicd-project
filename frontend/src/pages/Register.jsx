import React, { useState } from "react";
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

/**
 * Register page
 * POST /api/auth/register
 * Body: { name, email, password }
 * Backend returns a plain string message on success (e.g. "User registered with id=...")
 *
 * On success: navigate to /login and show a success message (brief)
 */
export default function Register() {
  const navigate = useNavigate();

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccessMsg(null);
    setLoading(true);

    try {
      const resp = await api.post("/auth/register", { name, email, password });

      // backend returns plain string message; show it
      const msg =
        resp.data && typeof resp.data === "string"
          ? resp.data
          : "Registered successfully";

      setSuccessMsg(msg);

      // small delay to let user read the success message, then go to login
      setTimeout(() => {
        navigate("/login");
      }, 900);
    } catch (err) {
      if (err.response && err.response.data) {
        setError(
          typeof err.response.data === "string"
            ? err.response.data
            : err.response.data.message || "Registration failed"
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
      <Box sx={{ mt: 8, display: "flex", flexDirection: "column", gap: 2 }}>
        <Typography variant="h5" align="center">
          Register
        </Typography>

        {error && <Alert severity="error">{error}</Alert>}
        {successMsg && <Alert severity="success">{successMsg}</Alert>}

        <form onSubmit={handleSubmit}>
          <Box sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
            <TextField
              label="Full name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              fullWidth
            />

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
              {loading ? "Registering…" : "Register"}
            </Button>

            <Button onClick={() => navigate("/login")} variant="text" fullWidth>
              Already have an account? Login
            </Button>
          </Box>
        </form>
      </Box>
    </Container>
  );
}
