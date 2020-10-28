package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import io.mockk.every
import io.mockk.mockk
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.enOrganisasjon
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.ResponseEntity

@ExtendWith(MockitoExtension::class)
class InnloggetBrukerControllerTest {

    var innloggingService: InnloggingService = mockk<InnloggingService>()
    lateinit var innloggetBrukerController:InnloggetBrukerController

    @BeforeEach
    fun setup(){
        innloggetBrukerController = InnloggetBrukerController(innloggingService)
    }

    @Test
    fun `skal returnere logget bruker tilbake med 1 altinn organisasjoner`() {
        // GITT
        val fnrPaloggetBruker = "00000000007"
        every { innloggingService.hentInnloggetBruker() } returns InnloggetBruker(fnrPaloggetBruker, setOf(enOrganisasjon()))

        // NÅR
        val innloggetBrukerResponse:ResponseEntity<InnloggetBruker> = innloggetBrukerController.hentInnloggetBruker()

        // SÅ
        assertThat(innloggetBrukerResponse.body?.identifikator).isEqualTo(fnrPaloggetBruker)
        assertThat(innloggetBrukerResponse.body?.altinnOrganisasjoner).hasSize(1)
    }
}