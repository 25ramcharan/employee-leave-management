# Employee Leave Management

A Spring Boot 4 application for managing employees, leave balances, and leave requests. The interface is served from `src/main/resources/static` and talks to a JSON API backed by Spring Data JPA.

## Requirements

- Java 17 or newer
- No external database is required. H2 stores data in `./data/leave-management`.

## Run

From the project folder on Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080). On first startup, the app creates 25 sample employees (including the original four) and one pending leave request. The H2 console is available at [http://localhost:8080/h2-console](http://localhost:8080/h2-console) with JDBC URL `jdbc:h2:file:./data/leave-management`, username `sa`, and an empty password.

Run the tests with:

```powershell
.\mvnw.cmd test
```

## Features

- Employee directory with department and available-day balances
- Add employees with unique email addresses
- Submit annual, sick, and personal leave requests
- Count requested weekdays and prevent overlapping active requests
- Approve or reject pending requests with an optional review note
- Deduct leave days from the employee balance only after approval
- Filter requests by status
- Persistent local H2 database and responsive static dashboard

## API

- `GET /api/employees` and `GET /api/employees/{id}`
- `POST /api/employees`
- `GET /api/leave-requests?status=PENDING`
- `GET /api/leave-requests/{id}`
- `POST /api/leave-requests`
- `POST /api/leave-requests/{id}/decision`

The starter project does not include authentication or authorization. The dashboard and review endpoints are intended for local demonstration until access control is added before deployment.