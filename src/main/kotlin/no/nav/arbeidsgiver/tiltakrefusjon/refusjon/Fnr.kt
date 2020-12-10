package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

data class Fnr(val verdi: String) {
    init {
        if (!erGyldigFnr(verdi)) {
            throw FnrException()
        }
    }
}

class FnrException : IllegalArgumentException("Ugyldig fødselsnummer. Må inneholde 11 tegn.")

fun erGyldigFnr(fnr: String): Boolean {
    return fnr.matches(Regex("^[0-9]{11}$"))
}