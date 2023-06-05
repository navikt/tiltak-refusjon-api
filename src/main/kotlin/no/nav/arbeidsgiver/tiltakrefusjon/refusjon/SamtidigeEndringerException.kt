package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException

internal class SamtidigeEndringerException : FeilkodeException(Feilkode.SAMTIDIGE_ENDRINGER)