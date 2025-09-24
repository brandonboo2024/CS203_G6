import { useEffect, useState } from "react";

export default function ProtectedPage() {
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchProtected = async () => {
      const token = localStorage.getItem("token"); // saved after login
      if (!token) {
        setError("No token found. Please login.");
        return;
      }

      try {
        const res = await fetch("http://localhost:8080/api/protected", {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        if (res.ok) {
          const data = await res.text(); // backend returns plain text
          setMessage(data);
        } else {
          setError("Access denied: " + res.status);
        }
      } catch (err) {
        setError("Something went wrong: " + err.message);
      }
    };

    fetchProtected();
  }, []);

  return (
    <div className="protected-wrapper">
      <div className="protected-card">
        <h1>Protected Page</h1>
        {message && <p>{message}</p>}
        {error && <p style={{ color: "red" }}>{error}</p>}
      </div>
    </div>
  );
}
