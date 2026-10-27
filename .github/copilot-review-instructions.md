# Code Review Instructions

Review changes against the patterns and boundaries in [`AGENTS.md`](../AGENTS.md). Prioritise correctness, security and payment logic over style.

## Security

- No secrets, tokens or real personal data in code, tests, config or logs. Flag any log statement that can include fødselsnummer, names, account numbers or tokens.
- Give extra scrutiny to authentication and authorization code: `autorisering/`, `altinn/`, `SecurityTokenConfiguration.kt`, `SecurityClientConfiguration.kt`, issuer config in `application-*.yml`, and `@ProtectedWithClaims`, `@Protected` and `@Unprotected` annotations.
- Every endpoint must have the right issuer: `tokenx` under `/api/arbeidsgiver`, `aad` under `/api/saksbehandler`. Flag new `@Unprotected` endpoints.
- Every read or write of a `Refusjon` or `Korreksjon` must go through the access checks in `InnloggetArbeidsgiver` or `InnloggetSaksbehandler`. Flag repository lookups by id in controllers that bypass them.
- Check that employer access still covers diskresjonskode 6/7 and that caseworker access still goes through poao-tilgang.
- Hiding data or actions by role in a frontend is not access control. The API must enforce it.
- Queries must use JPQL/derived queries with bound parameters, never string concatenation.
- Endpoints returning personal data should have `@AuditLogging`.

## Domain and payment logic

- Changes to `Refusjonsberegner.kt`, `Beregning`, 5G, minusbeløp or ferietrekk rules need tests that show the expected amounts.
- Status changes on `Refusjon`/`Korreksjon` must use `krevStatus` and register the relevant domain event.
- Use `Now` instead of `LocalDate.now()`/`Instant.now()`.
- Business errors use `FeilkodeException` with a `Feilkode`.

## Database

- Schema changes need a new Flyway migration `V<next>__<description>.sql`. Existing migrations must not be edited.
- SQL must work in both PostgreSQL (production) and H2 in PostgreSQL mode (tests).
- Flag destructive changes (dropping or renaming columns and tables) and ask how existing data is handled.

## Kafka

- Changes to message classes, topic names, the consumer group id or `JsonConfiguration` can break other teams. Flag them.
- Producers should use `@TransactionalEventListener` and log send failures.
- New Kafka beans must be guarded by `tiltak-refusjon.kafka.enabled`.

## Kotlin style

- Follow the existing style: Norwegian names for domain concepts, thin controllers, logic on the aggregates.
- Prefer null safety over `!!` in new code.

## Tests

- New behaviour needs tests. Use the builders in `TestData.kt` and synthetic fødselsnumre.
- Flag disabled or removed tests.

## GitHub Actions

- `actions/*` (GitHub, immutable) and `nais/*` (Nav) actions are trusted and may use version tags.
- Other third-party actions must be pinned to a full commit SHA with a version comment, for example `uses: owner/action@<sha> # v5.0.0`.
- Third-party actions in use today: `advanced-security/maven-dependency-submission-action@v5` in `dependabot-pom-scan.yml`. It is not pinned to a SHA.
- Workflows should set minimal `permissions` and `persist-credentials: false` on checkout.

## Over-editing

- Flag changes outside the scope of the task: reformatting, renaming, reordering imports, or refactoring unrelated code.
- Diffs should be minimal and focused. Unrelated cleanups belong in a separate pull request.
