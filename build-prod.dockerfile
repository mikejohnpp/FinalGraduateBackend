# Stage 1: Build native image
FROM ghcr.io/graalvm/graalvm-community:21 AS native-build

WORKDIR /app
COPY service-templete/pom.xml .
COPY common/pom.xml common/pom.xml
COPY maui-backend/pom.xml maui-backend/pom.xml
COPY .mvn/ .mvn/
COPY mvnw .

# Build parent + common
RUN ./mvnw -q -ntp -B -N install

# Copy common source code ← THÊM DÒNG NÀY
COPY common common
RUN ./mvnw -q -ntp -B -pl common -am install

COPY maui-backend maui-backend

# Build native executable
WORKDIR /app/maui-backend
RUN ./mvnw --no-transfer-progress native:compile -Pnative

# Stage 2: Runtime
# FROM alpine:latest
COPY --from=native-build /app/maui-backend/target/* /app/demo
ENTRYPOINT ["/app/demo"]