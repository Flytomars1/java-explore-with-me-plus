FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY ewm-stats-service/ewm-stats-server/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]