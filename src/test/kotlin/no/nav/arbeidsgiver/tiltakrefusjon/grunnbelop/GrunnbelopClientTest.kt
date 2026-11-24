package no.nav.arbeidsgiver.tiltakrefusjon.grunnbelop

import no.nav.arbeidsgiver.tiltakrefusjon.TiltakRefusjonApplication
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.aop.support.AopUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.resilience.annotation.EnableResilientMethods
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withServerError
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import java.time.LocalDate
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
    private lateinit var opprinneligRequestFactory: ClientHttpRequestFactory

    @BeforeEach
    fun setUp() {
        opprinneligRequestFactory = restTemplate.requestFactory
        server = MockRestServiceServer.bindTo(restTemplate).build()
    }

    @AfterEach
    fun tearDown() {
        restTemplate.requestFactory = opprinneligRequestFactory
    }

    @Test
    fun `henter og sorterer grunnbeløp uten omforsøk ved suksess`() {
        assertThat(TiltakRefusjonApplication::class.java.isAnnotationPresent(EnableResilientMethods::class.java)).isTrue()
        assertThat(AopUtils.isAopProxy(grunnbelopClient)).isTrue()
        forventSuksess()

        val grunnbelop = grunnbelopClient.alleGrunnbelop()

        assertThat(grunnbelop).containsExactly(
            entry(LocalDate.of(2024, 5, 1), 124028),
            entry(LocalDate.of(2025, 5, 1), 130160)
        )
        server.verify()
    }

    @Test
    fun `lykkes på tredje forsøk etter to feil`() {
        repeat(2) {
            server.expect(requestTo("https://grunnbelop.example.test")).andRespond(withServerError())
        }
        forventSuksess()

        assertThat(grunnbelopClient.alleGrunnbelop()).hasSize(2)
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

    @Test
    fun `prøver også på nytt når grunnbeløp-API gir tomt svar`() {
        repeat(3) {
            server.expect(requestTo("https://grunnbelop.example.test"))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON))
        }

        val feil = assertThrows<IllegalArgumentException> { grunnbelopClient.alleGrunnbelop() }

        assertThat(feil).hasMessage("Tomt svar fra grunnbeløp-API")
        server.verify()
    }

    private fun forventSuksess() {
        server.expect(requestTo("https://grunnbelop.example.test"))
            .andRespond(
                withSuccess(
                    """[{"dato":"2025-05-01","grunnbeløp":130160},{"dato":"2024-05-01","grunnbeløp":124028}]""",
                    MediaType.APPLICATION_JSON
                )
            )
    }
}
