import { useState } from "react";
import { Link } from "react-router-dom";
import "../App.css";
import { validateFileUpload } from "../utils/inputValidation";

export default function Profile() {
  const [user, setUser] = useState({
    name: localStorage.getItem("username") || "Guest",
    email: localStorage.getItem("email") || "unknown@email.com",
    avatar: localStorage.getItem("avatar") || "",
  });

  const [isEditing, setIsEditing] = useState(false);
  const [newAvatar, setNewAvatar] = useState(null);
  const [fileError, setFileError] = useState(null);

  const [newName, setNewName] = useState(user.username);
  const [newEmail, setNewEmail] = useState(user.email);

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    setFileError(null);

    if (file) {
      // Validate file before processing
      const validation = validateFileUpload(file);
      if (!validation.isValid) {
        setFileError(validation.errors.join(', '));
        return;
      }

      const reader = new FileReader();
      reader.onloadend = () => {
        setNewAvatar(reader.result); // temp preview before saving
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSave = () => {
    const updatedUser = { ...user, username: newName, email: newEmail, avatar: newAvatar || user.avatar }
    setUser(updatedUser);
    localStorage.setItem("username", newName);
    localStorage.setItem("email", newEmail);
    if (newAvatar) {
      localStorage.setItem("avatar", newAvatar);

    }
    setIsEditing(false);
    setNewAvatar(null);
  };

  return (
    <div className="container">
      <div className="page">
        <h1 style={{ color: "var(--accent)", marginBottom: "2rem" }}>My Profile</h1>

        <div className="profile-layout">
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
                {/* show editable inputs */}
                <label>
                  <strong>Name:</strong>
                  <input
                    type="text"
                    value={newName}
                    onChange={(e) => setNewName(e.target.value)}
                    style={{ marginLeft: "10px", padding: "6px" }}
                  />
                </label>

                <br />

                <label>
                  <strong>Email:</strong>
                  <input
                    type="email"
                    value={newEmail}
                    onChange={(e) => setNewEmail(e.target.value)}
                    style={{ marginLeft: "10px", padding: "6px" }}
                  />
                </label>

                <br />
                <input type="file" accept="image/*" onChange={handleFileChange} />
                {fileError && (
                  <div style={{
                    color: '#d32f2f',
                    fontSize: '0.9rem',
                    marginTop: '0.5rem',
                    padding: '0.5rem',
                    backgroundColor: '#ffebee',
                    border: '1px solid #f44336',
                    borderRadius: '4px'
                  }}>
                    {fileError}
                  </div>
                )}
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
          Need help? <Link to="/support">Open the Support & FAQ hub</Link>
        </p>
      </div>
    </div>
  );
}
