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


## Technical Report

The technical report is not present in this repository; it is included in the submission package.