package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Fnr
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TilgangskontrollService(
    val poaoTilgangService: PoaoTilgangService
) {
    val log = LoggerFactory.getLogger(TilgangskontrollService::class.java)

    fun harLeseTilgang(internIdentifikatorer: InternIdentifikatorer, deltakerFnr: String): Tilgang {
        return poaoTilgangService.harSkrivetilgang(internIdentifikatorer.azureOid, Fnr(deltakerFnr))
    }
}
