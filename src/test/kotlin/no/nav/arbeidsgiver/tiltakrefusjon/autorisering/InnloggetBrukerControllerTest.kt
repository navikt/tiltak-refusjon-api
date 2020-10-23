package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import io.mockk.every
import io.mockk.mockk
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.AltinnTilgangsstyringService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Fnr
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class InnloggetBrukerControllerTest {

    var context: TokenValidationContextHolder = mockk<TokenValidationContextHolder>()
    var altinnTilgangsstyringService = mockk<AltinnTilgangsstyringService>()
    lateinit var innloggetBrukerController: InnloggetBrukerController

    @BeforeEach
    fun setup(){
        innloggetBrukerController = InnloggetBrukerController(context, altinnTilgangsstyringService)
    }

    @Test
    fun `skal returnere logget bruker tilbake med ingen altinn organisasjoner`() {
        // GITT
        val fnr = Fnr("00000000007")
        every{ altinnTilgangsstyringService.hentTilganger(0,0,fnr)} returns emptyArray()
        every{ context.tokenValidationContext.getClaims(any()).subject} returns fnr.verdi


        // NAAR
        val innloggetBruker = innloggetBrukerController.hentInnloggetBruker()

        // DA
        assertThat(innloggetBruker.identifikator).isEqualTo(fnr.verdi)
        assertThat(innloggetBruker.altinnOrganisasjoner).hasSize(0)
    }
}