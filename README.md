# TECHNO-X — Backend Service (Spring Boot 3 + Java 21)

Production-ready, highly concurrent backend service for the **TECHNO-X** College Event Management Platform at Techno Group of Institutions (TIHS / TGI, Lucknow).

---

## 🚀 Tech Stack

- **Java**: 21 LTS
- **Framework**: Spring Boot 3.3.4
- **Security**: Spring Security 6 with stateless JWT Bearer tokens
- **Persistence**: Spring Data JPA / Hibernate 6 with HikariCP
- **Databases**:
  - **Local/Demo**: In-memory H2 with MySQL mode (instant zero-configuration startup)
  - **Dev/Prod**: MySQL 8.0+ with Flyway versioned migrations (V1–V12)
- **API Documentation**: OpenAPI 3 / SpringDoc Swagger UI
- **Concurrency**: Pessimistic Locking (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) on capacity check & reservation + optimistic `@Version` fallback
- **Build Tool**: Maven Wrapper (`mvnw.cmd`)

---

## 🔐 Demo Accounts & Credentials

| Role | Email | Password | Linked Profile / Notes |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin@technox.test` | `AdminPassword123!` | Super Administrator |
| **Faculty Coordinator** | `faculty@technox.test` | `FacultyPassword123!` | Dr. Vikram Malhotra (`FAC-CS-042`) |
| **Management Committee** | `committee@technox.test` | `CommitteePassword123!` | Priya Verma (Abhivyakti Cultural Council) |
| **Student** | `student@technox.test` | `StudentPassword123!` | Rohan Sharma (`TGI2025BCA768`), BCA 3rd Year |

---

## 🏃 Getting Started & Running

### 1. Instant Startup (Zero Setup — In-Memory H2 Profile)
No database configuration needed! Boots up with seeded roles, categories, clubs, demo users, and events:
```powershell
cd C:\Users\singh\.gemini\antigravity\scratch\techno-x-backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```
- **Server Port**: `8081` (avoids conflict if an external Tomcat service is running on 8080)
- **Swagger UI**: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs)
- **H2 Console**: [http://localhost:8081/h2-console](http://localhost:8081/h2-console) (JDBC URL: `jdbc:h2:mem:technox_local`)

### 2. MySQL Dev Profile (Flyway Migrations)
With MySQL 8 running locally on port 3306:
```powershell
# Set environment variables if needed
$env:DB_URL = "jdbc:mysql://localhost:3306/technox_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "YOUR_MYSQL_PASSWORD"

.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

### 3. Running Unit Tests
Executes the comprehensive JUnit 5 test suite:
```powershell
.\mvnw.cmd test
```

---

## 📦 Architecture & Key Modules

```
com.technox
├── common/
│   ├── config/          # OpenApiConfig, DataInitializer, SecurityConfig
│   ├── dto/             # ApiResponse<T>, PagedResponse<T>
│   ├── entity/          # BaseEntity (created_at, updated_at)
│   └── exception/       # GlobalExceptionHandler, BusinessException, ErrorCode
├── auth/                # Login, RefreshToken, PasswordReset, AuthService, AuthController
├── user/                # User entity, Role, Permission, UserRepository, UserDto
├── student/             # Student profile (studentId, course, year, qrCode), StudentController
├── faculty/             # Faculty profile (facultyCode, designation, dept), FacultyController
├── committee/           # CommitteeMember profile (committeeCode, roleTitle), CommitteeController
├── category/            # EventCategory (Technical, Cultural, Sports, etc.), CategoryController
├── club/                # Student clubs & societies, ClubController
├── event/               # Event lifecycle (DRAFT -> PENDING -> PUBLISHED -> CANCELLED)
├── registration/        # Pessimistic locking registration, Pass & QR generation
├── waitlist/            # Queue position tracking, automated promotion on cancellation
├── attendance/          # QR scan attendance marking, audit corrections
├── notification/        # In-app notifications & campus broadcasts
├── achievement/         # Badges and student achievements
├── analytics/           # High-level metrics, charts data, category distributions
├── report/              # CSV exports for registrations and attendance records
├── audit/               # Audit log trail for sensitive events
└── security/            # JwtTokenProvider, JwtAuthenticationFilter, CustomUserDetailsService
```

---

## 🛡️ Concurrency & High Load Protection

- **Race Condition Prevention**: `EventRepository.findByIdWithLock(Long id)` executes `SELECT ... FOR UPDATE` (Pessimistic Write Lock) during student registration.
- **Atomic Capacity Increment**: Only one transaction can evaluate and increment `registered_count` at a time.
- **Overcapacity Auto-Waitlist**: If `registered_count >= capacity`, the student is automatically enqueued into `waitlist_entries` with their exact queue position.
- **Auto-Promotion on Cancellation**: When a registered attendee cancels, the waitlist queue automatically promotes the top student, creates their confirmed registration, and issues their digital pass.

---

## 📡 API Endpoints Summary

### Authentication (`/api/auth`)
- `POST /api/auth/login` — Login with email + password, returns JWT and user profile
- `POST /api/auth/refresh` — Issue new access token using refresh token
- `POST /api/auth/logout` — Invalidate refresh token session
- `POST /api/auth/forgot-password` — Send OTP / reset instructions
- `POST /api/auth/reset-password` — Verify OTP and update password
- `GET /api/auth/me` — Current authenticated user profile

### Events (`/api/events`)
- `GET /api/events` — Paged list of published events with category/status filters
- `GET /api/events/{id}` — Event details by ID
- `GET /api/events/slug/{slug}` — Event details by URL-friendly slug
- `POST /api/events` — Create new event proposal (`FACULTY`, `COMMITTEE`, `ADMIN`)
- `PUT /api/events/{id}` — Update event details
- `PATCH /api/events/{id}/approve` — Approve pending event (`FACULTY`, `ADMIN`)
- `PATCH /api/events/{id}/publish` — Publish approved event
- `PATCH /api/events/{id}/cancel` — Cancel event and notify registrants

### Registrations (`/api/registrations`)
- `POST /api/registrations` — Register student for an event (concurrent-safe)
- `POST /api/registrations/manual` — Manual registration by faculty or admin
- `GET /api/registrations/student/{studentId}` — Registrations for a specific student
- `GET /api/registrations/event/{eventId}` — Registrations list for an event
- `DELETE /api/registrations/{id}` — Cancel registration (triggers auto-promotion)

### Attendance (`/api/attendance`)
- `POST /api/attendance/mark` — Mark attendance via scanned QR pass token
- `POST /api/attendance/corrections` — Audit-logged attendance status correction
- `GET /api/attendance/event/{eventId}` — Event attendance roster

### Waitlist (`/api/waitlist`)
- `GET /api/waitlist/event/{eventId}` — View event waitlist queue
- `DELETE /api/waitlist/{id}` — Remove entry from waitlist

### Reports & Analytics
- `GET /api/analytics` — Platform metrics & charts data
- `GET /api/reports/registrations/csv` — Export registrations to CSV
- `GET /api/reports/attendance/csv` — Export attendance records to CSV
- `GET /api/audit-logs` — Administrative audit logs with filter parameters
