FROM maven:3.8.5-openjdk-17 AS build
COPY pom.xml .
COPY src src

RUN mvn clean package -DskipTests

FROM openjdk:17.0.1-jdk-slim
COPY --from=build /target/Xchange-1.0-SNAPSHOT.jar xchange-app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "xchange-app.jar"]