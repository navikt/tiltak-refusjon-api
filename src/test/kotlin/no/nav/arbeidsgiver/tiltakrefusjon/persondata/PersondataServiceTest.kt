package no.nav.arbeidsgiver.tiltakrefusjon.persondata

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Fnr
import no.nav.team_tiltak.felles.persondata.pdl.domene.Diskresjonskode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("local")
@AutoConfigureWireMock(port = 8091)
class PersondataServiceTest {
    final val STRENGT_FORTROLIG_UTLAND_FNR = Fnr("28033114267")
    final val STRENGT_FORTROLIG_FNR = Fnr("16053900422")
    final val FORTROLIG_FNR = Fnr("26067114433")
    final val UGRADERT_FNR = Fnr("08120689976")
    final val UGRADERT_PERSON_TOM_RESPONSE_FNR = Fnr("23097010706")

    @Autowired
    lateinit var persondataService: PersondataService

    @Test
    fun `henter fra pdl og defaulter til UGRADERT for de som ikke finnes`() {
        val fnrSet = setOf(
            STRENGT_FORTROLIG_UTLAND_FNR,
            STRENGT_FORTROLIG_FNR,
            FORTROLIG_FNR,
            UGRADERT_FNR,
            UGRADERT_PERSON_TOM_RESPONSE_FNR
        )

        val diskresjonskoder = persondataService.hentDiskresjonskoder(fnrSet);
        assertThat(diskresjonskoder).hasSize(5)

        assertThat(diskresjonskoder[STRENGT_FORTROLIG_UTLAND_FNR]).isEqualTo(Diskresjonskode.STRENGT_FORTROLIG_UTLAND)
        assertThat(diskresjonskoder[STRENGT_FORTROLIG_FNR]).isEqualTo(Diskresjonskode.STRENGT_FORTROLIG)
        assertThat(diskresjonskoder[FORTROLIG_FNR]).isEqualTo(Diskresjonskode.FORTROLIG)
        assertThat(diskresjonskoder[UGRADERT_FNR]).isEqualTo(Diskresjonskode.UGRADERT)
        assertThat(diskresjonskoder[UGRADERT_PERSON_TOM_RESPONSE_FNR]).isEqualTo(Diskresjonskode.UGRADERT)
    }

}
