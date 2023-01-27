FROM gradle:7.4.2-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

FROM openjdk:11
RUN mkdir /app
EXPOSE 8080
COPY --from=build /home/gradle/src/build/libs/*.jar /app/application.jar
COPY --from=build /home/gradle/src/.env /app/.env
ENTRYPOINT ["java", "-jar","/app/application.jar"]
