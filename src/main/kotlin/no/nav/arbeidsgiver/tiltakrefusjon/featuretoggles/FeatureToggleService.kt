package no.nav.arbeidsgiver.tiltakrefusjon.featuretoggles

import io.getunleash.Unleash
import io.getunleash.UnleashContext
import io.getunleash.Variant
import io.micrometer.observation.annotation.Observed
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetSaksbehandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Observed
@Service
class FeatureToggleService @Autowired constructor(
    private val unleash: Unleash,
) {
    fun hentFeatureToggles(features: List<String?>, innloggetSaksbehandler: InnloggetSaksbehandler): Map<String?, Boolean> {
        return features.stream().collect(Collectors.toMap(
            { feature: String? -> feature },
            { feature: String? -> isEnabled(feature, innloggetSaksbehandler) }
        ))
    }

    fun hentVarianter(features: List<String?>, innloggetSaksbehandler: InnloggetSaksbehandler): Map<String, Variant> {
        return features.stream().collect(Collectors.toMap(
            { feature: String? -> feature }
        ) { feature: String? ->
            unleash.getVariant(
                feature!!, contextMedInnloggetBruker(innloggetSaksbehandler.identifikator))
        })
    }

    fun isEnabled(feature: String?, innloggetSaksbehandler: InnloggetSaksbehandler): Boolean {
        return unleash.isEnabled(feature!!, contextMedInnloggetBruker(innloggetSaksbehandler.identifikator))
    }

    fun isEnabled(feature: String?, identifikator: String): Boolean {
        return unleash.isEnabled(feature!!, contextMedInnloggetBruker(identifikator))
    }

    private fun contextMedInnloggetBruker(identifikator: String): UnleashContext {
        val builder = UnleashContext.builder()
        builder.userId(identifikator)
        return builder.build()
    }
}
