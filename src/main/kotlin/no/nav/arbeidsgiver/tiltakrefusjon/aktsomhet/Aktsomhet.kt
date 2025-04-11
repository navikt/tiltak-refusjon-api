package no.nav.arbeidsgiver.tiltakrefusjon.aktsomhet

import no.nav.team_tiltak.felles.persondata.pdl.domene.Diskresjonskode

data class Aktsomhet(
    val kreverAktsomhet: Boolean,
    val diskresjonskode: Diskresjonskode?
) {
    companion object {
        fun av(diskresjonskode: Diskresjonskode, isArbeidsgiver: Boolean): Aktsomhet {
            return Aktsomhet(
                diskresjonskode.erKode6Eller7(),
                if (isArbeidsgiver) null else diskresjonskode
            )
        }

        fun tom(): Aktsomhet {
            return Aktsomhet(
                false,
                null
            )
        }
    }
}
