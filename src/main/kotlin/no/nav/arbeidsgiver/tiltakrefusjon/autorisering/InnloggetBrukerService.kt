package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.arbeidsgiver.tiltakrefusjon.altinn.AltinnTilgangsstyringService
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.InntektskomponentService
import no.nav.arbeidsgiver.tiltakrefusjon.okonomi.KontoregisterService
import no.nav.arbeidsgiver.tiltakrefusjon.organisasjon.EregClient
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.*
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.apache.kafka.common.protocol.types.Field.Bool
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class InnloggetBrukerService(
    val context: TokenValidationContextHolder,
    val graphApiService: GraphApiService,
    val altinnTilgangsstyringService: AltinnTilgangsstyringService,
    val abacTilgangsstyringService: AbacTilgangsstyringService,
    val refusjonRepository: RefusjonRepository,
    val korreksjonRepository: KorreksjonRepository,
    val refusjonService: RefusjonService,
    val inntektskomponentService: InntektskomponentService,
    val kontoregisterService: KontoregisterService,
    val eregClient: EregClient
) {
    var logger: Logger = LoggerFactory.getLogger(javaClass)

    fun erArbeidsgiver(): Boolean {
        return context.tokenValidationContext.hasTokenFor("tokenx")
    }

    fun erSaksbehandler(): Boolean {
        return context.tokenValidationContext.hasTokenFor("aad")
    }

    fun erBeslutter(): Boolean {
        val groupClaim  = context.tokenValidationContext.getClaims("aad").get("groups") as List<String>
        return erSaksbehandler() && groupClaim.contains("1a1d2745-952f-4a0f-839f-9530145b1d4a")
    }

    fun harKorreksjonsTilgang(): Boolean {
        if(System.getenv("KORREKSJON_TILGANG") != null) {
            val identerMedTilgang = System.getenv("KORREKSJON_TILGANG") as List<String>
            if(identerMedTilgang.isNotEmpty()) {
                return identerMedTilgang.contains(navIdent())
            }
        }
        return false
    }

    fun navIdent(): String {
        return context.tokenValidationContext.getClaims("aad").getStringClaim("NAVident")
    }

    fun displayName(): String {
        val displayNameClaim = context.tokenValidationContext.getClaims("aad").get("name")
        if (displayNameClaim != null) {
            return displayNameClaim as String
        }
        return navIdent()
    }

    fun hentInnloggetArbeidsgiver(): InnloggetArbeidsgiver {
        return when {
            erArbeidsgiver() -> {
                val fnr = Fnr(context.tokenValidationContext.getClaims("tokenx").getStringClaim("pid"))
                InnloggetArbeidsgiver(fnr.verdi, altinnTilgangsstyringService, refusjonRepository, korreksjonRepository, refusjonService, eregClient)
            }
            else -> {
                throw RuntimeException("Feil ved token, kunne ikke identifisere arbeidsgiver")
            }
        }
    }

    fun hentInnloggetSaksbehandler(): InnloggetSaksbehandler {
        return when {
            erSaksbehandler() -> {
                // Todo når vi releaser beslutterkorrigering så er korreksjonstilgang samme som erBeslutter
                // val harKorreksjonTilgang = erBeslutter()
                InnloggetSaksbehandler(
                    identifikator = navIdent(),
                    navn = displayName(),
                    abacTilgangsstyringService = abacTilgangsstyringService,
                    refusjonRepository = refusjonRepository,
                    korreksjonRepository = korreksjonRepository,
                    refusjonService = refusjonService,
                    inntektskomponentService = inntektskomponentService,
                    kontoregisterService = kontoregisterService,
                    harKorreksjonTilgang = harKorreksjonsTilgang()
                )
            }
            else -> {
                throw RuntimeException("Feil ved token, kunne ikke identifisere saksbehandler")
            }
        }
    }

}
