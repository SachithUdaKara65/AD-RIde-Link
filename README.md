# RideLink

RideLink is a backend ride-sharing system developed using Spring Boot microservices and MongoDB.

## Project Services

- Account Service
- Driver & Vehicle Service
- Ride Management Service
- Fare & Payment Service

## Technology Stack

- Java 21 and Spring Boot
- Spring Data MongoDB
- REST APIs
- Spring Security and JWT, where applicable
- Swagger/OpenAPI
- JUnit and Mockito
- Postman
- GitHub Actions (for CI/CD, if configured)

## Repository Structure

- `account-service/` - account and authentication functionality
- `driver-vehicle-service/` - driver and vehicle functionality
- `ride-management-service/` - ride lifecycle functionality
- `fare-payment-service/` - fare and payment functionality
- `docs/` - architecture diagrams, OpenAPI documents, and Postman collections

## Development Rules

- Do not commit directly to `main`.
- Each member must work on a feature branch.
- Use pull requests for merging changes.
- Review changes before merging.
- Do not commit passwords, API keys, or database credentials.
- Each service must maintain its own database boundary.
- Account Service runs Spring Boot 4 and uses `spring.mongodb.uri` and `spring.mongodb.database`. Its default URI is `mongodb://localhost:27017/ridelink_account_db`, overridable with `ACCOUNT_MONGO_URI`; it refuses to start if the effective database is not `ridelink_account_db`.
- Driver & Vehicle Service runs Spring Boot 3 and uses `spring.data.mongodb.uri` and `spring.data.mongodb.database`. Set `DRIVER_MONGO_URI` in its local `.env` file or process environment; it refuses to start if the effective database is not `driver_vehicle_db`.
- Account Service persists `User` documents only through its own `UserRepository`. Driver & Vehicle Service persists only driver and vehicle domain models; its driver records and API DTOs must not contain account name, email, phone, password, or role. Cross-service data exchange, when needed, must use HTTP APIs and never another service's MongoDB connection or collections.
- Document API changes before integration.

## Team Members

| Name | Student ID | Service owned |
| --- | --- | --- |
| Udakara K.W.S | IT24101148 | Driver & Vehicle Service |
| Madhusanka S.P | IT24102575 | Account Service |
| Punchihewa S.D | IT24102605 | Ride Management Service |
| Chandima M.S.P | IT24102686 | Fare & Payment Service |


## Prerequisites

- Java Development Kit (JDK) 21
- MongoDB running locally or reachable over the network
- Git, to clone the repository
- Postman (optional, for API requests; see [Postman Collection](#postman-collection))

Each service includes a Maven Wrapper, so a separate Maven installation is not required.

## Driver onboarding

Register a driver through Account Service with `role` set to `DRIVER`. Account Service
returns a `userId` and bearer token. Use that token to create the driver's profile through
Driver & Vehicle Service before using driver or vehicle endpoints.
Both services must be configured with the same Base64-encoded `JWT_SECRET` and
`JWT_EXPIRATION_MS`; Driver & Vehicle Service reads these from its environment or local
`.env` file just like Account Service. Set its MongoDB connection URI in
`driver-vehicle-service/.env` as `DRIVER_MONGO_URI`; the local `.env` file is ignored by Git.
Driver login looks up the profile and returns its `driverId`; if the driver has not created
a profile yet, create it through `POST /api/drivers/me/profile` using the token returned at
registration, then log in again.

For a driver account that was registered before automatic provisioning was available, use
`POST /api/drivers/me/profile` on Driver & Vehicle Service with the driver's bearer token
and a JSON body containing `licenseNumber` and optional `serviceArea`. The response contains
the `driverId` to use with vehicle, availability, and location endpoints.
New profiles start as `OFFLINE`; set availability to `AVAILABLE` with
`PATCH /api/drivers/{driverId}/availability` before expecting the driver in
`GET /api/drivers/available`. Service-area matching ignores case and surrounding spaces.


## Technical Report

The technical report is not present in this repository; it is included in the submission package.