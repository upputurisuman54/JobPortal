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

function JobDetails() {
  const { jobId } = useParams();
  const navigate = useNavigate();

  const [job, setJob] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [existingApplication, setExistingApplication] = useState(null);
  const [checkingApplication, setCheckingApplication] = useState(true);

  const [applying, setApplying] = useState(false);
  const [applyError, setApplyError] = useState("");
  const [applySuccess, setApplySuccess] = useState(false);

  useEffect(() => {
    const loadJob = async () => {
      setLoading(true);
      setError("");
      try {
        const response = await api.get(`/api/jobs/${jobId}`);
        setJob(response.data);
      } catch (err) {
        setError("Could not load this job. It may no longer exist.");
      } finally {
        setLoading(false);
      }
    };
    loadJob();
  }, [jobId]);

  useEffect(() => {
    const checkExistingApplication = async () => {
      setCheckingApplication(true);
      try {
        const response = await api.get("/api/applications/my");
        const match = response.data.find(
          (app) => String(app.jobId) === String(jobId)
        );
        setExistingApplication(match || null);
      } catch (err) {
        setExistingApplication(null);
      } finally {
        setCheckingApplication(false);
      }
    };
    checkExistingApplication();
  }, [jobId]);

  const formatSalary = () => {
    if (!job) return "";
    if (job.salaryMin != null && job.salaryMax != null) {
      return `₹${job.salaryMin.toLocaleString()} - ₹${job.salaryMax.toLocaleString()}`;
    }
    if (job.salaryMin != null) {
      return `From ₹${job.salaryMin.toLocaleString()}`;
    }
    return "Not disclosed";
  };

  const formatDeadline = () => {
    if (!job || !job.applicationDeadline) return null;
    const date = new Date(job.applicationDeadline);
    return date.toLocaleDateString("en-IN", {
      day: "numeric",
      month: "short",
      year: "numeric",
    });
  };

  const handleApply = async () => {
    setApplying(true);
    setApplyError("");
    setApplySuccess(false);

    try {
      const profileResponse = await api.get("/api/candidate/profile");
      const resumeUrl = profileResponse.data.resumeUrl;

      if (!resumeUrl) {
        setApplyError(
          "You need to upload a resume before applying. Go to your profile to upload one."
        );
        setApplying(false);
        return;
      }

      const response = await api.post("/api/applications", {
        jobId: job.id,
        resumeUrl: resumeUrl,
      });

      setExistingApplication(response.data);
      setApplySuccess(true);
    } catch (err) {
      const message =
        (err.response && typeof err.response.data === "string" && err.response.data) ||
        (err.response && err.response.data && err.response.data.message) ||
        "Something went wrong while applying. Please try again.";
      setApplyError(message);
    } finally {
      setApplying(false);
    }
  };

  if (loading) {
    return (
      <div>
        <Navbar />
        <div className="dashboard-content">
          <p>Loading job details...</p>
        </div>
      </div>
    );
  }

  if (error || !job) {
    return (
      <div>
        <Navbar />
        <div className="dashboard-content">
          <p className="error-text">{error || "Job not found."}</p>
          <button onClick={() => navigate("/candidate/dashboard")}>
            Back to Browse Jobs
          </button>
        </div>
      </div>
    );
  }

  return (
    <div>
      <Navbar />
      <div className="dashboard-content">
        <button className="back-link" onClick={() => navigate("/candidate/dashboard")}>
          ← Back to Browse Jobs
        </button>

        <div className="job-details-card">
          <div className="job-details-header">
            <h1>{job.title}</h1>
            <span className="job-card-company">{job.companyName}</span>
          </div>

          <div className="job-card-meta">
            <span>{job.location}</span>
            <span>
              {job.experienceRequired != null
                ? `${job.experienceRequired} yrs exp`
                : "Fresher"}
            </span>
            <span>{formatSalary()}</span>
          </div>

          {job.skills && job.skills.length > 0 && (
            <div className="job-card-skills">
              {job.skills.map((skill) => (
                <span key={skill} className="skill-tag">
                  {skill}
                </span>
              ))}
            </div>
          )}

          {formatDeadline() && (
            <p className="job-card-deadline">Apply by {formatDeadline()}</p>
          )}

          <h2 className="job-details-section-title">Job Description</h2>
          <p className="job-details-description">{job.description}</p>

          {applyError && <div className="auth-error">{applyError}</div>}

          {checkingApplication && (
            <p className="checking-application-hint">Checking application status...</p>
          )}

          {!checkingApplication && existingApplication && (
            <div className="already-applied-box">
              <span className="already-applied-label">
                You already applied to this job
              </span>
              <span className={`status-badge status-${existingApplication.status.toLowerCase()}`}>
                {statusLabel(existingApplication.status)}
              </span>
              <button
                className="job-card-btn"
                onClick={() => navigate("/candidate/applications")}
              >
                View My Applications
              </button>
            </div>
          )}

          {!checkingApplication && !existingApplication && applySuccess && (
            <div className="apply-success">
              Application submitted successfully!
              <button
                className="job-card-btn"
                onClick={() => navigate("/candidate/applications")}
              >
                View My Applications
              </button>
            </div>
          )}

          {!checkingApplication && !existingApplication && !applySuccess && (
            <button
              className="apply-btn"
              onClick={handleApply}
              disabled={applying}
            >
              {applying ? "Applying..." : "Apply Now"}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}

export default JobDetails;