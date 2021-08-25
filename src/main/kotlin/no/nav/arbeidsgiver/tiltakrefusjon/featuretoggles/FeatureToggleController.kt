package no.nav.arbeidsgiver.tiltakrefusjon.featuretoggles

import no.finn.unleash.Variant
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@Unprotected
@RequestMapping("/api/saksbehandler/feature")
class FeatureToggleController @Autowired constructor(private val featureToggleService: FeatureToggleService) {
    @GetMapping
    fun feature(@RequestParam("feature") features: List<String?>): Map<String?, Boolean?>? {
        return featureToggleService.hentFeatureToggles(features)
    }

    @GetMapping("/variant")
    fun variant(@RequestParam("feature") features: List<String?>): Map<String, Variant> {
        return featureToggleService.hentVarianter(features)
    }
}