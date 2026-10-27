# AGENTS.md

`tiltak-refusjon-api` is the backend for refunds (refusjon) of wage subsidies and other employment measures (tiltak) at Nav. It is a Kotlin/Spring Boot service owned by team `arbeidsgiver` (team-tiltak) and runs on Nais (GCP).

## How it fits together

- **Input:** When a tilskuddsperiode (subsidy period) on an agreement is approved, the agreement system publishes a message on Kafka. This service consumes it and creates a `Refusjon` (`TilskuddsperiodeKafkaLytter` → `RefusjonService.opprettRefusjon`). Annulled and shortened periods arrive the same way.
- **Employers (arbeidsgiver)** log in through the frontend `tiltak-refusjon-arbeidsgiver` (TokenX). They fetch income from Inntektskomponenten (ikomp) and the bank account number from sokos-kontoregister, mark income lines, and approve (`godkjenn`) the claim.
- **Nav caseworkers (saksbehandler)** log in through the frontend `tiltak-refusjon-saksbehandler` (Azure AD). They search refunds, extend deadlines and create and complete korreksjoner (corrections).
- **Output:** Approved refunds and corrections go to Kafka for payment (consumed by tiltak-okonomi). Payment status comes back on `arbeidsgiver.tiltak-refusjon-endret-betalingsstatus`. Status changes, notifications (varsel) and audit events are also published on Kafka. All topics are in `Topics.kt`.
- **Other dependencies:** `arbeidsgiver-altinn-tilganger` (Altinn rights), `poao-tilgang` (caseworker access to a person), PDL (diskresjonskode via `tiltak-persondata`), Norg2 (unit names), `g.nav.no` (grunnbeløp), `tiltak-pdfgen` (PDF), Unleash (feature toggles).

