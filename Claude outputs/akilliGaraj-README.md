# Akıllı Garaj – Backend

REST API for a smart garage app. A user can add vehicles, track fuel purchases, and keep a maintenance history. The API also calculates average fuel consumption and the cost per kilometer.

The frontend of this project is here: [akilli-garaj-frontend](https://github.com/u220709029/akilli-garaj-frontend)

## Features

- **Vehicles** – add, list, update, and delete vehicles for a user.
- **Fuel records** – save each fill-up (liters, price per liter, odometer, full tank or not).
- **Fuel analysis** – total distance, total spent, average consumption (L/100 km), and cost per km.
- **Maintenance records** – save maintenance and repair records with cost, date, and next due date or odometer.
- **Upcoming maintenance** – find records that are due in the next N days (default is 15).
- **Register and login** – passwords are stored with BCrypt.
- **Validation and error handling** – request validation and a global exception handler.
- **Auditing** – JPA auditing is enabled on the entities.

## Tech stack

- Java 17
- Spring Boot 4.1 (Web, Data JPA, Security, Validation)
- MySQL
- MapStruct and Lombok
- Maven (Maven Wrapper included)

## Project structure

The Maven project is in the inner `akilliGaraj/` folder.

```
akilliGaraj/akilliGaraj/src/main/java/com/example/akilliGaraj/
├── controller/    # REST endpoints (vehicles, fuels, maintenances)
├── services/      # business logic
├── repository/    # Spring Data JPA repositories
├── entities/      # database entities (Vehicle, FuelRecord, MaintenanceRecord, User)
├── dtos/          # request and response objects
├── mapper/        # MapStruct mappers
├── security/      # Spring Security config, register and login
├── exception/     # global exception handler
└── audit/         # JPA auditing config
```

## Getting started

### Requirements

- JDK 17
- MySQL 8 (or similar)

### Steps

1. Create the database:

   ```sql
   CREATE DATABASE akilli_garaj_db;
   ```

2. Open `akilliGaraj/src/main/resources/application.properties` and set your own MySQL user and password:

   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/akilli_garaj_db
   spring.datasource.username=YOUR_DB_USER
   spring.datasource.password=YOUR_DB_PASSWORD
   ```

   The tables are created automatically (`ddl-auto=update`).

3. Run the application:

   ```bash
   cd akilliGaraj
   ./mvnw spring-boot:run
   ```

   The API starts at `http://localhost:8080`.

## API overview

Base path: `/api/v1`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/vehicles/user/{userId}` | Add a vehicle for a user |
| GET | `/vehicles/user/{userId}` | List a user's vehicles |
| GET | `/vehicles/{vehicleId}` | Get one vehicle |
| PUT | `/vehicles/{vehicleId}` | Update a vehicle |
| DELETE | `/vehicles/{vehicleId}` | Delete a vehicle |
| POST | `/fuels` | Add a fuel record |
| GET | `/fuels/vehicle/{vehicleId}` | Fuel history of a vehicle |
| GET | `/fuels/vehicle/{vehicleId}/summary` | Total distance, total spent, average consumption, cost per km |
| GET | `/fuels/vehicle/{vehicleId}/average-consumption` | Average consumption from the last two full-tank records |
| GET / PUT / DELETE | `/fuels/{recordId}` | Get, update, or delete a fuel record |
| POST | `/maintenances/` | Add a maintenance record |
| GET | `/maintenances/vehicle/{vehicleId}` | Maintenance history of a vehicle |
| GET | `/maintenances/upcoming?days=15` | Upcoming maintenance records |
| GET / PUT / DELETE | `/maintenances/{recordId}` | Get, update, or delete a maintenance record |
| POST | `/register` | Create a user (`userName` and `password` as request parameters) |
| POST | `/login` | Check username and password (JSON body) |

Maintenance types: `PERIODIC`, `REPAIR`, `INSPECTION`, `INSURANCE`.

## How average consumption is calculated

The API takes the last two **full tank** records of a vehicle. Then it uses this formula:

```
liters in the latest fill-up / distance between the two odometer values * 100
```

At least two full-tank records are needed.

## Security notes

- Access is controlled with Spring Security and HTTP Basic authentication.
- `GET`, `POST`, and `PUT` requests under `/api/v1/**` need the `ADMIN` role. Users created with `/register` get the `ROLE_USER` role, so an admin user must be added to the database by hand for now.
- `DELETE` requests are not allowed by the current security rules.
- Do not commit real database passwords. Keep them in environment variables or a local properties file.

## Roadmap

- Return a token (JWT) after login.
- Open the API to normal users, with checks so that each user sees only their own vehicles.
- Add more tests.
