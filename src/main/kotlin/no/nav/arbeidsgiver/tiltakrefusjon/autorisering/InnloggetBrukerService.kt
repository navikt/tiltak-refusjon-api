package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Fnr
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Identifikator
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.stereotype.Component
import java.lang.RuntimeException

@Component
class InnloggetBrukerService(val context: TokenValidationContextHolder){

     fun hentInnloggetIdent(): Identifikator{
         return hentPaloggetIdent()
     }

   private fun hentPaloggetIdent(): Identifikator {
        val valContext = context.tokenValidationContext
        val claims = valContext.getClaims("aad")

       if(Fnr.erGyldigFnr(claims.subject)){
           return Fnr(claims.subject)
       }
       if (NavIdent.erNavIdent(claims.subject)){
           return NavIdent(claims.subject)
       }
       throw RuntimeException("Ukjent ident") //TODO Kast noe fornuftig
    }

}