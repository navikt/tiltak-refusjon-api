package no.nav.arbeidsgiver.tiltakrefusjon

data class Organisasjon (
        val Name: String,
    val Type: String,
    val OrganizationNumber: String,
    val OrganizationForm: String,
    val Status: String,
    val ParentOrganizationNumber: String? = null
)
