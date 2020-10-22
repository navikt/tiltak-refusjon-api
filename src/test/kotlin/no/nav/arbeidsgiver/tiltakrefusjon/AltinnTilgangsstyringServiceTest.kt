package no.nav.arbeidsgiver.tiltakrefusjon

import io.mockk.every
import io.mockk.mockk
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.junit.jupiter.api.BeforeEach
import org.assertj.core.api.Assertions.assertThat

import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.net.URI


@SpringBootTest
@ActiveProfiles("local","wiremock")
class AltinnTilgangsstyringServiceTest{


    @Value("\${tiltak-refusjon.altinn-tilgangstyring.uri}")
    lateinit var noe: URI

    @Autowired
    lateinit var altinnTilgangsstyringService: AltinnTilgangsstyringService;
    var context: TokenValidationContextHolder = mockk<TokenValidationContextHolder>()

    @BeforeEach
    fun setup(){

    }

    @Test fun `skal returnere en liste med organisasjoner personen har tilgang til`(){
        // GITT
        val fnr = "00000000007"
        every { context.tokenValidationContext.getClaims(anyString()).subject} returns fnr

        // NAAR
        val organisasjoner: Array<Organisasjon>? = altinnTilgangsstyringService.hentTilganger(fnr)

        // DA
        assertThat(organisasjoner).hasSize(8)
    }

}