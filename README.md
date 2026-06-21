# ACME University — SWE Cloud/Backend Technical Assignment

A backend service for a hypothetical educational institute, **ACME University**.
The domain has two entities — **Lecturer** and **Student** — in a
**many-to-many** relationship: a lecturer can be assigned to many students, and
a student can have many lecturers.

This repository is a completed solution.

## API

JSON REST APIs:

| # | API | Endpoint                                       | Success | Errors | Notes |
|---|-----|------------------------------------------------|---------|--------|-------|
| 1 | **Create Lecturer** | `POST /api/v1/lecturers`                       | `201 Created` | `409` if `lecturerId` exists, `400` on invalid body | Create a lecturer if it doesn't already exist, otherwise return an appropriate error. |
| 2 | **Get Lecturer** | `GET /api/v1/lecturers/{lecturerId}`           | `200 OK` | `404` if not found | Return the lecturer's name, surname, and assigned students. |
| 3 | **Add Student** | `POST /api/v1/lecturers/{lecturerId}/students` | `201 Created` | `404` if lecturer missing, `400` on invalid body | Create the student (if not present) and assign to an existing lecturer; error if the lecturer doesn't exist. |
| 4 | **Get Student** | `GET /api/v1/students/{studentId}`             | `200 OK` | `404` if not found | Return the student's name, surname, and assigned lecturers. |

## Tech stack (chosen for the skeleton)

- **Java 21**, **Spring Boot 3.3**, **Maven**
- **Spring Web**, **Spring Data JPA**, **Bean Validation**
- **Flyway** (Schema migration)
- **Bucket4j** & **Caffeine** (rate limiting)
- **PostgreSQL** at runtime (via `docker compose`), **H2** in-memory for tests

You are free to swap the build tool or database — these are just defaults.

## Running

### Tests (no external dependencies — uses in-memory H2)

```bash
./mvnw test
```

The suite has two layers. **Unit tests** (Mockito, no Spring/DB). **Integration tests** (`@SpringBootTest` +
MockMvc on in-memory H2). The whole suite runs on in-memory H2 and needs
no Docker.

### The application (PostgreSQL via Docker)

```bash
docker compose up -d db          # start PostgreSQL
./mvnw spring-boot:run           # run the app against it
```

You can bring up the whole stack with:

```bash
docker compose up --build
```

## Build setup change

The Maven wrapper originally pointed at a private Adobe Artifactory server, which
isn't reachable outside that network. Repointed to the public Apache Maven. The
only file changed for this is `.mvn/wrapper/maven-wrapper.properties`:

```properties
# before
distributionUrl=https://artifactory.corp.adobe.com/artifactory/maven-sc-dev/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip
```
```properties
# after
distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip
```

**Spring Boot 3.3.5 → 3.3.11.** The parent version was bumped to pick up the fix
for [CVE-2025-22235](https://spring.io/security/cve-2025-22235/). The app isn't
actually exposed to that CVE but the upgrade clears the dependency scanner.

## Design choices

**Rate limiting.** Bucket4j applies a per-client (by IP)
token bucket to all `/api/**` requests. Limits are configurable via
`rate-limit.capacity` and `rate-limit.refill-period` (defaults: 50 requests per
second per client). Exhausted clients get `429 Too Many Requests` with a
`Retry-After` header, successful responses carry `X-Rate-Limit-Remaining`. The
Caffeine cache (`rate-limit.max-clients`, `rate-limit.client-ttl`) is used so a flood of distinct
IPs cannot grow memory without limit.

**Transactions.** Service methods are `@Transactional` (read-only for gets), so
each operation is atomic and lazy associations are initialised within the transaction.

**Schema / migrations.** The database schema is owned by **Flyway**. Hibernate runs
in `ddl-auto: validate`, so it only checks that the entities match the
migration-built schema and never alters it. The integration tests run on in-memory H2 with Flyway
disabled, so they need no external database.

**Concurrency / thread safety.** The service beans are stateless and the rate
limiter keeps per-client buckets in a `ConcurrentHashMap`, so there is no shared
mutable state to corrupt.
