package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import io.mockk.every
import io.mockk.mockk
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.AltinnTilgangsstyringService
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.Organisasjon
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.enOrganisasjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension


@ExtendWith(MockitoExtension::class)
class InnloggingBedriftServiceTest{
    var context: TokenValidationContextHolder = mockk<TokenValidationContextHolder>()
    var altinnTilgangsstyringService = mockk<AltinnTilgangsstyringService>()
    var refusjonRepository = mockk<RefusjonRepository>()
    lateinit var innloggetArbeidsgiverService: InnloggetArbeidsgiverService

    @BeforeEach
    fun setup(){
        innloggetArbeidsgiverService = InnloggetArbeidsgiverService(context, altinnTilgangsstyringService, refusjonRepository)
    }

    @Test
    fun `skal returnere logget bruker tilbake med ingen altinn organisasjoner`() {
        // GITT
        val fnr:String = "00000000007"
        every{ altinnTilgangsstyringService.hentTilganger(fnr)} returns emptySet()
        every{ context.tokenValidationContext.getClaims(any()).getStringClaim("pid")} returns fnr


        // NÅR
        val organasjoner = altinnTilgangsstyringService.hentTilganger(fnr)

        // SÅ
        Assertions.assertThat(organasjoner).isEmpty()
    }

    @Test
    fun `skal returnere 1 altinn organisasjon for innloggetbruker`() {
        // GITT
        val fnr:String = "00000000007"
        every{ altinnTilgangsstyringService.hentTilganger(fnr)} returns setOf<Organisasjon>(enOrganisasjon())
        every{ context.tokenValidationContext.getClaims(any()).subject} returns fnr

        // NÅR
        val organasjoner = altinnTilgangsstyringService.hentTilganger(fnr)

        // DA
        Assertions.assertThat(organasjoner).hasSize(1)
    }
}