# Use an official OpenJDK runtime as a parent image
FROM openjdk:17

# Set the working directory in the container
WORKDIR /app

# Copy the project JAR file to the container
COPY target/perpustakaan-0.0.1-SNAPSHOT.jar app.jar

# Make port 9090 available to the world outside this container
EXPOSE 9090
EXPOSE 9091

# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]