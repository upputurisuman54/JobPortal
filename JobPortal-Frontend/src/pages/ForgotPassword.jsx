import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../services/api";

function ForgotPassword() {
  const [step, setStep] = useState(1);

  const [email, setEmail] = useState("");
  const [requesting, setRequesting] = useState(false);
  const [requestError, setRequestError] = useState("");
  const [requestMessage, setRequestMessage] = useState("");

  const [otp, setOtp] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [resetting, setResetting] = useState(false);
  const [resetError, setResetError] = useState("");

  const navigate = useNavigate();

  const handleRequestOtp = async (e) => {
    e.preventDefault();
    setRequestError("");
    setRequestMessage("");
    setRequesting(true);

    try {
      const response = await api.post("/api/auth/forgot-password", { email });
      setRequestMessage(
        (response.data && response.data.message) ||
          "If an account exists with that email, a reset code has been sent."
      );
      setStep(2);
    } catch (err) {
      const message =
        (err.response && typeof err.response.data === "string" && err.response.data) ||
        (err.response && err.response.data && err.response.data.message) ||
        "Something went wrong. Please try again.";
      setRequestError(message);
    } finally {
      setRequesting(false);
    }
  };

  const handleResetPassword = async (e) => {
    e.preventDefault();
    setResetError("");

    if (newPassword !== confirmPassword) {
      setResetError("Passwords do not match.");
      return;
    }

    setResetting(true);

    try {
      await api.post("/api/auth/reset-password", {
        email,
        otp,
        newPassword,
      });
      navigate("/login");
    } catch (err) {
      const message =
        (err.response && typeof err.response.data === "string" && err.response.data) ||
        (err.response && err.response.data && err.response.data.message) ||
        "Could not reset your password. Please try again.";
      setResetError(message);
    } finally {
      setResetting(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1>{step === 1 ? "Forgot Password" : "Reset Password"}</h1>

        {step === 1 && (
          <>
            {requestError && <div className="auth-error">{requestError}</div>}

            <form onSubmit={handleRequestOtp}>
              <div className="form-group">
                <label htmlFor="email">Email</label>
                <input
                  id="email"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>

              <button type="submit" disabled={requesting}>
                {requesting ? "Sending Code..." : "Send Reset Code"}
              </button>
            </form>
          </>
        )}

        {step === 2 && (
          <>
            {requestMessage && (
              <div className="apply-success">{requestMessage}</div>
            )}
            {resetError && <div className="auth-error">{resetError}</div>}

            <form onSubmit={handleResetPassword}>
              <div className="form-group">
                <label htmlFor="otp">6-Digit Code</label>
                <input
                  id="otp"
                  type="text"
                  inputMode="numeric"
                  maxLength={6}
                  value={otp}
                  onChange={(e) => setOtp(e.target.value.replace(/\D/g, ""))}
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="newPassword">New Password</label>
                <input
                  id="newPassword"
                  type="password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="confirmPassword">Confirm New Password</label>
                <input
                  id="confirmPassword"
                  type="password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  required
                />
              </div>

              <button type="submit" disabled={resetting}>
                {resetting ? "Resetting..." : "Reset Password"}
              </button>
            </form>

            <p className="auth-switch">
              Didn't get a code?{" "}
              <button
                type="button"
                className="back-link"
                onClick={() => {
                  setStep(1);
                  setRequestMessage("");
                  setOtp("");
                }}
              >
                Try again
              </button>
            </p>
          </>
        )}

        <p className="auth-switch">
          Remembered your password? <Link to="/login">Log In</Link>
        </p>
      </div>
    </div>
  );
}

export default ForgotPassword;