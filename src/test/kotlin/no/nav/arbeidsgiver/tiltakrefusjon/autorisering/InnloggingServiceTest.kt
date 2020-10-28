package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import io.mockk.every
import io.mockk.mockk
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.AltinnTilgangsstyringService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Fnr
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension


@ExtendWith(MockitoExtension::class)
class InnloggingServiceTest{
    var context: TokenValidationContextHolder = mockk<TokenValidationContextHolder>()
    var altinnTilgangsstyringService = mockk<AltinnTilgangsstyringService>()
    lateinit var innloggetService: InnloggingService

    @BeforeEach
    fun setup(){
        innloggetService = InnloggingService(context, altinnTilgangsstyringService)
    }

    @Test
    fun `skal returnere logget bruker tilbake med ingen altinn organisasjoner`() {
        // GITT
        val fnr = Fnr("00000000007")
        every{ altinnTilgangsstyringService.hentTilganger(fnr)} returns emptySet()
        every{ context.tokenValidationContext.getClaims(any()).getStringClaim("pid")} returns fnr.verdi


        // NÅR
        val organasjoner = innloggetService.hentOrganisasjoner(fnr)

        // SÅ
        Assertions.assertThat(organasjoner).isEmpty()
    }

}