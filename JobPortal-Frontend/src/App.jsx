import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";
import Login from "./pages/Login";
import Register from "./pages/Register";
import ForgotPassword from "./pages/ForgotPassword";
import CandidateDashboard from "./pages/CandidateDashboard";
import JobDetails from "./pages/JobDetails";
import MyApplications from "./pages/MyApplications";
import AutoApply from "./pages/AutoApply";
import ResumeUpload from "./pages/ResumeUpload";
import AiChat from "./pages/AiChat";
import CandidateProfile from "./pages/CandidateProfile";
import RecruiterDashboard from "./pages/RecruiterDashboard";
import PostJob from "./pages/PostJob";
import EditJob from "./pages/EditJob";
import JobApplicants from "./pages/JobApplicants";

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/forgot-password" element={<ForgotPassword />} />
          <Route
            path="/candidate/dashboard"
            element={
              <ProtectedRoute allowedRole="CANDIDATE">
                <CandidateDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/candidate/jobs/:jobId"
            element={
              <ProtectedRoute allowedRole="CANDIDATE">
                <JobDetails />
              </ProtectedRoute>
            }
          />
          <Route
            path="/candidate/applications"
            element={
              <ProtectedRoute allowedRole="CANDIDATE">
                <MyApplications />
              </ProtectedRoute>
            }
          />
          <Route
            path="/candidate/auto-apply"
            element={
              <ProtectedRoute allowedRole="CANDIDATE">
                <AutoApply />
              </ProtectedRoute>
            }
          />
          <Route
            path="/candidate/resume"
            element={
              <ProtectedRoute allowedRole="CANDIDATE">
                <ResumeUpload />
              </ProtectedRoute>
            }
          />
          <Route
            path="/candidate/ai-chat"
            element={
              <ProtectedRoute allowedRole="CANDIDATE">
                <AiChat />
              </ProtectedRoute>
            }
          />
          <Route
            path="/candidate/profile"
            element={
              <ProtectedRoute allowedRole="CANDIDATE">
                <CandidateProfile />
              </ProtectedRoute>
            }
          />
          <Route
            path="/recruiter/dashboard"
            element={
              <ProtectedRoute allowedRole="RECRUITER">
                <RecruiterDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/recruiter/jobs/new"
            element={
              <ProtectedRoute allowedRole="RECRUITER">
                <PostJob />
              </ProtectedRoute>
            }
          />
          <Route
            path="/recruiter/jobs/:jobId/edit"
            element={
              <ProtectedRoute allowedRole="RECRUITER">
                <EditJob />
              </ProtectedRoute>
            }
          />
          <Route
            path="/recruiter/applicants/:jobId"
            element={
              <ProtectedRoute allowedRole="RECRUITER">
                <JobApplicants />
              </ProtectedRoute>
            }
          />
          <Route path="/" element={<Navigate to="/login" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;