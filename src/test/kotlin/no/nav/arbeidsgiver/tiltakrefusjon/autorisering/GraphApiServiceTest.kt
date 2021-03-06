package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(properties = [
    "tiltak-refusjon.graph-api.uri=http://localhost:8090/graph-api",
    "tiltak-refusjon.graph-api.fake=false"
])
@ActiveProfiles("local")
@AutoConfigureWireMock(port = 8090)
class GraphApiServiceTest {
    @Autowired
    lateinit var graphApiService: GraphApiServiceImpl

    @Test
    fun `kan gjøre GET`() {
        val response = graphApiService.hent()
        assertThat(response.onPremisesSamAccountName).isEqualTo("X123456")
        assertThat(response.displayName).isEqualTo("Navn Navnesen")
    }
}