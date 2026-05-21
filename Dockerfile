FROM gradle:9.4.1-jdk25 AS build
WORKDIR /app
COPY . .
RUN chmod +x gradlew && \
    ./gradlew bootJar --no-daemon

FROM eclipse-temurin:25-jre
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]