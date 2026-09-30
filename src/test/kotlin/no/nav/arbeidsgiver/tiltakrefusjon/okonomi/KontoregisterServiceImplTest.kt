package no.nav.arbeidsgiver.tiltakrefusjon.okonomi

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate
import java.net.URI

class KontoregisterServiceImplTest {

    @Test
    fun `kall med kontonummer`() {
        val restTemplate = RestTemplate()
        val server = MockRestServiceServer.bindTo(restTemplate).build()
        val properties = KontoregisterProperties(
            uri = URI("http://localhost:8090/kontoregister/api/v1/hent-kontonummer-for-organisasjon"),
            consumerId = "tiltak-refusjon-api",
        )
        val service = KontoregisterServiceImpl(properties, restTemplate)

        server.expect(requestTo("http://localhost:8090/kontoregister/api/v1/hent-kontonummer-for-organisasjon/990983666"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("""{"mottaker":"Bedrift AS","kontonr":"889640782"}""", MediaType.APPLICATION_JSON))

        val kontonr = service.hentBankkontonummer("990983666")

        assertThat(kontonr).isEqualTo("889640782")
        server.verify()
    }

    @Test
    fun `kall som returnerer at bedriften ikke finnes i register`() {
        val restTemplate = RestTemplate()
        val server = MockRestServiceServer.bindTo(restTemplate).build()
        val properties = KontoregisterProperties(
            uri = URI("http://localhost:8090/kontoregister/api/v1/hent-kontonummer-for-organisasjon"),
            consumerId = "tiltak-refusjon-api",
        )
        val service = KontoregisterServiceImpl(properties, restTemplate)

        server.expect(requestTo("http://localhost:8090/kontoregister/api/v1/hent-kontonummer-for-organisasjon/111234567"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.NOT_FOUND))

        val kontonr = service.hentBankkontonummer("111234567")

        assertThat(kontonr).isNull()
        server.verify()
    }
}
