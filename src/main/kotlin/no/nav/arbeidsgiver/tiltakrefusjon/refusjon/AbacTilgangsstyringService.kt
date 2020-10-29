package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.NavIdent
import no.nav.common.abac.Pep
import no.nav.common.abac.domain.AbacPersonId
import no.nav.common.abac.domain.request.ActionId
import no.nav.common.abac.exception.PepException
import org.springframework.stereotype.Service

@Service
class AbacTilgangsstyringService(val pep: Pep) {

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