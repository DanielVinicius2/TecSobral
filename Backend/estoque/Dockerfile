FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
COPY src src
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:25-jre

RUN apt-get update \
    && apt-get install -y --no-install-recommends postgresql-client \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 --create-home app \
    && mkdir -p /app/backups \
    && chown -R app:app /app

WORKDIR /app
COPY --from=build --chown=app:app /workspace/target/estoque-0.0.1-SNAPSHOT.jar /app/app.jar

ENV APP_BACKUP_PG_DUMP=/usr/bin/pg_dump \
    APP_BACKUP_PG_RESTORE=/usr/bin/pg_restore

USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
