package no.nav.arbeidsgiver.tiltakrefusjon.organisasjon.virksomhet

import Bruksperiode
import EregOrganisasjon
import Forretningsadresser
import Gyldighetsperiode
import InngaarIJuridiskEnheter
import Navn
import OrganisasjonDetaljer
import VirksomhetDetaljer
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class EregVirksomhetTest{
    @Test
    fun `skal kunne mappe riktig med vanlig enhet orgNr`(){

        // GITT
        val organisasjonsnummer = 123456789
        val type = "Organisasjon"
        val navn = Navn("Duck as","Duck as",Bruksperiode("fom"),Gyldighetsperiode("fom"))
        val organisasjonDetaljer: OrganisasjonDetaljer = OrganisasjonDetaljer(listOf(Forretningsadresser("Org","adresse", 878,"Oslo",1,Bruksperiode("fom"),Gyldighetsperiode("fom"))))

        val virksomhetDetaljer:VirksomhetDetaljer = VirksomhetDetaljer("Virksomhet","oppstartsdato","Eierskiftedato")
        val inngaarIJuridiskEnheter:List<InngaarIJuridiskEnheter> = listOf(InngaarIJuridiskEnheter(123456789,Navn("Duck as","Duck as",Bruksperiode("fom"),Gyldighetsperiode("fom")),Bruksperiode("Fom"),Gyldighetsperiode("fom")))

        // NÅR
        val eregOrganisasjon = EregOrganisasjon(organisasjonsnummer,type,navn,organisasjonDetaljer,inngaarIJuridiskEnheter)

        // DA
        assertTrue(eregOrganisasjon.tilDomeneObjekt().navnPåJuridiskEnhet.isNotEmpty())
        assertTrue(eregOrganisasjon.tilDomeneObjekt().bedriftGatenavn.isNotEmpty())
        assertTrue(eregOrganisasjon.tilDomeneObjekt().bedriftNr.isNotEmpty())
        assertTrue(eregOrganisasjon.tilDomeneObjekt().bedriftPostnummer.isNotEmpty())
        assertTrue(eregOrganisasjon.tilDomeneObjekt().juridiskEnhetOrganisasjonsnummer.isNotEmpty())
        assertTrue(eregOrganisasjon.tilDomeneObjekt().harBedriftAdresseOgJuridiskEnhet())

    }

    @Test
    fun `skal kunne teste har ikke påkrevd informasjon, mangful juridisk enhet and adresslinje is a dot when given a null`(){

        // GITT
        val organisasjonsnummer = 123456789
        val type = "Organisasjon"
        val navn = Navn("DUck AS","Duck AS",Bruksperiode("fom"),Gyldighetsperiode("fom"))
        val tomAdresselinje1 = null
        val organisasjonDetaljer: OrganisasjonDetaljer = OrganisasjonDetaljer(listOf(Forretningsadresser("Org",
            tomAdresselinje1, 878,"Oslo",1,Bruksperiode("fom"),Gyldighetsperiode("fom"))))

        val virksomhetDetaljer:VirksomhetDetaljer = VirksomhetDetaljer("Virksomhet","oppstartsdato","Eierskiftedato")
        val inngaarIJuridiskEnheter:List<InngaarIJuridiskEnheter> = listOf(InngaarIJuridiskEnheter(123456789,Navn("Duck as","",Bruksperiode("fom"),Gyldighetsperiode("fom")),Bruksperiode("Fom"),Gyldighetsperiode("fom")))

        // NÅR
        val eregOrganisasjon = EregOrganisasjon(organisasjonsnummer,type,navn,organisasjonDetaljer,inngaarIJuridiskEnheter)

        // DA
        assertFalse(eregOrganisasjon.tilDomeneObjekt().harBedriftAdresseOgJuridiskEnhet())
        Assertions.assertEquals(eregOrganisasjon.tilDomeneObjekt().bedriftGatenavn, ".")

    }
}