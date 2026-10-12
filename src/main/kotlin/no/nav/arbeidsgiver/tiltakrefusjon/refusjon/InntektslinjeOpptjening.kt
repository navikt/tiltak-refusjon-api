package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

data class InntektslinjeOpptjening(
    val inntektslinjeId: String,
    val erOpptjentIPeriode: Boolean,
) {
    companion object {
        fun map(request: EndreRefundertInntektslinjeRequest) = InntektslinjeOpptjening(request.inntektslinjeId, request.erOpptjentIPeriode)
    }
}
