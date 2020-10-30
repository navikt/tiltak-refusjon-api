package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import io.mockk.every
import io.mockk.mockk
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.AltinnTilgangsstyringService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Fnr
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.ResponseEntity

@ExtendWith(MockitoExtension::class)
class InnloggetArbeidsgiverControllerTest {

    var innloggetArbeidsgiverService: InnloggetArbeidsgiverService = mockk<InnloggetArbeidsgiverService>()
    var altinnTilgangsstyringService: AltinnTilgangsstyringService = mockk<AltinnTilgangsstyringService>()
    var refusjonRepository: RefusjonRepository = mockk<RefusjonRepository>()
    lateinit var innloggetArbeidsgiverController: InnloggetArbeidsgiverController

    @BeforeEach
    fun setup(){
        innloggetArbeidsgiverController = InnloggetArbeidsgiverController(innloggetArbeidsgiverService, refusjonRepository)
    }

    @Test
    fun `skal returnere logget bruker tilbake med ingen altinn organisasjoner`() {
        // GITT
        val fnrPaloggetBruker:String = "00000000007"
        every{ altinnTilgangsstyringService.hentTilganger(eq(fnrPaloggetBruker))} returns emptySet();
        val innloggetArbeidsgiver = InnloggetArbeidsgiver(fnrPaloggetBruker, altinnTilgangsstyringService, refusjonRepository)
        every { innloggetArbeidsgiverService.hentInnloggetArbeidsgiver() } returns innloggetArbeidsgiver

        // NÅR
        val innloggetArbeidsgiverResponse:ResponseEntity<InnloggetArbeidsgiver> = innloggetArbeidsgiverController.hentInnloggetBruker()

        // DA
        assertThat(innloggetArbeidsgiverResponse.body?.identifikator).isEqualTo(fnrPaloggetBruker)
        assertThat(innloggetArbeidsgiverResponse.body?.organisasjoner).hasSize(0)
    }
}