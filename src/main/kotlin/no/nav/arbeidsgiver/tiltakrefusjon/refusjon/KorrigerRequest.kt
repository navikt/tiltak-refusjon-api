package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

data class KorrigerRequest(
    val korreksjonsgrunner: Set<Korreksjonsgrunn>,
)
