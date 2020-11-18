package no.nav.arbeidsgiver.tiltakrefusjon.inntekt.request

import java.time.YearMonth

data class InntektRequest(val ident: Aktør, val maanedFom: YearMonth, val maanedTom: YearMonth, val ainntektsfilter: String)