# --- Stage 1: Build ---
# Uses a full JDK image with Maven to compile the app into a runnable jar.
# This stage's ~600MB of build tools never ends up in the final image.
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copy just the Maven wrapper + pom.xml first, and download dependencies
# before copying source code. Docker caches each layer - as long as
# pom.xml hasn't changed, this dependency download layer is reused on
# every future build instead of re-downloading everything from scratch.
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Now copy the actual source and build.
COPY src src
RUN ./mvnw clean package -DskipTests -B

# --- Stage 2: Run ---
# A much smaller image with just a JRE (no compiler, no build tools) -
# this is the image that actually gets deployed and run on Render.
FROM eclipse-temurin:21-jre AS run

WORKDIR /app

# Copy only the built jar from the build stage - none of the source code,
# Maven cache, or build tools make it into this final image.
COPY --from=build /app/target/*.jar app.jar

# Render sets PORT at runtime and routes traffic to it - the app reads
# this via server.port=${PORT:8080} in application.properties.
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
