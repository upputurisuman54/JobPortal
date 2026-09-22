
Then run:

```bash
npm run dev
```

The app will be available at `http://localhost:5173`.

## Environment Variables

The backend reads the following from the environment (never committed to source control):

| Variable | Required | Description |
|---|---|---|
| `DB_PASSWORD` | Yes | MySQL database password |
| `GEMINI_API_KEY` | Yes | Google Gemini API key |
| `MAIL_USERNAME` | Yes | Gmail address used to send notifications |
| `MAIL_PASSWORD` | Yes | Gmail app password (not your regular password) |
| `DB_URL` | No | Defaults to `jdbc:mysql://localhost:3306/jobportal_db` |
| `DB_USERNAME` | No | Defaults to `root` |
| `SERVER_PORT` | No | Defaults to `8080` |
| `ALLOWED_ORIGINS` | No | Comma-separated CORS origins, defaults to `http://localhost:5173` |
| `RESUME_UPLOAD_DIR` | No | Defaults to `uploads/resumes` |
| `SPRING_PROFILES_ACTIVE` | No | `dev` or `prod` — controls logging verbosity and schema validation strictness |

Example (PowerShell):

```powershell
$env:DB_PASSWORD="your_password"
$env:GEMINI_API_KEY="your_gemini_key"
$env:MAIL_USERNAME="your_email@gmail.com"
$env:MAIL_PASSWORD="your_gmail_app_password"
$env:SPRING_PROFILES_ACTIVE="dev"
mvn spring-boot:run
```

## API Overview

All endpoints are prefixed with `/api`. A selection of key routes:

| Method | Endpoint | Description |
|---|---|---|
| POST | `/auth/register`, `/auth/login` | Account creation and login |
| POST | `/auth/forgot-password`, `/auth/reset-password` | OTP-based password reset |
| GET | `/jobs`, `/jobs/search` | Browse and search job listings |
| POST | `/applications` | Apply to a job |
| POST | `/applications/auto-apply` | Trigger AI-driven auto-apply |
| GET/PUT | `/candidate/profile` | View/update candidate profile |
| POST | `/candidate/resume/upload` | Upload resume (PDF) |
| POST | `/ai-chat` | Chat with the Gemini-powered assistant |
| POST | `/ai-chat/download-pdf` | Export an AI response as a formatted PDF |
| GET/PUT | `/applications/job/{jobId}`, `/applications/{id}/status` | Recruiter: view applicants, update status |

## Project Structure
