package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject
import java.util.*

@Service
class AbacTilgangsstyringService(
    @Qualifier("anonymProxyRestTemplate") val restTemplate: RestTemplate,
    val abacConfig: AbacConfig,
) {

    fun harLeseTilgang(navIdent: String, deltakerFnr: String): Boolean {
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
        val abacResponse = restTemplate.postForObject<AbacResponse>(abacConfig.uri, request);
        return abacResponse.response.decision == "Permit"
    }
}

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
data class AbacResponse(val response: AbacResponseResponse)

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
data class AbacResponseResponse(val decision: String)