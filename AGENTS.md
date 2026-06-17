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
  - `Group` — has `description` (TEXT) and `createdAt` (Instant) fields added in V7.
  - `UserGroup` — has `status` (VARCHAR 20, default `'APPROVED'`) and `requestedAt` (Instant) fields. Possible values: `PENDING`, `APPROVED`, `REJECTED`.
  - `Post` — has `status` (VARCHAR 20, default `'APPROVED'`) field for group post moderation. Possible values: `PENDING`, `APPROVED`, `REJECTED`.
- **Soft delete**: Entities `User`, `Role`, `Post`, `Comment`, `Message`, `Conversation`, `Group` all have an `isActive` (`TINYINT(1) DEFAULT 1`) column. Services must query only active records (e.g. `findByIdAndIsActiveTrue`). Deletes set `isActive = false` instead of physically removing rows. `FriendStatus` enum is used for `UserFriend`.
- **DTOs (root)**: `LoginRequest`, `RegisterRequest`, `ApiResponse`, `JwtAuthResponse`, `CursorPageResponse<T>`, `PageResponse<T>`
  - `CursorPageResponse<T>` — record(`data`, `nextCursor`, `hasMore`); used for infinite scroll / cursor-based pagination
  - `PageResponse<T>` — record(`data`, `page`, `size`, `totalElements`, `totalPages`, `hasNext`, `hasPrevious`); used for standard offset pagination
- **DTOs (user sub-package)**:
  - `UserProfileDTO` — full profile view
  - `AuthorDTO` — reusable minimal author info (`id`, `name`, `avatar`, `nickName`)
  - `ProfileUpdateRequest`
- **DTOs (friend & group sub-packages)**:
  - `FriendshipDTO`, `FriendRequestDTO`, `FriendSuggestionDTO`
  - `GroupDTO` — includes `isJoined` and `isPending` (boolean) fields; `isPending = true` means the user sent a join request awaiting approval.
  - `GroupSummaryDTO`, `GroupMemberDTO`, `GroupCreateRequest`
  - **Group Admin view DTOs** (in `dto/group/views/`): `GroupAdminInfoDTO`, `GroupStatsDTO`, `WeeklyActivityDTO`, `MemberRequestDTO`, `PendingPostDTO`
  - **Group Admin request DTOs** (in `dto/group/requests/`): `MemberRequestActionRequest`
  - **Group response DTOs** (in `dto/group/responses/`): `JoinGroupResponse` — returned by `/groups/{id}/join`; contains `status` (`"APPROVED"` or `"PENDING"`).
  - **Mappers** (in `dto/group/mappers/`): `GroupMapper`, `GroupAdminMapper` — static-method-only classes.
  - **DTOs (conversation sub-package)**: `ConversationResponse`, `ConversationResponseDetail`, `MessageResponse`, `ChatMessageRequest`, `TypingIndicator`, etc.
  - **DTOs (admin sub-package)** — system-wide admin features:
    - `dto/admin/`: `UserAdminDTO`, `GroupAdminDTO`, `SystemStatsDTO`
      - `SystemStatsDTO` — record(`totalUsers`, `activeUsers`, `inactiveUsers`, `totalGroups`, `activeGroups`, `totalPosts`, `totalComments`); used by `/admin/reports`.
    - `dto/admin/requests/`: `AdminUserCreateRequest`, `AdminUserUpdateRequest`, `AdminGroupCreateRequest`, `AdminGroupUpdateRequest`
    - `dto/admin/sentiment/`: `SentimentStatsDTO`, `SentimentItemDTO`, `SentimentFilterRequest`
      - `SentimentStatsDTO` — record(`totalPosts`, `positivePosts`, `neutralPosts`, `negativePosts`, `totalComments`, `positiveComments`, `neutralComments`, `negativeComments`); sentiment overview.
      - `SentimentItemDTO` — record(`type` (`"POST"`/`"COMMENT"`), `id`, `content`, `sentiment`, `confidence`, `authorId`, `authorName`, `groupId`, `groupName`, `createdAt`); drill-down list item.
      - `SentimentFilterRequest` — record(`sentiment`, `fromDate`, `toDate`, `minConfidence`, `maxConfidence`, `keyword`, `groupId`); shared filter for overview + drill-down. Sentiment label values are lowercase `positive`, `neutral`, `negative` (matching `ai-service` `id2label`).

