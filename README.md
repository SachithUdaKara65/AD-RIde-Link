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
- GitHub Actions for CI (build and unit tests)

## Repository Structure

- `.github/workflows/` - GitHub Actions CI workflows
- `account-service/` - account and authentication functionality
- `driver-vehicle-service/` - driver and vehicle functionality
- `ride-management-service/` - ride lifecycle functionality
- `fare-payment-service/` - fare and payment functionality
- `docs/` - architecture diagrams and API documentation
- `postman/` - Postman collections and environment templates

## Development Rules

- Do not commit directly to `main`.
- Each member must work on a feature branch.
- Use pull requests for merging changes.
- Review changes before merging.
- Do not commit passwords, API keys, or database credentials.
- Each service owns a separate MongoDB database: `ridelink_account_db`, `driver_vehicle_db`, `ridelink_ride_db`, and `ridelink_fare_payment_db`. Services exchange data through APIs; they must not read or write another service's database.
- Account Service and Driver & Vehicle Service verify their configured MongoDB database at startup and refuse to run if it is not their own database. Driver & Vehicle Service also removes legacy Account Service `User` documents from its own `users` collection at startup.
- Driver & Vehicle Service stores the Account Service user ID as its account reference and only driver-domain data (license number, service area, availability, and location); it does not persist account name, email, phone, password, or role.
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

For local development, each service uses a separate MongoDB database by default. Account Service runs Spring Boot 4 and uses `spring.mongodb.*`; Driver & Vehicle Service runs Spring Boot 3 and uses `spring.data.mongodb.*`. Both the URI and database name are explicitly configured so a URI override cannot redirect either service into the other's database:

| Service | MongoDB URI property | Database property | Default database |
| --- | --- | --- | --- |
| Account Service | `spring.mongodb.uri=${ACCOUNT_MONGO_URI:mongodb://localhost:27017/ridelink_account_db}` | `spring.mongodb.database=ridelink_account_db` | `ridelink_account_db` |
| Driver & Vehicle Service | `spring.data.mongodb.uri=${DRIVER_MONGO_URI:mongodb://localhost:27017/driver_vehicle_db}` | `spring.data.mongodb.database=driver_vehicle_db` | `driver_vehicle_db` |

The other services use their own `RIDE_MONGO_URI` and `FARE_MONGO_URI` settings. Do not use one shared URI/database for multiple services. Existing records are not copied automatically if a service is switched to its dedicated database.

## Driver Registration

Register drivers through the Account Service (`POST /api/auth/register`) with role `DRIVER`, a phone number, and a license number. Account Service creates the matching driver profile by sending only the account ID and driver-domain fields (license number and optional service area) to Driver & Vehicle Service. The driver service does not persist the account name, email, phone, password, or role; legacy copies of those fields are removed when it starts. Direct driver-profile creation is not exposed in its Swagger UI.

For local development, Account Service and Driver & Vehicle Service load the same `DRIVER_SERVICE_TOKEN` and `RIDE_LINK_JWT_SECRET` from their service-level `.env` files. These files are Git-ignored. Account Service defaults to `http://localhost:8082` for Driver & Vehicle Service; override it with `DRIVER_SERVICE_URL` when needed. In deployed environments, supply managed secrets through environment variables instead of using development secrets. After changing the JWT secret, restart both services and log in again to obtain a token signed with the shared key.

## Technical Report

The technical report is not present in this repository; it is included in the submission package.