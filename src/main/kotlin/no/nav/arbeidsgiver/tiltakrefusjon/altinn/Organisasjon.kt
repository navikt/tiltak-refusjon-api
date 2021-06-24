package no.nav.arbeidsgiver.tiltakrefusjon.altinn

import com.fasterxml.jackson.databind.PropertyNamingStrategies.UpperCamelCaseStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(UpperCamelCaseStrategy::class)
data class Organisasjon(
        val name: String,
        val type: String,
        val organizationNumber: String,
        val organizationForm: String,
        val status: String,
        val parentOrganizationNumber: String? = null
)
