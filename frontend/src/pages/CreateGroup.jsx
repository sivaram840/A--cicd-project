import React, { useState, useContext } from "react";
import { useNavigate } from "react-router-dom";
import {
  Container,
  Box,
  Typography,
  TextField,
  Button,
  Alert,
  CircularProgress,
} from "@mui/material";
import api from "../api/axios";
import { AuthContext } from "../context/AuthContext";

/**
 * Create Group page
 * POST /api/groups
 * Body: { name, description? }
 *
 * On success:
 *  - If backend returns created group object with id -> navigate to /groups/{id}
 *  - Else navigate to /groups
 */
export default function CreateGroup() {
  const navigate = useNavigate();
  const { isAuthenticated } = useContext(AuthContext);

  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  // Basic guard: if not authenticated, go to login
  if (!isAuthenticated) {
    navigate("/login");
    return null;
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccessMsg(null);

    if (!name.trim()) {
      setError("Group name is required.");
      return;
    }

    setLoading(true);
    try {
      const payload = { name: name.trim() };
      if (description.trim()) payload.description = description.trim();

      const resp = await api.post("/groups", payload);

      // Try to find created group id from response
      const body = resp && resp.data ? resp.data : null;
      let createdId = null;

      if (!body) {
        // fallback: no body, just go to list
        navigate("/groups");
        return;
      }

      // If backend returns GroupDto or object with id
      if (typeof body === "object" && (body.id || body.groupId || bodyIdFromBody(body))) {
        createdId = body.id ?? body.groupId ?? bodyIdFromBody(body);
      } else if (typeof body === "string") {
        // backend sometimes returns "User registered with id=123" style; try to parse number
        const m = body.match(/id=(\d+)/);
        if (m) createdId = m[1];
      }

      setSuccessMsg("Group created.");

      // Redirect to group details if id available, else to groups list
      setTimeout(() => {
        if (createdId) {
          navigate(`/groups/${createdId}`);
        } else {
          navigate("/groups");
        }
      }, 600);
    } catch (err) {
      console.error("Create group error:", err);
      if (err.response && err.response.data) {
        setError(
          typeof err.response.data === "string"
            ? err.response.data
            : err.response.data.message || "Failed to create group"
        );
      } else {
        setError("Network or server error. Is backend running on :8080?");
      }
    } finally {
      setLoading(false);
    }
  };

  // helper: scan object for an id-like property (flexible)
  const bodyIdFromBody = (obj) => {
    try {
      if (!obj || typeof obj !== "object") return null;
      for (const k of Object.keys(obj)) {
        if (/id$/i.test(k) && (typeof obj[k] === "number" || (typeof obj[k] === "string" && /^\d+$/.test(obj[k])))) {
          return obj[k];
        }
      }
    } catch (e) {
      // ignore
    }
    return null;
  };

  return (
    <Container maxWidth="sm" sx={{ mt: 6 }}>
      <Box sx={{ mb: 3, display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <Typography variant="h5">Create Group</Typography>
        <Button variant="text" onClick={() => navigate("/groups")}>
          Back to groups
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {successMsg && (
        <Alert severity="success" sx={{ mb: 2 }}>
          {successMsg}
        </Alert>
      )}

      <Box
        component="form"
        onSubmit={handleSubmit}
        sx={{ display: "flex", flexDirection: "column", gap: 2 }}
      >
        <TextField
          label="Group name"
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
          fullWidth
        />

        <TextField
          label="Description (optional)"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          multiline
          rows={3}
          fullWidth
        />

        <Box sx={{ display: "flex", gap: 2, alignItems: "center" }}>
          <Button type="submit" variant="contained" disabled={loading}>
            {loading ? <CircularProgress size={20} /> : "Create Group"}
          </Button>

          <Button variant="outlined" onClick={() => navigate("/groups")} disabled={loading}>
            Cancel
          </Button>
        </Box>
      </Box>
    </Container>
  );
}
