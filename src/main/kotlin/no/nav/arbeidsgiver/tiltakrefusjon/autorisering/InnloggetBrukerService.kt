package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.arbeidsgiver.tiltakrefusjon.altinn.AltinnTilgangsstyringService
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.InntektskomponentService
import no.nav.arbeidsgiver.tiltakrefusjon.okonomi.KontoregisterService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Fnr
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.KorreksjonRepository
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonService
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
    val kontoregisterService: KontoregisterService
) {
    var logger: Logger = LoggerFactory.getLogger(javaClass)

    fun erArbeidsgiver(): Boolean {
        return context.tokenValidationContext.hasTokenFor("tokenx")
    }

    fun erSaksbehandler(): Boolean {
        return context.tokenValidationContext.hasTokenFor("aad")
    }

    fun hentInnloggetArbeidsgiver(): InnloggetArbeidsgiver {
        return when {
            erArbeidsgiver() -> {
                val fnr = Fnr(context.tokenValidationContext.getClaims("tokenx").getStringClaim("pid"))
                InnloggetArbeidsgiver(fnr.verdi, altinnTilgangsstyringService, refusjonRepository, refusjonService)
            }
            else -> {
                throw RuntimeException("Feil ved token, kunne ikke identifisere arbeidsgiver")
            }
        }
    }

    fun hentInnloggetSaksbehandler(): InnloggetSaksbehandler {
        return when {
            erSaksbehandler() -> {
                val (onPremisesSamAccountName, displayName) = graphApiService.hent()
                InnloggetSaksbehandler(
                    onPremisesSamAccountName,
                    displayName,
                    abacTilgangsstyringService,
                    refusjonRepository,
                    korreksjonRepository,
                    refusjonService,
                    inntektskomponentService,
                    kontoregisterService
                )
            }
            else -> {
                throw RuntimeException("Feil ved token, kunne ikke identifisere saksbehandler")
            }
        }
    }
}