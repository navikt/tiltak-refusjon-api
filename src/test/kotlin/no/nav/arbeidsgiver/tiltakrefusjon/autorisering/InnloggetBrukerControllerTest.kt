package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import io.mockk.every
import io.mockk.mockk
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Fnr
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
    fun `skal returnere logget bruker tilbake med ingen altinn organisasjoner`() {
        // GITT
        val fnrPaloggetBruker = Fnr("00000000007")
        every { innloggingService.hentPaloggetIdent() } returns fnrPaloggetBruker
        every{ innloggingService.hentTilganger(fnrPaloggetBruker)} returns emptySet()

        // NÅR
        val innloggetBrukerResponse:ResponseEntity<InnloggetBruker> = innloggetBrukerController.hentInnloggetBruker()

        // DA
        assertThat(innloggetBrukerResponse.body?.identifikator).isEqualTo(fnrPaloggetBruker.verdi)
        assertThat(innloggetBrukerResponse.body?.altinnOrganisasjoner).hasSize(0)
    }
}