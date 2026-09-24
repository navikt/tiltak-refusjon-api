package no.nav.arbeidsgiver.tiltakrefusjon.persondata

import no.nav.team_tiltak.felles.persondata.pdl.domene.Diskresjonskode
import org.springframework.stereotype.Service

@Service
class PersondataService(
    private val persondataGateway: PersondataGateway,
) {
    fun hentDiskresjonskode(fnr: String): Diskresjonskode {
        return persondataGateway.hentDiskresjonskode(fnr).orElse(Diskresjonskode.UGRADERT)
    }

    fun hentDiskresjonskoder(fnrSet: Set<String>): Map<String, Diskresjonskode> {
        return persondataGateway.hentDiskresjonskoderEllerDefault(fnrSet, Diskresjonskode.UGRADERT)
    }

}
