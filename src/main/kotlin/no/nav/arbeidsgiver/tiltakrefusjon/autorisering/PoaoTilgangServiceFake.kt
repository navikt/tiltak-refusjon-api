package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Fnr
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile(value = ["local", "test", "dockercompose", "dev-gcp-labs"])
class PoaoTilgangServiceFake : PoaoTilgangService {
    val deny = listOf(
        Fnr("07098142678")
    )

    override fun harSkrivetilgang(beslutterAzureUUID: UUID, fnr: Fnr) = !deny.contains(fnr)

    override fun harSkrivetilgang(beslutterAzureUUID: UUID, fnrSet: Set<Fnr>) =
        fnrSet.associateWith { true }

    override fun hentGrunn(beslutterAzureUUID: UUID, fnr: Fnr): String? = null

}
