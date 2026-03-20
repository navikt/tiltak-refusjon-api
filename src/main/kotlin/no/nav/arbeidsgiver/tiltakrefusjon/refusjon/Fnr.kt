package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.bekk.bekkopen.person.FodselsnummerValidator

data class Fnr(val verdi: String) {
    init {
        if (!FodselsnummerValidator.isValid(verdi)) {
            throw FnrException()
        }
    }
}

class FnrException : IllegalArgumentException("Ugyldig fødselsnummer. Må inneholde 11 tegn.")
