package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.RessursFinnesIkkeException
import no.nav.arbeidsgiver.tiltakrefusjon.enRefusjon
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.AltinnTilgangsstyringProperties
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.AltinnTilgangsstyringService
import no.nav.arbeidsgiver.tiltakrefusjon.SyntetiskeFnr
import no.nav.arbeidsgiver.tiltakrefusjon.`Suzanna Hansen`
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetArbeidsgiver
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBrukerService
import no.nav.arbeidsgiver.tiltakrefusjon.dokgen.PdfgenService
import no.nav.arbeidsgiver.tiltakrefusjon.persondata.PersondataService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.KorreksjonRepository
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonService
import no.nav.arbeidsgiver.tiltakrefusjon.utils.ulid
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.HttpMethod
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.util.Optional

@SyntetiskeFnr
class ArbeidsgiverRefusjonControllerTest {

    private lateinit var controller: ArbeidsgiverRefusjonController
    private lateinit var innloggetServiceMock: InnloggetBrukerService
    private lateinit var pdfgenService: PdfgenService

    @BeforeEach
    fun setup() {
        innloggetServiceMock = mock()
        pdfgenService = mock()
        controller = ArbeidsgiverRefusjonController(innloggetServiceMock, pdfgenService)
    }

    private fun lagInnloggetArbeidsgiver(refusjon: Refusjon?): InnloggetArbeidsgiver {
        val restTemplate = RestTemplate()
        val server = MockRestServiceServer.bindTo(restTemplate).build()
        repeat(2) {
            server.expect(requestTo("http://localhost/altinn-tilganger"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                    """{"isError":false,"hierarki":[{"orgnr":"888888888","altinn3Tilganger":[],"altinn2Tilganger":[],"underenheter":[{"orgnr":"999999999","altinn3Tilganger":["nav_tiltak_tiltaksrefusjon"],"altinn2Tilganger":["4936:1"],"underenheter":[],"navn":"Bedriften AS","organisasjonsform":"AS","erSlettet":false}],"navn":"Morselskap AS","organisasjonsform":"AS","erSlettet":false}],"orgNrTilTilganger":{},"tilgangTilOrgNr":{}}""",
                    MediaType.APPLICATION_JSON
                ))
        }
        val altinnService = AltinnTilgangsstyringService(
            AltinnTilgangsstyringProperties(
                arbeidsgiverAltinnTilgangerUri = URI("http://localhost/altinn-tilganger"),
                inntektsmeldingServiceCode = 4936,
                inntektsmeldingServiceEdition = 1,
                adressesperreServiceCode = 5516,
                adressesperreServiceEdition = 7,
            ),
            restTemplate
        )
        val refusjonRepository = mock<RefusjonRepository>()
        whenever(refusjonRepository.findById(anyString())).thenReturn(Optional.ofNullable(refusjon))
        val korreksjonRepository = mock<KorreksjonRepository>()
        val refusjonService = mock<RefusjonService>()
        val persondataService = mock<PersondataService>()
        whenever(persondataService.hentDiskresjonskode(any())).thenReturn(no.nav.team_tiltak.felles.persondata.pdl.domene.Diskresjonskode.UGRADERT)
        return InnloggetArbeidsgiver(
            "04511349341",
            altinnService,
            refusjonRepository,
            korreksjonRepository,
            refusjonService,
            persondataService
        )
    }

    @Test
    fun `test at pdf controller endepunkt ikke spitter ut noe om den ikke finnes`() {
        assertThat(controller.hentPDF("").body).isNull()
    }

    @Test
    fun `test at pdf controller endepunkt returnerer pdf som bytearray`() {
        val innloggetArbeidsgiver = lagInnloggetArbeidsgiver(enRefusjon())
        whenever(innloggetServiceMock.hentInnloggetArbeidsgiver()).thenReturn(innloggetArbeidsgiver)
        whenever(pdfgenService.refusjonPdf(any())).thenReturn(ByteArray(1))

        val forventetHeaders = HttpHeaders()
        forventetHeaders.contentType = MediaType.APPLICATION_PDF
        forventetHeaders[HttpHeaders.CONTENT_DISPOSITION] =
            "inline; filename=Refusjon om " + `Suzanna Hansen`().refusjonsgrunnlag.tilskuddsgrunnlag.tiltakstype.name + ".pdf"
        forventetHeaders.contentLength = 1

        assertThat(controller.hentPDF(ulid()).headers).isEqualTo(forventetHeaders)
        assertThat(controller.hentPDF(ulid()).body).isNotEmpty()
    }

    @Test
    fun `test at pdf controller endepunkt ikke finner refusjon`() {
        val innloggetArbeidsgiver = lagInnloggetArbeidsgiver(null)
        whenever(innloggetServiceMock.hentInnloggetArbeidsgiver()).thenReturn(innloggetArbeidsgiver)

        assertThrows<RessursFinnesIkkeException> { controller.hentPDF(ulid()) }
    }
}
