# HorizonDesk API

A REST API for help desk ticket management, built with Java and Spring Boot.

> ⚠️ **Project status:** This project **remains under active development**.
>
> 🔐 **Security roadmap:** Full integration with **Spring Security** (route-based authentication/authorization) will be implemented soon.

## Overview

HorizonDesk centralizes technical support operations, focusing on:

- user registration and management;
- department registration and management;
- ticket creation and lifecycle management;
- ticket activity history. 

## Core Stack

- Java 21
- Spring Boot 4.1.0
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- Bean Validation (Jakarta Validation)
- Lombok
- DocFlow / OpenAPI
- Testing with JUnit 5, Mockito, and AssertJ

## Architecture (summary)

Layered structure:

- `controller`: REST endpoints
- `service`: business logic
- `repository`: data access (JPA)
- `model`: entities and enums
- `dto`: input/output contracts
- `mapper`: conversion between entities and DTOs
- `exception`: global error handling
- `config`: locale, timezone, auditing, and password encoder

## Current Features

- **Users**
- create, retrieve by UUID, and update data
- change password, role, and department
- activate/deactivate user
- request account deletion
- list active technicians (paginated)
- **Departments**
- create, retrieve by UUID, and update
- activate/deactivate
- list (paginated) and list active options
- **Tickets**
- create, retrieve by UUID, and update
- change priority
- assign technician
- resolve, reopen, and close
- paginated search with filters
- view ticket history

## Base Endpoints

- `/api/v1/users`
- `/api/v1/departments`
- `/api/v1/tickets`

## API Documentation

In the `dev` profile, documentation is available at:

- Swagger UI: `/docs-horizondesk.html`
- OpenAPI JSON: `/api-horizondesk`

In the `prod` profile, documentation is disabled. ## Local setup and execution

### 1) Prerequisites

- JDK 21
- PostgreSQL

### 2) Environment variables

#### Development (`application-dev.yaml`)

- `DB_DEV_URL`
- `DB_DEV_USER`
- `DB_DEV_PASSWORD`

#### Production (`application-prod.yml`)

- `DB_PROD_URL`
- `DB_PROD_USERNAME`
- `DB_PROD_PASSWORD`

### 3) Running the application

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw spring-boot:run
```

> Default profile in `application.yml`: `dev`.

## Running tests

Windows:

```powershell
.\mvnw.cmd test
```

Linux/macOS:

```bash
./mvnw test
```

## Internationalization

Messages in:

- `src/main/resources/i18n/messages_pt_BR.properties`
- `src/main/resources/i18n/messages_en.properties`

Default locale configured: `pt_BR`.

## Next steps (roadmap)

- implementation of authentication/authorization using Spring Security;
- security hardening by profile and endpoint;
- continuous improvement of test coverage and observability.

## License

This project is licensed under the terms defined in `LICENSE`.