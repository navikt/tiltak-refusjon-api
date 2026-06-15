package no.nav.arbeidsgiver.tiltakrefusjon.altinn

import org.springframework.stereotype.Service

@Service
class AltinnTilgangsstyringService(
    val altinnTilgangsstyringProperties: AltinnTilgangsstyringProperties,
    val altinnTilgangsstyringKlient: AltinnTilgangsstyringKlient
) {
    private val ALTINN_2_ADRESSESPERRE =
        "${altinnTilgangsstyringProperties.adressesperreServiceCode}:${altinnTilgangsstyringProperties.adressesperreServiceEdition}"
    private val ALTINN_3_ADRESSESPERRE = "nav_tiltak_adressesperre"

    fun hentInntektsmeldingEllerRefusjonTilganger(fnr: String): Set<Organisasjon> {
        return altinnTilgangsstyringKlient.heltAltinnTilganger(fnr).tilGammeltFormat()
    }

    fun hentAdressesperreTilganger(fnr: String): Set<Organisasjon> {
        return altinnTilgangsstyringKlient.heltAltinnTilganger(fnr)
            .filter {
                it.altinn3Tilganger.contains(ALTINN_3_ADRESSESPERRE) || it.altinn2Tilganger.contains(
                    ALTINN_2_ADRESSESPERRE
                )
            }
            .tilGammeltFormat()
    }
}

private fun List<AltinnTilgang>.tilGammeltFormat(): Set<Organisasjon> =
    this.flatMap { org ->
        listOf(
            Organisasjon(
                organizationNumber = org.orgnr,
                name = org.navn,
                organizationForm = org.organisasjonsform,
                type = "Enterprise",
                status = if (org.erSlettet) "Inactive" else "Active",
                parentOrganizationNumber = null
            )
        ) + org.underenheter.map { underenhet ->
            Organisasjon(
                organizationNumber = underenhet.orgnr,
                name = underenhet.navn,
                organizationForm = underenhet.organisasjonsform,
                type = "Business",
                status = if (underenhet.erSlettet) "Inactive" else "Active",
                parentOrganizationNumber = org.orgnr
            )
        }
    }.toSet()
