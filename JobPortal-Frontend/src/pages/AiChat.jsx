import { useState, useEffect } from "react";
import Navbar from "../components/Navbar";
import api from "../services/api";

const QUICK_PROMPTS = [
  { label: "Review my resume", message: "Please review my resume and suggest improvements." },
  { label: "ATS check", message: "Check how well my resume matches common ATS keyword scans and suggest fixes." },
  { label: "Generate cover letter", message: "Write a cover letter for me based on my resume." },
];

function renderFormattedText(text) {
  const parts = text.split(/(\*\*[^*]+\*\*)/g);
  return parts.map((part, i) => {
    if (part.startsWith("**") && part.endsWith("**") && part.length > 4) {
      return <strong key={i}>{part.slice(2, -2)}</strong>;
    }
    return <span key={i}>{part}</span>;
  });
}

function AiChat() {
  const [applications, setApplications] = useState([]);
  const [selectedApplicationId, setSelectedApplicationId] = useState("");

  const [inputMessage, setInputMessage] = useState("");
  const [messages, setMessages] = useState([]);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState("");

  const [downloadingIndex, setDownloadingIndex] = useState(null);

  useEffect(() => {
    const loadApplications = async () => {
      try {
        const response = await api.get("/api/applications/my");
        setApplications(response.data);
      } catch (err) {
        setApplications([]);
      }
    };
    loadApplications();
  }, []);

  const sendMessage = async (messageText) => {
    const trimmed = messageText.trim();
    if (!trimmed) return;

    setError("");
    setSending(true);

    const userMessage = { role: "user", text: trimmed };
    setMessages((prev) => [...prev, userMessage]);
    setInputMessage("");

    try {
      const payload = { message: trimmed };
      if (selectedApplicationId) {
        payload.applicationId = Number(selectedApplicationId);
      }

      const response = await api.post("/api/ai-chat", payload);
      const aiMessage = { role: "ai", text: response.data.reply };
      setMessages((prev) => [...prev, aiMessage]);
    } catch (err) {
      const message =
        (err.response && typeof err.response.data === "string" && err.response.data) ||
        (err.response && err.response.data && err.response.data.message) ||
        "Something went wrong. Please try again.";
      setError(message);
    } finally {
      setSending(false);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    sendMessage(inputMessage);
  };

  const handleQuickPrompt = (message) => {
    sendMessage(message);
  };

  const handleDownloadPdf = async (text, index) => {
    setDownloadingIndex(index);
    try {
      const response = await api.post(
        "/api/ai-chat/download-pdf",
        { title: "", content: text },
        { responseType: "blob" }
      );

      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", "AI_Response.pdf");
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      setError("Could not generate PDF. Please try again.");
    } finally {
      setDownloadingIndex(null);
    }
  };

  return (
    <div>
      <Navbar />
      <div className="dashboard-content">
        <h1>AI Assistant</h1>

        <div className="ai-chat-controls">
          <label className="ai-chat-app-label">
            Link to an application (optional):
          </label>
          <select
            value={selectedApplicationId}
            onChange={(e) => setSelectedApplicationId(e.target.value)}
          >
            <option value="">None</option>
            {applications.map((app) => (
              <option key={app.id} value={app.id}>
                {app.jobTitle} - {app.companyName}
              </option>
            ))}
          </select>
        </div>

        <div className="quick-prompts">
          {QUICK_PROMPTS.map((prompt) => (
            <button
              key={prompt.label}
              className="quick-prompt-btn"
              onClick={() => handleQuickPrompt(prompt.message)}
              disabled={sending}
            >
              {prompt.label}
            </button>
          ))}
        </div>

        <div className="chat-window">
          {messages.length === 0 && (
            <p className="chat-empty-hint">
              Ask about your resume, request a cover letter, or use a quick prompt above.
            </p>
          )}

          {messages.map((msg, index) => (
            <div
              key={index}
              className={msg.role === "user" ? "chat-bubble-user" : "chat-bubble-ai"}
            >
              <p>{renderFormattedText(msg.text)}</p>
              {msg.role === "ai" && (
                <button
                  className="chat-download-btn"
                  onClick={() => handleDownloadPdf(msg.text, index)}
                  disabled={downloadingIndex === index}
                >
                  {downloadingIndex === index ? "Generating..." : "Download as PDF"}
                </button>
              )}
            </div>
          ))}

          {sending && <p className="chat-typing-hint">AI is typing...</p>}
        </div>

        {error && <div className="auth-error">{error}</div>}

        <form onSubmit={handleSubmit} className="chat-input-bar">
          <input
            type="text"
            placeholder="Type your message..."
            value={inputMessage}
            onChange={(e) => setInputMessage(e.target.value)}
            disabled={sending}
          />
          <button type="submit" disabled={sending || !inputMessage.trim()}>
            Send
          </button>
        </form>
      </div>
    </div>
  );
}

export default AiChat;