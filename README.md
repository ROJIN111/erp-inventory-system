# ERP Inventory System

ERP Inventory System is a Spring Boot + Vue 3 inventory management system packaged for Docker Compose deployment.

Contact: 2812447865@qq.com

## Services

| Service | Port | Description |
| --- | --- | --- |
| Web | `3000` | Vue frontend served by Nginx |
| Backend | `8080` | Spring Boot REST API |
| MySQL | `3307` | MySQL 8.0 container, mapped to container port `3306` |

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

## Database Initialization

The MySQL container initializes `erp_inventory` from:

- `src/main/resources/schema.sql`
- `src/main/resources/data.sql`
- `src/main/resources/demo-data.sql`

The database is stored in the Docker volume `mysql-data`.

Reset the database and import the initialization scripts again:

```bash
docker compose down -v
docker compose up --build
```

## Port Configuration

If local ports are already in use, edit the `ports` mappings in `docker-compose.yml`.

Default mappings:

```yaml
3000:80
8080:8080
3307:3306
```

## Included Files

This repository keeps the files required to build and run the system with Docker Compose:

- `docker-compose.yml`
- `Dockerfile`
- `docker/maven-settings.xml`
- `pom.xml`
- `src/main`
- `erp-inventory-web`
