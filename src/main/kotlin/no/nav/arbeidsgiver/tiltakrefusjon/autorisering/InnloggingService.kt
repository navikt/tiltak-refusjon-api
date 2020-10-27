package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.arbeidsgiver.tiltakrefusjon.altinn.AltinnTilgangsstyringService
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.Organisasjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Fnr
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Identifikator
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Utils
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.stereotype.Component

@Component
class InnloggingService(val context: TokenValidationContextHolder,
                        val altinnTilgangsstyringService: AltinnTilgangsstyringService) {
    fun hentPaloggetIdent():Identifikator{
        val valContext = context.tokenValidationContext
        val claims = valContext.getClaims("aad")
        return Fnr(claims.subject)
    }

    fun hentTilganger(personIdent: Identifikator): Set<Organisasjon> {
        return altinnTilgangsstyringService.hentTilganger(personIdent)
    }

    fun hentOrganisasjonerForPaloggetBruker(): Set<Organisasjon>? {
        return hentTilganger(hentPaloggetIdent())
    }

    fun sjekkHarTilgangTilBedrift(bedriftsnummerDetSokesOm:String) {
        if(Utils.erIkkeTomme(bedriftsnummerDetSokesOm) && !hentOrganisasjonerForPaloggetBruker()?.any{ it.organizationNumber == bedriftsnummerDetSokesOm }!!){
            throw TilgangskontrollException("Person har ikke tilgang")
        }
    }
}