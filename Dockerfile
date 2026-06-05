# ---- build stage ----
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace

# pull deps first so we get cached layers
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- runtime stage ----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# small init for proper signal handling
RUN apt-get update && apt-get install -y --no-install-recommends tini && rm -rf /var/lib/apt/lists/*

# non-root user
RUN groupadd -r app && useradd -r -g app app
RUN mkdir -p /app/data && chown -R app:app /app

COPY --from=build /workspace/target/*.jar /app/app.jar

USER app
EXPOSE 8080

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC"

ENTRYPOINT ["/usr/bin/tini","--"]
CMD ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
