package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "tiltak-refusjon.ad-grupper")
class AdGrupperConfig {
    lateinit var beslutter: String
    lateinit var fortroligAdresse: String
    lateinit var strengtFortroligAdresse: String
}
