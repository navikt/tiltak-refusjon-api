package no.nav.arbeidsgiver.tiltakrefusjon.organisasjon

import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException

class KallTilEregException(melding:String) :  FeilkodeException(Feilkode.TEKNISK_FEIL_EREGOPPSLAG)  {
}