package no.nav.arbeidsgiver.tiltakrefusjon.altinn

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestTemplate
import org.springframework.http.HttpMethod
import java.net.URI

class AltinnTilgangsstyringServiceTest {

    @Test
    fun `hentInntektsmeldingEllerRefusjonTilganger skal flate ting ut`() {
        val restTemplate = RestTemplate()
        val server = MockRestServiceServer.bindTo(restTemplate).build()
        val properties = AltinnTilgangsstyringProperties(
            arbeidsgiverAltinnTilgangerUri = URI("http://localhost/altinn-tilganger"),
            inntektsmeldingServiceCode = 4936,
            inntektsmeldingServiceEdition = 1,
            adressesperreServiceCode = 5516,
            adressesperreServiceEdition = 7,
        )
        val service = AltinnTilgangsstyringService(properties, restTemplate)
        val responseFraAltinn3 =
            "{\"isError\":false,\"hierarki\":[{\"orgnr\":\"811306312\",\"altinn3Tilganger\":[\"nav_tiltak_tiltaksrefusjon\"],\"altinn2Tilganger\":[\"4936:1\"],\"underenheter\":[{\"orgnr\":\"811306622\",\"altinn3Tilganger\":[\"nav_tiltak_tiltaksrefusjon\"],\"altinn2Tilganger\":[\"4936:1\"],\"underenheter\":[],\"navn\":\"DAVIK OG EIDSLANDET\",\"organisasjonsform\":\"BEDR\",\"erSlettet\":false}],\"navn\":\"DAVIK OG HORTEN\",\"organisasjonsform\":\"AS\",\"erSlettet\":false}],\"orgNrTilTilganger\":{},\"tilgangTilOrgNr\":{}}"

        server.expect(requestTo("http://localhost/altinn-tilganger"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(responseFraAltinn3, MediaType.APPLICATION_JSON))

        val organisasjoner = service.hentInntektsmeldingEllerRefusjonTilganger()

        assertThat(organisasjoner).hasSize(2)
        server.verify()
    }
}
