package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import io.micrometer.observation.annotation.Observed
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.AltinnTilgangsstyringService
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.InntektskomponentService
import no.nav.arbeidsgiver.tiltakrefusjon.norg.NorgService
import no.nav.arbeidsgiver.tiltakrefusjon.okonomi.KontoregisterService
import no.nav.arbeidsgiver.tiltakrefusjon.organisasjon.EregClient
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.*
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Issuer
import no.nav.arbeidsgiver.tiltakrefusjon.utils.getClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Observed
@Component
class InnloggetBrukerService(
    val context: TokenValidationContextHolder,
    val altinnTilgangsstyringService: AltinnTilgangsstyringService,
    val abacTilgangsstyringService: AbacTilgangsstyringService,
    val refusjonRepository: RefusjonRepository,
    val korreksjonRepository: KorreksjonRepository,
    val refusjonService: RefusjonService,
    val inntektskomponentService: InntektskomponentService,
    val kontoregisterService: KontoregisterService,
    val norgService: NorgService,
    val eregClient: EregClient,
    val beslutterRolleConfig: BeslutterRolleConfig
) {
    var logger: Logger = LoggerFactory.getLogger(javaClass)

    fun erArbeidsgiver(): Boolean {
        return context.getTokenValidationContext().hasTokenFor("tokenx")
    }

    fun erSaksbehandler(): Boolean {
        return context.getTokenValidationContext().hasTokenFor("aad")
    }

    fun erBeslutter(): Boolean {
        val groupClaim = context.getClaims(Issuer.AZURE)?.get("groups") as List<String>
        return erSaksbehandler() && groupClaim.contains(beslutterRolleConfig.id)
    }

    fun harKorreksjonsTilgang(): Boolean {
        return erBeslutter()
    }

    fun navIdent(): String {
        return context.getClaims(Issuer.AZURE)?.getStringClaim("NAVident") ?: throw IllegalArgumentException("Forsøker å hente navident for bruker som ikke er NAV-ansatt")
    }

    fun displayName(): String {
        val displayNameClaim = context.getClaims(Issuer.AZURE)?.get("name")
        if (displayNameClaim != null) {
            return displayNameClaim as String
        }
        return navIdent()
    }

    fun hentInnloggetArbeidsgiver(): InnloggetArbeidsgiver {
        return when {
            erArbeidsgiver() -> {
                val fnr = Fnr(context.getClaims(Issuer.TOKEN_X)?.getStringClaim("pid") ?: throw IllegalArgumentException("Forsøker å hente pid for bruker som ikke er arbeidsgiver"))
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
                InnloggetSaksbehandler(
                    identifikator = navIdent(),
                    navn = displayName(),
                    abacTilgangsstyringService = abacTilgangsstyringService,
                    refusjonRepository = refusjonRepository,
                    korreksjonRepository = korreksjonRepository,
                    refusjonService = refusjonService,
                    inntektskomponentService = inntektskomponentService,
                    kontoregisterService = kontoregisterService,
                    harKorreksjonTilgang = harKorreksjonsTilgang(),
                    norgeService = norgService
                )
            }

            else -> {
                throw RuntimeException("Feil ved token, kunne ikke identifisere saksbehandler")
            }
        }
    }

}
