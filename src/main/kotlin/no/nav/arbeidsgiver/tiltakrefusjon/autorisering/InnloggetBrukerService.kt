package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import io.opentelemetry.instrumentation.annotations.WithSpan
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.AltinnTilgangsstyringService
import no.nav.arbeidsgiver.tiltakrefusjon.featuretoggles.FeatureToggleService
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.InntektskomponentService
import no.nav.arbeidsgiver.tiltakrefusjon.okonomi.KontoregisterService
import no.nav.arbeidsgiver.tiltakrefusjon.organisasjon.EregClient
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.*
import no.nav.security.token.support.core.context.TokenValidationContextHolder
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
    val eregClient: EregClient,
    val featureToggleService: FeatureToggleService
) {
    var logger: Logger = LoggerFactory.getLogger(javaClass)

    fun erArbeidsgiver(): Boolean {
        return context.tokenValidationContext.hasTokenFor("tokenx")
    }

    fun erSaksbehandler(): Boolean {
        return context.tokenValidationContext.hasTokenFor("aad")
    }

    @WithSpan
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

    @WithSpan
    fun hentInnloggetSaksbehandler(): InnloggetSaksbehandler {
        return when {
            erSaksbehandler() -> {
                val (onPremisesSamAccountName, displayName) = graphApiService.hent()
                val harKorreksjonTilgang = featureToggleService.isEnabled("arbeidsgiver.tiltak-refusjon-api.korreksjon", onPremisesSamAccountName)

                InnloggetSaksbehandler(
                    identifikator = onPremisesSamAccountName,
                    navn = displayName,
                    abacTilgangsstyringService = abacTilgangsstyringService,
                    refusjonRepository = refusjonRepository,
                    korreksjonRepository = korreksjonRepository,
                    refusjonService = refusjonService,
                    inntektskomponentService = inntektskomponentService,
                    kontoregisterService = kontoregisterService,
                    harKorreksjonTilgang = harKorreksjonTilgang
                )
            }
            else -> {
                throw RuntimeException("Feil ved token, kunne ikke identifisere saksbehandler")
            }
        }
    }

}