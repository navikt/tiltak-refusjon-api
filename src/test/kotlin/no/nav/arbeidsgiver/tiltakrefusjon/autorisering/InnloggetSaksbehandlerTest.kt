package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.FakeInntektskomponentService
import no.nav.arbeidsgiver.tiltakrefusjon.norg.NorgService
import no.nav.arbeidsgiver.tiltakrefusjon.okonomi.FakeKontoregisterService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.*
import no.nav.arbeidsgiver.tiltakrefusjon.refusjoner
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles
import java.util.*

@SpringBootTest
@ActiveProfiles("local")
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 8091)
class InnloggetSaksbehandlerTest(
    @Autowired val refusjonRepository: RefusjonRepository,
    @Autowired val tilgangskontrollService: TilgangskontrollService,
    @Autowired val korreksjonRepository: KorreksjonRepository,
    @Autowired val refusjonService: RefusjonService,
    @Autowired val inntektskomponentService: FakeInntektskomponentService,
    @Autowired val kontoregisterService: FakeKontoregisterService,
    @Autowired val norgService: NorgService
) {
    @BeforeEach
    fun setUp() {
        refusjonRepository.deleteAll()
        refusjonRepository.saveAll(refusjoner())
    }

    val saksbehandler = InnloggetSaksbehandler(
        "Z123456",
        UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
        "Geir",
        tilgangskontrollService,
        norgService,
        refusjonRepository,
        korreksjonRepository,
        refusjonService,
        inntektskomponentService,
        kontoregisterService,
        true
    )

    @Test
    fun `saksbehandler får ikke opp refusjoner som den ikke har tilgang til`() {
        val alleRefusjoner = saksbehandler.finnAlle(HentSaksbehandlerRefusjonerQueryParametre(enhet = "1000", size = 1000))
        val refusjonerIRepository = refusjonRepository.findAll()

        val refusjonerSaksbehandlerHartilgangtil = alleRefusjoner.get("refusjoner") as List<Refusjon>

        assertNull(refusjonerSaksbehandlerHartilgangtil.find { it.refusjonsgrunnlag.tilskuddsgrunnlag.enhet != "1000" })
        assertNull(refusjonerSaksbehandlerHartilgangtil.find { it.deltakerFnr == "07098142678" })
        assertEquals(9, refusjonerIRepository.size - refusjonerSaksbehandlerHartilgangtil.size) // 9 avtaler som saksbehandler ikke har tilgang til med fnr som gir Deny i wiremock
    }

    @Test
    fun `saksbehandler henter refusjoner for ett BedriftNr`() {
        val bedriftNrDetSlåesOppPå = "998877665"

        val alleRefusjoner = saksbehandler.finnAlle(HentSaksbehandlerRefusjonerQueryParametre(size = 1000, bedriftNr = bedriftNrDetSlåesOppPå))
        val refusjonerSaksbehandlerHartilgangtil = alleRefusjoner.get("refusjoner") as List<Refusjon>
        Assertions.assertThat(refusjonerSaksbehandlerHartilgangtil).allMatch { it.bedriftNr == bedriftNrDetSlåesOppPå }
        assertEquals(4, refusjonerSaksbehandlerHartilgangtil.size)
    }
}
