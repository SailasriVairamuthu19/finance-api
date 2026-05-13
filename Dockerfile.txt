FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/finance-api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Duser.timezone=Asia/Kolkata", "-jar", "app.jar"]