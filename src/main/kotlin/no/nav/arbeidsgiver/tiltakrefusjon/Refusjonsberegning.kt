package no.nav.arbeidsgiver.tiltakrefusjon

fun beregnRefusjon(grunnlag: Refusjonsgrunnlag) : Int {
    return grunnlag.inntekt * grunnlag.prosent / 100
}