package no.nav.arbeidsgiver.tiltakrefusjon

import org.springframework.stereotype.Service

@Service
class AltinnTilgangsstyringService {
    fun hentTilganger(fnr: String): Set<AltinnOrganisasjoner> {
        return emptySet<AltinnOrganisasjoner>()
    }
}