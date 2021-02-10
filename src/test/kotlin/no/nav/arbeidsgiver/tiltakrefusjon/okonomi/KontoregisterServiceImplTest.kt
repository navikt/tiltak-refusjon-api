package no.nav.arbeidsgiver.tiltakrefusjon.okonomi

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(properties = [
    "tiltak-refusjon.kontoregister.uri=http://localhost:8090/kontoregister/api/v1/hent-kontonr-org/",
    "tiltak-refusjon.kontoregister.fake=false"
])
@ActiveProfiles("local")
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 8090)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KontoregisterServiceImplTest {
    @Autowired
    lateinit var kontoregisterkomponentService: KontoregisterkomponentService

    @Test
    fun `kall med kontonummer`() {
        val kontonr = kontoregisterkomponentService.hentBankkontonummer(
            "990983666"
        )
        assertThat(kontonr).isNotEmpty()
    }

    @Test
    @Disabled
    fun `kall som returnerer at bedriften ikke finnes i register`() {
        val kontonr = kontoregisterkomponentService.hentBankkontonummer(
            "111234567"
        )
        assertThat(kontonr).isNotEmpty()
    }

}