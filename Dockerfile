FROM gradle:7.4.2-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
#RUN gradle build --no-daemon

EXPOSE 8080
ENTRYPOINT [ "gradle", "run" ]


#RUN mkdir /app
#COPY --from=build /home/gradle/src/build/libs/*.jar /app/application.jar
#ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-jar","/app/application.jar"]