# Wovengold PDI Application

This is a REST API application for the WovenGold Product Delivery Inspection (PDI) system. 

## Features

- Customer information submission
- Indian state selection
- Image uploads (up to 4 images)
- Video upload
- Email notifications
- SMS notifications

## Technology Stack

- Java 17
- Spring Boot 3.2.3
- MySQL Database
- Maven
- Docker (optional)

## Running Locally

### Prerequisites

- JDK 17
- MySQL Database
- Maven (or use the included maven wrapper)

### Configuration

Edit `src/main/resources/application.properties` to configure:

- Database connection
- Email settings
- Upload directory
- SMS service (if applicable)

### Build and Run

```bash
# Using Maven
./mvnw clean package
java -jar target/wovengold-pdi-0.0.1-SNAPSHOT.jar

# Or use Spring Boot Maven plugin
./mvnw spring-boot:run
```

## Running with Docker

```bash
# Build and run with docker-compose
docker-compose up -d
```

## API Endpoints

### Submit PDI Form

```
POST /api/pdi/submit
```

Parameters:
- `customerName`: Customer's full name
- `state`: Indian state name
- `customerEmail`: Customer's email address
- `customerPhone`: Customer's 10-digit phone number
- `images`: Up to 4 image files (multipart/form-data)
- `video`: Video file (multipart/form-data)

### Get Indian States

```
GET /api/pdi/states
```

Returns a list of all Indian states.

## Sample Request

Using curl:

```bash
curl -X POST http://localhost:8080/api/pdi/submit \
  -F "customerName=John Doe" \
  -F "state=Maharashtra" \
  -F "customerEmail=john@example.com" \
  -F "customerPhone=1234567890" \
  -F "images=@image1.jpg" \
  -F "images=@image2.jpg" \
  -F "video=@video.mp4"
``` 