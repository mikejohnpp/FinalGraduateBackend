# AGENTS.md

## Modules

- `maui-backend` - Main Spring Boot application
- `common` - Shared entities in `org.social.entity` package
- `service-templete` - Template/demo module

## Build & Run

- **Build**: `cd maui-backend && mvn clean install` (or use `./mvnw`)
- **Run**: `mvn spring-boot:run` from `maui-backend/`
- **Single test**: `mvn test -Dtest=ClassName`
- **Native build**: `mvn package -Pnative` (requires GraalVM)

## Tech Stack

- Spring Boot 3.5.13, Java 21
- Maven (NOT Gradle)
- Spring Data JPA + MySQL
- JWT authentication via jjwt 0.11.5
- Spring Security

## Important Config (application.properties)

- DB: MySQL at configured IP:3306/maui_noithat (user: root)
- JWT secret: hardcoded (production should use env var)
- CORS: `allowed-origins=*` (allows all origins)
- Email: Gmail SMTP (smtp.gmail.com:587)
- H2 not configured - requires MySQL connection

## Architecture

- Modules: `maui-backend` (app), `common` (entities in `org.social.entity`)
- Entry: `DemoApplication.java`
- Requires `@EntityScan(basePackages = {"com.example.demo", "org.social.entity"})` and `@EnableJpaRepositories` for cross-module entities
- Controllers in `com.example.demo.controller`
- Entities in `org.social.entity` (from common module)
- Services in `com.example.demo.service`

## Gotchas

- `ddl-auto=none` - schema must exist; import `maui_noithat.sql` to create
- Uses custom `JwtFilter` for security
- No separate lint/typecheck (Maven only)
- Entities in `org.social.entity` require `@EntityScan` in DemoApplication
- Parent POM must be installed (`mvn -N install`) before building modules
- Use `maven:3.9-*` for Java 21 (3.8.x only supports Java 18)

## Docker

Dockerfiles nằm trong thư mục riêng theo module:

- **maui-backend**: `docker/maui-backend/maui-backend.dockerfile`
- **service-templete**: `docker/service-templete/service-templete.dockerfile`
- **native**: `docker/maui-backend-graal.dockerfile`

Dockerfiles dùng multi-stage Alpine build:
- Builder: `maven:3.9-eclipse-temurin-21-alpine`
- Runtime: `eclipse-temurin:21-jre-alpine` (~80-120MB)

## CI

- Dùng reusable workflows: `detect-changes.yml` → `build-module.yml`
- Build từng module độc lập khi có thay đổi
- Push image lên ghcr.io với tag `latest` và commit SHA
