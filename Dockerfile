FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY backend/pom.xml ./pom.xml
COPY backend/src/main ./src/main
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/target/guia-estabelecimentos-0.0.1-SNAPSHOT.jar ./app.jar
COPY frontend ./frontend
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
