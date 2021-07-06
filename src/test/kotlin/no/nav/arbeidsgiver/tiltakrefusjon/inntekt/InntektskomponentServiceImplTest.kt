package no.nav.arbeidsgiver.tiltakrefusjon.inntekt

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@SpringBootTest(properties = [
    "tiltak-refusjon.inntektskomponenten.uri=http://localhost:8090/inntektskomponenten-ws/rs/api/v1/hentinntektliste",
    "tiltak-refusjon.inntektskomponenten.fake=false"
])
@ActiveProfiles("local")
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 8090)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InntektskomponentServiceImplTest {
    @Autowired
    lateinit var inntektskomponentService: InntektskomponentService

    @Test
    fun `kall OK`() {
        val inntekter = inntektskomponentService.hentInntekter(
            "28128521498",
            "999999999",
            LocalDate.of(2020, 9, 1),
            LocalDate.of(2020, 10, 1)
        )
        assertThat(inntekter.first).hasSize(3)
        assertThat(inntekter.second).containsSubsequence("arbeidsInntektMaaned") // Property på høyeste nivå i JSON-responsen
    }

    @Test
    fun `kall med respons uten inntekt`() {
        val inntekter = inntektskomponentService.hentInntekter(
            "25119525430",
            "999999999",
            LocalDate.of(2020, 9, 1),
            LocalDate.of(2020, 10, 1)
        )
        assertThat(inntekter.first).hasSize(0)
        assertThat(inntekter.second).containsSubsequence("arbeidsInntektMaaned") // Property på høyeste nivå i JSON-responsen
    }

    // TODO: Skriv flere tester som treffer feilsituasjoner
}