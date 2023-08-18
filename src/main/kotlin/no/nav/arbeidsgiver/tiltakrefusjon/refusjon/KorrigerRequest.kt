package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

data class KorrigerRequest(
    val korreksjonsgrunner: Set<Korreksjonsgrunn>,
    val refusjonId: String,
    val unntakOmInntekterFremitid: Int,
    val annenKorreksjonsGrunn: String?
)
