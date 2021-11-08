package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

interface Operasjoner {
    fun oppgiInntektsgrunnlag(inntektsgrunnlag: Inntektsgrunnlag)
    fun oppgiBedriftKontonummer(bedrifKontonummer: String)
    fun endreBruttolønn(inntekterKunFraTiltaket: Boolean, bruttoLønn: Int?)
}