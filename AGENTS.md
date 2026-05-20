# OpenCode notes for this repo

## Structure
- Maven multi-module root `pom.xml` (`groupId: org.social`, `artifactId: social`) with 4 modules: `api-gateway`, `chat-service`, `user-service`, `common`.
- **Spring Boot 4.0.6** parent; **Java 21**; MySQL connector `9.0.0`.
- Entrypoints:
  - `api-gateway/src/main/java/org/social/apigateway/ApiGatewayApplication.java`
  - `user-service/src/main/java/org/social/userservice/UserServiceApplication.java`
  - `chat-service/src/main/java/org/social/chatservice/ChatServiceApplication.java`

## Module roles

### `common` (jar)
Shared library imported by all services (`org.social:common:1.0-SNAPSHOT`). Contains:
- **Entities**: `User`, `Role`, `RoleDetail`, `Post`, `PostDetail`, `Comment`, `Message`, `Conversation`, `ConversationUser`, `ConversationUserId`, `Group`, `UserGroup`, `UserGroupId`, `UserFriend`, `UserFriendId`
- **DTOs (root)**: `LoginRequest`, `RegisterRequest`, `ApiResponse`, `JwtAuthResponse`
- **DTOs (user sub-package)** — `dto/user/views/` and `dto/user/mappers/`:
  - `UserDTO` — record(`name`, `RoleDTO role`); used for general responses
  - `UserNoAuthenticateDTO` — record(`name`, `role`); public-facing, no sensitive fields
  - `UserWithAuthenticateDTO` — record(`name`, `role`, `isActive`); includes activation status
  - `RoleDTO` — record(`name`); flat, no back-reference to users
  - `UserMapper` — static utility class; methods: `mapUserToUserDTO`, `mapUserToUserNoAuthenticate`, `mapUserToUserWithAuthenticate`
- **Events**: `events/PingEvent`, `events/PongEvent` (Kafka transport records, top-level package, **not** under `dto/`)
- **Kafka kernel** (`kafka/`): `KafkaTopics`, `KafkaHeaders`, `support/EventEnvelope`, `support/EventPublisher`, `config/KafkaCommonProperties`, `config/KafkaErrorHandlingConfig`
- **Repositories**: `UserRepository`, `RoleRepository`, `CommentRepository`, `PostRepository`, `PostDetailRepository`
- **Config**: `WebConfig`, `ResponseApi` (legacy — do not use in new code)
- **Exception handling**:
  - `exceptions/GlobalExceptionHandler.java` — `@RestControllerAdvice`; handles 10 exception types (see below)
  - `exceptions/BusinessException.java` — custom runtime exception with `HttpStatus`
  - `exceptions/ResponseStatus.java` — HTTP status enum
  - `handler/GlobalExceptionHandler.java`, `handler/ResponseStatus.java` (legacy duplicates — kept for backward compat)
- **Key deps**: `spring-boot-starter-data-jpa`, `spring-boot-starter-data-rest`, `spring-boot-starter-validation`, `spring-kafka` (optional), Lombok

