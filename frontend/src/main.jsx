import React from "react";
import ReactDOM from "react-dom/client";
import { createBrowserRouter, RouterProvider, Navigate } from "react-router-dom";
import App from "./App.jsx";
import Login from "./pages/Login.jsx";
import Dashboard from "./pages/Dashboard.jsx";
import TariffCalc from "./pages/TariffCalc.jsx";
import Profile from "./pages/Profile.jsx";
import Register from "./pages/RegisterPage.jsx";
import History from "./pages/History.jsx";
import Simulation from "./pages/Simulation.jsx";  

import "./index.css";

// Inline ProtectedRoute
function ProtectedRoute({ children }) {
  const token = localStorage.getItem("token");
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  return children;
}

const router = createBrowserRouter([
  {
    path: "/",
    element: <App />,
    children: [
      // Redirect root "/" to /login
      { index: true, element: <Navigate to="login" replace /> },

      { path: "login", element: <Login /> },
      { path: "register", element: <Register /> },

      {
        path: "dashboard",
        element: (
          <ProtectedRoute>
            <Dashboard />
          </ProtectedRoute>
        ),
      },
      {
        path: "tariffs",
        element: (
          <ProtectedRoute>
            <TariffCalc />
          </ProtectedRoute>
        ),
      },
      {
        path: "profile",
        element: (
          <ProtectedRoute>
            <Profile />
          </ProtectedRoute>
        ),
      },
      {
        path: "history",
        element: (
          <ProtectedRoute>
            <History />
          </ProtectedRoute>
        ),
      },
      {
        path: "simulation",
        element: (
          <ProtectedRoute>
            <Simulation />
          </ProtectedRoute>
        ),
      },
    ],
  },
]);

ReactDOM.createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <RouterProvider router={router} />
  </React.StrictMode>
);
