package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

sealed interface RefunderingStatus {
    fun isSendtInn(): Boolean
}

