# 🛒 AmazonLite Microservices Platform

[![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-Gateway_&_Eureka-6DB33F?logo=spring&logoColor=white)](https://spring.io/projects/spring-cloud)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-ACID_Writes-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![MongoDB](https://img.shields.io/badge/MongoDB-Fast_Reads-47A248?logo=mongodb&logoColor=white)](https://www.mongodb.com/)
[![Redis](https://img.shields.io/badge/Redis-Refresh_Tokens-DC382D?logo=redis&logoColor=white)](https://redis.io/)
[![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-KRaft_Mode-231F20?logo=apachekafka&logoColor=white)](https://kafka.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![JWT](https://img.shields.io/badge/JWT-Stateless_Auth-black?logo=JSON%20web%20tokens)](https://jwt.io/)

AmazonLite is a high-performance, production-ready, AI-native multi-vendor e-commerce platform. Built on a Java-based microservices architecture, it emphasizes clean code, domain-driven design, zero-trust security, and horizontal scalability.

---

## 🏗 Architecture & Core Patterns

The system is engineered to handle high-throughput e-commerce operations using modern distributed system patterns:

* **API Gateway Offloading:** Centralized JWT validation at the Spring Cloud Gateway layer. The gateway verifies the cryptographic signature and propagates user identity downstream via sanitized HTTP headers (`X-Auth-User-Id`, `X-Auth-Role`).
* **CQRS (Command Query Responsibility Segregation):** The Product Catalog uses **PostgreSQL** for strict transactional writes and **MongoDB** for blazing-fast flexible reads, synchronized in real-time via **Apache Kafka** events.
* **Saga Pattern (Orchestration):** Distributed transactions across Order, Inventory, and Payment services managed via Kafka messaging to handle multi-database rollbacks (Compensating Transactions).
* **JOINED Inheritance Strategy:** The Authentication service uses JPA Joined Inheritance to separate Identity (`users`) from Behavior (`customers`, `sellers`, `admins`), ensuring strict referential integrity.
* **Stateless Auth with Revocation:** JWT-based authentication paired with a Redis-backed refresh token store, enabling instant token revocation without relying on database-heavy session management.
* **Anti-Spoofing Security:** Strict Gateway Route Validation prevents malicious header injection from external clients.

---

## 📁 Project Structure

```text
amazon-lite/
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