import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import api from "../services/api";

function RecruiterDashboard() {
  const [profile, setProfile] = useState(null);
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [deletingId, setDeletingId] = useState(null);
  const [deleteError, setDeleteError] = useState("");

  const navigate = useNavigate();

  const loadData = async () => {
    setLoading(true);
    setError("");
    try {
      const profileResponse = await api.get("/api/recruiter/profile");
      const myProfile = profileResponse.data;
      setProfile(myProfile);

      const jobsResponse = await api.get("/api/jobs");
      const allJobs = jobsResponse.data;

      const myJobs = allJobs.filter(
        (job) =>
          job.recruiterId === myProfile.id ||
          job.companyName === myProfile.companyName
      );

      setJobs(myJobs);
    } catch (err) {
      setError("Could not load your jobs. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleDelete = async (jobId) => {
    if (!window.confirm("Are you sure you want to delete this job posting?")) {
      return;
    }

    setDeletingId(jobId);
    setDeleteError("");

    try {
      await api.delete(`/api/jobs/${jobId}`);
      setJobs((prev) => prev.filter((job) => job.id !== jobId));
    } catch (err) {
      const message =
        (err.response && typeof err.response.data === "string" && err.response.data) ||
        "Could not delete this job. Please try again.";
      setDeleteError(message);
    } finally {
      setDeletingId(null);
    }
  };

  const formatSalary = (job) => {
    if (job.salaryMin != null && job.salaryMax != null) {
      return `₹${job.salaryMin.toLocaleString()} - ₹${job.salaryMax.toLocaleString()}`;
    }
    return "Not disclosed";
  };

  return (
    <div>
      <Navbar />
      <div className="dashboard-content">
        <div className="recruiter-header-row">
          <h1>My Job Postings</h1>
          <button className="apply-btn" onClick={() => navigate("/recruiter/jobs/new")}>
            + Post New Job
          </button>
        </div>

        {profile && (
          <p className="recruiter-company-line">
            Posting as <strong>{profile.companyName || profile.fullName}</strong>
          </p>
        )}

        {loading && <p>Loading your job postings...</p>}
        {error && <p className="error-text">{error}</p>}
        {deleteError && <p className="error-text">{deleteError}</p>}

        {!loading && !error && jobs.length === 0 && (
          <p>You haven't posted any jobs yet.</p>
        )}

        <div className="job-list">
          {jobs.map((job) => (
            <div key={job.id} className="job-card">
              <div className="job-card-header">
                <h3>{job.title}</h3>
              </div>

              <div className="job-card-meta">
                <span>{job.location}</span>
                <span>
                  {job.experienceRequired != null
                    ? `${job.experienceRequired} yrs exp`
                    : "Fresher"}
                </span>
                <span>{formatSalary(job)}</span>
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

              <div className="job-card-footer">
                <button
                  className="job-card-btn"
                  onClick={() => navigate(`/recruiter/applicants/${job.id}`)}
                >
                  View Applicants
                </button>
                <button
                  className="job-card-btn"
                  onClick={() => navigate(`/recruiter/jobs/${job.id}/edit`)}
                >
                  Edit
                </button>
                <button
                  className="job-card-btn danger-btn"
                  onClick={() => handleDelete(job.id)}
                  disabled={deletingId === job.id}
                >
                  {deletingId === job.id ? "Deleting..." : "Delete"}
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

export default RecruiterDashboard;