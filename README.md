# tiltak-refusjon-api

Backend for refusjon av lønnstilskudd og andre arbeidsmarkedstiltak i Nav. Arbeidsgivere sender inn refusjonskrav, og saksbehandlere følger opp og korrigerer dem.

Appen eies av team-tiltak (`arbeidsgiver`) og kjører på Nais i GCP.

## Slik henger det sammen

- Avtaleløsningen publiserer godkjente tilskuddsperioder på Kafka. Appen lager én refusjon per periode.
- Frontend-koden ligger i [tiltak-refusjon](https://github.com/navikt/tiltak-refusjon) og kjører som to apper:
  - Arbeidsgivere bruker `tiltak-refusjon-arbeidsgiver` og logger inn med ID-porten via TokenX.
  - Saksbehandlere bruker `tiltak-refusjon-saksbehandler` og logger inn med Azure AD.
- Godkjente refusjoner og korreksjoner sendes på Kafka til utbetaling. Betalingsstatus kommer tilbake på Kafka.

Appen henter data fra Inntektskomponenten, sokos-kontoregister, arbeidsgiver-altinn-tilganger, poao-tilgang, PDL, Norg2 og `g.nav.no`. Topics står i `Topics.kt`, og tilgangsstyringen mellom apper står i `.nais/nais.yml`.

## Kjøre lokalt

Du trenger Java 21 og Maven.

Start `LokalTiltakRefusjonApplication` i `src/test/kotlin/no/nav/arbeidsgiver/tiltakrefusjon/`. Den bruker H2 i minnet, WireMock for eksterne tjenester og laster testdata ved oppstart. Appen svarer på `http://localhost:8081`.

Innlogging lokalt går mot `tiltak-fakelogin`. PDF-generering krever `tiltak-pdfgen` på `localhost:9001`.

`DockerComposeTiltakRefusjonApplication` bruker PostgreSQL på `localhost:7432` og Kafka på `localhost:9092`. Repoet har ingen compose-fil for disse.

## Bygge og teste

```bash
mvn package -B -Dsurefire.useFile=false
```

Kommandoen er den samme som CI kjører. Den kompilerer, kjører alle tester og bygger `target/app.jar`.

## Deploy

- En push til en annen gren enn `master` bygges og testes.
- En merge til `master` deployes til dev-gcp og prod-gcp.
- Labs (`tiltak-refusjon-api-labs`) deployes hver ukedag og ved manuell kjøring av `bygg-og-deploy-labs.yml`.

## For utviklere og kodeagenter

[`AGENTS.md`](AGENTS.md) beskriver tilgangskontroll, mønstre, ordliste, Git-flyt og grenser for endringer.
