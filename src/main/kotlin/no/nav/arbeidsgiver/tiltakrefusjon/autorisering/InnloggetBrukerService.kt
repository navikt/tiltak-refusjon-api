package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import io.micrometer.observation.annotation.Observed
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.AltinnTilgangsstyringService
import no.nav.arbeidsgiver.tiltakrefusjon.featuretoggles.FeatureToggleService
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.InntektskomponentService
import no.nav.arbeidsgiver.tiltakrefusjon.norg.NorgService
import no.nav.arbeidsgiver.tiltakrefusjon.okonomi.KontoregisterService
import no.nav.arbeidsgiver.tiltakrefusjon.persondata.PersondataService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Fnr
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.KorreksjonRepository
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonService
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Issuer
import no.nav.arbeidsgiver.tiltakrefusjon.utils.getClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*

@Observed
@Component
class InnloggetBrukerService(
    val context: TokenValidationContextHolder,
    val altinnTilgangsstyringService: AltinnTilgangsstyringService,
    val tilgangskontrollService: TilgangskontrollService,
    val refusjonRepository: RefusjonRepository,
    val korreksjonRepository: KorreksjonRepository,
    val refusjonService: RefusjonService,
    val inntektskomponentService: InntektskomponentService,
    val kontoregisterService: KontoregisterService,
    val norgService: NorgService,
    val adGrupperConfig: AdGrupperConfig,
    val persondataService: PersondataService,
    val featureToggleService: FeatureToggleService,
) {
    var logger: Logger = LoggerFactory.getLogger(javaClass)

    fun erArbeidsgiver(): Boolean {
        return context.getTokenValidationContext().hasTokenFor("tokenx")
    }

    fun erSaksbehandler(): Boolean {
        return context.getTokenValidationContext().hasTokenFor("aad")
    }

    fun navIdent(): String {
        return context.getClaims(Issuer.AZURE)?.getStringClaim("NAVident")
            ?: throw IllegalArgumentException("Forsøker å hente navident for bruker som ikke er NAV-ansatt")
    }

    fun azureOid(): UUID {
        return context.getClaims(Issuer.AZURE)?.getStringClaim("oid")?.let { UUID.fromString(it) }
            ?: throw IllegalArgumentException("Forsøker å hente azure oid for bruker som ikke er NAV-ansatt")
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
                val fnr = Fnr(
                    context.getClaims(Issuer.TOKEN_X)?.getStringClaim("pid")
                        ?: throw IllegalArgumentException("Forsøker å hente pid for bruker som ikke er arbeidsgiver")
                )
                InnloggetArbeidsgiver(
                    fnr.verdi,
                    altinnTilgangsstyringService,
                    refusjonRepository,
                    korreksjonRepository,
                    refusjonService,
                    persondataService,
                    featureToggleService
                )
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
                    azureOid = azureOid(),
                    tilgangskontrollService = tilgangskontrollService,
                    refusjonRepository = refusjonRepository,
                    korreksjonRepository = korreksjonRepository,
                    refusjonService = refusjonService,
                    inntektskomponentService = inntektskomponentService,
                    kontoregisterService = kontoregisterService,
                    adGruppeTilganger = AdGruppeTilganger.av(adGrupperConfig, context),
                    norgeService = norgService,
                    persondataService = persondataService,
                    featureToggleService = featureToggleService
                )
            }

            else -> {
                throw RuntimeException("Feil ved token, kunne ikke identifisere saksbehandler")
            }
        }
    }

}
