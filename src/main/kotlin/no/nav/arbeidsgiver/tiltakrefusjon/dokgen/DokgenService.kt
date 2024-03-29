package no.nav.arbeidsgiver.tiltakrefusjon.dokgen

import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.core.instrument.MeterRegistry
import no.nav.arbeidsgiver.tiltakrefusjon.pdf.RefusjonTilPDF
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal
import java.util.*

@Service
class DokgenService(
    private val dokgenProperties: DokgenProperties,
    private val objectMapper: ObjectMapper,
    private val meterRegistry: MeterRegistry
) {

    fun refusjonPdf(refusjon: Refusjon): ByteArray {
        val refusjonTilPDF : RefusjonTilPDF =
            RefusjonTilPDFMapper.tilPDFdata(refusjon)
            gangOppSatserMed100(refusjonTilPDF)

        return try {
            val bytes: ByteArray = restOperations().postForObject(
                dokgenProperties.uri!!, refusjonTilPDF,
                ByteArray::class.java
            )!!
            meterRegistry.counter("refusjon.pdf.ok").increment()
            bytes
        } catch (e: RestClientException) {
            meterRegistry.counter("refusjon.pdf.feil").increment()
            throw e
        }
    }

    fun gangOppSatserMed100(refusjonTilPDF: RefusjonTilPDF)  {
        if (refusjonTilPDF.arbeidsgiveravgiftSats != null) {
           refusjonTilPDF.arbeidsgiveravgiftSats = String.format(Locale.ENGLISH,"%.1f", refusjonTilPDF.arbeidsgiveravgiftSats * 100).toDouble()
        }
        if (refusjonTilPDF.feriepengerSats != null) {
            refusjonTilPDF.feriepengerSats = String.format(Locale.ENGLISH,"%.1f", refusjonTilPDF.feriepengerSats * 100).toDouble()
        }
        if (refusjonTilPDF.otpSats != null) {
            refusjonTilPDF.otpSats = String.format(Locale.ENGLISH,"%.1f", refusjonTilPDF.otpSats * 100).toDouble()
        }
    }


    // Lager ny instans av RestOperations i stedet for å wire inn RestTemplate fordi det var vanskelig å få den til å bruke en ObjectMapper som hadde datoer på format 'yyyy-MM-dd' i stedet for et array
    private fun restOperations(): RestOperations {
        val rest = RestTemplate()
        //this is crucial!
        rest.messageConverters.add(0, mappingJacksonHttpMessageConverter())
        return rest
    }

    private fun mappingJacksonHttpMessageConverter(): MappingJackson2HttpMessageConverter {
        val converter = MappingJackson2HttpMessageConverter()
        converter.objectMapper = objectMapper
        return converter
    }
}

