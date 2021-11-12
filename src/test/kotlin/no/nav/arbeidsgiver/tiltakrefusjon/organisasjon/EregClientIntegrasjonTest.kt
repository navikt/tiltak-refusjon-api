package no.nav.arbeidsgiver.tiltakrefusjon.organisasjon

import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("local","wiremock")
@SpringBootTest
@DirtiesContext
class EregClientIntegrasjonTest{

    @Autowired
    lateinit var eregClient: EregClient

    @Test
    fun `kall mot Ereg returnerer en gyldig virksomhet objekt som er en OFFENTLIG led`(){
       val virksomhet =  eregClient.hentVirksomhet("979589255")
        assertThat(virksomhet.bedriftGatenavn).isEqualTo("Cecilie Thoresens vei 1")
        assertThat(virksomhet.bedriftNr).isEqualTo("979589255")
        assertThat(virksomhet.bedriftPostnummer).isEqualTo("1153")
        assertThat(virksomhet.navnPåJuridiskEnhet).isEqualTo("OSLO KOMMUNE")
        assertThat(virksomhet.juridiskEnhetOrganisasjonsnummer).isEqualTo("958935420")
    }
    @Test
    fun `kall mot Ereg returnerer en gyldig virksomhet objekt som har en OFFENTLIG juridisk enhet`(){
       val virksomhet =  eregClient.hentVirksomhet("975218279")
        assertThat(virksomhet.juridiskEnhetOrganisasjonsnummer).isEqualTo("964980543")
        assertThat(virksomhet.bedriftGatenavn).isEqualTo("Geilneset 31")
        assertThat(virksomhet.bedriftNr).isEqualTo("975218279")
        assertThat(virksomhet.bedriftPostnummer).isEqualTo("6030")
        assertThat(virksomhet.navnPåJuridiskEnhet).isEqualTo("SULA KOMMUNE")
    }

    @Test
    fun `kall mot Ereg returnerer en gyldig virksomhet objekt med IKKE offentlig juridisk enhet`(){
       val virksomhet =  eregClient.hentVirksomhet("910825526")
        assertThat(virksomhet.juridiskEnhetOrganisasjonsnummer).isEqualTo("994446096")
        assertThat(virksomhet.bedriftGatenavn).isEqualTo("Gålåsvegen 185")
        assertThat(virksomhet.bedriftNr).isEqualTo("874135402")
        assertThat(virksomhet.bedriftPostnummer).isEqualTo("2320")
        assertThat(virksomhet.navnPåJuridiskEnhet).isEqualTo("STOR-GAALAAS GÅRD Arne Bøhmer")
    }

    @Test
    fun `kall mot Ereg kaster en exception fordi virksomheten ikke finnes`(){
        assertThrows<FeilkodeException> {
            val virksomhet =  eregClient.hentVirksomhet("990983666")
        }
    }

}