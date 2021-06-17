package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException

class FantIngenBedriftKontonummerException : FeilkodeException(Feilkode.INGEN_BEDRIFTKONTONUMMER) {
}