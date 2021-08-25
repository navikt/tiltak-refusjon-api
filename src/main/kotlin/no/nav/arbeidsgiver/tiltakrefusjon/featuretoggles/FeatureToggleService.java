package no.nav.arbeidsgiver.tiltakrefusjon.featuretoggles;

import no.finn.unleash.Unleash;
import no.finn.unleash.UnleashContext;
import no.finn.unleash.UnleashContext.Builder;
import no.finn.unleash.Variant;
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBrukerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FeatureToggleService {

    private final Unleash unleash;
    private final InnloggetBrukerService innloggetBrukerService;

    @Autowired
    public FeatureToggleService(Unleash unleash, InnloggetBrukerService innloggetBrukerService) {
        this.unleash = unleash;
        this.innloggetBrukerService = innloggetBrukerService;
    }

    public Map<String, Boolean> hentFeatureToggles(List<String> features) {

        return features.stream().collect(Collectors.toMap(
                feature -> feature,
                feature -> isEnabled(feature)
        ));
    }

    public Map<String, Variant> hentVarianter(List<String> features) {

        return features.stream().collect(Collectors.toMap(
                feature -> feature,
                feature -> unleash.getVariant(feature, contextMedInnloggetBruker())
        ));
    }

    public Boolean isEnabled(String feature) {
        return unleash.isEnabled(feature, contextMedInnloggetBruker());
    }

    private UnleashContext contextMedInnloggetBruker() {
        Builder builder = UnleashContext.builder();
        builder.userId(innloggetBrukerService.hentInnloggetSaksbehandler().getIdentifikator());
        return builder.build();
    }

}
