# ACME University — SWE Cloud/Backend Technical Assignment

A backend service for a hypothetical educational institute, **ACME University**.
The domain has two entities — **Lecturer** and **Student** — in a
**many-to-many** relationship: a lecturer can be assigned to many students, and
a student can have many lecturers.

This repository is a **starter skeleton**. It compiles and boots, but the core
logic is left for you to implement. Search the codebase for `TODO` to find every
spot that needs your attention.

## Your task

Implement the following JSON REST APIs:

| # | API | Endpoint (suggested) | Notes |
|---|-----|----------------------|-------|
| 1 | **Create Lecturer** | `POST /api/lecturers` | Create a lecturer if it doesn't already exist, otherwise return an appropriate error. |
| 2 | **Get Lecturer** | `GET /api/lecturers/{lecturerId}` | Return the lecturer's name, surname, and assigned students. |
| 3 | **Add Student** | `POST /api/lecturers/{lecturerId}/students` | Create the student (if not present) and assign to an existing lecturer; error if the lecturer doesn't exist. |
| 4 | **Get Student** | `GET /api/students/{studentId}` | Return the student's name, surname, and assigned lecturers. |

The endpoint paths above are only suggestions. You are expected to design the
REST resources yourself — define the request mappings, HTTP methods, status
codes, and request/response binding in the (currently empty) controllers.

### Requirements

- Both entities have a unique identifier, a name, and a surname.
- All APIs consume and produce **JSON**.
- **Validation**: `name` and `surname` must not be null or blank and must
  consist only of alphanumeric characters. Enforce this with Bean Validation
  before persisting.
- **Integration tests** covering:
  - Lecturer creation and retrieval.
  - Student creation (existing and non-existing) and lecturer assignment.
  - Error cases (e.g. assigning a student to a non-existent lecturer).
- **Resiliency**: protect the database from excessive requests, leveraging an
  open-source library as needed.

## Where to implement what

| Area | File(s) | What's left for you |
|------|---------|---------------------|
| Entity mapping | `domain/Lecturer.java`, `domain/Student.java` | Minimal `@Entity`/`@Id` is provided so the app boots; add `@Column` constraints and map the many-to-many association (join table, owning side, sync helpers). |
| Validation | `web/dto/Create*Request.java` | Add the validation constraints. |
| Validation errors | `exception/GlobalExceptionHandler.java` | Map validation failures to HTTP 400. |
| Business logic | `service/LecturerService.java`, `service/StudentService.java` | Implement create/get/assign logic. |
| Controllers | `web/LecturerController.java`, `web/StudentController.java` | Write the request mappings and annotated handler methods; delegate to the services and choose status codes. |
| Resiliency | `config/RateLimitingInterceptor.java`, `pom.xml` | Add a rate-limiting library and implement throttling. |
| Tests | `src/test/java/...` | Implement the stubbed integration tests. |
| Containerization | `Dockerfile` | Complete the image build. |

## Tech stack (chosen for the skeleton)

- **Java 21**, **Spring Boot 3.3**, **Maven**
- **Spring Web**, **Spring Data JPA**, **Bean Validation**
- **PostgreSQL** at runtime (via `docker compose`), **H2** in-memory for tests

You are free to swap the build tool or database — these are just defaults.

## Running

### Tests (no external dependencies — uses in-memory H2)

```bash
./mvnw test
```

### The application (PostgreSQL via Docker)

```bash
docker compose up -d db          # start PostgreSQL
./mvnw spring-boot:run           # run the app against it
```

Once the `Dockerfile` is complete you can bring up the whole stack with:

```bash
docker compose up --build
```

## Submitting

When you're done, push your solution to a **public GitHub or GitLab
repository** and share the link so we can review it.
