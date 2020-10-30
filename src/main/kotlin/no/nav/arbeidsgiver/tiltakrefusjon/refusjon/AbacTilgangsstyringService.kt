package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.common.abac.Pep
import no.nav.common.abac.domain.AbacPersonId
import no.nav.common.abac.domain.request.ActionId
import no.nav.common.abac.exception.PepException
import org.springframework.stereotype.Service

@Service
class AbacTilgangsstyringService(val pep: Pep) {

    fun harLeseTilgang(navIdent: String, deltakerId: String): Boolean {
        return harDeltakerTilgang(navIdent, deltakerId, ActionId.READ)
    }

    fun harSkriveTilgang(navIdent: String, deltakerId: String): Boolean {
        return harDeltakerTilgang(navIdent, deltakerId, ActionId.WRITE)
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