import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import Navbar from "../components/Navbar";
import api from "../services/api";

function EntryLink({ url }) {
  return React.createElement(
    "a",
    {
      href: url,
      target: "_blank",
      rel: "noreferrer",
      className: "entry-card-link",
    },
    url
  );
}

function CandidateProfile() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");

  const [education, setEducation] = useState("");
  const [experienceYears, setExperienceYears] = useState("");
  const [skills, setSkills] = useState([]);
  const [skillInput, setSkillInput] = useState("");

  const [linkedinUrl, setLinkedinUrl] = useState("");
  const [githubUrl, setGithubUrl] = useState("");
  const [portfolioUrl, setPortfolioUrl] = useState("");

  const [projects, setProjects] = useState([]);
  const [showProjectForm, setShowProjectForm] = useState(false);
  const [projectTitle, setProjectTitle] = useState("");
  const [projectDescription, setProjectDescription] = useState("");
  const [projectLink, setProjectLink] = useState("");

  const [internships, setInternships] = useState([]);
  const [showInternshipForm, setShowInternshipForm] = useState(false);
  const [internshipCompany, setInternshipCompany] = useState("");
  const [internshipRole, setInternshipRole] = useState("");
  const [internshipDuration, setInternshipDuration] = useState("");
  const [internshipDescription, setInternshipDescription] = useState("");

  const [certificates, setCertificates] = useState([]);
  const [showCertificateForm, setShowCertificateForm] = useState(false);
  const [certTitle, setCertTitle] = useState("");
  const [certIssuer, setCertIssuer] = useState("");
  const [certLink, setCertLink] = useState("");

  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState("");
  const [saveSuccess, setSaveSuccess] = useState(false);

  const loadProfile = async () => {
    setLoading(true);
    setLoadError("");
    try {
      const response = await api.get("/api/candidate/profile");
      const data = response.data;
      setProfile(data);
      setEducation(data.education || "");
      setExperienceYears(
        data.experienceYears != null ? String(data.experienceYears) : ""
      );
      setSkills(data.skills ? Array.from(data.skills) : []);
      setLinkedinUrl(data.linkedinUrl || "");
      setGithubUrl(data.githubUrl || "");
      setPortfolioUrl(data.portfolioUrl || "");
      setProjects(data.projects ? data.projects : []);
      setInternships(data.internships ? data.internships : []);
      setCertificates(data.certificates ? data.certificates : []);
    } catch (err) {
      setLoadError("Could not load your profile. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProfile();
  }, []);

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

  const handleAddProject = (e) => {
    e.preventDefault();
    const trimmedTitle = projectTitle.trim();
    if (!trimmedTitle) return;

    setProjects([
      ...projects,
      {
        title: trimmedTitle,
        description: projectDescription.trim(),
        link: projectLink.trim(),
      },
    ]);
    setProjectTitle("");
    setProjectDescription("");
    setProjectLink("");
    setShowProjectForm(false);
  };

  const handleRemoveProject = (index) => {
    setProjects(projects.filter((_, i) => i !== index));
  };

  const handleAddInternship = (e) => {
    e.preventDefault();
    const trimmedCompany = internshipCompany.trim();
    const trimmedRole = internshipRole.trim();
    if (!trimmedCompany || !trimmedRole) return;

    setInternships([
      ...internships,
      {
        companyName: trimmedCompany,
        role: trimmedRole,
        duration: internshipDuration.trim(),
        description: internshipDescription.trim(),
      },
    ]);
    setInternshipCompany("");
    setInternshipRole("");
    setInternshipDuration("");
    setInternshipDescription("");
    setShowInternshipForm(false);
  };

  const handleRemoveInternship = (index) => {
    setInternships(internships.filter((_, i) => i !== index));
  };

  const handleAddCertificate = (e) => {
    e.preventDefault();
    const trimmedTitle = certTitle.trim();
    const trimmedIssuer = certIssuer.trim();
    if (!trimmedTitle || !trimmedIssuer) return;

    setCertificates([
      ...certificates,
      {
        title: trimmedTitle,
        issuer: trimmedIssuer,
        link: certLink.trim(),
      },
    ]);
    setCertTitle("");
    setCertIssuer("");
    setCertLink("");
    setShowCertificateForm(false);
  };

  const handleRemoveCertificate = (index) => {
    setCertificates(certificates.filter((_, i) => i !== index));
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    setSaveError("");
    setSaveSuccess(false);

    try {
      const payload = {
        education: education.trim() || null,
        experienceYears: experienceYears === "" ? null : Number(experienceYears),
        resumeUrl: profile.resumeUrl || null,
        linkedinUrl: linkedinUrl.trim() || null,
        githubUrl: githubUrl.trim() || null,
        portfolioUrl: portfolioUrl.trim() || null,
        skills: skills,
        projects: projects.map((p) => ({
          title: p.title,
          description: p.description || null,
          link: p.link || null,
        })),
        internships: internships.map((i) => ({
          companyName: i.companyName,
          role: i.role,
          duration: i.duration || null,
          description: i.description || null,
        })),
        certificates: certificates.map((c) => ({
          title: c.title,
          issuer: c.issuer,
          link: c.link || null,
        })),
      };

      const response = await api.put("/api/candidate/profile", payload);
      setProfile(response.data);
      setLinkedinUrl(response.data.linkedinUrl || "");
      setGithubUrl(response.data.githubUrl || "");
      setPortfolioUrl(response.data.portfolioUrl || "");
      setProjects(response.data.projects || []);
      setInternships(response.data.internships || []);
      setCertificates(response.data.certificates || []);
      setSaveSuccess(true);
    } catch (err) {
      const message =
        (err.response && typeof err.response.data === "string" && err.response.data) ||
        (err.response && err.response.data && err.response.data.message) ||
        "Could not save your profile. Please try again.";
      setSaveError(message);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div>
        <Navbar />
        <div className="dashboard-content">
          <p>Loading your profile...</p>
        </div>
      </div>
    );
  }

  if (loadError || !profile) {
    return (
      <div>
        <Navbar />
        <div className="dashboard-content">
          <p className="error-text">{loadError || "Profile not found."}</p>
        </div>
      </div>
    );
  }

  return (
    <div>
      <Navbar />
      <div className="dashboard-content">
        <h1>My Profile</h1>

        <div className="profile-readonly-card">
          <div className="profile-readonly-row">
            <span className="profile-label">Full Name</span>
            <span>{profile.fullName}</span>
          </div>
          <div className="profile-readonly-row">
            <span className="profile-label">Email</span>
            <span>{profile.email}</span>
          </div>
          <div className="profile-readonly-row">
            <span className="profile-label">Phone</span>
            <span>{profile.phone || "Not provided"}</span>
          </div>
          <div className="profile-readonly-row">
            <span className="profile-label">Resume</span>
            {profile.resumeUrl ? (
              <span className="status-badge status-hired">Uploaded</span>
            ) : (
              <span className="status-badge status-rejected">Not uploaded</span>
            )}
            <Link to="/candidate/resume" className="profile-resume-link">
              Manage Resume
            </Link>
          </div>
        </div>

        <form onSubmit={handleSave} className="profile-edit-card">
          <h2>Edit Details</h2>

          {saveError && <div className="auth-error">{saveError}</div>}
          {saveSuccess && (
            <div className="apply-success">Profile updated successfully.</div>
          )}

          <div className="form-group">
            <label htmlFor="education">Education</label>
            <input
              id="education"
              type="text"
              placeholder="e.g. B.Tech in Computer Science"
              value={education}
              onChange={(e) => setEducation(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label htmlFor="experienceYears">Years of Experience</label>
            <input
              id="experienceYears"
              type="number"
              min="0"
              placeholder="e.g. 0"
              value={experienceYears}
              onChange={(e) => setExperienceYears(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label htmlFor="linkedinUrl">LinkedIn URL</label>
            <input
              id="linkedinUrl"
              type="text"
              placeholder="e.g. https://linkedin.com/in/yourname"
              value={linkedinUrl}
              onChange={(e) => setLinkedinUrl(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label htmlFor="githubUrl">GitHub URL</label>
            <input
              id="githubUrl"
              type="text"
              placeholder="e.g. https://github.com/yourname"
              value={githubUrl}
              onChange={(e) => setGithubUrl(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label htmlFor="portfolioUrl">Portfolio URL</label>
            <input
              id="portfolioUrl"
              type="text"
              placeholder="e.g. https://yourportfolio.com"
              value={portfolioUrl}
              onChange={(e) => setPortfolioUrl(e.target.value)}
            />
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

          <h3 className="profile-section-title">Projects</h3>

          {showProjectForm && (
            <div className="entry-add-form">
              <input
                type="text"
                placeholder="Project title"
                value={projectTitle}
                onChange={(e) => setProjectTitle(e.target.value)}
              />
              <textarea
                placeholder="Short description (optional)"
                value={projectDescription}
                onChange={(e) => setProjectDescription(e.target.value)}
              />
              <input
                type="text"
                placeholder="Link (optional, e.g. GitHub URL)"
                value={projectLink}
                onChange={(e) => setProjectLink(e.target.value)}
              />
              <div className="entry-form-actions">
                <button type="button" className="entry-add-btn" onClick={handleAddProject}>
                  Add Project
                </button>
                <button
                  type="button"
                  className="entry-cancel-btn"
                  onClick={() => setShowProjectForm(false)}
                >
                  Cancel
                </button>
              </div>
            </div>
          )}

          <div className="entry-list">
            {projects.length === 0 && (
              <span className="profile-no-entries">No projects added yet.</span>
            )}
            {projects.map((project, index) => (
              <div key={index} className="entry-card">
                <div className="entry-card-header">
                  <strong>{project.title}</strong>
                  <button
                    type="button"
                    className="entry-remove-btn"
                    onClick={() => handleRemoveProject(index)}
                  >
                    Remove
                  </button>
                </div>
                {project.description && (
                  <p className="entry-card-description">{project.description}</p>
                )}
                {project.link && <EntryLink url={project.link} />}
              </div>
            ))}
          </div>

          {!showProjectForm && (
            <div className="entry-add-trigger">
              <button
                type="button"
                className="entry-add-btn"
                onClick={() => setShowProjectForm(true)}
              >
                + Add Project
              </button>
            </div>
          )}

          <h3 className="profile-section-title">Internships</h3>

          {showInternshipForm && (
            <div className="entry-add-form">
              <input
                type="text"
                placeholder="Company name"
                value={internshipCompany}
                onChange={(e) => setInternshipCompany(e.target.value)}
              />
              <input
                type="text"
                placeholder="Role"
                value={internshipRole}
                onChange={(e) => setInternshipRole(e.target.value)}
              />
              <input
                type="text"
                placeholder="Duration (optional, e.g. Jun 2025 – Aug 2025)"
                value={internshipDuration}
                onChange={(e) => setInternshipDuration(e.target.value)}
              />
              <textarea
                placeholder="Short description (optional)"
                value={internshipDescription}
                onChange={(e) => setInternshipDescription(e.target.value)}
              />
              <div className="entry-form-actions">
                <button
                  type="button"
                  className="entry-add-btn"
                  onClick={handleAddInternship}
                >
                  Add Internship
                </button>
                <button
                  type="button"
                  className="entry-cancel-btn"
                  onClick={() => setShowInternshipForm(false)}
                >
                  Cancel
                </button>
              </div>
            </div>
          )}

          <div className="entry-list">
            {internships.length === 0 && (
              <span className="profile-no-entries">No internships added yet.</span>
            )}
            {internships.map((internship, index) => (
              <div key={index} className="entry-card">
                <div className="entry-card-header">
                  <strong>
                    {internship.role} — {internship.companyName}
                  </strong>
                  <button
                    type="button"
                    className="entry-remove-btn"
                    onClick={() => handleRemoveInternship(index)}
                  >
                    Remove
                  </button>
                </div>
                {internship.duration && (
                  <div className="entry-card-meta">{internship.duration}</div>
                )}
                {internship.description && (
                  <p className="entry-card-description">{internship.description}</p>
                )}
              </div>
            ))}
          </div>

          {!showInternshipForm && (
            <div className="entry-add-trigger">
              <button
                type="button"
                className="entry-add-btn"
                onClick={() => setShowInternshipForm(true)}
              >
                + Add Internship
              </button>
            </div>
          )}

          <h3 className="profile-section-title">Certificates</h3>

          {showCertificateForm && (
            <div className="entry-add-form">
              <input
                type="text"
                placeholder="Certificate title"
                value={certTitle}
                onChange={(e) => setCertTitle(e.target.value)}
              />
              <input
                type="text"
                placeholder="Issuer (e.g. Coursera, AWS)"
                value={certIssuer}
                onChange={(e) => setCertIssuer(e.target.value)}
              />
              <input
                type="text"
                placeholder="Link (optional, e.g. credential URL)"
                value={certLink}
                onChange={(e) => setCertLink(e.target.value)}
              />
              <div className="entry-form-actions">
                <button
                  type="button"
                  className="entry-add-btn"
                  onClick={handleAddCertificate}
                >
                  Add Certificate
                </button>
                <button
                  type="button"
                  className="entry-cancel-btn"
                  onClick={() => setShowCertificateForm(false)}
                >
                  Cancel
                </button>
              </div>
            </div>
          )}

          <div className="entry-list">
            {certificates.length === 0 && (
              <span className="profile-no-entries">No certificates added yet.</span>
            )}
            {certificates.map((cert, index) => (
              <div key={index} className="entry-card">
                <div className="entry-card-header">
                  <strong>{cert.title}</strong>
                  <button
                    type="button"
                    className="entry-remove-btn"
                    onClick={() => handleRemoveCertificate(index)}
                  >
                    Remove
                  </button>
                </div>
                <div className="entry-card-meta">{cert.issuer}</div>
                {cert.link && <EntryLink url={cert.link} />}
              </div>
            ))}
          </div>

          {!showCertificateForm && (
            <div className="entry-add-trigger">
              <button
                type="button"
                className="entry-add-btn"
                onClick={() => setShowCertificateForm(true)}
              >
                + Add Certificate
              </button>
            </div>
          )}

          <button type="submit" className="apply-btn" disabled={saving}>
            {saving ? "Saving..." : "Save Changes"}
          </button>
        </form>
      </div>
    </div>
  );
}

export default CandidateProfile;