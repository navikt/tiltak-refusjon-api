package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

data class KorrigerBruttolønnRequest(val inntekterKunFraTiltaket: Boolean, val korrigertBruttoLønn: Int? = null)