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
            if (decision?.isPermit == true) {
                throw IllegalArgumentException("Kan ikke hente tilgangskode for en tillatelse")
            }

            val deny = if (decision?.isDeny == true) decision as Deny else {
                return UKJENT
            }

            return when (deny.reason) {
                "MANGLER_TILGANG_TIL_AD_GRUPPE", "IKKE_TILGANG_TIL_NAV_ENHET" -> IKKE_TILGANG_TIL_NAV_ENHET
                "POLICY_IKKE_IMPLEMENTERT" -> POLICY_IKKE_IMPLEMENTERT
                "IKKE_TILGANG_FRA_ABAC" -> IKKE_TILGANG_FRA_ABAC
                "IKKE_TILGANG_TIL_DELTAKER" -> IKKE_TILGANG_TIL_DELTAKER
                "UKLAR_TILGANG_MANGLENDE_INFORMASJON" -> UKLAR_TILGANG_MANGLENDE_INFORMASJON
                "EKSTERN_BRUKER_HAR_IKKE_TILGANG" -> EKSTERN_BRUKER_HAR_IKKE_TILGANG
                "IKKE_TILGANG_TIL_FORTROLIG_BRUKER" -> FORTROLIG_ADRESSE
                "IKKE_TILGANG_TIL_STRENGT_FORTROLIG_BRUKER", "IKKE_TILGANG_TIL_STRENGT_FORTROLIG_UTLAND_BRUKER" -> STRENGT_FORTROLIG_ADRESSE
                "IKKE_TILGANG_TIL_SKJERMET_PERSON" -> EGNE_ANSATTE
                else -> UKJENT
            }
        }
    }
}
