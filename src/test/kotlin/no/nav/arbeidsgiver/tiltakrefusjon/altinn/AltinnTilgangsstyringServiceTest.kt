package no.nav.arbeidsgiver.tiltakrefusjon.altinn

import io.mockk.every
import io.mockk.mockk
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Fnr
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
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

    @Test fun `skal returnere en tom liste med organisasjoner når personen har ikke tilgang til noen`(){
        // GITT
        val serviceCode = null
        val serviceEdition = null
        val fnr = Fnr("10000000007")
        every { context.tokenValidationContext.getClaims(any()).subject} returns fnr.verdi

        // NAAR
        val organisasjoner: Array<Organisasjon>? = altinnTilgangsstyringService.hentTilganger(serviceCode, serviceEdition,fnr)

        // DA
        assertThat(organisasjoner).hasSize(8)
    }

}