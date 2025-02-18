package no.nav.arbeidsgiver.tiltakrefusjon.utils

import no.nav.security.token.support.core.context.TokenValidationContextHolder

enum class Issuer(public val iss: String) {
    TOKEN_X("tokenx"), AZURE("aad")
}

fun TokenValidationContextHolder.getClaims(issuer: Issuer) = if (this.getTokenValidationContext().hasTokenFor(issuer.iss)) this.getTokenValidationContext().getClaims(issuer.iss) else null
fun TokenValidationContextHolder.erAzureBruker(): Boolean = this.getTokenValidationContext().hasTokenFor(Issuer.AZURE.iss)
fun TokenValidationContextHolder.erTokenXBruker(): Boolean = this.getTokenValidationContext().hasTokenFor(Issuer.TOKEN_X.iss)
fun TokenValidationContextHolder.sesjonsId() = this.getTokenValidationContext().anyValidClaims?.get("sid")
    ?: throw Exception("Kunne ikke finne sesjonsid for token. Claims: ${this.getTokenValidationContext().anyValidClaims?.allClaims?.keys ?: emptyList()}")
fun TokenValidationContextHolder.jwtId() = this.getTokenValidationContext().anyValidClaims?.get("jti")
    ?: throw Exception("Kunne ikke finne sesjonsid for token. Claims: ${this.getTokenValidationContext().anyValidClaims?.allClaims?.keys ?: emptyList()}")
fun TokenValidationContextHolder.sub() = this.getTokenValidationContext().anyValidClaims?.get("sub")
    ?: throw Exception("Kunne ikke finne sesjonsid for token. Claims: ${this.getTokenValidationContext().anyValidClaims?.allClaims?.keys ?: emptyList()}")
