package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Fnr
import no.nav.common.rest.client.RestClient
import no.nav.poao_tilgang.api.dto.response.TilgangsattributterResponse
import no.nav.poao_tilgang.client.Decision
import no.nav.poao_tilgang.client.Decision.Deny
import no.nav.poao_tilgang.client.NavAnsattTilgangTilEksternBrukerPolicyInput
import no.nav.poao_tilgang.client.PoaoTilgangCachedClient
import no.nav.poao_tilgang.client.PoaoTilgangClient
import no.nav.poao_tilgang.client.PoaoTilgangHttpClient
import no.nav.poao_tilgang.client.PolicyRequest
import no.nav.poao_tilgang.client.TilgangType
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile(value = ["dev-gcp", "prod-gcp"])
class PoaoTilgangServiceImpl(
    @Value("\${tiltak-refusjon.poao-tilgang.url}") poaoTilgangUrl: String,
    clientConfigurationProperties: ClientConfigurationProperties,
    oAuth2AccessTokenService: OAuth2AccessTokenService
) : PoaoTilgangService {
    private var klient: PoaoTilgangClient = PoaoTilgangCachedClient.createDefaultCacheClient(
        PoaoTilgangHttpClient(
            poaoTilgangUrl,
            { oAuth2AccessTokenService.getAccessToken(clientConfigurationProperties.registration["poao-tilgang"]!!).accessToken!! },
            RestClient.baseClient()
        )
    )

    override fun harSkrivetilgang(beslutterAzureUUID: UUID, fnr: Fnr) =
        hentSkrivetilgang(beslutterAzureUUID, fnr.verdi)?.isPermit ?: false

    override fun harSkrivetilgang(beslutterAzureUUID: UUID, fnrSet: Set<Fnr>) =
        hentSkrivetilganger(beslutterAzureUUID, fnrSet)
            .mapValues { it.value.isPermit }

    override fun hentGrunn(beslutterAzureUUID: UUID, fnr: Fnr): String? {
        return hentSkrivetilgang(beslutterAzureUUID, fnr.verdi)?.let { decision: Decision ->
            if (decision.isDeny && decision is Deny) {
                decision.reason
            } else {
                null
            }
        }
    }

    private fun hentSkrivetilganger(beslutterAzureUUID: UUID, fnrSet: Set<Fnr>): Map<Fnr, Decision> {
        val requestIdOgIdent: MutableMap<UUID, Fnr> = HashMap<UUID, Fnr>()

        val policyRequestList = fnrSet.stream()
            .map { fnr: Fnr ->
                val requestId = UUID.randomUUID()
                requestIdOgIdent[requestId] = fnr
                PolicyRequest(
                    requestId,
                    NavAnsattTilgangTilEksternBrukerPolicyInput(
                        beslutterAzureUUID,
                        TilgangType.SKRIVE,
                        fnr.verdi
                    )
                )
            }
            .toList()

        val resultat: MutableMap<Fnr, Decision> = mutableMapOf();
        klient.evaluatePolicies(policyRequestList)
            .get()?.let { policyResultList ->
                policyResultList.forEach {
                    val fnr = requestIdOgIdent[it.requestId]
                    if (fnr != null) {
                        resultat[fnr] = it.decision
                    }
                }
            }
        return resultat
    }

    private fun hentSkrivetilgang(beslutterAzureUUID: UUID, fnr: String): Decision? {
        return klient.evaluatePolicy(
            NavAnsattTilgangTilEksternBrukerPolicyInput(
                beslutterAzureUUID,
                TilgangType.SKRIVE,
                fnr
            )
        ).get()
    }

    private fun hentTilgangsattributter(fnr: String): TilgangsattributterResponse? {
        return klient.hentTilgangsAttributter(fnr).get()
    }
}
