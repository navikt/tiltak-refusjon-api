package no.nav.arbeidsgiver.tiltakrefusjon.altinn

import io.mockk.every
import io.mockk.mockk
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Fnr
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles


@SpringBootTest
@ActiveProfiles("local","wiremock")
@DirtiesContext
class AltinnTilgangsstyringServiceTest(){

    @Autowired
    lateinit var altinnTilgangsstyringService: AltinnTilgangsstyringService;
    var context: TokenValidationContextHolder = mockk<TokenValidationContextHolder>()

    @Test fun `skal returnere en liste med 8 organisasjoner personen har tilgang til`(@Autowired altinnTilgangsstyringProperties:AltinnTilgangsstyringProperties){
        // GITT
        altinnTilgangsstyringProperties.serviceCode = 4936
        val fnr = Fnr("10000000007")
        every { context.tokenValidationContext.getClaims(any()).getStringClaim("pid")} returns fnr.verdi

        // NÅR
        val organisasjoner: Set<Organisasjon>? = altinnTilgangsstyringService.hentTilganger(fnr.verdi)

        // SÅ
        assertThat(organisasjoner).hasSize(8)
    }

    @Test fun `skal kaste en Altinnfeil om personen ikke finnes`(@Autowired altinnTilgangsstyringProperties:AltinnTilgangsstyringProperties){
        // GITT
        altinnTilgangsstyringProperties.serviceCode = 4936
        val fnr = Fnr("01234567890")
        every { context.tokenValidationContext.getClaims(any()).getStringClaim("pid")} returns fnr.verdi

        // NÅR
        assertThrows<AltinnFeilException> {
            altinnTilgangsstyringService.hentTilganger(fnr.verdi)
        }
    }

    @Test fun `skal returnere en tom liste med organisasjoner om personen ikke har tilgang`(@Autowired altinnTilgangsstyringProperties:AltinnTilgangsstyringProperties) {
        // GITT
        altinnTilgangsstyringProperties.serviceCode = 5516
        altinnTilgangsstyringService = AltinnTilgangsstyringService(altinnTilgangsstyringProperties)
        val fnr = Fnr("01234567890")
        every { context.tokenValidationContext.getClaims(any()).getStringClaim("pid") } returns fnr.verdi

        // NÅR
        val organisasjoner: Set<Organisasjon>? = altinnTilgangsstyringService.hentTilganger(fnr.verdi)

        // SÅ
        assertThat(organisasjoner).hasSize(0)
    }

    @Test fun `skal kasten en exception om feil serviceCode er gitt`(@Autowired altinnTilgangsstyringProperties:AltinnTilgangsstyringProperties) {
        // GITT
        altinnTilgangsstyringProperties.serviceCode = 0
        altinnTilgangsstyringService = AltinnTilgangsstyringService(altinnTilgangsstyringProperties)
        val fnr = Fnr("01234567890")
        every { context.tokenValidationContext.getClaims(any()).getStringClaim("pid") } returns fnr.verdi

        // NÅR
        assertThrows<AltinnFeilException> {
            val organisasjoner: Set<Organisasjon>? = altinnTilgangsstyringService.hentTilganger(fnr.verdi)
        }
    }

}