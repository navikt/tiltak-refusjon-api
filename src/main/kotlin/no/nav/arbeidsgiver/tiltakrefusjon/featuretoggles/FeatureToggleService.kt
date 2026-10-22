package no.nav.arbeidsgiver.tiltakrefusjon.featuretoggles

import io.getunleash.Unleash
import io.getunleash.UnleashContext
import io.getunleash.variant.Variant
import io.micrometer.observation.annotation.Observed
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBruker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Observed
@Service
class FeatureToggleService @Autowired constructor(
    private val unleash: Unleash,
) {
    fun hentFeatureToggles(features: List<String?>, innloggetBruker: InnloggetBruker): Map<String?, Boolean> {
        return features.stream().collect(Collectors.toMap(
            { feature: String? -> feature },
            { feature: String? -> isEnabled(feature, innloggetBruker) }
        ))
    }

    fun hentVarianter(features: List<String?>, innloggetBruker: InnloggetBruker): Map<String, Variant> {
        return features.stream().collect(Collectors.toMap(
            { feature: String? -> feature }
        ) { feature: String? ->
            unleash.getVariant(
                feature!!, contextMedInnloggetBruker(innloggetBruker.identifikator))
        })
    }

    fun isEnabled(feature: String?, innloggetBruker: InnloggetBruker): Boolean {
        return unleash.isEnabled(feature!!, contextMedInnloggetBruker(innloggetBruker.identifikator))
    }

    private fun contextMedInnloggetBruker(identifikator: String): UnleashContext {
        val builder = UnleashContext.builder()
        builder.userId(identifikator)
        return builder.build()
    }
}
