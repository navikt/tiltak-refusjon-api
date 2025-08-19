package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.poao_tilgang.client.Decision
import no.nav.poao_tilgang.client.Decision.Deny

enum class Avslagskode {
    STRENGT_FORTROLIG_ADRESSE,
    FORTROLIG_ADRESSE,
    EGNE_ANSATTE,
    MANGLER_TILGANG_TIL_AD_GRUPPE,
    POLICY_IKKE_IMPLEMENTERT,
    IKKE_TILGANG_FRA_ABAC,
    IKKE_TILGANG_TIL_NAV_ENHET,
    UKLAR_TILGANG_MANGLENDE_INFORMASJON,
    EKSTERN_BRUKER_HAR_IKKE_TILGANG,
    SLUTTDATO_PASSERT,
    UTGATT,
    IKKE_TILGANG_PAA_TILTAK,
    UKJENT,
    INGEN_RESPONS,
    IKKE_TILGANG_TIL_DELTAKER;

    companion object {
        fun parse(decision: Decision?): Avslagskode {
            require(decision?.isPermit ?: false) { "Kan ikke hente tilgangskode for en tillatelse" }

            val deny = if (decision.isDeny) decision as Deny else {
                return UKJENT
            }

            return when (deny.reason) {
                "MANGLER_TILGANG_TIL_AD_GRUPPE" -> {
                    when {
                        deny.message.contains("0000-GA-Strengt_Fortrolig_Adresse") -> STRENGT_FORTROLIG_ADRESSE
                        deny.message.contains("0000-GA-Fortrolig_Adresse") -> FORTROLIG_ADRESSE
                        deny.message.contains("0000-GA-Egne_ansatte") -> EGNE_ANSATTE
                        else -> IKKE_TILGANG_TIL_NAV_ENHET
                    }
                }
                "POLICY_IKKE_IMPLEMENTERT" -> POLICY_IKKE_IMPLEMENTERT
                "IKKE_TILGANG_FRA_ABAC" -> IKKE_TILGANG_FRA_ABAC
                "IKKE_TILGANG_TIL_NAV_ENHET" -> IKKE_TILGANG_TIL_NAV_ENHET
                "IKKE_TILGANG_TIL_DELTAKER" -> IKKE_TILGANG_TIL_DELTAKER
                "UKLAR_TILGANG_MANGLENDE_INFORMASJON" -> UKLAR_TILGANG_MANGLENDE_INFORMASJON
                "EKSTERN_BRUKER_HAR_IKKE_TILGANG" -> EKSTERN_BRUKER_HAR_IKKE_TILGANG
                else -> UKJENT
            }
        }
    }
}
