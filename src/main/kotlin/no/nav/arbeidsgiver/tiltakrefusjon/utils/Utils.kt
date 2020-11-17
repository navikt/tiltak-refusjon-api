package no.nav.arbeidsgiver.tiltakrefusjon.utils


fun erIkkeTomme(vararg objekter: Any?): Boolean {
    for (objekt in objekter) {
        if (objekt is String && objekt.isEmpty()) {
            return false
        }
        if (objekt == null) {
            return false
        }
    }
    return true
}

fun erNoenTomme(vararg objekter: Any?): Boolean {
    return !erIkkeTomme(*objekter)
}