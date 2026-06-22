package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

sealed class Maks5GResultat {
    class InnenforMaks : Maks5GResultat() {
        override fun equals(other: Any?): Boolean {
            return other is InnenforMaks
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }

        override fun toString(): String {
            return this.javaClass.name
        }
    }
    data class OverMaks(val maksbelop: Int) : Maks5GResultat()
}
