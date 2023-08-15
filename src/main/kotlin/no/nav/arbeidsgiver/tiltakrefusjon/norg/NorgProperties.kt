package no.nav.arbeidsgiver.tiltakrefusjon.norg

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "tiltak-refusjon.norg")
data class NorgProperties(
    var uri: String = ""
)
