import React, { useContext } from "react";
import {
  BrowserRouter,
  Routes,
  Route,
  Navigate,
  Link,
  useNavigate,
} from "react-router-dom";
import { AuthContext } from "./context/AuthContext.jsx";

import Login from "./pages/Login.jsx";
import Register from "./pages/Register.jsx";
import Groups from "./pages/Groups.jsx";
import CreateGroup from "./pages/CreateGroup.jsx";
import GroupDetails from "./pages/GroupDetails.jsx";
import AddExpense from "./pages/AddExpense.jsx";
import Settlements from "./pages/Settlements.jsx";

import { AppBar, Toolbar, Typography, Button, Box } from "@mui/material";

/**
 * ProtectedRoute: render children only when authenticated, otherwise redirect to /login
 */
function ProtectedRoute({ children }) {
  const { isAuthenticated } = useContext(AuthContext);
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  return children;
}

/**
 * TopNav component
 */
function TopNavInner() {
  const { isAuthenticated, logout, user } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <AppBar position="static">
      <Toolbar sx={{ display: "flex", justifyContent: "space-between" }}>
        {/* Left side: logo + nav links */}
        <Box sx={{ display: "flex", alignItems: "center", gap: 2 }}>
          <Typography
            variant="h6"
            component={Link}
            to="/"
            sx={{ color: "inherit", textDecoration: "none" }}
          >
            Splitwise-dupe
          </Typography>

          <Button color="inherit" component={Link} to="/groups">
            Groups
          </Button>
        </Box>

        {/* Right side: user info + auth buttons */}
        <Box sx={{ display: "flex", alignItems: "center", gap: 2 }}>
          {isAuthenticated && user && (
            <Typography variant="body1" sx={{ color: "inherit" }}>
              {`Hi, ${user.name}`}
            </Typography>
          )}

          {!isAuthenticated ? (
            <>
              <Button color="inherit" component={Link} to="/login">
                Login
              </Button>
              <Button color="inherit" component={Link} to="/register">
                Register
              </Button>
            </>
          ) : (
            <Button color="inherit" onClick={handleLogout}>
              Logout
            </Button>
          )}
        </Box>
      </Toolbar>
    </AppBar>
  );
}

/**
 * TopNav wrapper
 */
function TopNav() {
  return <TopNavInner />;
}

export default function App() {
  return (
    <BrowserRouter>
      <TopNav />

      <main className="app-root">
        <div className="app-content">
          <div className="app-container">
            <Routes>
              <Route path="/" element={<Navigate to="/groups" replace />} />

              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />

              {/* Protected routes */}
              <Route
                path="/groups"
                element={
                  <ProtectedRoute>
                    <Groups />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/groups/create"
                element={
                  <ProtectedRoute>
                    <CreateGroup />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/groups/:id"
                element={
                  <ProtectedRoute>
                    <GroupDetails />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/groups/:id/add-expense"
                element={
                  <ProtectedRoute>
                    <AddExpense />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/groups/:id/settlements"
                element={
                  <ProtectedRoute>
                    <Settlements />
                  </ProtectedRoute>
                }
              />

              {/* Catch-all */}
              <Route path="*" element={<Navigate to="/groups" replace />} />
            </Routes>
          </div>
        </div>
      </main>
    </BrowserRouter>
  );
}
