# OpenCode notes for this repo

## Structure
- Maven multi-module root `pom.xml` (`groupId: org.social`, `artifactId: social`) with 6 Java modules: `api-gateway`, `auth-service`, `chat-service`, `user-service`, `notification-service`, `common`.
- **Spring Boot 4.0.6** parent; **Java 21**; MySQL connector `9.0.0`.
- 3 Python microservices (managed via `uv` or `conda`): `ai-service`, `preprocessor-service`, `srl-service`.
- Entrypoints:
  - `api-gateway/src/main/java/org/social/apigateway/ApiGatewayApplication.java`
  - `auth-service/src/main/java/org/social/authservice/AuthServiceApplication.java`
  - `user-service/src/main/java/org/social/userservice/UserServiceApplication.java`
  - `chat-service/src/main/java/org/social/chatservice/ChatServiceApplication.java`
  - `notification-service/src/main/java/org/social/notificationservice/NotificationServiceApplication.java`
  - `ai-service/src/main.py`
  - `preprocessor-service/src/main.py`
  - `srl-service/src/main.py`

## Module roles

### `common` (jar)
Shared library imported by all services (`org.social:common:1.0-SNAPSHOT`). Contains:
- **Entities**: `User`, `Role`, `RoleDetail`, `Post`, `PostLike`, `PostLikeId`, `Comment`, `CommentLike`, `CommentLikeId`, `Message`, `Conversation`, `ConversationUser`, `ConversationUserId`, `Group`, `UserGroup`, `UserGroupId`, `UserFriend`, `UserFriendId`
- **Soft delete**: Entities `User`, `Role`, `Post`, `Comment`, `Message`, `Conversation`, `Group` all have an `isActive` (`TINYINT(1) DEFAULT 1`) column. Services must query only active records (e.g. `findByIdAndIsActiveTrue`). Deletes set `isActive = false` instead of physically removing rows. `FriendStatus` enum is used for `UserFriend`.
- **DTOs (root)**: `LoginRequest`, `RegisterRequest`, `ApiResponse`, `JwtAuthResponse`, `CursorPageResponse<T>`, `PageResponse<T>`
  - `CursorPageResponse<T>` — record(`data`, `nextCursor`, `hasMore`); used for infinite scroll / cursor-based pagination
  - `PageResponse<T>` — record(`data`, `page`, `size`, `totalElements`, `totalPages`, `hasNext`, `hasPrevious`); used for standard offset pagination
- **DTOs (user sub-package)**:
  - `UserProfileDTO` — full profile view
  - `AuthorDTO` — reusable minimal author info (`id`, `name`, `avatar`, `nickName`)
  - `ProfileUpdateRequest`
- **DTOs (friend & group sub-packages)**: `FriendshipDTO`, `FriendRequestDTO`, `FriendSuggestionDTO`, `GroupDTO`, `GroupSummaryDTO`, `GroupMemberDTO`, `GroupCreateRequest`
- **DTOs (conversation sub-package)**: `ConversationResponse`, `ConversationResponseDetail`, `MessageResponse`, `ChatMessageRequest`, `TypingIndicator`, etc.
- **Events**: `PingEvent`, `PongEvent`, `AnalyzeSentimentEvent`, `PostAnalyzeResultEvent` (Kafka transport records)
- **Kafka kernel** (`kafka/`): `KafkaTopics`, `KafkaHeaders`, `support/EventEnvelope`, `support/EventPublisher`, `config/KafkaCommonProperties`, `config/KafkaErrorHandlingConfig`
- **Repositories**: `UserRepository`, `RoleRepository`, `CommentRepository`, `CommentLikeRepository`, `PostRepository`, `PostLikeRepository`, `ConversationRepository`, `ConversationUserRepository`, `MessageRepository`, `GroupRepository`, `UserGroupRepository`, `UserFriendRepository`
- **Exception handling**:
  - `exceptions/GlobalExceptionHandler.java` — `@RestControllerAdvice`; handles 10 exception types
  - `exceptions/BusinessException.java` — custom runtime exception with `HttpStatus`
  - `exceptions/ResponseStatus.java` — HTTP status enum

