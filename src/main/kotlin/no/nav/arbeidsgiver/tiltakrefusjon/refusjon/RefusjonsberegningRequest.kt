package no.nav.arbeidsgiver.tiltakrefusjon.refusjon


data class RefusjonsberegningRequest(
        val fnr: String,
        val bedriftNr: String,
        val refusjonFraDato: String,
        val refusjonTilDato: String
) {
    fun erUtfylt(): Boolean {
        return !(fnr.isBlank() || bedriftNr.isBlank() || refusjonFraDato.isBlank() || refusjonTilDato.isBlank())
    }
}