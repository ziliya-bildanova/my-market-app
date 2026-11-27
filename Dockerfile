FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:21-jre
RUN useradd --system --create-home market
WORKDIR /app
COPY --from=build /build/target/my-market-app-*.jar app.jar
USER market
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
