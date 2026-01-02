# Car Fuel Tracker

A Java application for managing cars and tracking fuel consumption with a REST API backend and CLI client.

## Project Structure

```
car-fuel-tracker/
├── backend/          # Backend REST API server
└── cli/              # Command-line interface client
```

## Features

### Backend (Part 1 & 2)
- REST API for car and fuel management
- In-memory storage (no database required)
- Manual Java Servlet implementation
- Error handling with proper HTTP status codes

### CLI (Part 3)
- Command-line interface for interacting with the backend
- HTTP communication using `java.net.http.HttpClient`
- Support for creating cars, adding fuel entries, and viewing statistics

## Requirements

- Java 11 or higher
- Maven 3.6+

## Building the Project

### Backend

```bash
cd backend
mvn clean package
```

### CLI

```bash
cd cli
mvn clean package
```

## Running the Application

### 1. Start the Backend Server

```bash
cd backend
mvn exec:java
```

The server will start on `http://localhost:8080`

### 2. Use the CLI Client

In a separate terminal:

```bash
cd cli
mvn exec:java -Dexec.args="<command>"
```

## CLI Commands

### Create a Car

```bash
mvn exec:java -Dexec.args="create-car --brand Toyota --model Corolla --year 2018"
```

### Add Fuel Entry

```bash
mvn exec:java -Dexec.args="add-fuel --carId 1 --liters 40 --price 52.5 --odometer 45000"
```

### View Fuel Statistics

```bash
mvn exec:java -Dexec.args="fuel-stats --carId 1"
```

Expected output:
```
Total fuel: 120.0 L
Total cost: 155.00
Average consumption: 8.0 L/100km
```

## API Endpoints

### REST API

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/cars` | Create a new car |
| GET | `/api/cars` | List all cars |
| GET | `/api/cars/{id}` | Get car by ID |
| POST | `/api/cars/{id}/fuel` | Add fuel entry to a car |
| GET | `/api/cars/{id}/fuel/stats` | Get fuel statistics for a car |

### Servlet Endpoint

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/servlet/fuel-stats?carId={id}` | Get fuel statistics (manual servlet implementation) |

## API Request/Response Examples

### Create Car

**Request:**
```json
POST /api/cars
{
  "brand": "Toyota",
  "model": "Corolla",
  "year": 2018
}
```

**Response:**
```json
{
  "id": 1,
  "brand": "Toyota",
  "model": "Corolla",
  "year": 2018,
  "fuelEntries": []
}
```

### Add Fuel Entry

**Request:**
```json
POST /api/cars/1/fuel
{
  "liters": 40,
  "price": 52.5,
  "odometer": 45000
}
```

**Response:**
```json
{
  "id": 1,
  "liters": 40.0,
  "price": 52.5,
  "odometer": 45000.0,
  "timestamp": "2024-01-15T10:30:00"
}
```

### Get Fuel Statistics

**Request:**
```
GET /api/cars/1/fuel/stats
```

**Response:**
```json
{
  "totalFuel": 120.0,
  "totalCost": 155.00,
  "averageConsumption": "8.0 L/100km",
  "totalDistance": 500.0
}
```

## Error Handling

The API returns appropriate HTTP status codes:
- `200 OK` - Successful request
- `404 Not Found` - Car not found
- `400 Bad Request` - Invalid request parameters
- `500 Internal Server Error` - Server error

## Technical Details

### Backend
- **Framework**: Spark Java (lightweight web framework)
- **JSON Processing**: Jackson
- **Storage**: In-memory (HashMap)
- **Servlet**: Manual HttpServlet implementation

### CLI
- **HTTP Client**: `java.net.http.HttpClient` (Java 11+)
- **JSON Processing**: Jackson
- **Argument Parsing**: Custom parser

## Development Notes

- The backend uses in-memory storage, so data is lost when the server stops
- The servlet implementation demonstrates manual request/response handling
- All endpoints return JSON responses
- CORS is enabled for cross-origin requests

## License

This project is part of a technical assignment.
