#FROM gradle:8.5.0-jdk21-jammy AS builder
#WORKDIR /app
#COPY build.gradle.kts settings.gradle.kts ./
##ENTRYPOINT ["/bin/sh"]
#RUN gradle --no-daemon build
#COPY . .
#RUN gradle --no-daemon bootJar

FROM eclipse-temurin:22-jre-alpine
ENV PORT=8000
VOLUME /tmp
RUN touch app.jar
#COPY --from=builder /app/build/libs/*.jar app.jar
COPY ./build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar", "--debug"]
