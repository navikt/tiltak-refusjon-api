package no.nav.arbeidsgiver.tiltakrefusjon.altinn

import com.fasterxml.jackson.databind.PropertyNamingStrategy.UpperCamelCaseStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(UpperCamelCaseStrategy::class)
data class Organisasjon (
        val Name: String,
        val Type: String,
        val OrganizationNumber: String,
        val OrganizationForm: String,
        val Status: String,
        val ParentOrganizationNumber: String? = null
)