### `api-gateway`
Authentication & gateway service. Runs on port **8080** (dev profile). Key deps:
- `spring-cloud-starter-gateway-server-webmvc` (Spring Cloud `2025.1.0`)
- `spring-boot-starter-security`, `spring-boot-starter-mail`, `spring-boot-starter-validation`
- **JWT**: `io.jsonwebtoken` (jjwt) version **0.13.0** (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`) — hardcoded in module POM, overrides root's `0.11.5`
- `jackson-databind`, MySQL, Lombok, `common` module

Key source files:
- `security/SecurityConfig.java` — Spring Security filter chain; stateless JWT, CORS for `http://localhost:5173`
- `security/Endpoints.java` — public/private endpoint lists
- `filters/JwtAuthFilter.java` — `OncePerRequestFilter`; extracts Bearer token, validates, sets `SecurityContext`; returns JSON error on expired/invalid tokens
- `controllers/AuthController.java` — REST controller at `/api/auth`
- `configs/PasswordEncoderConfig.java` — BCrypt `PasswordEncoder` bean
- `services/impl/JWTServiceImpl.java` — reads `jwt.secret` and `jwt.expiration` from properties; signs with HS256; JWT claims include `roles` (single role name) and `type` (`"access"` or `"refresh"`)
- `services/impl/EmailServiceImpl.java` — email sending via Spring Mail
- `services/impl/UserServiceImpl.java` — `UserDetailsService` implementation; loads user by email

Auth REST endpoints (`/api/auth`):
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/register` | Public | Register; sends activation email |
| POST | `/login` | Public | Login; returns access token + sets `refreshToken` HttpOnly cookie (7 days) |
| GET | `/active?code=` | Public | Activate account by activation code |
| POST | `/refresh-token` | Public | Rotate access + refresh tokens using cookie |
| POST | `/logout` | Private | Logout; clears `refreshToken` cookie |

Gateway routing (active in dev profile):
| Route ID | Predicate | Target | Filters |
|----------|-----------|--------|---------|
| `user-service` | `Path=/users/**` | `http://localhost:9090/` | `StripPrefix=1` |
| `chat-service` | `Path=/chat/**` | `http://localhost:9091/` | `StripPrefix=1` |

### `user-service`
User-facing microservice. Runs on port **9090** (dev profile), **8080** (prod profile). Key deps:
- `spring-boot-starter-data-jpa`, `spring-boot-starter-webmvc`, MySQL, Lombok, `common` module

Entrypoint: `UserServiceApplication.java` — annotated with `@EntityScan("org.social.common.entities")` and `@EnableJpaRepositories("org.social.common.repositories")` to pick up shared JPA classes from `common`.

Key source files:
- `controllers/HelloController.java` — `@RequestMapping("/hello")`, `GET` → returns all users (temporary/test endpoint)
- `services/UserService.java` — interface with `getAll()` method
- `services/impl/UserServiceImpl.java` — queries `UserRepository.findAll()`

### `chat-service`
Chat/messaging microservice. Runs on port **9091** (dev profile), **8080** (prod profile). Key deps:
- `spring-boot-starter-data-jpa`, `spring-boot-starter-webmvc`, MySQL, Lombok, `common` module

Entrypoint: `ChatServiceApplication.java` — annotated with `@EntityScan("org.social.common.entities")`, `@EnableJpaRepositories("org.social.common.repositories")`, and `@ComponentScan` covering `org.social.chatservice` + `org.social.common.exceptions`.

Key source files:
- `TestController.java` — `@RequestMapping("/hello")`, `GET` → returns "Hello" (temporary/test endpoint)

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

### `modules.txt`
Lists trackable modules for CI change detection (one per line):
```
user-service
api-gateway
chat-service
```

## Runtime config

All services use **Spring profile-based configuration**. Config is in `application.yaml` (dev) and `application-prod.yaml` (prod).

### `api-gateway` config

#### `application.yaml` (dev)
- Port: **8080**, context-path: `/`
- DB: `jdbc:mysql://100.106.249.45:3306/FinalGraduateDB?zeroDateTimeBehavior=convertToNull` (remote host; switch to `localhost` for local dev)
- `jpa.hibernate.ddl-auto: none`; `physical-strategy: PhysicalNamingStrategyStandardImpl`; `show-sql: true`
- **Mail**: Gmail SMTP (`smtp.gmail.com:587`, TLS)
- **Gateway routes**: proxies `Path=/users/**` → `http://localhost:9090/` (user-service), `Path=/chat/**` → `http://localhost:9091/` (chat-service)
- JWT config:
  ```yaml
  jwt:
    secret: <plaintext-secret>
    expiration: 86400000   # 1 day in ms; refresh token = 7x this value
  ```

#### `application-prod.yaml`
- Same structure as dev (currently identical content — needs differentiation for production)

### `user-service` config

#### `application-dev.yaml`
- Port: **9090**
- Same DB URL as `api-gateway`; `jpa.hibernate.ddl-auto: none`; `show-sql: true`

#### `application-prod.yaml`
- Port: **8080** (matches Docker container exposed port)
- Same DB and JPA settings

### `chat-service` config

#### `application.yaml` (dev)
- Port: **9091**
- Same DB URL as other modules; `jpa.hibernate.ddl-auto: none`; `show-sql: true`

#### `application-prod.yaml`
- Port: **8080** (matches Docker container exposed port)
- Same DB and JPA settings

## Key conventions
- **Response wrapper**: `org.social.common.dto.ApiResponse<T>` is the **only** response type. Use `ApiResponse.ok(msg)`, `ApiResponse.ok(msg, data)`, `ApiResponse.error(status, msg)`, `ApiResponse.error(status, msg, data)`. Do **not** use `ResponseApi` in new code — it is legacy.
- **No inline comments in code**: Do not add Javadoc or inline comments to production code. Use clear method/variable names instead. Documentation lives in AGENTS.md and plan files.
- Refresh token is stored as an **HttpOnly cookie** (`refreshToken`), not in response body
- JWT access token claims: `sub` (email), `roles` (single role name string), `type` (`"access"`)
- JWT refresh token claims: same structure but `type` = `"refresh"`, expiration = `7 × jwtExpirationMs`
- CORS allows only `http://localhost:5173` (Vite dev server) — update `Endpoints.front_end_host` for production
- When adding a new module, register it in root `pom.xml` `<modules>` **and** `modules.txt`
- Downstream services consume entities/repositories from `common` — must annotate the main class with `@EntityScan` and `@EnableJpaRepositories` pointing to `org.social.common.*` packages

## Exception handling conventions
- All services rely on `exceptions/GlobalExceptionHandler` from `common` (picked up via `@ComponentScan("org.social.common.exceptions")`).
- `GlobalExceptionHandler` covers (in order, specific → general):
  1. `MethodArgumentNotValidException` — `@RequestBody` validation fails → 400 + field error map
  2. `ConstraintViolationException` — `@PathVariable`/`@RequestParam` validation fails → 400 + field error map
  3. `HttpMessageNotReadableException` — malformed JSON body → 400
  4. `MethodArgumentTypeMismatchException` — wrong path variable type (e.g. `/posts/abc`) → 400
  5. `MissingServletRequestParameterException` — required `@RequestParam` absent → 400
  6. `HttpMediaTypeNotSupportedException` — wrong Content-Type → 415
  7. `HttpRequestMethodNotSupportedException` — wrong HTTP method → 405
  8. `NoResourceFoundException` — URL not found (Spring Boot 3.2+) → 404
  9. `ResponseStatusException` — thrown from services via `throw new ResponseStatusException(...)` → dynamic status
  10. `DataIntegrityViolationException` — DB unique/FK constraint violated → 409
  11. `BusinessException` — custom domain exception → status from exception
  12. `Exception` (catch-all) → 500
- Throw `BusinessException(HttpStatus, message)` for domain errors from services.
- Throw `ResponseStatusException(HttpStatus, reason)` for simple not-found / forbidden checks in services.
- **`Exception.class` handler must always be last** — placing it before specific handlers will silently swallow them.

## JSON circular reference
Entities have bidirectional JPA relationships (e.g. `User ↔ Role`) that cause `StackOverflowError` if serialized directly. **Preferred solution: always use DTOs instead of returning raw entities from controllers.** Use `UserMapper` (or similar static mappers in `dto/<entity>/mappers/`) to convert entities to flat DTO records before returning responses. DTO records in `dto/user/views/` are designed to be non-circular by construction (e.g. `RoleDTO` has no `users` field). Do **not** use `@JsonIgnore` or `@JsonManagedReference/@JsonBackReference` on entities as the primary fix — those are last-resort patches.

## DTO conventions
- **Location**: ALL DTOs, Requests, Responses, and Mappers MUST be placed in the `common` module under `org.social.common.dto.*`. Do NOT create DTO-related classes inside individual microservices.
- DTOs are written as **Java Records** (Java 21) for brevity and immutability.
- DTOs for a domain object are grouped under `dto/<entity>/views/` (response shapes) and `dto/<entity>/mappers/` (static conversion methods).
- Mapper classes use static methods only — no Spring beans, no dependency injection.
- Naming: `<Entity>DTO` (full), `<Entity>NoAuthenticateDTO` (public/anonymous view), `<Entity>WithAuthenticateDTO` (authenticated view with status fields).

## API Testing with Hurl
We use [Hurl](https://hurl.dev/) for end-to-end API testing.
- **Location**: All tests are located in `/tests/api/`.
- **Environment Variables**: Tests must be environment-agnostic. Use `{{host}}` for base URLs, loaded from `/tests/api/vars/dev.env` or `prod.env`.
- **Authentication**: For protected endpoints, capture the token from a login request at the top of the test file using the `[Captures]` block, and inject it into subsequent requests via the `Authorization: Bearer {{token}}` header.
- **Execution**: Run tests using the helper script `tests/api/run-all.sh`.

## Kafka conventions

- **Cluster**: Strimzi `final-graduate-cluster` running in K8s namespace `kafka`.
  - External dev bootstrap (NodePort, Tailscale): `100.106.249.45:31835`
  - In-cluster bootstrap: `final-graduate-cluster-kafka-bootstrap.kafka.svc.cluster.local:9092`
  - Override with env var `KAFKA_BOOTSTRAP`.
- **Shared kernel** in `common` (`org.social.common.kafka.*`):
  - `KafkaTopics` — topic name constants
  - `KafkaHeaders` — custom Kafka header names (`X-Event-Id`, `X-Event-Type`, ...)
  - `support/EventEnvelope` — record wrapping every payload (`eventId`, `eventType`, `traceId`, `occurredAt`, `source`, `version`, `payload`)
  - `support/EventPublisher` — wrapper around `KafkaTemplate` that sets headers + logs send result; each service exposes a `@Bean EventPublisher` with its own `source` name
  - `config/KafkaCommonProperties` — `@ConfigurationProperties("app.kafka")`
  - `config/KafkaErrorHandlingConfig` — auto-config for `DefaultErrorHandler` + `DeadLetterPublishingRecoverer` (`<topic>.dlt`); retries 3× with 1s back-off; `BusinessException` and `ResponseStatusException` are non-retryable
- **Event DTOs**: live in `common/events/` (top-level, NOT under `dto/`), written as Java records. They are transport contracts, separate from REST DTOs.
- **Demo topics** (current scope): `demo.ping` (chat-service → user-service), `demo.pong` (user-service → chat-service). DLQ topic is `<topic>.dlt`.
- **Keys**: use a meaningful entity id as record key so events of the same entity stay ordered within a partition.
- **Producer config**: `acks=all`, idempotent, JSON serializer.
- **Consumer config**: manual ack (`ack-mode: manual_immediate`), `ErrorHandlingDeserializer` + `JsonDeserializer`, default value type `EventEnvelope`, trusted packages limited to `org.social.common.events` and `org.social.common.kafka.support`.
- **Service wiring**: each service that uses Kafka must add `org.social.common.kafka` to its `@ComponentScan` so `KafkaCommonProperties` and the auto-config are picked up.
- **Per-service code layout**: producer / listener classes live under `<service>/messaging/`. Do **not** put event records in service modules.
- **Idempotency**: consumers must be idempotent. Use `EventEnvelope.eventId` (or a `processed_event` table later) to skip duplicates.

### Demo flow (ping / pong)
| Step | Service | Action |
|------|---------|--------|
| 1 | chat-service | `GET /ping?msg=hello` → `PingPublisher` publishes `PingEvent` to `demo.ping` |
| 2 | user-service | `PingListener` consumes `demo.ping`, logs the message, then publishes a `PongEvent` to `demo.pong` |
| 3 | chat-service | `PongListener` consumes `demo.pong`, logs the reply |

To trigger: call `GET http://localhost:9091/ping?msg=hello` (chat-service) and watch logs of both services.
