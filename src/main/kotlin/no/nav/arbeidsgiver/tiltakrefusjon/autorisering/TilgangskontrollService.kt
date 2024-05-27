package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import org.springframework.stereotype.Service

@Service
class TilgangskontrollService(
    val abacTilgangsstyringService: AbacTilgangsstyringService
) {
    fun harLeseTilgang(internIdentifikatorer: InternIdentifikatorer, deltakerFnr: String): Boolean {
        return abacTilgangsstyringService.harLeseTilgang(internIdentifikatorer.navIdent, deltakerFnr)
    }
}
