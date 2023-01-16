package no.nav.arbeidsgiver.tiltakrefusjon.dokgen

import lombok.Data
import org.springframework.stereotype.Component
import java.net.URI

@Data
@Component
//@ConfigurationProperties(prefix = "tiltak-refusjon.dokgen")
class DokgenProperties {
    val uri: URI? = null
}
