package no.nav.arbeidsgiver.tiltakrefusjon.dokgen

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.net.URI

@Component
@ConfigurationProperties(prefix = "tiltak-refusjon.pdfgen")
data class PdfgenProperties (var uri: URI? = null)
