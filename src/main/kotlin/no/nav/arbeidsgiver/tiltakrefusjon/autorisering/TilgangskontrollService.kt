package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Fnr
import org.springframework.stereotype.Service

@Service
class TilgangskontrollService(
    private val poaoTilgangService: PoaoTilgangService
) {
    fun harLeseTilgang(internIdentifikatorer: InternIdentifikatorer, deltakerFnr: String): Tilgang {
        return poaoTilgangService.harSkrivetilgang(internIdentifikatorer.azureOid, Fnr(deltakerFnr))
    }
}