- **Events**: `PingEvent`, `PongEvent`, `AnalyzeSentimentEvent`, `PostAnalyzeResultEvent` (Kafka transport records)
- **Kafka kernel** (`kafka/`): `KafkaTopics`, `KafkaHeaders`, `support/EventEnvelope`, `support/EventPublisher`, `config/KafkaCommonProperties`, `config/KafkaErrorHandlingConfig`
- **Repositories**: `UserRepository`, `RoleRepository`, `CommentRepository`, `CommentLikeRepository`, `PostRepository`, `PostLikeRepository`, `ConversationRepository`, `ConversationUserRepository`, `MessageRepository`, `GroupRepository`, `UserGroupRepository`, `UserFriendRepository`
  - `UserGroupRepository` — queries filter by `status = 'APPROVED'` where appropriate. Added `findByGroupIdAndStatus`, `countByGroupIdAndStatus`, search/gender filter queries, `findByGroupIdAndUserIdsAndStatus` for batch ops.
  - `PostRepository` — feed queries now filter `status = 'APPROVED' OR NULL`. Added `findByGroupIdAndStatusAndIsActiveTrue`, `countByGroupIdAndCreatedAtBetween` for admin stats.
  - `PostLikeRepository` — added `countByGroupIdAndCreatedAtAfter` for weekly reaction stats.
  - `CommentRepository` — implements `JpaSpecificationExecutor<Comment>`; added `countByGroupIdAndCreatedAtAfter` (weekly comment stats) and `countByIsActiveTrue` (system report).
  - `UserRepository` — added `countByIsActiveTrue`, `countByIsActiveFalse` for system report.
  - `GroupRepository` — added `count`, `countByIsActiveTrue` for system report.
  - `PostRepository` — added `countByIsActiveTrue` for system report.
- **Exception handling**:
  - `exceptions/GlobalExceptionHandler.java` — `@RestControllerAdvice`; handles 10 exception types
  - `exceptions/BusinessException.java` — custom runtime exception with `HttpStatus`
  - `exceptions/ResponseStatus.java` — HTTP status enum

### `api-gateway`

Gateway service. Runs on port **8080** (dev profile). Key deps:

- `spring-cloud-starter-gateway-server-webmvc` (Spring Cloud `2025.1.0`)
- `spring-cloud-starter-openfeign`
- Proxies `Path=/auth/**` to `auth-service` (no StripPrefix), `Path=/users/**` to `user-service`, `Path=/chat/**` to `chat-service`.
- **Stateless gateway**: no DB/JPA, no JWT secret, no mail. Authentication is fully delegated to `auth-service`.
- `filters/JwtAuthFilter.java` — validates every request by calling `auth-service GET /auth/validate-token` via `client/AuthClient` (Feign). On success it injects downstream headers `X-User-Email`, `X-User-Id`, `X-User-Role` (user-service consumes `X-User-Email`). On failure returns 401 JSON.
- `client/AuthClient.java` — Feign client to `auth-service` (url from `auth-service.url`).
- `dto/TokenValidateResponse.java` — maps the `ApiResponse` returned by `validate-token`.
- `security/Endpoints.java` — public auth paths (`/auth/login`, `/auth/register`, `/auth/active`, `/auth/refresh-token`, `/auth/logout`, `/auth/validate-token`); everything else authenticated.
- Login/register/active/logout endpoints now live in `auth-service`, not the gateway.

### `auth-service`

Authentication service. Runs on port **9093** (dev profile). It is the **single source of truth for authentication** — login, register, activation, refresh, logout, and token validation all live here. Key deps:

