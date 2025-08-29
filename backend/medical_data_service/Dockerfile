FROM openjdk:17-jdk-alpine
COPY /target/MedicalDataService-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "/app.jar"]