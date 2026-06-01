package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

interface Tilgang {
    fun erTillat(): Boolean

    class Tillat : Tilgang {
        override fun erTillat(): Boolean {
            return true
        }
    }

    class Avvis(val tilgangskode: Avslagskode?) : Tilgang {
        override fun erTillat(): Boolean {
            return false
        }
    }
}
