FROM eclipse-temurin:21-jre

RUN apt-get update && \
    apt-get install -y curl ca-certificates && \
    curl -fsSL https://deb.nodesource.com/setup_22.x | bash - && \
    apt-get install -y nodejs && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY target/*.jar ./filepilot-ai.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "filepilot-ai.jar"]