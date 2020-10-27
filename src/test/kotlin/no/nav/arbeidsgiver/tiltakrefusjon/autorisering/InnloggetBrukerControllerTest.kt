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
import org.springframework.http.ResponseEntity

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
        every{ altinnTilgangsstyringService.hentTilganger(fnr)} returns emptySet()
        every{ context.tokenValidationContext.getClaims(any()).get("pid").toString()} returns fnr.verdi


        // NÅR
        val innloggetBrukerResponse:ResponseEntity<InnloggetBruker> = innloggetBrukerController.hentInnloggetBruker()

        // DA
        assertThat(innloggetBrukerResponse.body?.identifikator).isEqualTo(fnr.verdi)
        assertThat(innloggetBrukerResponse.body?.altinnOrganisasjoner).hasSize(0)
    }
}