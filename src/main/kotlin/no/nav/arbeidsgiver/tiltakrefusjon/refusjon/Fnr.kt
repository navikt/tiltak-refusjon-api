package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.exceptions.RefusjonException

data class Fnr(override val verdi: String) : Identifikator{

    init{
        if(!erGyldigFnr(verdi)){
            throw RefusjonException("Ugyldig fødselsnummer. Må inneholde 11 tegn.")
        }
    }

    fun erGyldigFnr(fnr:String): Boolean{
        return fnr.matches(Regex("^[0-9]{11}$"))
    }

}