- `spring-boot-starter-security`, `spring-boot-starter-validation`, `spring-boot-starter-mail`
- **JWT**: `io.jsonwebtoken` (jjwt) version **0.13.0**
  Entrypoint: `AuthServiceApplication.java` (annotated `@EnableAsync` for async activation email).
  Key source files:
- `controllers/AuthController.java` — REST controller at `/auth`
- `filters/JwtAuthFilter.java` — `OncePerRequestFilter`
- `services/JWTService.java` — generates/validates tokens
- `services/UserService.java` (+ impl) — `register`, `kichHoatTaiKhoan`, `findByEmail`, `loadUserByUsername`
- `services/EmailService.java` (+ impl) — sends activation email (`@Async`)
  Auth REST endpoints (`/auth`):
- `POST /auth/register` — Registers a new user and sends activation email
- `POST /auth/login` — Authenticates, returns `JwtAuthResponse` (access token + userId), sets HttpOnly `refreshToken` cookie
- `GET /auth/active?code=...` — Activates account via activation code
- `POST /auth/refresh-token` — Refreshes access token via HttpOnly cookie
- `POST /auth/logout` — Clears the `refreshToken` cookie
- `GET /auth/validate-token` — Validates JWT and returns `{email, role, userId}` for internal calls (e.g. from api-gateway and chat-service)
- SMTP mail config lives in `auth-service` `application.yaml` (moved from api-gateway).

### `user-service`

User-facing microservice. Runs on port **9090** (dev profile), **8080** (prod profile).
Key source files:

- `controllers/PostController.java` — Post CRUD + like/unlike + search/suggested
- `controllers/CommentController.java` — Comment CRUD + replies + like/unlike
- `controllers/FriendController.java` — Friend requests, list, suggestions (`/friends`)
- `controllers/GroupController.java` — Group CRUD, join/leave, members, feed (`/groups`). `POST /groups/{id}/join` now returns `JoinGroupResponse { status }` instead of `void`.
- `controllers/GroupAdminController.java` — 8 Group Admin endpoints at `/groups/{groupId}/admin/**`. Requires caller to be ADMIN of the group (enforced in service layer).
- `controllers/UserController.java` — Profile CRUD, avatar/cover upload (`/profile`)
- `controllers/admin/AdminUserController.java` — System-wide admin user CRUD (`/admin/users`).
- `controllers/admin/AdminGroupController.java` — System-wide admin group CRUD (`/admin/groups`).
- `controllers/admin/AdminSentimentController.java` — Sentiment analytics at `/admin/sentiment`: `GET /overview` (counts per label for posts + comments), `GET /items` (drill-down list, `type=post|comment`). Both accept the `SentimentFilterRequest` query params (`sentiment`, `fromDate`, `toDate`, `minConfidence`, `maxConfidence`, `keyword`, `groupId`); `items` adds `page`/`size`.
- `controllers/admin/AdminReportController.java` — System report at `/admin/reports`: `GET /overview` (returns `SystemStatsDTO`), `GET /export` (downloads CSV with UTF-8 BOM, `Content-Disposition: attachment`).
- `messaging/listeners/PostAnalyzeResultListener.java` — Listens to AI NLP results.
- `services/GroupAdminService.java` + `services/impl/GroupAdminServiceImpl.java` — Business logic for group admin operations (info, stats, member requests, pending posts).
- `services/AdminSentimentService.java` + `services/impl/AdminSentimentServiceImpl.java` — Sentiment overview (counts via `JpaSpecificationExecutor.count`) + drill-down items.
- `services/AdminReportService.java` + `services/impl/AdminReportServiceImpl.java` — System stats overview + CSV export (`metric,value` rows).
- `specifications/PostSentimentSpecification.java`, `specifications/CommentSentimentSpecification.java` — Build dynamic `Specification` from `SentimentFilterRequest`. Filter only records where `sentiment IS NOT NULL` and `isActive = true`. **Note**: `Comment.content` is `@Lob` (CLOB) so keyword `LIKE` must NOT wrap it in `lower()`; `Post.content` is VARCHAR and uses `lower()`.

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

