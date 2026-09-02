import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import JobCard from "../components/JobCard";
import api from "../services/api";

function CandidateDashboard() {
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [titleFilter, setTitleFilter] = useState("");
  const [locationFilter, setLocationFilter] = useState("");
  const [skillFilter, setSkillFilter] = useState("");
  const [experienceFilter, setExperienceFilter] = useState("ALL");

  const navigate = useNavigate();

  const loadAllJobs = async () => {
    setLoading(true);
    setError("");
    try {
      const response = await api.get("/api/jobs");
      setJobs(response.data);
    } catch (err) {
      setError("Could not load jobs. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAllJobs();
  }, []);

  const handleSearch = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    try {
      const params = {};
      if (titleFilter.trim()) params.title = titleFilter.trim();
      if (locationFilter.trim()) params.location = locationFilter.trim();
      if (skillFilter.trim()) params.skill = skillFilter.trim();

      const response = await api.get("/api/jobs/search", { params });
      setJobs(response.data);
    } catch (err) {
      setError("Search failed. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const handleClearFilters = () => {
    setTitleFilter("");
    setLocationFilter("");
    setSkillFilter("");
    setExperienceFilter("ALL");
    loadAllJobs();
  };

  const handleViewDetails = (jobId) => {
    navigate(`/candidate/jobs/${jobId}`);
  };

  const matchesExperience = (job) => {
    if (experienceFilter === "ALL") return true;

    const exp = job.experienceRequired == null ? 0 : job.experienceRequired;

    if (experienceFilter === "FRESHER") return exp === 0;
    if (experienceFilter === "ONE_TO_TWO") return exp >= 1 && exp <= 2;
    if (experienceFilter === "THREE_PLUS") return exp >= 3;

    return true;
  };

  const displayedJobs = jobs.filter(matchesExperience);

  return (
    <div>
      <Navbar />
      <div className="dashboard-content">
        <h1>Browse Jobs</h1>

        <form onSubmit={handleSearch} className="search-bar">
          <input
            type="text"
            placeholder="Job title"
            value={titleFilter}
            onChange={(e) => setTitleFilter(e.target.value)}
            disabled={loading}
          />
          <input
            type="text"
            placeholder="Location"
            value={locationFilter}
            onChange={(e) => setLocationFilter(e.target.value)}
            disabled={loading}
          />
          <input
            type="text"
            placeholder="Skill"
            value={skillFilter}
            onChange={(e) => setSkillFilter(e.target.value)}
            disabled={loading}
          />
          <select
            value={experienceFilter}
            onChange={(e) => setExperienceFilter(e.target.value)}
            disabled={loading}
          >
            <option value="ALL">Any experience</option>
            <option value="FRESHER">Fresher (0 yrs)</option>
            <option value="ONE_TO_TWO">1-2 yrs</option>
            <option value="THREE_PLUS">3+ yrs</option>
          </select>
          <button type="submit" disabled={loading}>
            {loading ? "Searching..." : "Search"}
          </button>
          <button type="button" onClick={handleClearFilters} disabled={loading}>
            Clear
          </button>
        </form>

        {loading && <p>Loading jobs...</p>}
        {error && <p className="error-text">{error}</p>}

        {!loading && !error && displayedJobs.length === 0 && (
          <p>No jobs found matching your search.</p>
        )}

        <div className="job-list">
          {displayedJobs.map((job) => (
            <JobCard key={job.id} job={job} onViewDetails={handleViewDetails} />
          ))}
        </div>
      </div>
    </div>
  );
}

export default CandidateDashboard;