Both frontend apps are built from one repo, [navikt/tiltak-refusjon](https://github.com/navikt/tiltak-refusjon). The old repos `tiltak-refusjon-arbeidsgiver` and `tiltak-refusjon-saksbehandler` are archived. The app names are unchanged.

The inbound and outbound access policy is in `.nais/nais.yml`.

## Access control

Both frontends call the same API under different path prefixes:

| Path                                    | Issuer               | Users                                              |
| --------------------------------------- | -------------------- | -------------------------------------------------- |
| `/api/arbeidsgiver/**`                  | `tokenx`             | Employers, via `tiltak-refusjon-arbeidsgiver`      |
| `/api/saksbehandler/**`                 | `aad`                | Nav employees, via `tiltak-refusjon-saksbehandler` |
| `/internal/admin/**`                    | `azure-access-token` | Admin group in the `@ProtectedWithClaims` claim    |
| `/internal/actuator/**`                 | none                 | Nais health checks and Prometheus                  |
| `/fake-kafka/**`                        | none                 | Only when `tiltak-refusjon.kafka.fake=true`        |

Access is enforced in two layers, and both are required:

1. **Token issuer** on the controller with `@ProtectedWithClaims(issuer = ...)` from token-support (`SecurityTokenConfiguration.kt`).
2. **Resource access** in the domain objects `InnloggetArbeidsgiver` and `InnloggetSaksbehandler` (`autorisering/`), created by `InnloggetBrukerService`:
   - Employers must have the Altinn right `nav_tiltak_tiltaksrefusjon` for the refund's `bedriftNr`. For participants with diskresjonskode 6 or 7, the employer also needs the address-block right (`sjekkHarTilgangTilRefusjonerForBedrift`).
   - Caseworkers are checked against `poao-tilgang` per participant (`TilgangskontrollService`). Correction actions also require the beslutter AD group (`AdGruppeTilganger`).

Every endpoint that reads or changes a `Refusjon` or `Korreksjon` must go through `finnRefusjon`/`finnKorreksjon` or an equivalent check in these classes. Never look up a refund directly from the repository in a controller.

`FeatureToggleController` is `@Unprotected`. It relies on `InnloggetBrukerService` throwing when no valid token is present.

Any role-based hiding in the frontends is not a security control. The checks above are.

## Setup & Local Development

Requirements: Java 21 and Maven. There is no Maven wrapper.

Dependencies come from Maven Central and Nav's GitHub package mirror (`https://github-package-registry-mirror.gc.nav.no/cached/maven-release`, configured in `pom.xml`). No credentials are needed.

The production entry point `TiltakRefusjonApplication` refuses to start without the `MILJO` environment variable. Run locally with one of these classes in `src/test/kotlin/no/nav/arbeidsgiver/tiltakrefusjon/`:

- **`LokalTiltakRefusjonApplication`** (profiles `local`, `wiremock`, `testdata`). Uses in-memory H2 in PostgreSQL mode, Kafka disabled, fake kontoregister/ikomp/Norg/poao-tilgang, Unleash mock, and WireMock stubs from `src/test/resources/mappings/` on port 8090. The app runs on port 8081 and loads test data at startup (`TestDataLaster`). Config: `src/test/resources/application-local.yml`.
- **`DockerComposeTiltakRefusjonApplication`** (profiles `dockercompose`, `wiremock`, `testdata`). Expects PostgreSQL on `localhost:7432` and Kafka on `localhost:9092`. The repo does not contain a compose file for these. Config: `src/test/resources/application-dockercompose.yml`.

Local tokens are validated against `tiltak-fakelogin` (`FAKE_LOGIN_URI`, default `https://tiltak-fakelogin.ekstern.dev.nav.no`). PDF generation needs `tiltak-pdfgen` on `localhost:9001` (`TILTAK_PDFGEN_URI`).

With the Unleash mock, a request header `features: enabled` turns on all toggles. Otherwise all toggles are off.

### Labs

`tiltak-refusjon-api-labs` runs in dev-gcp with the local setup (H2, WireMock, test data). `bygg-og-deploy-labs.yml` removes `<scope>test</scope>` from `pom.xml` during the build and uses `DockerfileLabs`, which starts `LokalTiltakRefusjonApplicationKt` from the test classes. Changes to test classes, `application-local.yml` or WireMock mappings therefore also affect labs.

## Build & Test Commands

CI (`bygg-branch.yml` and `bygg-og-deploy.yml`) runs:

```bash
mvn package -B -Dsurefire.useFile=false
```

This compiles, runs all tests and builds `target/app.jar` with the CycloneDX SBOM. Run a single test class with `mvn test -Dtest=RefusjonTest`.

Run `mvn package -B -Dsurefire.useFile=false` before every commit. There is no separate lint or formatting step in CI. IntelliJ code style is in `.idea/codeStyles/`.

## Project Structure

```text
.github/workflows/   CI: build branch, build and deploy (dev + prod), labs, dependency snapshot
.nais/               Nais manifests and alerts
src/main/kotlin/no/nav/arbeidsgiver/tiltakrefusjon/
  refusjon/          Core domain: Refusjon, Korreksjon, calculation, controllers, repositories, Kafka
  refusjon/events/   Domain events registered on aggregates and logged in hendelseslogg
  autorisering/      Logged-in user, Altinn/poao-tilgang/AD group access checks
  altinn/            Client for arbeidsgiver-altinn-tilganger
  tilskuddsperiode/  Kafka messages and listener for subsidy periods
  inntekt/           Inntektskomponenten client (and fake)
  okonomi/           sokos-kontoregister client (and fake)
  utregning/         Presentation of the calculation (Utregning)
  varsling/          Notifications to employers via Kafka, scheduled jobs
  audit/             Audit logging (@AuditLogging) to Kafka or console
  hendelseslogg/     Event log shown to caseworkers
  featuretoggles/    Unleash setup and endpoints
  persondata/ norg/ grunnbelop/ dokgen/ pdf/ rapport/ automatisk_utbetaling/ ...
src/main/resources/
  application*.yml   Shared config + per-profile config (dev-gcp, prod-gcp)
  db/migration/      Flyway migrations (V1__ ... V63__)
src/test/kotlin/     Tests, test data builders (TestData.kt), local runner apps
src/test/resources/  Local/dockercompose config, WireMock mappings, Cucumber features
Dockerfile           Production image
DockerfileLabs       Labs image (runs the local app from test classes)
admin-kall.http      Example calls to /internal/admin
```

### `.nais/`

- `nais.yml` is a **template** with `{{...}}` placeholders. Values come from `dev.yml` (dev-gcp) and `prod.yml` (prod-gcp) when deploying.
- `alertsNy.yml` and `unleash-apitoken.yml` are deployed to both clusters. `alertsTesting.yml` is deployed only to dev.
- `dev-gcp-labs.yml` is a standalone manifest for the labs app, not a template.

A change to `nais.yml` affects both environments. Add environment values to both `dev.yml` and `prod.yml`.

## Testing

- JUnit 5, AssertJ, mockito-kotlin, Spring Boot Test with MockMvc, WireMock, `spring-kafka-test` and Cucumber.
- Tests live in `src/test/kotlin/` mirroring the main package, named `<Class>Test.kt`. Test names are Norwegian sentences in backticks.
- Domain tests (`RefusjonTest`, `KorreksjonTest`, `RefusjonsberegnerTest`) are plain unit tests. Tests without a Spring context that use fødselsnumre need the `@SyntetiskeFnr` annotation.
- Spring tests use `@SpringBootTest` with `@ActiveProfiles("local")`, an H2 database, and `@MockitoBean` for external services. `RefusjonApiTest` shows how to call the API with arbeidsgiver and saksbehandler tokens.
- Build test data with the helpers in `TestData.kt` (`enRefusjon()`, `etTilskuddsgrunnlag()`, ...) instead of creating entities by hand.
- Cucumber scenarios for the calculation are in `src/test/resources/features/refusjon.feature`, with steps in `RefusjonsberegningSteps.kt`, run by `RunCucumberTest`.
- Only use synthetic test data. Never use real fødselsnumre, names or account numbers.

Areas without dedicated tests: most of `AdminController`, `PoaoTilgangServiceImpl`, the Kafka producers, `StatusJobb`, `UbetaltRefusjonRapport`, PDF generation and the Norg and grunnbeløp clients. Tests use H2, not PostgreSQL, so PostgreSQL-specific SQL in migrations is not fully verified by CI.

## Patterns

Use the existing solution. Do not add a parallel variant.

- **Domain logic** lives on the aggregates `Refusjon` and `Korreksjon` (`AbstractAggregateRoot`). State changes check status with `krevStatus` and register a domain event with `registerEvent`. Controllers stay thin. They get the logged-in user from `InnloggetBrukerService` and call methods on it.
- **Errors:** throw `FeilkodeException(Feilkode.X)` for business errors. It becomes HTTP 400 with a `feilkode` header the frontends read. Add new codes to the `Feilkode` enum. `TilgangskontrollException` becomes 403, `RessursFinnesIkkeException` 404 (`ExceptionHåndtering.kt`).
- **Concurrent edits:** employer write endpoints take `If-Unmodified-Since` and call `sjekkSistEndret`. Update `sistEndret` via `RefusjonService.oppdaterSistEndret`.
- **Time:** use `Now` (`utils/Now.java`), not `LocalDate.now()`, so tests can control time.
- **IDs:** entities use ULID strings (`ulid()`).
- **Persistence:** Spring Data JPA repositories with derived queries or JPQL `@Query` with named parameters. `spring.jpa.open-in-view` is off. Use `@Transactional` on write endpoints.
- **Migrations:** Flyway in `src/main/resources/db/migration/`, named `V<next number>__<description>.sql`. Never edit an existing migration. Production runs PostgreSQL 16, tests run H2 in PostgreSQL mode, so the SQL must work in both.
- **Kafka:** topic names in `Topics.kt`. Producers listen to domain events with `@TransactionalEventListener` so messages are sent only after commit. Consumers take a `String` and parse with the shared `ObjectMapper`. Kafka beans are guarded by `@ConditionalOnProperty("tiltak-refusjon.kafka.enabled")`. Outgoing messages use `JsonConfiguration.kafkaJsonMapper`, which writes dates as numbers to keep the contract with consumers.
- **JSON:** Jackson 3 (`tools.jackson`). Shared setup in `JsonConfiguration.medFellesOppsett()`, which accepts property names starting with æ, ø or å.
- **Outgoing HTTP:** `RestTemplate` beans in `SecurityClientConfiguration` add tokens through token-support client registrations in `application-<env>.yml`. In other profiles they have no token.
- **External services with a fake:** select by `@ConditionalOnProperty("tiltak-refusjon.<service>.fake")` or `@Profile`. Add a fake and a WireMock mapping when adding an integration.
- **Feature toggles:** Unleash via `FeatureToggleService.isEnabled(feature, innloggetBruker)`. Toggles live in the `team-tiltak` Unleash instance.
- **Audit logging:** annotate endpoints that return personal data with `@AuditLogging("<description>")`.
- **Scheduled jobs:** `@Scheduled` with ShedLock (`ShedLockConfiguration`), since the app runs two replicas.
- **Logging:** SLF4J. Log IDs (refusjon id, avtaleNr), never fødselsnummer, names, account numbers or tokens.

## Domain Glossary

| Term                          | Meaning                                                                                      | Type                                   |
| ----------------------------- | -------------------------------------------------------------------------------------------- | -------------------------------------- |
| Refusjon                      | An employer's refund claim for one subsidy period                                            | `Refusjon`                             |
| Korreksjon                    | Correction of an already processed refund, made by a caseworker                              | `Korreksjon`                           |
| Refundering                   | Common interface for `Refusjon` and `Korreksjon`                                             | `Refundering`                          |
| Tilskuddsperiode              | Subsidy period in an agreement. One refund per period                                        | `TilskuddsperiodeGodkjentMelding`      |
| Tilskuddsgrunnlag             | Snapshot of agreement data for the period: rates, amount, participant, employer              | `Tilskuddsgrunnlag`                    |
| Refusjonsgrunnlag             | Everything the calculation is based on: tilskuddsgrunnlag, income, account, beregning        | `Refusjonsgrunnlag`                    |
| Inntektsgrunnlag/Inntektslinje | Income fetched from Inntektskomponenten, line by line                                        | `Inntektsgrunnlag`, `Inntektslinje`    |
| Beregning                     | Result of the refund calculation                                                             | `Beregning`, `Refusjonsberegner.kt`    |
| Utregning                     | Line-by-line presentation of the calculation                                                 | `Utregning`                            |
| Tiltakstype                   | Type of measure, e.g. varig/midlertidig lønnstilskudd, sommerjobb, VTAO, mentor             | `Tiltakstype`                          |
| Avtale / avtaleNr / løpenummer | The agreement and the period's sequence number                                              | fields on `Tilskuddsgrunnlag`          |
| Bedrift / bedriftNr           | Employer organisation (sub-unit) number                                                      | `bedriftNr`                            |
| Deltaker / deltakerFnr        | Participant in the measure, identified by fødselsnummer                                      | `deltakerFnr`, `Fnr`                   |
| Arbeidsgiver                  | Employer user logged in via ID-porten/TokenX                                                 | `InnloggetArbeidsgiver`                |
| Saksbehandler / Beslutter     | Nav employee. Beslutter is the AD group that may make corrections                            | `InnloggetSaksbehandler`, `BrukerRolle` |
| Veileder                      | Nav advisor responsible for the agreement                                                    | `veilederNavIdent`                     |
| Enhet                         | Nav unit                                                                                     | `enhet`                                |
| Frist for godkjenning         | Deadline for the employer to submit. Can be extended (forlenge frist)                        | `fristForGodkjenning`                  |
| Status                        | Refund status, e.g. FOR_TIDLIG, KLAR_FOR_INNSENDING, SENDT_KRAV, UTBETALT, UTGÅTT            | `RefusjonStatus`                       |
| Korreksjonstype               | Correction status, e.g. UTKAST, TILLEGSUTBETALING, OPPGJORT, TILBAKEKREVING                  | `Korreksjonstype`                      |
| Korreksjonsgrunn              | Reason for a correction                                                                      | `Korreksjonsgrunn`                     |
| Minusbeløp                    | Negative result carried over to the next refund                                              | `Minusbelop`                           |
| Grunnbeløp (G) / 5G           | National insurance base amount. Some measure types are capped at 5G                         | `Grunnbelop`, `Tiltakstype.har5gBegrensning()` |
| Ferietrekk                    | Holiday pay deduction in the income                                                          | `Refusjonsgrunnlag.ferietrekkIPerioden` |
| Fratrekk refunderbart beløp   | Deduction for sick pay or other refunded amounts                                             | `FratrekkRefunderbarBeløp`             |
| BedriftKID                    | Optional KID number for the payment                                                          | `bedriftKid`                           |
| Diskresjonskode (kode 6/7)    | Protected address. Needs extra access                                                        | `Diskresjonskode`                      |
| Hendelseslogg                 | Event log of actions on a refund                                                             | `Hendelseslogg`                        |
| Varsel / varsling             | Notification to the employer                                                                 | `varsling/`                            |
| Automatisk utbetaling         | VTAO and mentor are sent for payment without employer approval                               | `Tiltakstype.utbetalesAutomatisk()`    |

## Code Style

- Kotlin, with Norwegian names for domain concepts (`godkjennForArbeidsgiver`, `fristForGodkjenning`) and Norwegian log messages and comments, as in the existing code.
- **Minimal Editing:** change only what the task requires. Do not reformat, rename or reorder unrelated code, and do not "fix" code you were not asked to touch. Keep diffs small and reviewable.
- Follow the patterns of the surrounding code before introducing a new one.

## Git Workflow

- Keep commits as small as possible.
- Never commit directly to `master`. A push to `master` deploys to production.
- Work on a branch and open a pull request. CI builds every branch except `master`.
- Commit messages: Conventional Commits in Norwegian, `type(scope): beskrivelse`, for example `fix(arbeidsgiver): Rette opp i visning av dato i refusjonsside`. Types: `feat`, `fix`, `chore`, `refactor`, `test`, `docs`.
- The commit message should include a short title, but should also have a body that explains more in details what has been changed and why it was changed. If the reason for the changes are unclear just from the context of the code, please ask for more information to make the message descriptive enough for future readers.

## Boundaries

### ✅ Always

- Run `mvn package -B -Dsurefire.useFile=false` before committing, the same as CI.
- Check resource access through `InnloggetArbeidsgiver`/`InnloggetSaksbehandler` for any endpoint that touches a refund or correction.
- Add a new Flyway migration for schema changes.
- Write tests for changes in calculation, status transitions and access control.
- Use synthetic test data.

### ⚠️ Ask First

- Changes to authentication and authorization: `autorisering/`, `altinn/`, `SecurityTokenConfiguration.kt`, `SecurityClientConfiguration.kt`, `@Protected*`/`@Unprotected` annotations, issuer config in `application-*.yml`.
- Changes to the refund calculation (`Refusjonsberegner.kt`, `Beregning`, 5G and minusbeløp rules) or status transitions on `Refusjon`/`Korreksjon`. These decide how much money is paid out.
- Database schema changes and data migrations.
- Kafka message formats, topic names, the consumer group id and JSON serialization (`JsonConfiguration`). Other teams consume these messages.
- API request/response contracts used by the two frontends, and message types that mirror another system (`TilskuddsperiodeGodkjentMelding`, `BetalingStatusEndringMelding`, Altinn and ikomp responses).
- New endpoints under `/internal/admin`.
- `.nais/` manifests: access policy, AD groups, external hosts, resources.
- New dependencies or version bumps in `pom.xml`.

### 🚫 Never

- Commit secrets, tokens or real personal data.
- Log fødselsnummer, names, account numbers or tokens.
- Edit an existing Flyway migration.
- Skip or disable tests to make the build pass.
- Commit or push directly to `master`.
