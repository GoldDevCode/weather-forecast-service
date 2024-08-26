# Use an official OpenJDK runtime as a parent image
FROM openjdk:21-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the Spring Boot jar file to the container
COPY target/weather-forecast-service-0.0.1-SNAPSHOT.jar weather-forecast-service.jar

# Expose the port the app runs on
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "weather-forecast-service.jar"]