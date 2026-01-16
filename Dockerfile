# -------- Build stage --------
FROM gradle:8.7-jdk17 AS build
WORKDIR /app

COPY build.gradle settings.gradle gradlew /app/
COPY gradle /app/gradle
RUN chmod +x /app/gradlew

COPY . /app
RUN ./gradlew clean bootJar -x test

# -------- Run stage --------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
