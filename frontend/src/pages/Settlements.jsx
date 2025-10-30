import React, { useEffect, useState, useContext } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  Container,
  Box,
  Typography,
  Button,
  CircularProgress,
  Alert,
  TextField,
  MenuItem,
  Select,
  FormControl,
  InputLabel,
  Grid,
  Paper,
  List,
  ListItem,
  ListItemText,
} from "@mui/material";
import api from "../api/axios";
import { AuthContext } from "../context/AuthContext";

/**
 * Settlements page
 * - GET /api/groups/{groupId}/settlements
 * - POST /api/groups/{groupId}/settlements
 *
 * POST body (RecordSettlementRequest):
 * {
 *   fromUserId: Long,
 *   toUserId: Long,
 *   amount: BigDecimal,
 *   currency: String,
 *   note: String
 * }
 */
export default function Settlements() {
  const { id: groupId } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated } = useContext(AuthContext);

  const [loading, setLoading] = useState(true);
  const [members, setMembers] = useState([]);
  const [settlements, setSettlements] = useState([]);
  const [error, setError] = useState(null);

  // form state
  const [fromUserId, setFromUserId] = useState("");
  const [toUserId, setToUserId] = useState("");
  const [amount, setAmount] = useState("");
  const [currency, setCurrency] = useState("INR");
  const [note, setNote] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [submitSuccess, setSubmitSuccess] = useState(null);
  const [submitError, setSubmitError] = useState(null);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate("/login");
      return;
    }
    fetchAll();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [groupId, isAuthenticated]);

  const fetchAll = async () => {
    setLoading(true);
    setError(null);
    try {
      // load members
      let fetchedMembers = [];
      try {
        const resp = await api.get(`/groups/${groupId}`);
        if (resp?.data?.members && Array.isArray(resp.data.members)) {
          fetchedMembers = resp.data.members;
        }
      } catch {}
      if (fetchedMembers.length === 0) {
        try {
          const resp2 = await api.get(`/groups/${groupId}/members`);
          if (Array.isArray(resp2.data)) fetchedMembers = resp2.data;
        } catch {}
      }
      setMembers(fetchedMembers);

      // settlements
      const respS = await api.get(`/groups/${groupId}/settlements`);
      setSettlements(Array.isArray(respS.data) ? respS.data : []);
    } catch (err) {
      console.error(err);
      setError("Failed to load data. Is backend running on :8080?");
    } finally {
      setLoading(false);
    }
  };

  const displayName = (m) =>
    m.name ?? m.fullName ?? m.email ?? String(m.id ?? m.userId ?? m);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitError(null);
    setSubmitSuccess(null);

    if (!fromUserId || !toUserId) {
      setSubmitError("Choose both From and To users.");
      return;
    }
    const num = Number(amount);
    if (!amount || isNaN(num) || num <= 0) {
      setSubmitError("Enter a valid amount greater than 0.");
      return;
    }
    if (fromUserId === toUserId) {
      setSubmitError("From and To cannot be the same user.");
      return;
    }

    setSubmitting(true);
    try {
      const payload = {
        fromUserId: Number(fromUserId),
        toUserId: Number(toUserId),
        amount: Number(num),
        currency: currency || "INR",
        note: note || undefined,
      };

      await api.post(`/groups/${groupId}/settlements`, payload);

      await fetchAll();
      setSubmitSuccess("Settlement recorded.");
      setAmount("");
      setNote("");
      setTimeout(() => setSubmitSuccess(null), 1200);
    } catch (err) {
      console.error("Failed to record settlement:", err);
      if (err.response && err.response.data) {
        setSubmitError(
          typeof err.response.data === "string"
            ? err.response.data
            : err.response.data.message || "Failed to record settlement"
        );
      } else {
        setSubmitError("Network or server error while recording settlement.");
      }
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <Container sx={{ mt: 6, textAlign: "center" }}>
        <CircularProgress />
      </Container>
    );
  }

  return (
    <Container sx={{ mt: 6 }}>
      <Box sx={{ display: "flex", justifyContent: "space-between", mb: 3 }}>
        <Typography variant="h5">Settlements</Typography>
        <Button variant="text" onClick={() => navigate(`/groups/${groupId}`)}>
          Back to group
        </Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Grid container spacing={2}>
        {/* Record settlement form */}
        <Grid item xs={12} md={5}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" sx={{ mb: 1 }}>Record settlement</Typography>
            {submitError && <Alert severity="error" sx={{ mb: 1 }}>{submitError}</Alert>}
            {submitSuccess && <Alert severity="success" sx={{ mb: 1 }}>{submitSuccess}</Alert>}

            <Box component="form" onSubmit={handleSubmit} sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
              <FormControl fullWidth>
                <InputLabel id="from-label">From (payer)</InputLabel>
                <Select
                  labelId="from-label"
                  value={fromUserId}
                  label="From (payer)"
                  onChange={(e) => setFromUserId(e.target.value)}
                >
                  {members.map((m) => {
                    const uid = String(m.id ?? m.userId ?? m.email ?? m);
                    return <MenuItem key={uid} value={uid}>{displayName(m)}</MenuItem>;
                  })}
                </Select>
              </FormControl>

              <FormControl fullWidth>
                <InputLabel id="to-label">To (receiver)</InputLabel>
                <Select
                  labelId="to-label"
                  value={toUserId}
                  label="To (receiver)"
                  onChange={(e) => setToUserId(e.target.value)}
                >
                  {members.map((m) => {
                    const uid = String(m.id ?? m.userId ?? m.email ?? m);
                    return <MenuItem key={uid} value={uid}>{displayName(m)}</MenuItem>;
                  })}
                </Select>
              </FormControl>

              <TextField
                label="Amount"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                type="number"
                inputProps={{ step: "0.01", min: "0" }}
                required
              />

              <TextField
                label="Currency"
                value={currency}
                onChange={(e) => setCurrency(e.target.value)}
                fullWidth
              />

              <TextField
                label="Note (optional)"
                value={note}
                onChange={(e) => setNote(e.target.value)}
                fullWidth
              />

              <Box sx={{ display: "flex", gap: 1 }}>
                <Button type="submit" variant="contained" disabled={submitting}>
                  {submitting ? "Saving…" : "Record"}
                </Button>
                <Button variant="outlined" onClick={() => { setAmount(""); setNote(""); }}>
                  Clear
                </Button>
              </Box>
            </Box>
          </Paper>
        </Grid>

        {/* Settlements list */}
        <Grid item xs={12} md={7}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" sx={{ mb: 1 }}>Recent settlements</Typography>
            {settlements.length === 0 ? (
              <Typography variant="body2">No settlements recorded yet.</Typography>
            ) : (
              <List>
                {settlements.map((s) => {
                  const sid = s.id ?? s.settlementId ?? s.id;
                  const from = s.fromUserName ?? s.fromUser?.name ?? s.fromUserId;
                  const to = s.toUserName ?? s.toUser?.name ?? s.toUserId;
                  const amt = typeof s.amount === "number" ? s.amount : (s.amount ?? "");
                  const cur = s.currency ?? "";
                  const createdAt = s.createdAt ?? s.timestamp ?? "";

                  return (
                    <ListItem key={String(sid)}>
                      <ListItemText
                        primary={`${from} → ${to} — ${amt} ${cur}`}
                        secondary={createdAt ? String(createdAt) : null}
                      />
                    </ListItem>
                  );
                })}
              </List>
            )}
          </Paper>
        </Grid>
      </Grid>
    </Container>
  );
}
