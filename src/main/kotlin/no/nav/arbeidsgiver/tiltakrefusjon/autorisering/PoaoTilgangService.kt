package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Fnr
import java.util.*

interface PoaoTilgangService {
    fun harSkrivetilgang(beslutterAzureUUID: UUID, fnr: Fnr): Boolean
    fun harSkrivetilgang(beslutterAzureUUID: UUID, fnrSet: Set<Fnr>): Map<Fnr, Boolean>
    fun hentGrunn(beslutterAzureUUID: UUID, fnr: Fnr): String?
}
