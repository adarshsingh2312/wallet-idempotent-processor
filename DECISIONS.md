# Decisions Log

## 1. How did I handle the concurrency race condition?

Used database-level pessimistic locking (`SELECT ... FOR UPDATE` via
`@Lock(LockModeType.PESSIMISTIC_WRITE)` on `WalletRepository.findByIdForUpdate()`),
wrapped inside a `@Transactional` service method. The lock is acquired first,
before the idempotency check — this fully serializes concurrent requests for
the same wallet: only one thread can read and mutate the balance at a time,
and every subsequent thread sees the already-updated balance once it acquires
the lock. The lock is held until the transaction commits or rolls back, so no
two threads can ever read a stale balance.

## 2. Where did my AI assistant give an incorrect or sub-optimal suggestion?

- Initially generated `Transaction.transactionId` (via AI-assisted scaffolding)
  with `@GeneratedValue`, which is wrong for an idempotency key — it must come
  from the client's request payload, not be DB-generated, otherwise duplicate
  detection can never work.
- Missed `@Enumerated(EnumType.STRING)` on the `Type` and `Status` enum fields
  initially — without it, Hibernate stores enums by ordinal position (0, 1, 2),
  which silently breaks if the enum's declaration order ever changes.
- Initial verification code used the locking query (`findByIdForUpdate`) to
  just read the balance for test assertions, outside of any active
  transaction. This threw `TransactionRequiredException` at runtime, since a
  pessimistic lock can only be acquired within an active DB transaction. Fixed
  by adding a separate, lock-free `findByUserId()` for read-only verification.