### `api-gateway`
Gateway service. Runs on port **8080** (dev profile). Key deps:
- `spring-cloud-starter-gateway-server-webmvc` (Spring Cloud `2025.1.0`)
- Proxies `Path=/users/**` to `user-service`, `Path=/chat/**` to `chat-service`
- Includes SMTP Mail config for notifications.

### `auth-service`
Authentication service. Runs on port **9093** (dev profile). Key deps:
- `spring-boot-starter-security`, `spring-boot-starter-validation`
- **JWT**: `io.jsonwebtoken` (jjwt) version **0.13.0**
Entrypoint: `AuthServiceApplication.java`
Key source files:
- `controllers/AuthController.java` — REST controller at `/auth`
- `filters/JwtAuthFilter.java` — `OncePerRequestFilter`
- `services/JWTService.java` — generates/validates tokens
Auth REST endpoints (`/auth`):
- `POST /auth/refresh-token` — Refreshes access token via HttpOnly cookie
- `GET /auth/validate-token` — Validates JWT and returns `{email, role, userId}` for internal calls (e.g. from chat-service)

### `user-service`
User-facing microservice. Runs on port **9090** (dev profile), **8080** (prod profile).
Key source files:
- `controllers/PostController.java` — Post CRUD + like/unlike + search/suggested
- `controllers/CommentController.java` — Comment CRUD + replies + like/unlike
- `controllers/FriendController.java` — Friend requests, list, suggestions (`/friends`)
- `controllers/GroupController.java` — Group CRUD, join/leave, members, feed (`/groups`)
- `controllers/UserController.java` — Profile CRUD, avatar/cover upload (`/profile`)
- `messaging/listeners/PostAnalyzeResultListener.java` — Listens to AI NLP results.

### `chat-service`
Chat/messaging microservice. Runs on port **9091** (dev profile), **8080** (prod profile). Key deps:
- `spring-boot-starter-websocket`, `spring-boot-starter-security`, `spring-cloud-starter-openfeign`
Key source files:
- `controllers/ChatController.java` — STOMP `@MessageMapping` handlers (`/chat.send`, `/chat.typing`)
- `controllers/ConversationController.java` — REST endpoints for creating and listing conversations
- `websocket/WebSocketConfiguration.java` — Configures STOMP endpoint (`/app_socket`)
- `websocket/WebSocketAuthInterceptor.java` — Validates connection token using Feign client
- `feignClient/UserClient.java` — Calls `auth-service` to validate token.

### `notification-service`
Notification microservice. Runs on port **9092** (dev profile), **8080** (prod profile). Listens for Kafka events and handles external notifications (emails).

### `preprocessor-service`
Python microservice for NLP text preprocessing.
- **Tech**: Python 3.12, Stanza (CoreNLP), BeautifulSoup, `confluent-kafka`
- **Pipeline**: cleans tweets, removes HTML/quotes/elongated/abbreviations, and lemmatizes text via Stanza.
- **Workflow**: consumes `dev.post.analyze.preprocessor`, calls `srl-service` HTTP API for Semantic Role Labeling, and produces to `dev.post.analyze.sentiment`

### `srl-service`
Python microservice for Semantic Role Labeling (SRL).
- **Tech**: Python 3.8 (conda), AllenNLP 2.10.1, FastAPI
- **Model**: `structured-prediction-srl-bert`
- **API**: `POST /predict` returns `ARG0` (Subject), `V` (Verb), `ARG1` (Object)

### `ai-service`
Python microservice for Implicit Sentiment Analysis.
- **Tech**: Python 3.12, PyTorch, Transformers, `confluent-kafka`
- **Model**: `MikeJohnP/HTC_ImplicitSentiment` (Hierarchical Tensor Composition)
- **Workflow**: consumes `dev.post.analyze.sentiment` (with SRL args), predicts sentiment, and produces to `dev.post.analyze.result`

