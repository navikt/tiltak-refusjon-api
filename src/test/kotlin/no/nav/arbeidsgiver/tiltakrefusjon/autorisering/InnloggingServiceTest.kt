package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import io.mockk.every
import io.mockk.mockk
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.AltinnTilgangsstyringService
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.Organisasjon
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.enOrganisasjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Fnr
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
        every{ context.tokenValidationContext.getClaims(any()).subject} returns fnr.verdi


        // NÅR
        val organasjoner = innloggetService.hentTilganger(fnr)

        // DA
        Assertions.assertThat(organasjoner).isEmpty()
    }

    @Test
    fun `skal returnere ingen altinn organisasjoner for innloggetbruker`() {
        // GITT
        val fnr = Fnr("00000000007")
        every{ altinnTilgangsstyringService.hentTilganger(fnr)} returns setOf<Organisasjon>(enOrganisasjon())
        every{ context.tokenValidationContext.getClaims(any()).subject} returns fnr.verdi

        // NÅR
        val organasjoner = innloggetService.hentOrganisasjonerForPaloggetBruker()

        // DA
        Assertions.assertThat(organasjoner).hasSize(1)
    }

    @Test
    fun `skal ikke kaste en exceptioon om søkes med en tom bedriftsnummmer`() {
        // GITT
        val fnr = Fnr("00000000007")
        every{ altinnTilgangsstyringService.hentTilganger(fnr)} returns setOf<Organisasjon>(enOrganisasjon())
        every{ context.tokenValidationContext.getClaims(any()).subject} returns fnr.verdi

        // NÅR
       innloggetService.sjekkHarTilgangTilBedrift("")
    }

    @Test
    fun `skal returnere kaste en exception om person ikke har tilgang`() {
        // GITT
        val fnr = Fnr("00000000007")
        every{ altinnTilgangsstyringService.hentTilganger(fnr)} returns setOf<Organisasjon>(enOrganisasjon())
        every{ context.tokenValidationContext.getClaims(any()).subject} returns fnr.verdi

        // NÅR
        assertThrows<TilgangskontrollException> {
            innloggetService.sjekkHarTilgangTilBedrift("1234")
        }
    }
}