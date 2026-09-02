import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import api from "../services/api";

function PostJob() {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [location, setLocation] = useState("");
  const [salaryMin, setSalaryMin] = useState("");
  const [salaryMax, setSalaryMax] = useState("");
  const [experienceRequired, setExperienceRequired] = useState("");
  const [applicationDeadline, setApplicationDeadline] = useState("");
  const [skills, setSkills] = useState([]);
  const [skillInput, setSkillInput] = useState("");

  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  const navigate = useNavigate();

  const handleAddSkill = (e) => {
    e.preventDefault();
    const trimmed = skillInput.trim();
    if (!trimmed) return;

    const alreadyExists = skills.some(
      (s) => s.toLowerCase() === trimmed.toLowerCase()
    );
    if (!alreadyExists) {
      setSkills([...skills, trimmed]);
    }
    setSkillInput("");
  };

  const handleRemoveSkill = (skillToRemove) => {
    setSkills(skills.filter((s) => s !== skillToRemove));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (!title.trim() || !description.trim() || !location.trim()) {
      setError("Title, description, and location are required.");
      return;
    }

    setSubmitting(true);

    try {
      const payload = {
        title: title.trim(),
        description: description.trim(),
        location: location.trim(),
        salaryMin: salaryMin === "" ? null : Number(salaryMin),
        salaryMax: salaryMax === "" ? null : Number(salaryMax),
        experienceRequired: experienceRequired === "" ? null : Number(experienceRequired),
        applicationDeadline: applicationDeadline || null,
        skills: skills,
      };

      await api.post("/api/jobs", payload);
      navigate("/recruiter/dashboard");
    } catch (err) {
      const message =
        (err.response && typeof err.response.data === "string" && err.response.data) ||
        "Could not post this job. Please check the details and try again.";
      setError(message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <Navbar />
      <div className="dashboard-content">
        <h1>Post a New Job</h1>

        <form onSubmit={handleSubmit} className="profile-edit-card">
          {error && <div className="auth-error">{error}</div>}

          <div className="form-group">
            <label htmlFor="title">Job Title</label>
            <input
              id="title"
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g. Java Backend Developer - Fresher"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="description">Description</label>
            <textarea
              id="description"
              rows="5"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Describe the role, responsibilities, and requirements"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="location">Location</label>
            <input
              id="location"
              type="text"
              value={location}
              onChange={(e) => setLocation(e.target.value)}
              placeholder="e.g. Hyderabad"
              required
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="salaryMin">Salary Min (₹)</label>
              <input
                id="salaryMin"
                type="number"
                min="0"
                value={salaryMin}
                onChange={(e) => setSalaryMin(e.target.value)}
                placeholder="e.g. 300000"
              />
            </div>

            <div className="form-group">
              <label htmlFor="salaryMax">Salary Max (₹)</label>
              <input
                id="salaryMax"
                type="number"
                min="0"
                value={salaryMax}
                onChange={(e) => setSalaryMax(e.target.value)}
                placeholder="e.g. 450000"
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="experienceRequired">Experience Required (yrs)</label>
              <input
                id="experienceRequired"
                type="number"
                min="0"
                value={experienceRequired}
                onChange={(e) => setExperienceRequired(e.target.value)}
                placeholder="e.g. 0"
              />
            </div>

            <div className="form-group">
              <label htmlFor="applicationDeadline">Application Deadline</label>
              <input
                id="applicationDeadline"
                type="date"
                value={applicationDeadline}
                onChange={(e) => setApplicationDeadline(e.target.value)}
              />
            </div>
          </div>

          <div className="form-group">
            <label>Skills</label>
            <div className="skill-input-row">
              <input
                type="text"
                placeholder="Type a skill and press Add"
                value={skillInput}
                onChange={(e) => setSkillInput(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === "Enter") {
                    handleAddSkill(e);
                  }
                }}
              />
              <button type="button" onClick={handleAddSkill}>
                Add
              </button>
            </div>

            <div className="skill-tag-list">
              {skills.length === 0 && (
                <span className="profile-no-skills">No skills added yet.</span>
              )}
              {skills.map((skill) => (
                <span key={skill} className="skill-tag removable-skill">
                  {skill}
                  <button
                    type="button"
                    className="skill-remove-btn"
                    onClick={() => handleRemoveSkill(skill)}
                  >
                    ×
                  </button>
                </span>
              ))}
            </div>
          </div>

          <button type="submit" className="apply-btn" disabled={submitting}>
            {submitting ? "Posting..." : "Post Job"}
          </button>
        </form>
      </div>
    </div>
  );
}

export default PostJob;