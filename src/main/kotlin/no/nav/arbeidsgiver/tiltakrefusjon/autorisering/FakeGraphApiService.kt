package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.arbeidsgiver.tiltakrefusjon.utils.Issuer
import no.nav.arbeidsgiver.tiltakrefusjon.utils.getClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty("tiltak-refusjon.graph-api.fake")
class FakeGraphApiService(val context: TokenValidationContextHolder) : GraphApiService {
    override fun hent(): GraphApiService.GraphApiResponse {
        val claims = context.getClaims(Issuer.AZURE)
        return GraphApiService.GraphApiResponse(claims!!.getStringClaim("NAVident"), "Navn Testnavn")
    }
}