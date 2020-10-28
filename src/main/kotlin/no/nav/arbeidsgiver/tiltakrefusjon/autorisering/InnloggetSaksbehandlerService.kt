package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.common.abac.Pep
import no.nav.common.abac.domain.AbacPersonId
import no.nav.common.abac.domain.request.ActionId
import no.nav.common.abac.exception.PepException
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.stereotype.Service
import java.lang.Exception

@Service
class InnloggetSaksbehandlerService(val context: TokenValidationContextHolder, val pep: Pep) {

    fun hentInnloggetSaksbehandler(): InnloggetSaksbehandler {
        val navIdent: String = context.tokenValidationContext.getClaims("aad").subject
        return InnloggetSaksbehandler(NavIdent(navIdent))
    }

    fun harLeseTilgang(navIdent: NavIdent, deltakerId: String): Boolean {
        return harDeltakerTilgang(navIdent.verdi, deltakerId, ActionId.READ)
    }

    fun harSkriveTilgang(navIdent: NavIdent, deltakerId: String): Boolean {
        return harDeltakerTilgang(navIdent.verdi, deltakerId, ActionId.WRITE)
    }

    private fun harDeltakerTilgang(saksbehandlerId: String, deltakerId: String, actionId: ActionId):Boolean{
        val personId = AbacPersonId.fnr(deltakerId)
        try {
            pep.sjekkVeilederTilgangTilBruker(saksbehandlerId, actionId, personId)
        } catch (e: PepException) {
            return false;
        } catch (e: Exception) {
            return false
        }
        return true
    }
}

