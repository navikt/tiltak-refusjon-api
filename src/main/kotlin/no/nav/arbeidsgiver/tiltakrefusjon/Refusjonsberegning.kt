package no.nav.arbeidsgiver.tiltakrefusjon

import no.nav.arbeidsgiver.tiltakrefusjon.domain.Refusjonsgrunnlag

fun beregnRefusjon(grunnlag: Refusjonsgrunnlag) : Int {
    return grunnlag.inntekt * grunnlag.prosent / 100
}

