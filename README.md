# JobPortal — AI-Powered Job Portal

A full-stack job portal connecting candidates and recruiters, with an integrated AI assistant (Google Gemini) for resume review, cover letter generation, and automated job matching.

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen?logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-Vite-61DAFB?logo=react&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)
![JWT](https://img.shields.io/badge/Auth-JWT-000000?logo=jsonwebtokens&logoColor=white)
![Gemini](https://img.shields.io/badge/AI-Google%20Gemini-8E75B2?logo=googlegemini&logoColor=white)

---

## Screenshots

### Candidate Experience

| Login | Browse Jobs |
|---|---|
| ![Login](https://github.com/upputurisuman54/JobPortal/blob/master/Screenshots/Screenshot%202026-09-22%20105853.png?raw=true) | ![Candidate Dashboard](https://github.com/upputurisuman54/JobPortal/blob/master/Screenshots/Screenshot%202026-09-22%20105940.png?raw=true) |

| My Applications | Auto Apply |
|---|---|
| ![My Applications](https://github.com/upputurisuman54/JobPortal/blob/master/Screenshots/Screenshot%202026-09-22%20110008.png?raw=true
) | ![Auto Apply](https://github.com/upputurisuman54/JobPortal/blob/master/Screenshots/Screenshot%202026-09-22%20110031.png?raw=true
) |

| Resume Management | AI Assistant |
|---|---|
| ![Resume](screenshots/resume.png) | ![AI Assistant](screenshots/ai-assistant.png) |

**Candidate Profile**

![Candidate Profile](screenshots/candidate-profile.png)

### Recruiter Experience

| Recruiter Dashboard | Post a New Job |
|---|---|
| ![Recruiter Dashboard](screenshots/recruiter-dashboard.png) | ![Post Job](screenshots/post-job.png) |

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [API Overview](#api-overview)
- [Project Structure](#project-structure)
- [Roadmap](#roadmap)
- [Author](#author)

---

## Overview

JobPortal is a complete recruitment platform built from the ground up with a Spring Boot REST API backend and a React (Vite) frontend. It supports two user roles — **Candidates** and **Recruiters** — each with a dedicated workflow, and integrates Google's Gemini AI to help candidates improve their resumes, generate cover letters, and even auto-apply to matching jobs.

This project was built end-to-end as a solo full-stack effort, covering authentication, secure file handling, AI integration, transactional email, and a fully responsive UI.

## Features

### For Candidates
- Browse and search jobs by title, location, and skill (case- and whitespace-insensitive matching)
- View job details and apply with an uploaded resume
- Track all applications with live status updates (Applied → Shortlisted → Hired/Rejected)
- Upload/download resume (PDF), with text extraction for AI context
- Build a rich profile: education, experience, skills, LinkedIn/GitHub/Portfolio links, plus Projects, Internships, and Certificates sections
- **AI Assistant** (Gemini-powered): resume review, ATS-friendliness check, and full cover letter generation — with clean, properly formatted PDF export including clickable email/link annotations
- **Auto Apply**: automatically applies to jobs matching a configurable skill-overlap threshold and experience requirement, with a confirmation safeguard before submitting
- Forgot-password flow via emailed one-time OTP code

### For Recruiters
- Post, edit, and delete job listings
- Dashboard of all posted jobs with applicant counts
- Review applicants per job, download resumes securely, and update application status
- Automatic candidate-facing email notifications on status changes (shortlisted, hired, rejected)

### Platform-wide
- JWT-based authentication with role-based access control (Spring Security)
- Responsive design across desktop, tablet, and mobile breakpoints
- Transactional email via Gmail SMTP (application confirmations, status updates, password reset codes)

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React (Vite), React Router, Axios |
| Backend | Spring Boot 4.1.0, Java 17 |
| Database | MySQL 8, Spring Data JPA / Hibernate |
| Auth | Spring Security, JWT (jjwt), BCrypt |
| AI | Google Gemini API |
| PDF Generation | Apache PDFBox (custom resume/cover-letter renderer with clickable link annotations) |
| Email | Spring Mail (Gmail SMTP) |
| Build Tools | Maven, npm/Vite |

## Architecture

```
┌─────────────────┐        REST/JSON + JWT        ┌──────────────────────┐
│  React (Vite)    │  ───────────────────────────▶ │   Spring Boot API     │
│  Frontend        │  ◀─────────────────────────── │   (Java 17)           │
└─────────────────┘                                └──────────┬───────────┘
                                                                │
                                     ┌──────────────────────────┼──────────────────────────┐
                                     ▼                          ▼                          ▼
                              ┌────────────┐          ┌──────────────────┐        ┌──────────────┐
                              │   MySQL    │          │   Gemini AI API   │        │  Gmail SMTP   │
                              │  Database  │          │  (resume/chat)    │        │  (email svc)  │
                              └────────────┘          └──────────────────┘        └──────────────┘
```

Deployment target: Dockerized backend and frontend on a single AWS EC2 instance, with Nginx as a reverse proxy, MySQL on AWS RDS, and HTTPS via a custom domain (in progress).

## Getting Started

### Prerequisites
- Java 17
- Node.js 18+ and npm
- MySQL 8
- Maven (or use the included `mvnw` wrapper)

### Backend Setup

```bash
cd jobportal
```

Set the required environment variables (see Environment Variables section below), then run:

```bash
mvn spring-boot:run
```

The API will start on `http://localhost:8080`.

### Frontend Setup

```bash
cd jobportal-frontend
npm install
```

Create a `.env.development` file in this folder:

```
VITE_API_BASE_URL=http://localhost:8080
```

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

```
JobPortal/
├── jobportal/                  # Spring Boot backend
│   ├── src/main/java/com/example/JobPortal/
│   │   ├── controller/         # REST endpoints
│   │   ├── service/            # Business logic
│   │   ├── entity/             # JPA entities
│   │   ├── repository/         # Spring Data repositories
│   │   ├── dto/                # Request/response objects
│   │   └── security/           # JWT + Spring Security config
│   └── src/main/resources/
│       ├── application.properties
│       ├── application-dev.properties
│       └── application-prod.properties
└── jobportal-frontend/         # React (Vite) frontend
    └── src/
        ├── pages/               # Route-level page components
        ├── components/          # Shared UI components
        ├── context/             # Auth context
        └── services/            # Axios API client
```

## Roadmap

- [x] Core candidate and recruiter flows
- [x] AI-powered resume review, cover letters, and auto-apply
- [x] Responsive design pass
- [x] Deployment code hardening (externalized config, dev/prod profiles)
- [ ] Dockerized deployment to AWS EC2 with Nginx and RDS
- [ ] Custom domain + HTTPS
- [ ] CI/CD via GitHub Actions

## Author

**Suman Upputuri**
Java Full Stack Developer

- GitHub: [@upputurisuman54](https://github.com/upputurisuman54)
- Portfolio: [react-portfolio-nine-sooty.vercel.app](https://react-portfolio-nine-sooty.vercel.app/)
- LinkedIn: [linkedin.com/in/upputuri-suman-a0730726b](https://linkedin.com/in/upputuri-suman-a0730726b)

---

*This project was built as a hands-on learning exercise covering full-stack development, AI integration, and DevOps deployment practices.*
