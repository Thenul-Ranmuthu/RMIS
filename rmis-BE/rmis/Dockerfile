FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/*.jar app.jar

RUN mkdir -p /app/uploads/certifications

EXPOSE 5050

ENTRYPOINT ["java", "-jar", "app.jar"]