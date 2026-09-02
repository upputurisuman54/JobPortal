import { useContext } from "react";
import { useNavigate, Link } from "react-router-dom";
import { AuthContext } from "../context/AuthContext";

function Navbar() {
  const { user, logout } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <nav className="navbar">
      <div className="navbar-left">
        <div className="navbar-brand">JobPortal</div>
        {user && user.role === "CANDIDATE" && (
          <div className="navbar-links">
            <Link to="/candidate/dashboard">Browse Jobs</Link>
            <Link to="/candidate/applications">My Applications</Link>
            <Link to="/candidate/auto-apply">Auto Apply</Link>
            <Link to="/candidate/resume">Resume</Link>
            <Link to="/candidate/ai-chat">AI Assistant</Link>
            <Link to="/candidate/profile">Profile</Link>
          </div>
        )}
        {user && user.role === "RECRUITER" && (
          <div className="navbar-links">
            <Link to="/recruiter/dashboard">My Jobs</Link>
            <Link to="/recruiter/jobs/new">Post Job</Link>
          </div>
        )}
      </div>
      <div className="navbar-right">
        {user && (
          <>
            <span className="navbar-user">
              {user.fullName} ({user.role === "CANDIDATE" ? "Candidate" : "Recruiter"})
            </span>
            <button onClick={handleLogout} className="navbar-logout">
              Logout
            </button>
          </>
        )}
      </div>
    </nav>
  );
}

export default Navbar;