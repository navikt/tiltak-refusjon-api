package no.nav.arbeidsgiver.tiltakrefusjon.inntekt

import tools.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.http.MediaType
import org.springframework.test.web.client.ExpectedCount.once
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestTemplate
import java.time.LocalDate

@SpringBootTest(
    properties = [
        "tiltak-refusjon.inntektskomponenten.uri=http://localhost:8090/inntektskomponenten",
        "tiltak-refusjon.inntektskomponenten.fake=false"
    ]
)
@ActiveProfiles("local")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InntektskomponentServiceImplTest {
    @Autowired
    @Qualifier("ikompRestTemplate")
    lateinit var ikompRestTemplate: RestTemplate

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var inntektskomponentService: InntektskomponentService

    private lateinit var server: MockRestServiceServer

    @BeforeEach
    fun setUp() {
        server = MockRestServiceServer.bindTo(ikompRestTemplate).ignoreExpectOrder(true).build()
    }

    @Test
    fun `kall OK`() {
        stubResponse("25522617728", "2020-09", "2020-10")
        val inntekter = inntektskomponentService.hentInntekter(
            "25522617728",
            "999999999",
            LocalDate.of(2020, 9, 1),
            LocalDate.of(2020, 10, 1)
        )
        assertThat(inntekter.first).hasSize(3)
        assertThat(inntekter.second).containsSubsequence("arbeidsInntektMaaned") // Property på høyeste nivå i JSON-responsen
    }

    @Test
    fun `kall med respons uten inntekt`() {
        stubResponse("22488604671", "2020-09", "2020-10")
        val inntekter = inntektskomponentService.hentInntekter(
            "22488604671",
            "999999999",
            LocalDate.of(2020, 9, 1),
            LocalDate.of(2020, 10, 1)
        )
        assertThat(inntekter.first).hasSize(0)
        assertThat(inntekter.second).containsSubsequence("arbeidsInntektMaaned") // Property på høyeste nivå i JSON-responsen
    }

    @Test
    fun `kall med respons uten a-melding`() {
        stubResponse("06518900968", "2020-09", "2020-10")
        val inntekter = inntektskomponentService.hentInntekter(
            fnr = "06518900968",
            bedriftnummerDetSøkesPå = "999999999",
            datoFra = LocalDate.of(2020, 9, 1),
            datoTil = LocalDate.of(2020, 10, 1)
        )
        assertThat(inntekter.first).isEmpty()
        assertThat(inntekter.second).contains("06518900968")
    }

    @Test
    fun `kall som feiler gir ikke tom liste men feiler`() {
        assertThrows<Throwable> {
            val inntekter = inntektskomponentService.hentInntekter(
                fnr = "12345678912",
                bedriftnummerDetSøkesPå = "999999999",
                datoFra = LocalDate.of(2020, 9, 1),
                datoTil = LocalDate.of(2020, 10, 1)
            )
        }
    }

    private fun stubResponse(fnr: String, maanedFom: String, maanedTom: String) {
        val response = loadResponseBodyFor(fnr, maanedFom, maanedTom)
        server.expect(
            once(),
            requestTo("http://localhost:8090/inntektskomponenten/rs/api/v1/hentinntektliste")
        ).andExpect(method(org.springframework.http.HttpMethod.POST))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.ident.identifikator").value(fnr))
            .andExpect(jsonPath("$.maanedFom").value(maanedFom))
            .andExpect(jsonPath("$.maanedTom").value(maanedTom))
            .andRespond(withSuccess(response, MediaType.APPLICATION_JSON))
    }

    private fun loadResponseBodyFor(fnr: String, maanedFom: String, maanedTom: String): String {
        val root = objectMapper.readTree(
            requireNotNull(javaClass.getResource("/mappings/inntektskomponenten.json")).readText()
        )
        val mapping = root["mappings"].first { mapping ->
            val body = mapping["request"]["bodyPatterns"][0]["equalToJson"]
            body["ident"]["identifikator"].asString() == fnr &&
                body["maanedFom"].asString() == maanedFom &&
                body["maanedTom"].asString() == maanedTom
        }
        return objectMapper.writeValueAsString(mapping["response"]["jsonBody"])
    }
}
