package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.micrometer.observation.annotation.Observed
import no.nav.arbeidsgiver.tiltakrefusjon.caching.ABAC
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject
import java.util.*

@Service
@Observed
class AbacTilgangsstyringService(
    val abacApi: AbacApi
) {
    @Cacheable(cacheNames = [ABAC])
    fun harLeseTilgang(navIdent: String, deltakerFnr: String): Boolean {
        // En funksjon kan ikke både være cacheable og retryable og oppføre seg som forventet
        // så vi skiller ut selve abac-kallet til en egen klasse
        val abacResponse = abacApi.kall(navIdent, deltakerFnr)
        return abacResponse.response.decision == "Permit"
    }
}

@Component
class AbacApi(
    @Qualifier("anonymProxyRestTemplate") val restTemplate: RestTemplate,
    val abacConfig: AbacConfig
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)
    @Retryable
    fun kall(navIdent: String, deltakerFnr: String): AbacResponse {
        val body = """
            {
              "Request": {
                "AccessSubject": {
                  "Attribute": [
                    {
                      "AttributeId": "urn:oasis:names:tc:xacml:1.0:subject:subject-id",
                      "Value": "$navIdent"
                    },
                    {
                      "AttributeId": "no.nav.abac.attributter.subject.felles.subjectType",
                      "Value": "InternBruker"
                    }
                  ]
                },
                "Environment": {
                  "Attribute": [
                    {
                      "AttributeId": "no.nav.abac.attributter.environment.felles.pep_id",
                      "Value": ""
                    }
                  ]
                },
                "Action": {
                  "Attribute": [
                    {
                      "AttributeId": "urn:oasis:names:tc:xacml:1.0:action:action-id",
                      "Value": "read"
                    }
                  ]
                },
                "Resource": [
                  {
                    "Attribute": [
                      {
                        "AttributeId": "no.nav.abac.attributter.resource.felles.resource_type",
                        "Value": "no.nav.abac.attributter.resource.felles.person"
                      },
                      {
                        "AttributeId": "no.nav.abac.attributter.resource.felles.domene",
                        "Value": "veilarb"
                      },
                      {
                        "AttributeId": "no.nav.abac.attributter.resource.felles.person.fnr",
                        "Value": "$deltakerFnr"
                      }
                    ]
                  }
                ]
              }
            }
        """.trimIndent()
        val headers = HttpHeaders()
        headers["Nav-Consumer-Id"] = "tiltak-refusjon-api"
        headers["Nav-Call-Id"] = UUID.randomUUID().toString()
        headers["Content-Type"] = "application/json"
        val request = HttpEntity(body, headers)
        return restTemplate.postForObject<AbacResponse>(abacConfig.uri, request);
    }

    // Signatur må matche abacKall (også returtype!), pluss et exception-argument først
    @Recover
    protected fun recover(e: Exception, navIdent: String, deltakerFnr: String): AbacResponse {
        log.error("ABAC-kall feilet", e)
        throw e;
    }
}

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
data class AbacResponse(val response: AbacResponseResponse)

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
data class AbacResponseResponse(val decision: String)