package no.nav.arbeidsgiver.tiltakrefusjon.featuretoggles

import no.finn.unleash.Unleash
import no.finn.unleash.UnleashContext
import no.finn.unleash.Variant
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBrukerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class FeatureToggleService @Autowired constructor(
    private val unleash: Unleash,
    private val innloggetBrukerService: InnloggetBrukerService,
) {
    fun hentFeatureToggles(features: List<String?>): Map<String?, Boolean> {
        return features.stream().collect(Collectors.toMap(
            { feature: String? -> feature },
            { feature: String? -> isEnabled(feature) }
        ))
    }

    fun hentVarianter(features: List<String?>): Map<String, Variant> {
        return features.stream().collect(Collectors.toMap(
            { feature: String? -> feature }
        ) { feature: String? ->
            unleash.getVariant(
                feature!!, contextMedInnloggetBruker())
        })
    }

    fun isEnabled(feature: String?): Boolean {
        return unleash.isEnabled(feature!!, contextMedInnloggetBruker())
    }

    private fun contextMedInnloggetBruker(): UnleashContext {
        val builder = UnleashContext.builder()
        builder.userId(innloggetBrukerService.hentInnloggetSaksbehandler().identifikator)
        return builder.build()
    }
}