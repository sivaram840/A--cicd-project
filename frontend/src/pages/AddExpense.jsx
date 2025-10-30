import React, { useEffect, useState, useContext } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  Container,
  Box,
  Typography,
  TextField,
  Button,
  MenuItem,
  Select,
  FormControl,
  InputLabel,
  CircularProgress,
  Alert,
  FormControlLabel,
  Checkbox,
  Grid,
} from "@mui/material";
import api from "../api/axios";
import { AuthContext } from "../context/AuthContext";

export default function AddExpense() {
  const { id: groupId } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated } = useContext(AuthContext);

  const [loading, setLoading] = useState(true);
  const [members, setMembers] = useState([]);
  const [error, setError] = useState(null);

  const [note, setNote] = useState("");
  const [amount, setAmount] = useState("");
  const [currency, setCurrency] = useState("INR");
  const [payerId, setPayerId] = useState("");
  const [splitType, setSplitType] = useState("EQUAL");

  const [participantsState, setParticipantsState] = useState({});

  useEffect(() => {
    if (!isAuthenticated) {
      navigate("/login");
      return;
    }
    loadMembers();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [groupId, isAuthenticated]);

  const loadMembers = async () => {
    setLoading(true);
    setError(null);
    try {
      const resp = await api.get(`/groups/${groupId}`);
      console.log("GET /groups/{id} response:", resp.data);
      const fetched = resp?.data?.members ?? [];

      // Defensive: if backend returns membership DTOs with "userId" and "name" or "id" and "name"
      const normalized = fetched.map((m) => ({
        id: m.userId ?? m.id,
        name: m.name ?? m.userName ?? m.fullName ?? m.email,
        email: m.email ?? "",
      }));

      setMembers(normalized);

      const init = {};
      normalized.forEach((m) => {
        const uid = String(m.id);
        init[uid] = { checked: true, percent: "", amount: "" };
      });
      setParticipantsState(init);

      if (normalized.length > 0) {
        setPayerId(String(normalized[0].id));
      }
    } catch (err) {
      console.error("Failed to load members:", err);
      setError(
        "Could not load group members. Check console and backend logs. Is backend running?"
      );
      setMembers([]);
    } finally {
      setLoading(false);
    }
  };

  const displayName = (m) => m.name ?? `User ${m.id}`;

  const toggleMemberChecked = (userId) => {
    setParticipantsState((p) => ({
      ...p,
      [userId]: {
        ...(p[userId] || { percent: "", amount: "" }),
        checked: !(p[userId]?.checked ?? false),
      },
    }));
  };

  const setMemberPercent = (userId, value) =>
    setParticipantsState((p) => ({
      ...p,
      [userId]: { ...(p[userId] || {}), percent: value },
    }));

  const setMemberAmount = (userId, value) =>
    setParticipantsState((p) => ({
      ...p,
      [userId]: { ...(p[userId] || {}), amount: value },
    }));

  const effectiveMembers = members;

  const checkedCount = Object.values(participantsState).filter((v) => v.checked)
    .length;
  const equalShare =
    checkedCount > 0 && amount
      ? (Number(amount) / checkedCount).toFixed(2)
      : "0.00";

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    try {
      const numAmount = Number(amount);
      if (!note.trim()) throw new Error("Description is required.");
      if (!amount || isNaN(numAmount) || numAmount <= 0)
        throw new Error("Please enter a valid amount greater than 0.");
      if (!payerId) throw new Error("Please select payer.");

      const participants = Object.entries(participantsState)
        .filter(([, v]) => v.checked)
        .map(([uid, v]) => ({
          userId: Number(uid),
          percent: v.percent,
          amount: v.amount,
        }));

      if (participants.length === 0) throw new Error("Select at least one participant.");

      if (splitType === "PERCENT") {
        const sumPercent = participants.reduce(
          (s, p) => s + (Number(p.percent) || 0),
          0
        );
        if (Math.abs(sumPercent - 100) > 0.001)
          throw new Error(`Percents must sum to 100 (current: ${sumPercent}).`);
      } else if (splitType === "CUSTOM") {
        const sumAmt = participants.reduce(
          (s, p) => s + (Number(p.amount) || 0),
          0
        );
        if (Math.abs(sumAmt - numAmount) > 0.01)
          throw new Error(
            `Individual amounts must sum to total (${numAmount}). Current: ${sumAmt}`
          );
      }

      const shares = participants.map((p) => {
        const shareObj = { userId: p.userId };
        if (splitType === "PERCENT") shareObj.percent = Number(p.percent || 0);
        if (splitType === "CUSTOM") shareObj.amount = Number(p.amount || 0);
        return shareObj;
      });

      const payload = {
        amount: numAmount,
        payerId: Number(payerId),
        splitType,
        currency: currency || undefined,
        note: note.trim(),
        shares: shares.length > 0 ? shares : undefined,
      };

      console.log("Posting expense payload:", payload);

      const response = await api.post(`/groups/${groupId}/expenses`, payload);
      console.log("POST /expenses response:", response.status, response.data);

      // ensure success status codes 200-299
      if (response.status >= 200 && response.status < 300) {
        navigate(`/groups/${groupId}`);
      } else {
        // show server response if not success
        const serverMsg =
          response.data && (response.data.message || JSON.stringify(response.data));
        throw new Error(serverMsg || `Unexpected status ${response.status}`);
      }
    } catch (err) {
      console.error("Error creating expense:", err);
      // show clear message in UI
      if (err.response && err.response.data) {
        // axios error
        setError(
          typeof err.response.data === "string"
            ? err.response.data
            : err.response.data.message || JSON.stringify(err.response.data)
        );
      } else {
        setError(err.message || String(err));
      }
      // do not navigate — let user fix
    }
  };

  if (loading) {
    return (
      <Container maxWidth="md" sx={{ mt: 6, textAlign: "center" }}>
        <CircularProgress />
      </Container>
    );
  }

  return (
    <Container maxWidth="md" sx={{ mt: 6 }}>
      <Box sx={{ display: "flex", justifyContent: "space-between", mb: 2 }}>
        <Typography variant="h5">Add Expense</Typography>
        <Button variant="text" onClick={() => navigate(`/groups/${groupId}`)}>
          Back
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Box
        component="form"
        onSubmit={handleSubmit}
        sx={{ display: "flex", flexDirection: "column", gap: 2 }}
      >
        <TextField
          label="Description"
          value={note}
          onChange={(e) => setNote(e.target.value)}
          required
          fullWidth
        />

        <TextField
          label="Amount"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
          type="number"
          inputProps={{ step: "0.01", min: "0" }}
          required
          fullWidth
        />

        <TextField
          label="Currency"
          value={currency}
          onChange={(e) => setCurrency(e.target.value)}
          fullWidth
        />

        <FormControl fullWidth>
          <InputLabel id="payer-label">Payer</InputLabel>
          <Select
            labelId="payer-label"
            value={payerId}
            onChange={(e) => setPayerId(e.target.value)}
            required
          >
            {effectiveMembers.map((m) => {
              const uid = String(m.id);
              return (
                <MenuItem key={uid} value={uid}>
                  {displayName(m)}
                </MenuItem>
              );
            })}
          </Select>
        </FormControl>

        <FormControl fullWidth>
          <InputLabel id="split-type-label">Split type</InputLabel>
          <Select
            labelId="split-type-label"
            value={splitType}
            onChange={(e) => setSplitType(e.target.value)}
          >
            <MenuItem value="EQUAL">EQUAL</MenuItem>
            <MenuItem value="PERCENT">PERCENT</MenuItem>
            <MenuItem value="CUSTOM">CUSTOM</MenuItem>
          </Select>
        </FormControl>

        <Box>
          <Typography variant="subtitle1" sx={{ mb: 1 }}>
            Participants
          </Typography>
          <Grid container spacing={1}>
            {effectiveMembers.map((m) => {
              const uid = String(m.id);
              const state =
                participantsState[uid] ?? {
                  checked: true,
                  percent: "",
                  amount: "",
                };
              return (
                <Grid item xs={12} md={6} key={uid}>
                  <Box sx={{ border: "1px solid #eee", borderRadius: 1, p: 1 }}>
                    <FormControlLabel
                      control={
                        <Checkbox
                          checked={!!state.checked}
                          onChange={() => toggleMemberChecked(uid)}
                        />
                      }
                      label={displayName(m)}
                    />

                    {splitType === "EQUAL" && (
                      <Typography variant="body2">Share: {equalShare}</Typography>
                    )}

                    {splitType === "PERCENT" && (
                      <TextField
                        label="Percent"
                        value={state.percent}
                        onChange={(e) => setMemberPercent(uid, e.target.value)}
                        type="number"
                        inputProps={{ step: "0.1", min: "0", max: "100" }}
                        fullWidth
                        sx={{ mt: 1 }}
                      />
                    )}

                    {splitType === "CUSTOM" && (
                      <TextField
                        label="Amount"
                        value={state.amount}
                        onChange={(e) => setMemberAmount(uid, e.target.value)}
                        type="number"
                        inputProps={{ step: "0.01", min: "0" }}
                        fullWidth
                        sx={{ mt: 1 }}
                      />
                    )}
                  </Box>
                </Grid>
              );
            })}
          </Grid>
        </Box>

        <Box sx={{ display: "flex", gap: 2 }}>
          <Button type="submit" variant="contained">
            Save expense
          </Button>
          <Button variant="outlined" onClick={() => navigate(`/groups/${groupId}`)}>
            Cancel
          </Button>
        </Box>
      </Box>
    </Container>
  );
}
