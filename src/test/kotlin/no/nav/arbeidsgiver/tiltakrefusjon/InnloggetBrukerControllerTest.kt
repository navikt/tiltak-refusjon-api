package no.nav.arbeidsgiver.tiltakrefusjon

import io.mockk.every
import io.mockk.mockk
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach

@ExtendWith(MockitoExtension::class)
class InnloggetBrukerControllerTest {


    var context: TokenValidationContextHolder = mockk<TokenValidationContextHolder>()
    lateinit var innloggetBrukerController:InnloggetBrukerController

    @BeforeEach
    fun setup(){
        innloggetBrukerController = InnloggetBrukerController(context)
    }

    @Test
    fun skal_returnere_logget_bruker_tilbake() {
        // GITT
        every{ context.tokenValidationContext.getClaims(any()).subject} returns "007"

        // NAAR
        val innloggetBruker = innloggetBrukerController.hentInnloggetBruker()

        // OG
        assertThat(innloggetBruker.identifikator).isEqualTo("007")
    }
}