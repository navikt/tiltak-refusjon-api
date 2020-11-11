package no.nav.arbeidsgiver.tiltakrefusjon.refusjon


data class RefusjonsberegningRequest  (
        var fnr: String? = null,
        var bedriftNr: String? = null,
        var refusjonFraDato: String? = null,
        var refusjonTilDato: String? = null
){
    fun erUtfylt():Boolean{
        return !(fnr.isNullOrEmpty() || bedriftNr.isNullOrEmpty() || refusjonFraDato.isNullOrEmpty() || refusjonTilDato.isNullOrEmpty())
    }
}