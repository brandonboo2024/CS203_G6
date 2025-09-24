import React from "react";
import ReactDOM from "react-dom/client";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import App from "./App.jsx";
import Login from "./pages/Login.jsx";
import Dashboard from "./pages/Dashboard.jsx";
import TariffCalc from "./pages/TariffCalc.jsx";
import Profile from "./pages/Profile.jsx";
import Register from "./pages/RegisterPage.jsx";
import History from "./pages/History.jsx";
import Simulation from "./pages/Simulation.jsx";  

import "./index.css";

const router = createBrowserRouter([
  {
    path: "/",
    element: <App />,
    children: [
      { path: "/", element: <Dashboard /> },
      { path: "/tariffs", element: <TariffCalc /> },
      { path: "/profile", element: <Profile /> },
      { path: "/login", element: <Login /> },
      { path: "/register", element: <Register /> }, 
      { path: "/history", element: <History /> },
      { path: "/simulation", element: <Simulation /> }
    ],
  },
]);


ReactDOM.createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <RouterProvider router={router} />
  </React.StrictMode>
);