## Python Services Conventions
- **Package Manager**: Use `uv` for modern Python projects (ai-service, preprocessor-service), `conda` for legacy compat (srl-service uses Python 3.8 for AllenNLP).
- **Kafka Client**: Use `confluent-kafka`.
- **EventEnvelope**: Python producers MUST package payloads in the `EventEnvelope` JSON format (matching the Java standard: `eventId`, `eventType`, `traceId`, `occurredAt`, `source`, `version`, `payload`).
- **Kafka Headers**: Python producers MUST include headers `X-Event-Id`, `X-Event-Type`, `X-Event-Source`, `X-Event-Version`, `X-Occurred-At`.

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
- Java Services: **`build.dockerfile`** (multi-stage: Maven builder → JRE 21 runtime).
- Python Services: Specific `Dockerfile` in each python service dir. Use multi-stage builds (builder with `uv`, production with slim images).

### `modules.txt`
Lists trackable modules for CI change detection (one per line):
```
user-service
api-gateway
chat-service
notification-service
auth-service
```

## Runtime config
- `api-gateway`: port **8080**
- `user-service`: port **9090**
- `chat-service`: port **9091**
- `notification-service`: port **9092**
- `auth-service`: port **9093**

## Key conventions
- **Response wrapper**: `org.social.common.dto.ApiResponse<T>` is the **only** response type.
- **No inline comments in code**: Do not add Javadoc or inline comments to production code.
- Refresh token is stored as an **HttpOnly cookie** (`refreshToken`), not in response body.
- JWT access token claims: `sub` (email), `roles` (single role name string), `type` (`"access"`)

## Soft delete conventions
- All major entities (`User`, `Role`, `Post`, `Comment`, `Message`, `Conversation`, `Group`) have an `isActive` column.
- Join/mapping tables (`UserFriend`, `UserGroup`, `PostLike`, `ConversationUser`) do **not** have `isActive`. Services must physically delete or update custom status columns (like `FriendStatus` in `UserFriend`).
- Services must **never** call `deleteById()` on soft-deletable entities. Instead, set `isActive = false` and `save()`.

## Pagination conventions
- **Cursor pagination (infinite scroll)**: Response `CursorPageResponse<T>` (`data`, `nextCursor`, `hasMore`). `cursor` = ISO-8601 timestamp.
- **Standard offset pagination**: Response `PageResponse<T>` (`data`, `page`, `size`, `totalElements`, `totalPages`, `hasNext`, `hasPrevious`). Built with **JPA Specification**.

## Exception handling conventions
- The project uses a **centralized exception handling architecture** (`GlobalExceptionHandler` in `common`).
- Use `ResourceNotFoundException("Tên Resource", identifier)` for 404s.
- Use `BusinessException(ErrorCode)` for logic errors.

## DTO conventions
- **Location**: ALL DTOs MUST be placed in the `common` module under `org.social.common.dto.*`.
- DTOs are written as **Java Records** (Java 21). *Exception: `ConversationResponse`, `MessageResponse` inside `dto/conversation` currently use Lombok `@Data` classes.*
- Mapper classes use static methods only.

## Database Migrations
Located in the `migrations/` directory at project root.
Current migrations:
- `V2__add_friend_status_columns.sql` — friend feature tables.
- `V3__add_user_profile_columns.sql` — user profile fields.
Migrations are currently executed manually against the `FinalGraduateDB` schema.

## API Testing with Hurl
Tests are located in `/tests/api/`. Environment variables loaded from `/tests/api/vars/dev.env`.
Run tests using `tests/api/run-all.sh`.
Current test suites:
- `posts.hurl` (Post CRUD)
- `comments.hurl` (Comment CRUD)
- `friends.hurl` (Friend requests and list)
- `users.hurl` (User profiles)

## Kafka conventions
- **Shared kernel** in `common` (`org.social.common.kafka.*`):
  - `support/EventEnvelope` — record wrapping every payload (`eventId`, `eventType`, `traceId`, `occurredAt`, `source`, `version`, `payload`)
  - `support/EventPublisher` — wrapper around `KafkaTemplate`
- **Keys**: use a meaningful entity id as record key so events of the same entity stay ordered within a partition.
- **NLP Event Flow**:
  1. `user-service` publishes new post to `dev.post.analyze.preprocessor`.
  2. `preprocessor-service` cleans text, fetches SRL from HTTP API, publishes to `dev.post.analyze.sentiment`.
  3. `ai-service` predicts sentiment, publishes to `dev.post.analyze.result`.
  4. `user-service` consumes result via `PostAnalyzeResultListener` to update the DB.
