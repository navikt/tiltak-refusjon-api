package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.arbeidsgiver.tiltakrefusjon.altinn.AltinnTilgangsstyringService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.stereotype.Component

@Component
class InnloggetArbeidsgiverService(val context: TokenValidationContextHolder,
                                   val altinnTilgangsstyringService: AltinnTilgangsstyringService,
                                   val refusjonRepository: RefusjonRepository) {

    fun hentInnloggetArbeidsgiver(): InnloggetArbeidsgiver {
        val fnr = context.tokenValidationContext.getClaims("tokenx").getStringClaim("pid")
        return InnloggetArbeidsgiver(fnr, altinnTilgangsstyringService, refusjonRepository)
    }
}