FROM maven:3.9.14-eclipse-temurin-21-alpine AS builder

ARG MODULE

WORKDIR /build

# Copy the dependency specifications
COPY pom.xml pom.xml
COPY common/pom.xml common/pom.xml
COPY ${MODULE}/pom.xml ${MODULE}/pom.xml

# Fix parent pom.xml to build exactly what module need
RUN sed -i '/<modules>/,/<\/modules>/c\
<modules>\n\
  <module>common</module>\n\
  <module>'"$MODULE"'</module>\n\
</modules>' pom.xml

# Debug pom.xml
#RUN cat pom.xml

# Install parent POM to local repository
RUN mvn -q -ntp -B -N install

# Resolve dependencies for `common` module, e.g., shared libraries
# Also build all the required projects needed by the common module.
# In this case, it will also resolve dependencies for the `root` module.
RUN mvn -q -ntp -B -pl common -am dependency:go-offline
# Copy full sources for `common` module
COPY common common
# Install the common module in the local Maven repo (`.m2`)
# This will also install the `root` module.
# See: `la -lat ~/.m2/repository/org/example/*/*`
RUN mvn -q -B -pl common install

# Resolve dependencies for the main application
RUN mvn -q -ntp -B -pl ${MODULE} -am dependency:go-offline
# Copy sources for main application
COPY ${MODULE} ${MODULE}

# RUN mvn -q -ntp -B -pl ${MODULE} clean package -DskipTests
# Package the common and application modules together
RUN mvn -q -ntp -B -pl common,${MODULE} package -DskipTests

FROM eclipse-temurin:21-jre-alpine

RUN mkdir -p /app

ARG MODULE

WORKDIR /app

COPY --from=builder /build/${MODULE}/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]