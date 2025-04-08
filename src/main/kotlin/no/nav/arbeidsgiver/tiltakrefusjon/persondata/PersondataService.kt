package no.nav.arbeidsgiver.tiltakrefusjon.persondata

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Fnr
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.team_tiltak.felles.persondata.PersondataClient
import no.nav.team_tiltak.felles.persondata.pdl.domene.Diskresjonskode
import org.springframework.stereotype.Service

@Service
class PersondataService(
    clientConfigurationProperties: ClientConfigurationProperties,
    persondataProperties: PersondataProperties,
    val oAuth2AccessTokenService: OAuth2AccessTokenService,
) {
    private val clientProperties = clientConfigurationProperties.registration["pdl-api"]

    private val persondataClient =
        PersondataClient(persondataProperties.uri) { clientProperties?.let { oAuth2AccessTokenService.getAccessToken(it).access_token } }

    fun hentDiskresjonskode(fnr: Fnr): Diskresjonskode {
        return persondataClient.hentDiskresjonskode(fnr.verdi).orElse(Diskresjonskode.UGRADERT)
    }

    fun hentDiskresjonskoder(fnrSet: Set<Fnr>): Map<Fnr, Diskresjonskode> {
        return persondataClient.hentDiskresjonskoderEllerDefault(
            fnrSet.map { it.verdi }.toSet(),
            { fnr: String -> Fnr(fnr) },
            Diskresjonskode.UGRADERT
        )
    }

}
