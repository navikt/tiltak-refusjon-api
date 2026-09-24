package no.nav.arbeidsgiver.tiltakrefusjon.persondata

import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.team_tiltak.felles.persondata.PersondataClient
import no.nav.team_tiltak.felles.persondata.pdl.domene.Diskresjonskode
import org.springframework.stereotype.Component
import java.util.Optional

interface PersondataGateway {
    fun hentDiskresjonskode(fnr: String): Optional<Diskresjonskode>
    fun hentDiskresjonskoderEllerDefault(fnrSet: Set<String>, default: Diskresjonskode): Map<String, Diskresjonskode>
}

@Component
class PersondataGatewayImpl(
    clientConfigurationProperties: ClientConfigurationProperties,
    persondataProperties: PersondataProperties,
    oAuth2AccessTokenService: OAuth2AccessTokenService,
) : PersondataGateway {
    private val clientProperties = clientConfigurationProperties.registration["pdl-api"]
    private val persondataClient =
        PersondataClient(persondataProperties.uri) { clientProperties?.let { oAuth2AccessTokenService.getAccessToken(it).access_token } }

    override fun hentDiskresjonskode(fnr: String): Optional<Diskresjonskode> =
        persondataClient.hentDiskresjonskode(fnr)

    override fun hentDiskresjonskoderEllerDefault(fnrSet: Set<String>, default: Diskresjonskode): Map<String, Diskresjonskode> =
        persondataClient.hentDiskresjonskoderEllerDefault(fnrSet, default)
}
