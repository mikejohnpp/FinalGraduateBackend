# OpenCode notes for this repo

## Structure
- Maven multi-module root `pom.xml` (`groupId: org.social`, `artifactId: social`) with 3 modules: `api-gateway`, `service-templete`, `common`.
- **Spring Boot 4.0.6** parent; **Java 21**; MySQL connector `9.0.0`.
- Entrypoints:
  - `api-gateway/src/main/java/org/social/apigateway/ApiGatewayApplication.java`
  - `service-templete/src/main/java/org/social/servicetemplete/ServiceTempleteApplication.java`

## Module roles

### `common` (jar)
Shared library imported by both services (`org.social:common:1.0-SNAPSHOT`). Contains:
- **Entities**: `User`, `Role`, `RoleDetail`, `Post`, `PostDetail`, `Comment`, `Message`, `Conversation`, `ConversationUser`, `ConversationUserId`, `Group`, `UserGroup`, `UserGroupId`, `UserFriend`, `UserFriendId`
- **DTOs**: `LoginRequest`, `RegisterRequest`, `ApiResponse`, `JwtAuthResponse`
- **Repositories**: `UserRepository`, `RoleRepository`, `CommentRepository`
- **Config**: `WebConfig`, `ResponseApi`
- **Exception handling**: `BusinessException`, `GlobalExceptionHandler` (under both `exceptions/` and `handler/`)
- **Key deps**: `spring-boot-starter-data-jpa`, `spring-boot-starter-data-rest`, `spring-boot-starter-validation`, Lombok

### `api-gateway`
Authentication & gateway service. Runs on port **8081**. Key deps:
- `spring-cloud-starter-gateway-server-webmvc` (Spring Cloud `2025.1.1`)
- `spring-boot-starter-security`, `spring-boot-starter-mail`, `spring-boot-starter-validation`
- **JWT**: `io.jsonwebtoken` (jjwt) version **0.13.0** (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`)
- `jackson-databind`, MySQL, Lombok, `common` module

Key source files:
- `security/SecurityConfig.java` — Spring Security filter chain; stateless JWT, CORS for `http://localhost:5173`
- `security/Endpoints.java` — public/private endpoint lists
- `filters/JwtAuthFilter.java` — `OncePerRequestFilter`; extracts Bearer token, validates, sets `SecurityContext`
- `controllers/AuthController.java` — REST controller at `/api/auth`
- `services/impl/JWTServiceImpl.java` — reads `jwt.secret` and `jwt.expiration` from properties; signs with HS256

Auth REST endpoints (`/api/auth`):
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/dang-ky` | Public | Register; sends activation email |
| POST | `/dang-nhap` | Public | Login; returns access token + sets `refreshToken` HttpOnly cookie (7 days) |
| GET | `/kich-hoat?ma=` | Public | Activate account by token |
| POST | `/refresh-token` | Public | Rotate access + refresh tokens using cookie |
| POST | `/dang-xuat` | Private | Logout; clears `refreshToken` cookie |

### `service-templete`
Blank service skeleton — no controllers yet. No fixed port (defaults to 8080). Key deps:
- `spring-boot-starter-data-jpa`, `spring-boot-starter-web`, MySQL, Lombok, `common` module
- Build plugins: `graalvm native-maven-plugin`, `spring-boot-maven-plugin`, `maven-compiler-plugin`

## Build and test
- Maven wrapper at repo root: `./mvnw` (Maven 3.9.14 image in Docker, Java 21).
- Build a single module with deps: `./mvnw -pl <module> -am package -DskipTests`
- Build all: `./mvnw package -DskipTests`
- Common library must be installed before other modules when building manually: `./mvnw -pl common install`

## CI / Docker

### GitHub Actions (`.github/workflows/`)
| File | Trigger | Purpose |
|------|---------|---------|
| `main.yml` | PR → `main` | Orchestrator: runs detect, then matrix-builds changed modules |
| `detect-changes.yml` | `workflow_call` | Compares changed files vs `modules.txt`; if `common/` changed → rebuilds **all** modules |
| `build-module.yml` | `workflow_call` | Docker build + push to GHCR (`ghcr.io/<owner>/<module>:<sha>`) |

### Docker build
- **`build.dockerfile`** (used by CI) — multi-stage: Maven 3.9.14 / JDK 21 builder → JRE 21 runtime.
  ```sh
  docker build --build-arg MODULE=api-gateway -f build.dockerfile -t myimage .
  ```
  Builds `common` first, then `MODULE`; exposes port `8080` inside container.
- **`build-prod.dockerfile`** — references `maui-backend/` (module not in repo). **Do not use** without adding that module.

### `modules.txt`
Lists trackable modules for CI change detection (one per line):
```
service-templete
api-gateway
```
Add new microservices here so CI detects their changes.

## Runtime config

### `api-gateway/src/main/resources/application.yaml`
- Port: **8081**, context-path: `/`
- DB: `jdbc:mysql://100.106.249.45:3306/FinalGraduateDB?zeroDateTimeBehavior=convertToNull` (remote host; switch to `localhost` for local dev)
- `jpa.hibernate.ddl-auto: none`; `physical-strategy: PhysicalNamingStrategyStandardImpl`; `show-sql: true`
- Spring Cloud Gateway routes are all commented out (not active)

### `api-gateway/src/main/resources/application-dev.properties`
JWT secrets — **non-production only**:
```properties
jwt.secret=<base64-encoded-secret>
jwt.expiration=86400000   # 1 day in ms; refresh token = 7x this value
```

### `service-templete/src/main/resources/application.yaml`
- No explicit port (defaults to 8080)
- Same DB URL as `api-gateway`; Flyway and Hibernate dialect are commented out

## Key conventions
- Response wrapper: `org.social.dto.ApiResponse<T>` with static helpers `ApiResponse.ok(...)` and `ApiResponse.error(status, msg)`
- Refresh token is stored as an **HttpOnly cookie** (`refreshToken`), not in response body
- CORS allows only `http://localhost:5173` (Vite dev server) — update `Endpoints.front_end_host` for production
- `service-templete` is a **template** for new microservices; copy and rename when adding a new service
- When adding a new module, register it in root `pom.xml` `<modules>` **and** `modules.txt`

