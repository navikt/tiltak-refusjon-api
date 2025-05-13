package no.nav.arbeidsgiver.tiltakrefusjon.altinn

import io.mockk.every
import io.mockk.mockk
import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.assertFeilkode
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Fnr
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.RestTemplate


@SpringBootTest
@ActiveProfiles("local")
@AutoConfigureWireMock(port = 8091)
class AltinnTilgangsstyringServiceTest {

    @Autowired
    lateinit var altinnTilgangsstyringService: AltinnTilgangsstyringService;
    var context: TokenValidationContextHolder = mockk<TokenValidationContextHolder>()

    @Test
    fun `skal returnere en liste med 8 organisasjoner personen har tilgang til`(@Autowired altinnTilgangsstyringProperties: AltinnTilgangsstyringProperties) {
        // GITT
        altinnTilgangsstyringProperties.inntektsmeldingServiceCode = 4936
        val fnr = Fnr("10000000007")
        every { context.getTokenValidationContext().getClaims(any()).getStringClaim("pid") } returns fnr.verdi

        // NÅR
        val organisasjoner: Set<Organisasjon> = altinnTilgangsstyringService.hentInntektsmeldingTilganger(fnr.verdi)

        // SÅ
        assertThat(organisasjoner).hasSize(8)
    }

    @Test
    fun `skal kaste en Altinnfeil om personen ikke finnes`(@Autowired altinnTilgangsstyringProperties: AltinnTilgangsstyringProperties) {
        // GITT
        altinnTilgangsstyringProperties.inntektsmeldingServiceCode = 4936
        val fnr = Fnr("21234567890")
        every { context.getTokenValidationContext().getClaims(any()).getStringClaim("pid") } returns fnr.verdi

        // NÅR
        assertFeilkode(Feilkode.ALTINN) {
            altinnTilgangsstyringService.hentInntektsmeldingTilganger(fnr.verdi)
        }
    }

    @Test
    fun `skal returnere en tom liste med organisasjoner om personen ikke har tilgang`(@Autowired altinnTilgangsstyringProperties: AltinnTilgangsstyringProperties) {
        // GITT
        altinnTilgangsstyringProperties.inntektsmeldingServiceCode = 5516
        altinnTilgangsstyringService = AltinnTilgangsstyringService(altinnTilgangsstyringProperties, RestTemplate())
        val fnr = Fnr("21234567890")
        every { context.getTokenValidationContext().getClaims(any()).getStringClaim("pid") } returns fnr.verdi

        // NÅR
        val organisasjoner: Set<Organisasjon> = altinnTilgangsstyringService.hentInntektsmeldingTilganger(fnr.verdi)

        // SÅ
        assertThat(organisasjoner).hasSize(0)
    }

    @Test
    fun `skal kasten en exception om feil serviceCode er gitt`(@Autowired altinnTilgangsstyringProperties: AltinnTilgangsstyringProperties) {
        // GITT
        altinnTilgangsstyringProperties.inntektsmeldingServiceCode = 0
        altinnTilgangsstyringService = AltinnTilgangsstyringService(altinnTilgangsstyringProperties, RestTemplate())
        val fnr = Fnr("21234567890")
        every { context.getTokenValidationContext().getClaims(any()).getStringClaim("pid") } returns fnr.verdi

        // NÅR
        assertFeilkode(Feilkode.ALTINN) {
            altinnTilgangsstyringService.hentInntektsmeldingTilganger(fnr.verdi)
        }
    }

}
