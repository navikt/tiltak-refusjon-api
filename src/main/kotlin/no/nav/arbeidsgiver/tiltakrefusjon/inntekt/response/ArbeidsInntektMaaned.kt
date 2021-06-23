package no.nav.arbeidsgiver.tiltakrefusjon.inntekt.response

data class ArbeidsInntektMaaned(
    val aarMaaned: String,
    var arbeidsInntektInformasjon: ArbeidsInntektInformasjon,
)