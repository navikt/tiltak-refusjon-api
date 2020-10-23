package no.nav.arbeidsgiver.tiltakrefusjon.altinn

import io.mockk.every
import io.mockk.mockk
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

    @Test fun `skal returnere en liste med organisasjoner personen har tilgang til`(){
        // GITT
        val fnr = "00000000007"
        every { context.tokenValidationContext.getClaims(any()).subject} returns fnr

        // NAAR
        val organisasjoner: Array<Organisasjon>? = altinnTilgangsstyringService.hentTilganger(fnr)

        // DA
        assertThat(organisasjoner).hasSize(8)
    }

}