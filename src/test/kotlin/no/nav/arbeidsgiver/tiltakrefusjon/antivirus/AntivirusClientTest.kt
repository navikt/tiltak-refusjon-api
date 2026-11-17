package no.nav.arbeidsgiver.tiltakrefusjon.antivirus

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withException
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import java.io.IOException
import java.net.URI
import java.util.stream.Stream

class AntivirusClientTest {

    @Test
    fun `tolker rene filer og virus og teller resultat`() {
        val fixture = fixture()
        val requestBody = byteArrayOf(0x41, 0x00, 0x7f)

        fixture.server.expect(requestTo(SCAN_URI))
            .andExpect(method(HttpMethod.PUT))
            .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
            .andExpect(content().bytes(requestBody))
            .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
            .andExpect(headerDoesNotExist(HttpHeaders.AUTHORIZATION))
            .andRespond(withSuccess("""[{"result":"OK","virus":"","error":"","filename":"ignored.txt"}]""", MediaType.APPLICATION_JSON))
        assertThat(fixture.client.skann(requestBody)).isEqualTo(AntivirusResultat.REN)
        fixture.server.verify()

        fixture.server.reset()
        fixture.server.expect(requestTo(SCAN_URI))
            .andRespond(withSuccess("""[{"result":"FOUND","virus":"Example.Test","error":""}]""", MediaType.APPLICATION_JSON))
        assertThat(fixture.client.skann(requestBody)).isEqualTo(AntivirusResultat.VIRUS)
        fixture.server.verify()

        assertThat(fixture.meterRegistry.get("refusjon.antivirus").tag("resultat", "REN").counter().count()).isEqualTo(1.0)
        assertThat(fixture.meterRegistry.get("refusjon.antivirus").tag("resultat", "VIRUS").counter().count()).isEqualTo(1.0)
    }

    @ParameterizedTest
    @MethodSource("skannefeilSvar")
    fun `antivirusfeilrespons med HTTP 200 gir skannefeil`(body: String) {
        val fixture = fixture()
        fixture.server.expect(requestTo(SCAN_URI))
            .andRespond(withSuccess(body, MediaType.APPLICATION_JSON))

        val exception = skannFeil(fixture.client)

        assertThat(exception.grunn).isEqualTo(AntivirusFeil.SKANNEFEIL)
        assertThat(exception.cause).isNull()
        assertThat(exception.message).isEqualTo("Antivirusskanningen feilet: SKANNEFEIL")
        assertThat(fixture.meterRegistry.get("refusjon.antivirus").tag("resultat", "FEIL").counter().count()).isEqualTo(1.0)
        fixture.server.verify()
    }

    @ParameterizedTest
    @MethodSource("ugyldigeSvar")
    fun `ugyldig eller motstridende svar gir ugyldig svar`(body: String) {
        val fixture = fixture()
        fixture.server.expect(requestTo(SCAN_URI))
            .andRespond(withSuccess(body, MediaType.APPLICATION_JSON))

        assertThat(skannFeil(fixture.client).grunn).isEqualTo(AntivirusFeil.UGYLDIG_SVAR)
        fixture.server.verify()
    }

    @Test
    fun `tom respons gir ugyldig svar`() {
        val fixture = fixture()
        fixture.server.expect(requestTo(SCAN_URI))
            .andRespond(withSuccess("", MediaType.APPLICATION_JSON))

        assertThat(skannFeil(fixture.client).grunn).isEqualTo(AntivirusFeil.UGYLDIG_SVAR)
        fixture.server.verify()
    }

    @Test
    fun `respons uten JSON content type gir ugyldig svar`() {
        val fixture = fixture()
        fixture.server.expect(requestTo(SCAN_URI))
            .andRespond(withSuccess("""[{"result":"OK","virus":"","error":""}]""", MediaType.TEXT_PLAIN))

        assertThat(skannFeil(fixture.client).grunn).isEqualTo(AntivirusFeil.UGYLDIG_SVAR)
        fixture.server.verify()
    }

