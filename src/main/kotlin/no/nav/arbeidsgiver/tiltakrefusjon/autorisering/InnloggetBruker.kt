package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.arbeidsgiver.tiltakrefusjon.altinn.Organisasjon

data class InnloggetBruker (
        val identifikator: String,
        val altinnOrganisasjoner: Set<Organisasjon>
){
    fun sjekkOmTilgang(bedriftsnummerDetSokesOm:String){
        if(altinnOrganisasjoner.none { it.organizationNumber == bedriftsnummerDetSokesOm }){
            throw TilgangskontrollException("Person har ikke tilgang")
        }
    }
}

