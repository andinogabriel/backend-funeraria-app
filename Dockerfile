FROM openjdk:11-jdk-alpine
COPY target/backend-funeraria-app-0.0.1-SNAPSHOT.jar java-app.jar
ENTRYPOINT ["java", "-jar", "java-app.jar"]
LABEL authors="Gabriel"