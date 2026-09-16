# Wallet Idempotent Processor

An idempotent payment/wallet event processor that safely handles duplicate
webhook payloads and concurrent debit requests without corrupting wallet
balances.

## Tech Stack
- Java 17, Spring Boot 4.x
- Spring Data JPA + Hibernate
- H2 (in-memory database)
- JUnit 5

## How to Run
```bash
mvn spring-boot:run
```
App starts on `http://localhost:8080`.

## How to Run Tests
Open `TransactionServiceTest` in IntelliJ and run it directly, or:
```bash
mvn test
```
No external setup required — H2 runs entirely in-memory and is seeded fresh
on every run.

## API
`POST /api/v1/transactions/process`
```json
{
  "transactionId": "uuid",
  "userId": "uuid",
  "amount": 100.00,
  "type": "DEBIT"
}
```
- Returns `200 OK` on success
- Returns `409 Conflict` if `transactionId` was already processed
- Returns `400 Bad Request` on insufficient funds or missing wallet

## Design Notes
See `DECISIONS.md` for concurrency handling and known AI-assistance pitfalls.