package no.nav.arbeidsgiver.tiltakrefusjon.dokgen

import io.micrometer.core.instrument.MeterRegistry
import no.nav.arbeidsgiver.tiltakrefusjon.pdf.RefusjonTilPDF
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

@Service
class PdfgenService(
    private val pdfgenProperties: PdfgenProperties,
    private val meterRegistry: MeterRegistry,
    private val noAuthRestTemplate: RestTemplate
) {
    fun refusjonPdf(refusjon: Refusjon): ByteArray {
        val refusjonTilPDF : RefusjonTilPDF = RefusjonTilPDFMapper.tilPDFdata(refusjon)

        return try {
            val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
            val request = HttpEntity(refusjonTilPDF, headers)
            val uri = requireNotNull(pdfgenProperties.uri) { "URL til pdfgen mengler" }
            val bytes: ByteArray = noAuthRestTemplate.postForObject("${uri}/tiltak-refusjon/tiltak-refusjon", request, ByteArray::class.java)
                ?: throw IllegalStateException("pdfgen returnerte null")
            meterRegistry.counter("refusjon.pdf.ok").increment()
            bytes
        } catch (e: RestClientException) {
            meterRegistry.counter("refusjon.pdf.feil").increment()
            throw e
        }
    }
}
