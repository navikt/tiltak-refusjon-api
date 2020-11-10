package no.nav.arbeidsgiver.tiltakrefusjon.inntekt.request


data class Ident (
     val identifikator: String? = null,
     val aktoerType:String = "NATURLIG_IDENT"
)