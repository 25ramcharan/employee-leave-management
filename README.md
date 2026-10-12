# Employee Leave Management

A Spring Boot 4 application for managing employees, leave balances, and leave requests. The frontend lives in `frontend/`; the Spring Boot API and persistence layer live in `backend/`. The backend packages the frontend into the application so it is served from `/` alongside the JSON API.

## Requirements

- Java 17 or newer
- No external database is required. H2 stores data in `./data/leave-management`.

## Run

Build the React frontend once from the repository root:

```powershell
npm --prefix frontend ci
npm --prefix frontend run build
```

Then start Spring Boot from the repository root on Windows:

```powershell
.\backend\mvnw.cmd -f backend\pom.xml spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080). On first startup, the app creates 25 sample employees (including the original four) and one pending leave request. H2 stores the local database in `./data/leave-management` with username `sa` and no password.

For frontend development with Vite, start Spring Boot as above and run `npm --prefix frontend run dev` in another terminal. Vite serves the React UI and proxies `/api` requests to Spring Boot.

## Deploy to Render

The repository includes a Dockerfile and `render.yaml` for deployment as a Render web service. Create a new Blueprint in Render and select this GitHub repository; Render will build the container and provide the deployed URL after the service becomes healthy. The service listens on Render's assigned port and serves the dashboard at `/`.

The Render configuration uses an in-memory H2 database for the demo, so data is reset when the service restarts or redeploys. The application has no authentication; use sample data only and do not enter real employee information.

Run the backend tests from the repository root with:

```powershell
.\backend\mvnw.cmd -f backend\pom.xml test
```

## Features

- Employee directory with department and available-day balances
- Add employees with unique email addresses
- Submit annual, sick, and personal leave requests
- Count requested weekdays and prevent overlapping active requests
- Approve or reject pending requests with an optional review note
- Deduct leave days from the employee balance only after approval
- Filter requests by status
- Persistent local H2 database and responsive React dashboard

## API

- `GET /api/employees` and `GET /api/employees/{id}`
- `POST /api/employees`
- `GET /api/leave-requests?status=PENDING`
- `GET /api/leave-requests/{id}`
- `POST /api/leave-requests`
- `POST /api/leave-requests/{id}/decision`

The starter project does not include authentication or authorization. The dashboard and review endpoints are intended for local demonstration until access control is added before deployment.