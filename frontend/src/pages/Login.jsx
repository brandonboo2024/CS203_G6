export default function Login() {
  return (
    <div>
      <h1>Login</h1>
      <form style={{ display: "grid", gap: "1rem", maxWidth: "300px" }}>
        <input type="email" placeholder="Email" />
        <input type="password" placeholder="Password" />
        <button>Login</button>
      </form>
    </div>
  );
}
