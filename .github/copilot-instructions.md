# Copilot Instructions

`tiltak-refusjon-api` is the backend for refunds of wage subsidies and other employment measures at Nav. It serves two frontends: `tiltak-refusjon-arbeidsgiver` (employers, TokenX) and `tiltak-refusjon-saksbehandler` (Nav employees, Azure AD).

## Tech stack

Kotlin, Spring Boot 4, Java 21, Maven, Spring Data JPA, PostgreSQL with Flyway, Kafka, token-support, Unleash, Nais (GCP).

## Before committing

Run the same command as CI:

```bash
mvn package -B -Dsurefire.useFile=false
```

## More

Read [`AGENTS.md`](../AGENTS.md) for access control, local setup, project structure, patterns, domain glossary, Git workflow and boundaries.

## Review
Read [copilot-review-instructions.md](.github/copilot-review-instructions.md) for review specific instructions.