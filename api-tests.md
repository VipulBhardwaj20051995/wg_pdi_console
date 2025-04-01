# WovenGold PDI API Testing Guide

This document contains instructions for testing the WovenGold PDI (Product Delivery Inspection) API endpoints.

## Prerequisites

- The application must be running
- You can update the port number if needed (examples use port 9090)
- Make sure you have curl installed or use Postman for testing

## 1. Health Check Endpoint

This endpoint confirms if the API is up and running:

```bash
curl http://localhost:9090/health
```

Expected response:
```json
{
  "status": "UP",
  "message": "WovenGold PDI Application is running"
}
```

## 2. Welcome Endpoint

```bash
curl http://localhost:9090/
```

Expected response:
```json
{
  "application": "WovenGold PDI",
  "status": "UP",
  "description": "WovenGold Product Delivery Inspection API"
}
```

## 3. Get Indian States

This endpoint returns the list of all Indian states:

```bash
curl http://localhost:9090/api/pdi/states
```

## 4. Submit PDI Form

This endpoint submits a new PDI form with customer details and files:

### Using curl:

```bash
# Replace with your actual image and video files
curl -X POST http://localhost:9090/api/pdi/submit \
  -F "customerName=John Doe" \
  -F "state=Maharashtra" \
  -F "customerEmail=example@test.com" \
  -F "customerPhone=1234567890" \
  -F "images=@/path/to/image1.jpg" \
  -F "images=@/path/to/image2.jpg" \
  -F "video=@/path/to/video.mp4"
```

### Using Postman:

1. Create a new POST request to `http://localhost:9090/api/pdi/submit`
2. Select `form-data` for the request body
3. Add the following form fields:
   - customerName: John Doe
   - state: Maharashtra  
   - customerEmail: example@test.com
   - customerPhone: 1234567890
   - images: (select file) image1.jpg
   - images: (select file) image2.jpg
   - video: (select file) video.mp4
4. Send the request

## 5. Access H2 Console (Development Only)

To view database records, access the H2 console:

1. Go to `http://localhost:9090/h2-console` in your browser
2. Use these connection settings:
   - JDBC URL: `jdbc:h2:mem:wovengold_pdi`
   - Username: `sa`
   - Password: `password`
3. Click "Connect"
4. Run SQL queries like `SELECT * FROM PDI_SUBMISSIONS` to see submitted data

## Notes on Testing

- The application uses an in-memory H2 database, so data will be lost when the application is restarted
- Email and SMS sending are disabled by default in the testing configuration
- Uploaded files are stored in the `./uploads` directory relative to the application 