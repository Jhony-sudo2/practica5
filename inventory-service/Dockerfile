FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./
COPY order-service/pom.xml order-service/pom.xml
COPY payment-service/pom.xml payment-service/pom.xml
COPY inventory-service/pom.xml inventory-service/pom.xml
COPY shipping-service/pom.xml shipping-service/pom.xml
COPY inventory-service/src inventory-service/src
RUN mvn -B -pl inventory-service -am package -DskipTests
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN useradd --system --uid 10001 appuser
COPY --from=build /app/inventory-service/target/inventory-service-1.0.0.jar app.jar
USER appuser
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
