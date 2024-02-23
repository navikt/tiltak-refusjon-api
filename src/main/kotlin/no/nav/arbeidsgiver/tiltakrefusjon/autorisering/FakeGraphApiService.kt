package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty("tiltak-refusjon.graph-api.fake")
class FakeGraphApiService(val context: TokenValidationContextHolder) : GraphApiService {
    override fun hent(): GraphApiService.GraphApiResponse {
        val claims = context.getTokenValidationContext().getClaims("aad")
        return GraphApiService.GraphApiResponse(claims.getStringClaim("NAVident"), "Navn Testnavn")
    }
}