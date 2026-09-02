import { useState, useEffect } from "react";
import Navbar from "../components/Navbar";
import api from "../services/api";

function ResumeUpload() {
  const [profile, setProfile] = useState(null);
  const [loadingProfile, setLoadingProfile] = useState(true);
  const [profileLoadError, setProfileLoadError] = useState("");

  const [selectedFile, setSelectedFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [uploadError, setUploadError] = useState("");
  const [uploadResult, setUploadResult] = useState(null);

  const [downloading, setDownloading] = useState(false);
  const [downloadError, setDownloadError] = useState("");

  const loadProfile = async () => {
    setLoadingProfile(true);
    setProfileLoadError("");
    try {
      const response = await api.get("/api/candidate/profile");
      setProfile(response.data);
    } catch (err) {
      if (err.response && err.response.status === 404) {
        setProfile(null);
      } else {
        setProfileLoadError(
          "Could not load your profile right now. Please refresh and try again."
        );
      }
    } finally {
      setLoadingProfile(false);
    }
  };

  useEffect(() => {
    loadProfile();
  }, []);

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    setUploadError("");
    setUploadResult(null);

    if (file && file.type !== "application/pdf") {
      setUploadError("Only PDF files are allowed.");
      setSelectedFile(null);
      return;
    }

    setSelectedFile(file);
  };

  const handleUpload = async () => {
    if (!selectedFile) {
      setUploadError("Please choose a PDF file first.");
      return;
    }

    setUploading(true);
    setUploadError("");
    setUploadResult(null);

    try {
      const formData = new FormData();
      formData.append("file", selectedFile);

      const response = await api.post("/api/candidate/resume/upload", formData, {
        headers: { "Content-Type": undefined },
      });

      setUploadResult(response.data);
      setSelectedFile(null);
      loadProfile();
    } catch (err) {
      const message =
        (err.response && typeof err.response.data === "string" && err.response.data) ||
        (err.response && err.response.data && err.response.data.message) ||
        "Upload failed. Please try again.";
      setUploadError(message);
    } finally {
      setUploading(false);
    }
  };

  const handleDownload = async () => {
    setDownloading(true);
    setDownloadError("");

    try {
      const response = await api.get("/api/candidate/resume/download", {
        responseType: "blob",
      });

      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", "resume.pdf");
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      setDownloadError("Could not download resume. Please try again.");
    } finally {
      setDownloading(false);
    }
  };

  const hasResume = profile && profile.resumeUrl;

  return (
    <div>
      <Navbar />
      <div className="dashboard-content">
        <h1>Resume</h1>

        {loadingProfile && <p>Loading your profile...</p>}

        {!loadingProfile && profileLoadError && (
          <p className="error-text">{profileLoadError}</p>
        )}

        {!loadingProfile && !profileLoadError && (
          <div className="resume-status-card">
            {hasResume ? (
              <>
                <p className="resume-status-line">
                  <span className="status-badge status-hired">Resume Uploaded</span>
                </p>
                <button
                  className="job-card-btn"
                  onClick={handleDownload}
                  disabled={downloading}
                >
                  {downloading ? "Downloading..." : "Download Current Resume"}
                </button>
                {downloadError && <p className="error-text">{downloadError}</p>}
              </>
            ) : (
              <p className="resume-status-line">
                <span className="status-badge status-rejected">No Resume Uploaded</span>
              </p>
            )}
          </div>
        )}

        <div className="resume-upload-card">
          <h2>{hasResume ? "Replace Resume" : "Upload Resume"}</h2>
          <p className="resume-upload-hint">PDF files only.</p>

          <input
            type="file"
            accept="application/pdf"
            onChange={handleFileChange}
            className="resume-file-input"
            disabled={uploading}
          />

          {uploadError && <div className="auth-error">{uploadError}</div>}

          {uploadResult && (
            <div className="apply-success">
              {uploadResult.message || "Resume uploaded successfully."}
            </div>
          )}

          <button
            className="apply-btn"
            onClick={handleUpload}
            disabled={uploading || !selectedFile}
          >
            {uploading ? "Uploading..." : "Upload Resume"}
          </button>

          {uploadResult && uploadResult.extractedText && (
            <div className="extracted-text-preview">
              <h2>Extracted Text Preview</h2>
              <p>{uploadResult.extractedText.slice(0, 500)}
                {uploadResult.extractedText.length > 500 ? "..." : ""}
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default ResumeUpload;