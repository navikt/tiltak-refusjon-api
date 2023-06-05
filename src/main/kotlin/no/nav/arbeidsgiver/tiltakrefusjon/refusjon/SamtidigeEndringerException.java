package no.nav.arbeidsgiver.tiltakrefusjon.refusjon;

import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode;
import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException;

public class SamtidigeEndringerException extends FeilkodeException {

    public SamtidigeEndringerException() {
        super(Feilkode.SAMTIDIGE_ENDRINGER);
    }


}
