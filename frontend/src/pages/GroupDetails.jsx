import React, { useEffect, useState, useContext } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  Container,
  Box,
  Typography,
  Button,
  CircularProgress,
  Alert,
  Card,
  CardContent,
  Grid,
  List,
  ListItem,
  ListItemText,
  Divider,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
} from "@mui/material";
import api from "../api/axios";
import { AuthContext } from "../context/AuthContext";

export default function GroupDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated } = useContext(AuthContext);

  const [group, setGroup] = useState(null);
  const [expenses, setExpenses] = useState([]);
  const [balances, setBalances] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Add member state
  const [newMemberEmail, setNewMemberEmail] = useState("");
  const [addMemberError, setAddMemberError] = useState(null);
  const [addMemberSuccess, setAddMemberSuccess] = useState(null);
  const [addingMember, setAddingMember] = useState(false);

  // Settlement dialog state
  const [settleOpen, setSettleOpen] = useState(false);
  const [fromUser, setFromUser] = useState("");
  const [toUser, setToUser] = useState("");
  const [amount, setAmount] = useState("");
  const [settleLoading, setSettleLoading] = useState(false);
  const [settleError, setSettleError] = useState(null);
  const [settleSuccess, setSettleSuccess] = useState(null);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate("/login");
      return;
    }
    fetchAll();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id, isAuthenticated]);

  const fetchAll = async () => {
    setLoading(true);
    setError(null);
    try {
      // Group meta
      const resp = await api.get(`/groups/${id}`);
      setGroup(resp.data);

      // Expenses
      try {
        const respExp = await api.get(`/groups/${id}/expenses`);
        setExpenses(Array.isArray(respExp.data) ? respExp.data : []);
      } catch {
        setExpenses([]);
      }

      // Balances
      try {
        const respBal = await api.get(`/groups/${id}/balances`);
        setBalances(Array.isArray(respBal.data) ? respBal.data : []);
      } catch {
        setBalances([]);
      }
    } catch (err) {
      console.error("Failed to load group details:", err);
      setError("Failed to load group details. Is backend running on :8080?");
    } finally {
      setLoading(false);
    }
  };

  // Add member by email
  const handleAddMember = async (e) => {
    e.preventDefault();
    if (!newMemberEmail.trim()) return;
    setAddMemberError(null);
    setAddMemberSuccess(null);
    setAddingMember(true);
    try {
      await api.post(`/groups/${id}/members`, {
        email: newMemberEmail.trim(),
        role: "MEMBER",
      });
      setAddMemberSuccess(`Added ${newMemberEmail}`);
      setNewMemberEmail("");
      await fetchAll();
    } catch (err) {
      console.error("Add member error:", err);
      if (err.response && err.response.data) {
        setAddMemberError(
          typeof err.response.data === "string"
            ? err.response.data
            : err.response.data.message || "Failed to add member"
        );
      } else {
        setAddMemberError("Network or server error.");
      }
    } finally {
      setAddingMember(false);
    }
  };

  // Settlement handlers
  const openSettleDialog = () => {
    setSettleError(null);
    setSettleSuccess(null);
    setFromUser("");
    setToUser("");
    setAmount("");
    setSettleOpen(true);
  };
  const closeSettleDialog = () => setSettleOpen(false);

  const submitSettlement = async () => {
    setSettleError(null);
    setSettleSuccess(null);

    if (!fromUser || !toUser || !amount) {
      setSettleError("Please fill all fields.");
      return;
    }

    setSettleLoading(true);
    try {
      const payload = {
        fromUserId: fromUser,
        toUserId: toUser,
        amount: Number(amount),
      };
      await api.post(`/groups/${id}/settlements`, payload);
      setSettleSuccess("Settlement recorded.");
      await fetchAll();
      setTimeout(() => setSettleOpen(false), 700);
    } catch (err) {
      console.error("Settlement error:", err);
      setSettleError(
        err.response?.data?.message ||
          err.response?.data ||
          "Failed to record settlement"
      );
    } finally {
      setSettleLoading(false);
    }
  };

  if (loading) {
    return (
      <Container maxWidth="lg" sx={{ mt: 8, textAlign: "center" }}>
        <CircularProgress />
      </Container>
    );
  }

  if (error) {
    return (
      <Container maxWidth="lg" sx={{ mt: 6 }}>
        <Alert severity="error">{error}</Alert>
      </Container>
    );
  }

  const prettyMoney = (v) =>
    typeof v === "number" ? v.toFixed(2) : String(v ?? "");

  // helper to safely get display name from various DTO shapes
  const memberDisplayName = (m) =>
    m?.name ?? m?.userName ?? m?.user?.name ?? m?.email ?? `User ${m?.id ?? m?.userId ?? ""}`;

  return (
    <Container maxWidth="lg" sx={{ mt: 6 }}>
      <Box sx={{ display: "flex", justifyContent: "space-between", mb: 3 }}>
        <Typography variant="h4">{group?.name ?? `Group ${id}`}</Typography>

        <Box>
          <Button variant="outlined" sx={{ mr: 1 }} onClick={() => navigate("/groups")}>
            Back
          </Button>
          <Button variant="contained" sx={{ mr: 1 }} onClick={() => navigate(`/groups/${id}/add-expense`)}>
            Add Expense
          </Button>
          <Button variant="contained" onClick={() => navigate(`/groups/${id}/settlements`)}>
            Settlements
          </Button>
        </Box>
      </Box>

      <Grid container spacing={2}>
        {/* Members */}
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Members
              </Typography>
              <Divider sx={{ mb: 1 }} />
              <List dense>
                {group?.members?.length ? (
                  group.members.map((m) => (
                    <ListItem key={m.id ?? m.userId}>
                      <ListItemText primary={memberDisplayName(m)} secondary={m.email ?? ""} />
                    </ListItem>
                  ))
                ) : (
                  <ListItem>
                    <ListItemText primary="No members yet" />
                  </ListItem>
                )}
              </List>

              {/* Add member form (only if owner) */}
              {group?.ownerId && (
                <>
                  <Divider sx={{ my: 2 }} />
                  <Typography variant="subtitle1">Add Member</Typography>
                  {addMemberError && <Alert severity="error" sx={{ my: 1 }}>{addMemberError}</Alert>}
                  {addMemberSuccess && <Alert severity="success" sx={{ my: 1 }}>{addMemberSuccess}</Alert>}
                  <Box component="form" onSubmit={handleAddMember} sx={{ display: "flex", gap: 1, mt: 1 }}>
                    <TextField size="small" label="User email" value={newMemberEmail} onChange={(e) => setNewMemberEmail(e.target.value)} fullWidth />
                    <Button type="submit" variant="contained" disabled={addingMember}>
                      {addingMember ? "Adding…" : "Add"}
                    </Button>
                  </Box>
                </>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Balances */}
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Balances
              </Typography>
              <Divider sx={{ mb: 1 }} />
              {balances.length === 0 ? (
                <Typography>No balances</Typography>
              ) : (
                <List dense>
                  {balances.map((b) => {
                    // Use backend's netBalance if present, fall back to other shapes
                    const bal = Number(b?.netBalance ?? b?.balance ?? b?.amount ?? 0);
                    const name = b?.name ?? b?.userName ?? `User ${b?.userId ?? ""}`;
                    return (
                      <ListItem key={b.userId ?? b.id ?? name}>
                        <ListItemText
                          primary={name}
                          secondary={
                            bal > 0
                              ? `is owed ${prettyMoney(bal)}`
                              : bal < 0
                              ? `owes ${prettyMoney(Math.abs(bal))}`
                              : "settled"
                          }
                        />
                      </ListItem>
                    );
                  })}
                </List>
              )}
              <Box sx={{ mt: 2 }}>
                <Button size="small" onClick={openSettleDialog}>Record settlement</Button>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Expenses */}
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Expenses</Typography>
              <Divider sx={{ mb: 1 }} />
              {expenses.length === 0 ? (
                <Typography>No expenses yet</Typography>
              ) : (
                <List dense>
                  {expenses.map((e) => {
                    const title = e.note ?? e.description ?? `Expense ${e.id}`;
                    const amt = typeof e.amount === "number" ? e.amount : Number(e.amount || 0);
                    const payer = e.payerName ?? e.paidByName ?? `Payer: ${e.payerId ?? ""}`;
                    return (
                      <ListItem key={e.id}>
                        <ListItemText primary={`${title} — ${prettyMoney(amt)}`} secondary={payer} />
                      </ListItem>
                    );
                  })}
                </List>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Settlement dialog */}
      <Dialog open={settleOpen} onClose={closeSettleDialog}>
        <DialogTitle>Record Settlement</DialogTitle>
        <DialogContent sx={{ minWidth: 360, display: "flex", flexDirection: "column", gap: 2 }}>
          {settleError && <Alert severity="error">{settleError}</Alert>}
          {settleSuccess && <Alert severity="success">{settleSuccess}</Alert>}

          <TextField label="From user id" value={fromUser} onChange={(e) => setFromUser(e.target.value)} fullWidth />
          <TextField label="To user id" value={toUser} onChange={(e) => setToUser(e.target.value)} fullWidth />
          <TextField label="Amount" value={amount} onChange={(e) => setAmount(e.target.value)} type="number" inputProps={{ step: "0.01", min: "0" }} fullWidth />
        </DialogContent>
        <DialogActions>
          <Button onClick={closeSettleDialog}>Cancel</Button>
          <Button onClick={submitSettlement} disabled={settleLoading}>{settleLoading ? "Saving…" : "Save"}</Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
}
