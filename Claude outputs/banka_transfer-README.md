# Banking Transfer API

A backend API for money transfers between bank accounts. It is built with Spring Boot and uses an **event-driven** design: after a transfer is saved, the API sends an event to RabbitMQ, and a separate listener handles the notification.

## Features

- **Login with JWT** – users log in and get a token. All other endpoints need this token.
- **Safe transfers** – the balance check, the balance update, and the history record run in one database transaction. If anything fails, everything is rolled back.
- **IBAN validation** – requests are checked for a valid Turkish IBAN format (`TR...`).
- **Async notifications** – after a successful transfer, the API publishes an event to RabbitMQ. A listener reads it in the background, so the user gets the response without waiting.
- **Database migrations** – Flyway creates the tables and inserts test data.
- **API docs** – Swagger UI is included.
- **CI** – GitHub Actions builds the project on every push and pull request.

## How a transfer works

```
Client ──POST /api/v1/transfers──▶ TransferService
                                      │ 1. find both accounts by IBAN
                                      │ 2. check the balance
                                      │ 3. update both balances + save history (one transaction)
                                      │ 4. publish an event to RabbitMQ (banka.exchange)
                                      ▼
Client ◀── 200 OK ──────────────  response

RabbitMQ ──▶ NotificationListener  (reads the event and logs the notification)
```

## Tech stack

- Java 17, Spring Boot 3.3
- Spring Security and JWT (jjwt)
- Spring Data JPA and Hibernate
- RabbitMQ (Spring AMQP)
- PostgreSQL
- Flyway
- MapStruct and Lombok
- springdoc OpenAPI (Swagger UI)
- Docker and Docker Compose
- GitHub Actions
- JUnit and Mockito

## API endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/auth/login` | No | Log in and get a JWT |
| GET | `/api/v1/accounts/{iban}/balance` | Yes | Get account balance |
| POST | `/api/v1/transfers` | Yes | Transfer money between two accounts |

## Getting started

### Option 1: Run everything with Docker

You only need Docker. This starts the database, RabbitMQ, and the application.

```bash
docker-compose up -d --build
docker-compose logs -f app
```

### Option 2: Run the app from your IDE

Start only the database and RabbitMQ in Docker:

```bash
docker-compose up -d db rabbitmq
```

Then set the database password and run `BankaApplication.java` from your IDE:

```bash
export DB_PASSWORD=12345   # the password used in docker-compose.yml
```

The default settings in `application.properties` connect to PostgreSQL at `localhost:5432/bankadb` and to RabbitMQ at `localhost`.

Docker services:

| Service | Port |
|---------|------|
| Application | 8080 |
| PostgreSQL | 5432 |
| RabbitMQ | 5672 (management UI: 15672) |

### Configuration

Do not keep real passwords in the repository. You can override settings with environment variables, for example:

```bash
DB_URL=jdbc:postgresql://localhost:5432/bankadb
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password
JWT_SECRET=your_long_random_secret
```

The JWT secret has a default value in the code. Change it before using the project anywhere real.

## Try it

1. Open Swagger UI: http://localhost:8080/swagger-ui.html
2. Call `POST /api/v1/auth/login` with the test user:

   ```json
   { "email": "gonderen@banka.com", "password": "123456" }
   ```

3. Copy the token from the response. Click **Authorize** and enter `Bearer <token>`.
4. Call `POST /api/v1/transfers` with a body like this:

   ```json
   {
     "fromIban": "TR110000000000000000000001",
     "toIban": "TR220000000000000000000002",
     "amount": 100,
     "description": "Test transfer"
   }
   ```

   The migration creates two test accounts:

   | User | IBAN | Balance |
   |------|------|---------|
   | gonderen@banka.com | `TR110000000000000000000001` | 10000 TRY |
   | alici@banka.com | `TR220000000000000000000002` | 500 TRY |

5. Check the application logs. You will see the message that the RabbitMQ listener received.

## Project structure

```
├── src/main/java/com/banka/banka/
│   ├── controller/   # REST endpoints
│   ├── service/      # business logic (transfer, auth, account) + NotificationListener
│   ├── repository/   # Spring Data JPA repositories
│   ├── entity/       # User, Account, TransactionHistory
│   ├── dto/          # request, response, and event objects
│   ├── mapper/       # MapStruct mappers
│   ├── security/     # JWT utilities and user details
│   ├── filter/       # JWT authentication filter
│   ├── config/       # security, RabbitMQ, OpenAPI
│   ├── validation/   # IBAN validator
│   └── exception/    # custom exceptions and global handler
├── src/main/resources/db/migration/   # Flyway scripts (V1 schema, V2 test data)
├── src/test/                          # unit tests for the transfer service
├── Dockerfile                         # multi-stage build (Maven, then a small JRE image)
├── docker-compose.yml                 # app + PostgreSQL + RabbitMQ
└── .github/workflows/ci-pipeline.yml  # GitHub Actions build
```

## Tests

```bash
./mvnw test
```

`TransferServiceTest` checks two cases: a successful transfer, and a transfer that fails because of insufficient balance.

## CI

The GitHub Actions workflow runs on pushes and pull requests to `main` or `master`. It sets up Java 17 and builds the project with Maven (the build step skips tests for now).
