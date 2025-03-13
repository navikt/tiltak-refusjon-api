package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Fnr
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TilgangskontrollService(
    val abacTilgangsstyringService: AbacTilgangsstyringService,
    val poaoTilgangService: PoaoTilgangService
) {
    val log = LoggerFactory.getLogger(TilgangskontrollService::class.java)

    fun harLeseTilgang(internIdentifikatorer: InternIdentifikatorer, deltakerFnr: String): Boolean {
        val abacLeseTilgang = abacTilgangsstyringService.harLeseTilgang(internIdentifikatorer.navIdent, deltakerFnr)
        val poaoLeseTilgang = poaoTilgangService.harSkrivetilgang(internIdentifikatorer.azureOid, Fnr(deltakerFnr))
        if (abacLeseTilgang != poaoLeseTilgang) {
            log.error("Avvik i lesetilgang mellom abac {} og poao tilgang {}", abacLeseTilgang, poaoLeseTilgang)
        }
        return abacLeseTilgang
    }
}
