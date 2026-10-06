FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/*.jar ./filepilot-ai.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "filepilot-ai.jar"]