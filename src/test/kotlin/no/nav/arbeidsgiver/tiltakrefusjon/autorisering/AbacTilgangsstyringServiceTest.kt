package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import no.nav.arbeidsgiver.tiltakrefusjon.IntegrasjonerMockServer
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.HttpClientErrorException

@SpringBootTest
@ActiveProfiles("local", "wiremock")
@DirtiesContext
class AbacTilgangsstyringServiceTest {

    @Autowired
    lateinit var abacTilgangsstyringService: AbacTilgangsstyringService

    @Autowired
    lateinit var integrasjonerMockServer: IntegrasjonerMockServer

    @BeforeEach
    fun before() {
        // Nullstill wiremock (og tellere som teller antall treff mot api'ene!)
        integrasjonerMockServer.getServer().resetAll()
    }

    @Test
    fun `abac-kall caches`() {
        val server = integrasjonerMockServer.getServer()

        server.verify(0, postRequestedFor(WireMock.urlEqualTo("/abac-tilgangstyring")));
        Assertions.assertTrue(abacTilgangsstyringService.harLeseTilgang("A123456", "0000000000"))
        Assertions.assertFalse(abacTilgangsstyringService.harLeseTilgang("A123456", "07098142678"))
        server.verify(2, postRequestedFor(WireMock.urlEqualTo("/abac-tilgangstyring")));

        Assertions.assertTrue(abacTilgangsstyringService.harLeseTilgang("A123456", "0000000000"))
        Assertions.assertTrue(abacTilgangsstyringService.harLeseTilgang("A123456", "0000000000"))
        Assertions.assertFalse(abacTilgangsstyringService.harLeseTilgang("A123456", "07098142678"))

        // Alle kall ovenfor var cachet
        server.verify(2, postRequestedFor(WireMock.urlEqualTo("/abac-tilgangstyring")));
    }

    @Test
    fun `abac-feil caches ikke`() {
        val server = integrasjonerMockServer.getServer()

        server.verify(0, postRequestedFor(WireMock.urlEqualTo("/abac-tilgangstyring")));
        Assertions.assertThrows(HttpClientErrorException::class.java) {
            abacTilgangsstyringService.harLeseTilgang("A123456", "40404040404")
        }
        server.verify(3, postRequestedFor(WireMock.urlEqualTo("/abac-tilgangstyring")));

        Assertions.assertThrows(HttpClientErrorException::class.java) {
            abacTilgangsstyringService.harLeseTilgang("A123456", "40404040404")
        }

        // Feil ble ikke cachet
        server.verify(6, postRequestedFor(WireMock.urlEqualTo("/abac-tilgangstyring")));
    }
}