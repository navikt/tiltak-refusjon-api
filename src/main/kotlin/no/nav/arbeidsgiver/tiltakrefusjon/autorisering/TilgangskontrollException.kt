package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException

class TilgangskontrollException : FeilkodeException {
    constructor() : super(Feilkode.INGEN_TILGANG)
    constructor(feilkode: Feilkode) : super(feilkode)

    companion object {
        fun fraAvvisning(avvisning: Tilgang.Avvis): TilgangskontrollException {
            return when (avvisning.tilgangskode) {
                Avslagskode.STRENGT_FORTROLIG_ADRESSE -> TilgangskontrollException(Feilkode.IKKE_TILGANG_TIL_DELTAKER_STRENGT_FORTROLIG)
                Avslagskode.FORTROLIG_ADRESSE -> TilgangskontrollException(Feilkode.IKKE_TILGANG_TIL_DELTAKER_FORTROLIG)
                Avslagskode.EGNE_ANSATTE -> TilgangskontrollException(Feilkode.IKKE_TILGANG_TIL_DELTAKER_SKJERMET)
                else -> TilgangskontrollException(Feilkode.IKKE_TILGANG_TIL_REFUSJON)
            }
        }
    }
}
