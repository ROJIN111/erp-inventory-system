# ERP Inventory System

ERP Inventory System is a full-stack inventory management application for small and medium-sized warehouse scenarios. It is built with Spring Boot, Vue 3, MySQL, and Docker Compose.

The project focuses on practical inventory workflows: product master data, inbound orders, outbound orders, stock checks, inventory transaction history, warning messages, user permissions, and operation logs.

Contact: 2812447865@qq.com

## Project Value

Traditional warehouse management often depends on Excel sheets or manual records. That makes stock changes hard to trace, approval responsibility unclear, and inventory warnings easy to miss.

This system provides a structured workflow for inventory operations:

- Product data is managed with SKU, category, price, unit, current stock, and safety stock range.
- Inbound and outbound orders use approval workflows before changing stock.
- Every stock change generates an inventory transaction record.
- Low-stock and high-stock warnings help users find risk earlier.
- RBAC permissions separate different user roles and backend API access.
- Operation logs record key business actions for later review.

## Core Features

| Module | Description |
| --- | --- |
| Dashboard | Shows product count, warning count, inbound/outbound statistics, and chart summaries. |
| Product Management | Maintains product name, SKU, category, price, unit, current stock, and safety stock thresholds. |
| Inbound Management | Creates inbound orders and increases stock after approval. |
| Outbound Management | Creates outbound orders, checks available stock, and decreases stock after approval. |
| Stock Check | Supports inventory check records, check items, stock snapshots, and stock adjustment. |
| Inventory Transactions | Records product, transaction type, quantity, before/after stock, related order, and operator. |
| Inventory Warnings | Generates and handles low-stock/high-stock warning messages. |
| User & Role Permission | Uses JWT authentication and role-based access control. |
| Operation Logs | Records important business operations with an AOP logging layer. |

## Tech Stack

| Layer | Technology |
| --- | --- |
| Backend | Java 17, Spring Boot 3.0.4, Spring Security, MyBatis-Plus, JJWT, EasyExcel, Knife4j |
| Frontend | Vue 3, Vite, TypeScript, Element Plus, Pinia, Vue Router, Axios, ECharts |
| Database | MySQL 8.0 |
| Deployment | Docker Compose, Nginx, Maven, npm |

## Architecture

```text
Browser
  |
  v
Vue 3 Web App / Nginx  (:3000)
  |
  | /api reverse proxy
  v
Spring Boot Backend    (:8080)
  |
  v
MySQL 8.0              (:3307 -> 3306)
```

Docker Compose starts three services:

- `mysql`: stores business data and runs SQL initialization scripts.
- `backend`: builds and runs the Spring Boot REST API.
- `web`: builds the Vue app and serves it through Nginx.

## Quick Start

Requirements:

- Docker
- Docker Compose

Start all services:

```bash
docker compose up --build
```

Open the web app:

```text
http://localhost:3000
```

Default account:

```text
admin / admin123
```

## Service Ports

| Service | Port | Description |
| --- | --- | --- |
| Web | `3000` | Vue frontend served by Nginx |
| Backend | `8080` | Spring Boot REST API |
| MySQL | `3307` | MySQL mapped to container port `3306` |

If these ports are already in use, edit the `ports` mappings in `docker-compose.yml`.

## Database Initialization

The MySQL container automatically creates the `erp_inventory` database and executes:

- `src/main/resources/schema.sql`
- `src/main/resources/data.sql`
- `src/main/resources/demo-data.sql`

The database is stored in the Docker volume `mysql-data`.

Reset the database and import the initialization scripts again:

```bash
docker compose down -v
docker compose up --build
```

## Useful Commands

Run in background:

```bash
docker compose up -d --build
```

View service status:

```bash
docker compose ps
```

View logs:

```bash
docker compose logs
```

View backend logs only:

```bash
docker compose logs backend
```

Stop services:

```bash
docker compose down
```

## Repository Scope

This repository keeps the files required to build and run the system with Docker Compose:

- `docker-compose.yml`
- `Dockerfile`
- `docker/maven-settings.xml`
- `pom.xml`
- `src/main`
- `erp-inventory-web`

Only deployment-related source code, SQL scripts, and Docker configuration are included in this public repository.

## Project Highlights

- Full-stack inventory workflow from product setup to inbound/outbound approval.
- Stock consistency is maintained through service-layer business rules and transaction records.
- JWT authentication and RBAC permissions protect frontend routes and backend APIs.
- Docker Compose provides a repeatable one-command deployment path.
- SQL scripts are idempotent enough for local reset and repeated demo setup.
