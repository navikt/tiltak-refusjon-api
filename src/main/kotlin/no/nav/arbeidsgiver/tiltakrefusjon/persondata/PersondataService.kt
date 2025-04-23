package no.nav.arbeidsgiver.tiltakrefusjon.persondata

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

    fun hentDiskresjonskode(fnr: String): Diskresjonskode {
        return persondataClient.hentDiskresjonskode(fnr).orElse(Diskresjonskode.UGRADERT)
    }

    fun hentDiskresjonskoder(fnrSet: Set<String>): Map<String, Diskresjonskode> {
        return persondataClient.hentDiskresjonskoderEllerDefault(
            fnrSet.map { it }.toSet(),
            Diskresjonskode.UGRADERT
        )
    }

}
