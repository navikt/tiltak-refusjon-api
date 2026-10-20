package no.nav.arbeidsgiver.tiltakrefusjon.featuretoggles


import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBrukerService
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@Unprotected
@RequestMapping("/api")
class FeatureToggleController @Autowired constructor(
    private val featureToggleService: FeatureToggleService,
    private val innloggetBrukerService: InnloggetBrukerService) {
    @GetMapping("/saksbehandler/feature")
    fun saksbehandlerFeature(@RequestParam("feature") features: List<String?>): Map<String?, Boolean?>? {
        return featureToggleService.hentFeatureToggles(features, innloggetBrukerService.hentInnloggetSaksbehandler())
    }

    @GetMapping("/arbeidsgiver/feature")
    fun arbeidsgiverFeature(@RequestParam("feature") features: List<String?>): Map<String?, Boolean?>? {
        return featureToggleService.hentFeatureToggles(features, innloggetBrukerService.hentInnloggetArbeidsgiver())
    }
}
