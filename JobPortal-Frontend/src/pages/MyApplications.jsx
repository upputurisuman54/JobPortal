import { useState, useEffect } from "react";
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

function MyApplications() {
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const navigate = useNavigate();

  useEffect(() => {
    const loadApplications = async () => {
      setLoading(true);
      setError("");
      try {
        const response = await api.get("/api/applications/my");
        setApplications(response.data);
      } catch (err) {
        setError("Could not load your applications. Please try again.");
      } finally {
        setLoading(false);
      }
    };
    loadApplications();
  }, []);

  const formatDate = (dateString) => {
    if (!dateString) return "";
    const date = new Date(dateString);
    return date.toLocaleDateString("en-IN", {
      day: "numeric",
      month: "short",
      year: "numeric",
    });
  };

  return (
    <div>
      <Navbar />
      <div className="dashboard-content">
        <h1>My Applications</h1>

        {loading && <p>Loading your applications...</p>}
        {error && <p className="error-text">{error}</p>}

        {!loading && !error && applications.length === 0 && (
          <div>
            <p>You haven't applied to any jobs yet.</p>
            <button onClick={() => navigate("/candidate/dashboard")}>
              Browse Jobs
            </button>
          </div>
        )}

        <div className="application-list">
          {applications.map((app) => (
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
                <span>Applied on {formatDate(app.appliedAt)}</span>
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
    </div>
  );
}

export default MyApplications;