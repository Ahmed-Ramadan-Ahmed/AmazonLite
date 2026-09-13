# AmazonLite Microservices Platform

AmazonLite is a high-performance, production-ready, multi-vendor e-commerce platform. It is built on a Java-based microservices architecture, emphasizing clean code, domain-driven design, security, and extreme scalability.

## 🏗 Architecture Overview

The system is designed to handle high-throughput e-commerce operations using modern distributed system patterns:

* **API Gateway Offloading:** Centralized JWT validation at the Spring Cloud Gateway layer. The gateway verifies the token and propagates user identity downstream via clean HTTP headers (`X-Auth-User-Id`, `X-Auth-Role`), keeping microservices stateless and unaware of cryptography.
* **CQRS (Command Query Responsibility Segregation):** The Product Catalog uses PostgreSQL for strict transactional writes (Command) and MongoDB for blazing-fast flexible reads (Query), synchronized in real-time via Apache Kafka events.
* **Database per Service:** Strict domain isolation. Each microservice manages its own data store.
* **JOINED Inheritance Strategy:** The Authentication service uses JPA Joined Inheritance to separate Identity (`users` table) from Behavior (`customers`, `sellers`, `admins` tables), allowing strict referential integrity without sparse NULL columns.
* **Stateless Auth with Revocation:** JWT-based authentication paired with a Redis-backed refresh token store, allowing instant token revocation without relying on database-heavy session management.
* **Saga Pattern (Orchestration):** *[In Progress]* Distributed transactions across Order, Inventory, and Payment services managed via Kafka messaging to handle cross-service rollbacks (Compensating Transactions).

## 🛠 Tech Stack

* **Language:** Java 17
* **Framework:** Spring Boot 3.x, Spring Cloud (Eureka, Gateway)
* **Relational Database:** PostgreSQL (Auth Service, Product Command, Order Service)
* **NoSQL Database:** MongoDB (Product Query)
* **Caching & Tokens:** Redis (Refresh Tokens)
* **Message Broker:** Apache Kafka (KRaft mode)
* **Build Tool:** Maven (Multi-module Monorepo)
* **Containerization:** Docker & Docker Compose
* **API Documentation:** OpenAPI 3 / Swagger UI

## 📁 Project Structure

├── common-shared/       # Universal dictionary: DTOs, Kafka Events, global enums
├── discovery-server/    # Netflix Eureka: Service registry and discovery
├── api-gateway/         # Spring Cloud Gateway: Routing and JWT security filter
├── auth-service/        # Identity management, JWT generation, and Role profiles
├── product-service/     # Product catalog (CQRS: Postgres + Mongo + Kafka)
├── order-service/       # Order processing and Saga orchestrator
├── docker-compose.yml   # Local infrastructure definition
└── pom.xml              # Root Maven POM

## 🚀 Getting Started

### Prerequisites
* Java 17
* Maven 3.8+
* Docker and Docker Compose

### 1. Start the Infrastructure
Spin up PostgreSQL, MongoDB, Redis, and Kafka in the background:
docker-compose up -d

### 2. Build the Shared Library
Because microservices depend on `common-shared`, you must install it to your local Maven cache first:
mvn clean install -pl common-shared

### 3. Run the Microservices
Open separate terminal tabs and start the services in this exact order:

**1. Service Registry:**
mvn spring-boot:run -pl discovery-server

**2. API Gateway:**
mvn spring-boot:run -pl api-gateway

**3. Backend Services:**
mvn spring-boot:run -pl auth-service
mvn spring-boot:run -pl product-service
mvn spring-boot:run -pl order-service

## 📖 API Documentation

Once the services are running, you can access the live interactive API documentation via Swagger UI:

* **Auth Service API:** http://localhost:8081/swagger-ui/index.html
* **API Gateway Route:** Accessing services via the gateway (Port 8080) automatically applies the JWT security filters.

### Testing Flow
1. Register a Seller via `/api/v1/auth/register`.
2. Login via `/api/v1/auth/login` to receive a JWT Access Token.
3. Use the Gateway URL (`http://localhost:8080/product-service/api/v1/products`) with the `Authorization: Bearer <token>` header to create a product.
4. Verify the CQRS sync by fetching the product from MongoDB via a `GET` request.

## 🛡 Security Note
This project implements Gateway Route Validation to prevent **Header Spoofing**. Downstream services blindly trust the `X-Auth-*` headers injected by the Gateway. The Gateway is strictly configured to strip these headers from incoming external requests and only append them after successful JWT cryptographic validation.