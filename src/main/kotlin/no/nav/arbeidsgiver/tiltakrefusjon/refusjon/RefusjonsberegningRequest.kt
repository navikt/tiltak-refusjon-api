package no.nav.arbeidsgiver.tiltakrefusjon.refusjon


data class RefusjonsberegningRequest  (
        var fnr: String,
        var bedriftNr: String,
        var refusjonFraDato: String,
        var refusjonTilDato: String
){
    fun erUtfylt():Boolean{
        return !(fnr.isBlank() || bedriftNr.isBlank() || refusjonFraDato.isBlank() || refusjonTilDato.isBlank())
    }
}