package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.common.abac.Pep
import no.nav.common.abac.VeilarbPep
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "tiltak-refusjon.abac-tilgangstyring")
class TilgangskontrollConfig {

    var uri: String = ""
    var username: String = ""
    var password: String = ""

    @Bean
    fun veilarbPep(): Pep {
        return VeilarbPep(uri, username, password)
    }
}