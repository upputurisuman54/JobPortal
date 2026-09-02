import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
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

function getAvailableActions(status) {
  if (status === "APPLIED") {
    return [
      { label: "Shortlist", newStatus: "SHORTLISTED" },
      { label: "Reject", newStatus: "REJECTED" },
    ];
  }
  if (status === "SHORTLISTED") {
    return [
      { label: "Hire", newStatus: "HIRED" },
      { label: "Reject", newStatus: "REJECTED" },
    ];
  }
  return [];
}

function JobApplicants() {
  const { jobId } = useParams();
  const navigate = useNavigate();

  const [job, setJob] = useState(null);
  const [applicants, setApplicants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [updatingId, setUpdatingId] = useState(null);
  const [updateError, setUpdateError] = useState("");

  const [downloadingId, setDownloadingId] = useState(null);
  const [downloadErrorId, setDownloadErrorId] = useState(null);

  const loadData = async () => {
    setLoading(true);
    setError("");
    try {
      const [jobResponse, applicantsResponse] = await Promise.all([
        api.get(`/api/jobs/${jobId}`),
        api.get(`/api/applications/job/${jobId}`),
      ]);
      setJob(jobResponse.data);
      setApplicants(applicantsResponse.data);
    } catch (err) {
      const message =
        (err.response && typeof err.response.data === "string" && err.response.data) ||
        "Could not load applicants for this job.";
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [jobId]);

  const handleStatusUpdate = async (applicationId, newStatus) => {
    setUpdatingId(applicationId);
    setUpdateError("");

    try {
      const response = await api.put(`/api/applications/${applicationId}/status`, {
        status: newStatus,
      });

      setApplicants((prev) =>
        prev.map((app) => (app.id === applicationId ? response.data : app))
      );
    } catch (err) {
      const message =
        (err.response && typeof err.response.data === "string" && err.response.data) ||
        "Could not update status. Please try again.";
      setUpdateError(message);
    } finally {
      setUpdatingId(null);
    }
  };

  const handleViewResume = async (applicationId) => {
    setDownloadingId(applicationId);
    setDownloadErrorId(null);

    try {
      const response = await api.get(`/api/applications/${applicationId}/resume`, {
        responseType: "blob",
      });

      const url = window.URL.createObjectURL(
        new Blob([response.data], { type: "application/pdf" })
      );
      window.open(url, "_blank", "noopener,noreferrer");
      setTimeout(() => window.URL.revokeObjectURL(url), 60000);
    } catch (err) {
      setDownloadErrorId(applicationId);
    } finally {
      setDownloadingId(null);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return "";
    const date = new Date(dateString);
    return date.toLocaleDateString("en-IN", {
      day: "numeric",
      month: "short",
      year: "numeric",
    });
  };

  if (loading) {
    return (
      <div>
        <Navbar />
        <div className="dashboard-content">
          <p>Loading applicants...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div>
        <Navbar />
        <div className="dashboard-content">
          <p className="error-text">{error}</p>
          <button onClick={() => navigate("/recruiter/dashboard")}>
            Back to My Jobs
          </button>
        </div>
      </div>
    );
  }

  return (
    <div>
      <Navbar />
      <div className="dashboard-content">
        <button className="back-link" onClick={() => navigate("/recruiter/dashboard")}>
          Back to My Jobs
        </button>

        <h1>Applicants{job ? ` for ${job.title}` : ""}</h1>

        {updateError && <p className="error-text">{updateError}</p>}

        {applicants.length === 0 && (
          <p>No one has applied to this job yet.</p>
        )}

        <div className="application-list">
          {applicants.map((app) => (
            <div key={app.id} className="application-card">
              <div className="application-card-header">
                <div>
                  <h3>{app.candidateName}</h3>
                  <span className="job-card-company">{app.candidateEmail}</span>
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

              {downloadErrorId === app.id && (
                <p className="error-text">
                  Could not open resume. This candidate may not have uploaded one.
                </p>
              )}

              <div className="applicant-actions">
                <button
                  className="job-card-btn"
                  onClick={() => handleViewResume(app.id)}
                  disabled={downloadingId === app.id}
                >
                  {downloadingId === app.id ? "Opening..." : "View Resume"}
                </button>

                {getAvailableActions(app.status).map((action) => (
                  <button
                    key={action.newStatus}
                    className={
                      action.newStatus === "REJECTED"
                        ? "job-card-btn danger-btn"
                        : "job-card-btn"
                    }
                    onClick={() => handleStatusUpdate(app.id, action.newStatus)}
                    disabled={updatingId === app.id}
                  >
                    {updatingId === app.id ? "Updating..." : action.label}
                  </button>
                ))}

                {getAvailableActions(app.status).length === 0 && (
                  <span className="applicant-final-status">
                    Final status - no further action available
                  </span>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

export default JobApplicants;