package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.AbacTilgangsstyringService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.stereotype.Service

@Service
class InnloggetSaksbehandlerService(val context: TokenValidationContextHolder, val refusjonRepository: RefusjonRepository, val abacTilgangsstyringService: AbacTilgangsstyringService) {

    fun hentInnloggetSaksbehandler(): InnloggetSaksbehandler {
        val navIdent: String = context.tokenValidationContext.getClaims("aad").getStringClaim("NAVident")
        return InnloggetSaksbehandler(navIdent, abacTilgangsstyringService, refusjonRepository)
    }
}