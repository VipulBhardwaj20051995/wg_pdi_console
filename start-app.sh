#!/bin/bash

echo "Starting WovenGold PDI Application"
echo "---------------------------------"

# Kill any existing Java processes (optional)
echo "Stopping existing Java processes..."
pkill -f "java -jar target/wovengold-pdi-0.0.1-SNAPSHOT.jar" || true

# Build the application
echo "Building the application..."
./mvnw clean package -DskipTests

# Check if build was successful
if [ $? -ne 0 ]; then
    echo "Build failed! See errors above."
    exit 1
fi

# Create uploads directory if it doesn't exist
if [ ! -d "./uploads" ]; then
    echo "Creating uploads directory..."
    mkdir -p ./uploads
fi

# Run the application
echo "Starting the application on port 9090..."
echo "API testing instructions are in api-tests.md"
echo "---------------------------------"
echo "Press Ctrl+C to stop the application when done"

# Run the application
java -jar target/wovengold-pdi-0.0.1-SNAPSHOT.jar --server.port=9090 