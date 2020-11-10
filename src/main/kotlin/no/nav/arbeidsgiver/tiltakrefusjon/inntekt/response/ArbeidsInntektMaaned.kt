package no.nav.arbeidsgiver.tiltakrefusjon.inntekt.response

import ArbeidsInntektInformasjon


data class ArbeidsInntektMaaned (
     val aarMaaned: String? = null,
     var arbeidsInntektInformasjon: ArbeidsInntektInformasjon?
)