    @ParameterizedTest
    @MethodSource("ikkeVellykkedeHttpStatus")
    fun `ikke 2xx inklusive redirect gir HTTP feil`(status: HttpStatus) {
        val fixture = fixture()
        fixture.server.expect(requestTo(SCAN_URI))
            .andRespond(withStatus(status))

        val exception = skannFeil(fixture.client)

        assertThat(exception.grunn).isEqualTo(AntivirusFeil.HTTP_FEIL)
        assertThat(exception.message).doesNotContain(status.reasonPhrase)
        fixture.server.verify()
    }

    @Test
    fun `transportfeil gir tilkoblingsfeil uten underliggende feil`() {
        val fixture = fixture()
        fixture.server.expect(requestTo(SCAN_URI))
            .andRespond(withException(IOException("private upstream detail")))

        val exception = skannFeil(fixture.client)

        assertThat(exception.grunn).isEqualTo(AntivirusFeil.TILKOBLINGSFEIL)
        assertThat(exception.cause).isNull()
        assertThat(exception.message).doesNotContain("private upstream detail")
        fixture.server.verify()
    }

    @Test
    fun `gyldig svar over 8 KiB godtas`() {
        val fixture = fixture()
        val body = """[{"result":"OK","virus":"","error":""}]""" + " ".repeat(9000)
        fixture.server.expect(requestTo(SCAN_URI))
            .andRespond(withSuccess(body, MediaType.APPLICATION_JSON))

        assertThat(fixture.client.skann(byteArrayOf())).isEqualTo(AntivirusResultat.REN)
        fixture.server.verify()
    }

    private fun skannFeil(client: AntivirusClient): AntivirusException =
        catchThrowable { client.skann(byteArrayOf(0x41)) } as AntivirusException

    private fun fixture(): Fixture {
        val restTemplate = AntivirusConfiguration().antivirusRestTemplate()
        val meterRegistry = SimpleMeterRegistry()
        return Fixture(
            client = AntivirusClient(
                URI(SCAN_URI),
                restTemplate,
                meterRegistry
            ),
            server = MockRestServiceServer.bindTo(restTemplate).build(),
            meterRegistry = meterRegistry
        )
    }

    private data class Fixture(
        val client: AntivirusClient,
        val server: MockRestServiceServer,
        val meterRegistry: SimpleMeterRegistry
    )

    companion object {
        private const val SCAN_URI = "http://clamav.test/api/v2/scan"

        @JvmStatic
        fun ugyldigeSvar(): Stream<String> = Stream.of(
            "{",
            "null",
            "[]",
            "[null]",
            "{}",
            """{"result":"OK","virus":"","error":""}""",
            """[{"result":"OK","virus":""}]""",
            """[{"result":null,"virus":"","error":""}]""",
            """[{"result":"OK","virus":null,"error":""}]""",
            """[{"result":"OK","virus":"","error":null}]""",
            """[{"result":"UNKNOWN","virus":"","error":""}]""",
            """[{"result":"ok","virus":"","error":""}]""",
            """[{"result":"OK","virus":"found","error":""}]""",
            """[{"result":"FOUND","virus":"","error":""}]""",
            """[{"result":"OK","virus":17,"error":""}]""",
            """[{"result":"OK","virus":"","error":""}] {}""",
            """[{"result":"OK","virus":"","error":""},{"result":"OK","virus":"","error":""}]"""
        )

        @JvmStatic
        fun skannefeilSvar(): Stream<String> = Stream.of(
            """[{"result":"OK","virus":"","error":false}]""",
            """[{"result":"ERROR","virus":"","error":"scanner unavailable"}]""",
            """[{"result":"OK","virus":"","error":"scanner unavailable"}]""",
            """[{"result":"FOUND","virus":"Example.Test","error":"scanner unavailable"}]"""
        )

        @JvmStatic
        fun ikkeVellykkedeHttpStatus(): Stream<HttpStatus> =
            Stream.of(HttpStatus.BAD_GATEWAY, HttpStatus.FOUND)
    }
}
