package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

sealed class Maks5GResultat {
    class InnenforMaks : Maks5GResultat()
    data class OverMaks(val maksbelop: Int) : Maks5GResultat()
}
