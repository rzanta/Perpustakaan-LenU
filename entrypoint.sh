#!/bin/sh
# entrypoint.sh

# Install dependencies and build the project
mvn clean install

# Run the JAR file
exec java -jar /app/app.jar
