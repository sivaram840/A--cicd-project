import React, { useEffect, useState, useContext, useCallback } from "react";
import {
  Container,
  Box,
  Typography,
  Button,
  CircularProgress,
  Alert,
  Card,
  CardContent,
  CardActions,
  Grid,
} from "@mui/material";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";
import { AuthContext } from "../context/AuthContext";

export default function Groups() {
  const navigate = useNavigate();
  const { isAuthenticated, user } = useContext(AuthContext);

  const [groups, setGroups] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Redirect to login if not authenticated
  useEffect(() => {
    if (!isAuthenticated) {
      navigate("/login");
    }
  }, [isAuthenticated, navigate]);

  const parseGroupsFromResponse = (respData) => {
    if (!respData) return [];
    if (Array.isArray(respData)) return respData;
    if (respData.groups && Array.isArray(respData.groups)) return respData.groups;
    if (respData.data && Array.isArray(respData.data)) return respData.data;

    if (typeof respData === "object") {
      for (const v of Object.values(respData)) {
        if (Array.isArray(v)) return v;
      }
    }
    return [];
  };

  const fetchGroups = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const resp = await api.get("/groups");
      const list = parseGroupsFromResponse(resp.data);
      setGroups(list);
    } catch (err) {
      console.error("Failed to fetch groups:", err);
      if (err.response && err.response.status === 401) {
        setError("Not authenticated. Please login again.");
      } else if (err.response && err.response.status === 403) {
        setError("Access forbidden: Create a group or ask an admin to add you.");
      } else {
        setError("Could not load groups. Is backend running on http://localhost:8080?");
      }
      setGroups([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchGroups();
  }, [fetchGroups]);

  const handleCreate = () => navigate("/groups/create");
  const handleOpenGroup = (id) => navigate(`/groups/${id}`);
  const handleRefresh = () => fetchGroups();

  return (
    <Container maxWidth="lg" sx={{ mt: 6 }}>
      <Box sx={{ display: "flex", justifyContent: "space-between", mb: 3 }}>
        <Box>
          <Typography variant="h4">Groups</Typography>
          {user && (
            <Typography variant="subtitle1" color="text.secondary">
              Welcome, {user.name}
            </Typography>
          )}
        </Box>

        <Box sx={{ display: "flex", gap: 1 }}>
          <Button variant="outlined" onClick={handleRefresh} disabled={loading}>
            {loading ? "Refreshing…" : "Refresh"}
          </Button>
          <Button variant="contained" onClick={handleCreate}>
            Create group
          </Button>
        </Box>
      </Box>

      {loading && (
        <Box sx={{ display: "flex", justifyContent: "center", mt: 6 }}>
          <CircularProgress />
        </Box>
      )}

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {!loading && !error && groups.length === 0 && (
        <Alert severity="info" sx={{ mb: 2 }}>
          No groups yet. Create your first group!
        </Alert>
      )}

      <Grid container spacing={2} sx={{ mt: 1 }}>
        {groups.map((g) => {
          const id = g.id ?? g.groupId ?? g._id ?? "unknown";
          const name = g.name ?? g.title ?? g.groupName ?? "Unnamed group";
          const desc = g.description ?? g.desc ?? g.note ?? "";
          const membersCount =
            typeof g.membersCount === "number"
              ? g.membersCount
              : Array.isArray(g.members)
              ? g.members.length
              : typeof g.memberCount === "number"
              ? g.memberCount
              : null;

          return (
            <Grid item xs={12} sm={6} md={4} key={String(id)}>
              <Card>
                <CardContent>
                  <Typography variant="h6" component="div" gutterBottom>
                    {name}
                  </Typography>

                  {desc && (
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                      {desc}
                    </Typography>
                  )}

                  <Typography variant="caption" color="text.secondary">
                    ID: {String(id)}
                  </Typography>

                  {membersCount !== null && (
                    <Typography variant="body2" sx={{ mt: 1 }}>
                      {`${membersCount} member${membersCount === 1 ? "" : "s"}`}
                    </Typography>
                  )}
                </CardContent>

                <CardActions>
                  <Button size="small" onClick={() => handleOpenGroup(id)}>
                    View
                  </Button>
                </CardActions>
              </Card>
            </Grid>
          );
        })}
      </Grid>
    </Container>
  );
}
