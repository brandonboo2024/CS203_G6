import { useState, useEffect } from "react";
import "../App.css";

export default function Profile() {
  const [user, setUser] = useState({
    name: localStorage.getItem("username") || "Guest",
    email: localStorage.getItem("email") || "unknown@email.com",
    avatar: localStorage.getItem("avatar") || "",
  });

  const [isEditing, setIsEditing] = useState(false);
  const [newAvatar, setNewAvatar] = useState(null);

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setNewAvatar(reader.result); // temp preview before saving
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSave = () => {
    if (newAvatar) {
      localStorage.setItem("avatar", newAvatar);
      setUser({ ...user, avatar: newAvatar });
    }
    setIsEditing(false);
    setNewAvatar(null);
  };

  return (
    <div className="container">
      <div className="page">
        <h1 style={{ color: "var(--accent)", marginBottom: "2rem" }}>My Profile</h1>

        <div style={{ display: "flex", gap: "2rem", alignItems: "center" }}>
          {/* Avatar */}
          <div
            style={{
              width: "120px",
              height: "120px",
              borderRadius: "50%",
              background: "#444",
              overflow: "hidden",
              flexShrink: 0,
            }}
          >
            <img
              src={newAvatar || user.avatar || ""}
              alt="Avatar"
              style={{
                width: "100%",
                height: "100%",
                objectFit: "cover",
                display: (newAvatar || user.avatar) ? "block" : "none",
              }}
            />
            {!(newAvatar || user.avatar) && (
              <span
                style={{
                  color: "#aaa",
                  fontSize: "0.8rem",
                  display: "block",
                  textAlign: "center",
                  marginTop: "45px",
                }}
              >
                No Photo
              </span>
            )}
          </div>

          {/* User Info */}
          <div className="card" style={{ flex: 1 }}>
            <p><strong>Name:</strong> {user.name}</p>
            <p><strong>Email:</strong> {user.email}</p>

            {isEditing && (
              <>
                <input type="file" accept="image/*" onChange={handleFileChange} />
                <button onClick={handleSave} style={{ marginTop: "1rem" }}>Save</button>
              </>
            )}
          </div>
        </div>

        {/* Edit button */}
        {!isEditing ? (
          <div style={{ marginTop: "2rem", textAlign: "center" }}>
            <button onClick={() => setIsEditing(true)}>Edit Profile</button>
          </div>
        ) : (
          <div style={{ marginTop: "2rem", textAlign: "center" }}>
            <button onClick={() => { setIsEditing(false); setNewAvatar(null); }}>
              Cancel
            </button>
          </div>
        )}

        {/* Support */}
        <p style={{ marginTop: "2rem", color: "var(--text-muted)" }}>
          Need Help? <a href="/support">Support/FAQ</a>
        </p>
      </div>
    </div>
  );
}
