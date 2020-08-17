package no.nav.arbeidsgiver.tiltakrefusjon

data class Refusjonsgrunnlag(
        val inntekter: List<Inntektslinje>,
        val prosent: Int
)
