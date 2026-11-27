package no.nav.arbeidsgiver.tiltakrefusjon.grunnbelop

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.resilience.annotation.EnableResilientMethods
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withServerError
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import java.util.concurrent.TimeUnit

@SpringJUnitConfig(GrunnbelopClientTest.RetryTestConfiguration::class)
@TestPropertySource(properties = ["tiltak-refusjon.grunnbelop.uri=https://grunnbelop.example.test"])
class GrunnbelopClientTest {
    @TestConfiguration(proxyBeanMethods = false)
    @EnableResilientMethods
    @Import(GrunnbelopClient::class)
    class RetryTestConfiguration {
        @Bean
        fun noAuthRestTemplate() = RestTemplate()
    }

    @Autowired
    lateinit var grunnbelopClient: GrunnbelopClient

    @Autowired
    @Qualifier("noAuthRestTemplate")
    lateinit var restTemplate: RestTemplate

    private lateinit var server: MockRestServiceServer

    @BeforeEach
    fun setUp() {
        server = MockRestServiceServer.bindTo(restTemplate).build()
    }

    @Test
    fun `henter grunnbeløp uten omforsøk ved suksess`() {
        forventSuksess()

        assertThat(grunnbelopClient.alleGrunnbelop()).hasSize(1)
        server.verify()
    }

    @Test
    fun `lykkes på tredje forsøk etter to feil`() {
        repeat(2) {
            server.expect(requestTo("https://grunnbelop.example.test")).andRespond(withServerError())
        }
        forventSuksess()

        assertThat(grunnbelopClient.alleGrunnbelop()).hasSize(1)
        server.verify()
    }

    @Test
    fun `stopper etter tre kall med ett sekund mellom forsøkene og kaster siste feil`() {
        val kalltidspunkter = mutableListOf<Long>()
        repeat(3) { forsok ->
            server.expect(requestTo("https://grunnbelop.example.test")).andRespond { request ->
                kalltidspunkter.add(System.nanoTime())
                withServerError().body("Feil ${forsok + 1}").createResponse(request)
            }
        }

        val feil = assertThrows<HttpServerErrorException> { grunnbelopClient.alleGrunnbelop() }

        assertThat(feil.responseBodyAsString).isEqualTo("Feil 3")
        server.verify()
        assertThat(kalltidspunkter).hasSize(3)
        kalltidspunkter.zipWithNext { forrige, neste ->
            assertThat(TimeUnit.NANOSECONDS.toMillis(neste - forrige)).isGreaterThanOrEqualTo(1000L)
        }
    }

    private fun forventSuksess() {
        server.expect(requestTo("https://grunnbelop.example.test"))
            .andRespond(
                withSuccess(
                    """[{"dato":"2025-05-01","grunnbeløp":130160}]""",
                    MediaType.APPLICATION_JSON
                )
            )
    }
}
