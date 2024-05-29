package no.nav.arbeidsgiver.tiltakrefusjon.okonomi

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
    properties = [
        "tiltak-refusjon.kontoregister.uri=http://localhost:8090/kontoregister/api/v1/hent-kontonummer-for-organisasjon/",
        "tiltak-refusjon.kontoregister.fake=false"
    ]
)
@ActiveProfiles("local")
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 8090)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KontoregisterServiceImplTest {
    @Autowired
    lateinit var kontoregisterService: KontoregisterService

    @Test
    fun `kall med kontonummer`() {
        val kontonr = kontoregisterService.hentBankkontonummer("990983666")
        assertThat(kontonr).isNotEmpty
    }

    @Test
    fun `kall som returnerer at bedriften ikke finnes i register`() {
        val kontonr = kontoregisterService.hentBankkontonummer("111234567")
        assertThat(kontonr).isNull()
    }
}