| File                 | Trigger         | Purpose                                                                                  |
| -------------------- | --------------- | ---------------------------------------------------------------------------------------- |
| `main.yml`           | PR → `main`     | Orchestrator: runs detect, then matrix-builds changed modules                            |
| `detect-changes.yml` | `workflow_call` | Compares changed files vs `modules.txt`; if `common/` changed → rebuilds **all** modules |
| `build-module.yml`   | `workflow_call` | Docker build + push to GHCR (`ghcr.io/<owner>/<module>:<sha>`)                           |

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
- Join/mapping tables (`UserFriend`, `PostLike`, `ConversationUser`) do **not** have `isActive`. Services must physically delete or update custom status columns (like `FriendStatus` in `UserFriend`).
- `UserGroup` does **not** have `isActive`. Instead, use `status` column: `PENDING` (awaiting admin approval), `APPROVED` (active member), `REJECTED`. Never delete `UserGroup` rows for leave — use `deleteById` for leave, but never for pending/rejected transitions.
- Services must **never** call `deleteById()` on soft-deletable entities. Instead, set `isActive = false` and `save()`.

## Group join workflow conventions

- `POST /groups/{id}/join` returns `JoinGroupResponse { status: "APPROVED" | "PENDING" }`.
- If group `privacy = PUBLIC` → `UserGroup.status = 'APPROVED'` immediately.
- If group `privacy = PRIVATE` → `UserGroup.status = 'PENDING'`, awaiting admin action via `GroupAdminController`.
- All queries that count members (`memberCount`) or display the news feed must filter `UserGroup.status = 'APPROVED'` and `Post.status = 'APPROVED'` respectively.
- Group post approval: posts submitted to a group where approval is required are created with `Post.status = 'PENDING'`. Admin approves/rejects via `GroupAdminController`.

## Pagination conventions

- **Cursor pagination (infinite scroll)**: Response `CursorPageResponse<T>` (`data`, `nextCursor`, `hasMore`). `cursor` = ISO-8601 timestamp.
- **Standard offset pagination**: Response `PageResponse<T>` (`data`, `page`, `size`, `totalElements`, `totalPages`, `hasNext`, `hasPrevious`). Built with **JPA Specification**.

## Exception handling conventions

- The project uses a **centralized exception handling architecture** (`GlobalExceptionHandler` in `common`).
- Use `ResourceNotFoundException("Tên Resource", identifier)` for 404s.
- Use `BusinessException(ErrorCode)` for logic errors.

## DTO conventions

- **Location**: ALL DTOs MUST be placed in the `common` module under `org.social.common.dto.*`.
- DTOs are written as **Java Records** (Java 21). _Exception: `ConversationResponse`, `MessageResponse` inside `dto/conversation` currently use Lombok `@Data` classes._
- Mapper classes use static methods only.

## Database Migrations

Located in the `migrations/` directory at project root.
Current migrations:

- `V2__add_friend_status_columns.sql` — friend feature tables.
- `V3__add_user_profile_columns.sql` — user profile fields.
- `V7__group_admin_features.sql` — Group Admin feature: adds `groups.description`, `groups.created_at`, `user_group.status`, `user_group.requested_at`, `posts.status`, `post_likes.created_at`.
  Migrations are currently executed manually against the `FinalGraduateDB` schema (run in Docker: `docker exec mysql_server mysql -u root FinalGraduateDB < migrations/<file>.sql`).

## API Testing with Hurl

Tests are located in `/tests/api/`. Environment variables loaded from `/tests/api/vars/dev.env`.
Run tests using `tests/api/run-all.sh`.
Current test suites:

- `posts.hurl` (Post CRUD)
- `comments.hurl` (Comment CRUD)
- `friends.hurl` (Friend requests and list)
- `users.hurl` (User profiles)
- `admin_sentiment.hurl` (Sentiment overview + drill-down items + filters)
- `admin_report.hurl` (System stats overview + CSV export)

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
