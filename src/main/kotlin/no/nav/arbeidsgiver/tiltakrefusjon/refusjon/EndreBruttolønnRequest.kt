package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

data class EndreBruttolønnRequest(val inntekterKunFraTiltaket: Boolean, val bruttoLønn: Int? = null)