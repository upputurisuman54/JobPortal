function JobCard({ job, onViewDetails }) {
  const formatSalary = () => {
    if (job.salaryMin != null && job.salaryMax != null) {
      return `₹${job.salaryMin.toLocaleString()} - ₹${job.salaryMax.toLocaleString()}`;
    }
    if (job.salaryMin != null) {
      return `From ₹${job.salaryMin.toLocaleString()}`;
    }
    return "Not disclosed";
  };

  const formatDeadline = () => {
    if (!job.applicationDeadline) return null;
    const date = new Date(job.applicationDeadline);
    return date.toLocaleDateString("en-IN", {
      day: "numeric",
      month: "short",
      year: "numeric",
    });
  };

  return (
    <div className="job-card">
      <div className="job-card-header">
        <h3>{job.title}</h3>
        <span className="job-card-company">{job.companyName}</span>
      </div>

      <div className="job-card-meta">
        <span>{job.location}</span>
        <span>{job.experienceRequired != null ? `${job.experienceRequired} yrs exp` : "Fresher"}</span>
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

      <p className="job-card-description">
        {job.description && job.description.length > 150
          ? job.description.slice(0, 150) + "..."
          : job.description}
      </p>

      <div className="job-card-footer">
        {formatDeadline() && (
          <span className="job-card-deadline">Apply by {formatDeadline()}</span>
        )}
        <button onClick={() => onViewDetails(job.id)} className="job-card-btn">
          View Details
        </button>
      </div>
    </div>
  );
}

export default JobCard;