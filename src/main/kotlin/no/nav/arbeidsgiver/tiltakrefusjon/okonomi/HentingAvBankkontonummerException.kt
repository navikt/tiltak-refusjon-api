package no.nav.arbeidsgiver.tiltakrefusjon.okonomi

import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException

class HentingAvBankkontonummerException : FeilkodeException(Feilkode.TEKNISK_FEIL_BANKKONTONUMMEROPPSLAG)