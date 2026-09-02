import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import api from "../services/api";

function statusLabel(status) {
  if (status === "APPLIED") return "Applied";
  if (status === "SHORTLISTED") return "Shortlisted";
  if (status === "HIRED") return "Hired";
  if (status === "REJECTED") return "Rejected";
  return status;
}

function statusClass(status) {
  if (status === "APPLIED") return "status-applied";
  if (status === "SHORTLISTED") return "status-shortlisted";
  if (status === "HIRED") return "status-hired";
  if (status === "REJECTED") return "status-rejected";
  return "";
}

function AutoApply() {
  const [preferredTitle, setPreferredTitle] = useState("");
  const [preferredLocation, setPreferredLocation] = useState("");

  const [running, setRunning] = useState(false);
  const [error, setError] = useState("");
  const [result, setResult] = useState(null);

  const navigate = useNavigate();

  const handleRun = async (e) => {
    e.preventDefault();

    const confirmed = window.confirm(
      "This will immediately submit real applications to every matching job " +
        "(at least 70% skill match, and meeting the experience requirement). " +
        "This cannot be undone. Continue?"
    );
    if (!confirmed) return;

    setRunning(true);
    setError("");
    setResult(null);

    try {
      const payload = {
        preferredTitle: preferredTitle.trim() || null,
        preferredLocation: preferredLocation.trim() || null,
      };

      const response = await api.post("/api/applications/auto-apply", payload);
      setResult(response.data);
    } catch (err) {
      const message =
        (err.response && typeof err.response.data === "string" && err.response.data) ||
        (err.response && err.response.data && err.response.data.message) ||
        "Could not run auto apply. Please try again.";
      setError(message);
    } finally {
      setRunning(false);
    }
  };

  return (
    <div>
      <Navbar />
      <div className="dashboard-content">
        <h1>Auto Apply</h1>

        <div className="auth-error" style={{ marginBottom: "20px" }}>
          Auto Apply immediately submits real job applications on your behalf.
          It applies to jobs matching your filters below where your skills
          overlap at least 70% and you meet the experience requirement. This
          cannot be undone, and you will not be asked to confirm each
          individual application.
        </div>

        <form onSubmit={handleRun} className="profile-edit-card">
          {error && <div className="auth-error">{error}</div>}

          <div className="form-group">
            <label htmlFor="preferredTitle">Preferred Job Title (optional)</label>
            <input
              id="preferredTitle"
              type="text"
              placeholder="e.g. Java Backend Developer"
              value={preferredTitle}
              onChange={(e) => setPreferredTitle(e.target.value)}
              disabled={running}
            />
          </div>

          <div className="form-group">
            <label htmlFor="preferredLocation">Preferred Location (optional)</label>
            <input
              id="preferredLocation"
              type="text"
              placeholder="e.g. Hyderabad"
              value={preferredLocation}
              onChange={(e) => setPreferredLocation(e.target.value)}
              disabled={running}
            />
          </div>

          <button type="submit" className="apply-btn" disabled={running}>
            {running ? "Running Auto Apply..." : "Run Auto Apply"}
          </button>
        </form>

        {result && (
          <div className="profile-edit-card" style={{ marginTop: "20px" }}>
            <h2>Results</h2>
            <p style={{ marginBottom: "16px" }}>
              Considered {result.totalJobsConsidered} matching job
              {result.totalJobsConsidered === 1 ? "" : "s"}, applied to{" "}
              {result.totalApplied}.
            </p>

            {result.totalApplied === 0 && (
              <p>No new applications were submitted. Try broadening your filters.</p>
            )}

            <div className="application-list">
              {result.applications.map((app) => (
                <div key={app.id} className="application-card">
                  <div className="application-card-header">
                    <div>
                      <h3>{app.jobTitle}</h3>
                      <span className="job-card-company">{app.companyName}</span>
                    </div>
                    <span className={`status-badge ${statusClass(app.status)}`}>
                      {statusLabel(app.status)}
                    </span>
                  </div>

                  <div className="application-card-meta">
                    {app.matchScore != null && (
                      <span>AI Match Score: {app.matchScore}%</span>
                    )}
                  </div>

                  <button
                    className="job-card-btn"
                    onClick={() => navigate(`/candidate/jobs/${app.jobId}`)}
                  >
                    View Job
                  </button>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default AutoApply;