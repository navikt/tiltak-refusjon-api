package no.nav.arbeidsgiver.tiltakrefusjon.inntekt

import InntektListe
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.response.ArbeidsInntektMaaned
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.response.InntektResponse
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Inntektslinje
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonsberegningRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID


@Service
class InntektskomponentConsumer(@Value("\${tiltak-refusjon.inntektskomponenten.uri}") val url: String,
                                @Value("\${tiltak-refusjon.inntektskomponenten.filter}") val ainntektsfilter: String,
                                @Value("\${tiltak-refusjon.inntektskomponenten.consumer-id}") val consumerId: String,
                                @Qualifier("anonymProxyRestTemplate") val restTemplate: RestTemplate) {

    private val log = LoggerFactory.getLogger(InntektskomponentConsumer::class.java)

    fun hentInntekter(refusjonsberegningRequest: RefusjonsberegningRequest): List<Inntektslinje> {
        try{
            val response = restTemplate.exchange<InntektResponse>(getUrl(refusjonsberegningRequest), HttpMethod.POST, hentHttpHeaders())
            val inntekter = response.body!!.arbeidsInntektMaaned
            return  inntekterForBedrift(inntekter, refusjonsberegningRequest.bedriftNr)
        }catch (ex: Exception){
             log.warn("Kall til Inntektskomponenten feilet: {}", ex.message)
            throw HentingAvInntektException()
        }
    }

    private fun inntekterForBedrift(månedsInntektList: List<ArbeidsInntektMaaned>?, bedriftnummerDetSøkesOm: String): List<Inntektslinje> {
        val inntekterTotalt = mutableListOf<Inntektslinje>()
        månedsInntektList?.forEach {
            val arbeidsinntektListe: List<InntektListe>? = it.arbeidsInntektInformasjon?.inntektListe
            arbeidsinntektListe?.filter{it.virksomhet?.identifikator?.toString().equals(bedriftnummerDetSøkesOm)}?.forEach {
                        var fom: LocalDate? = null
                        var tom: LocalDate? = null
                        if(!it.opptjeningsperiodeFom.isNullOrEmpty()){
                            fom = LocalDate.parse(it.opptjeningsperiodeFom)
                        }
                        if(!it.opptjeningsperiodeTom.isNullOrEmpty()){
                            tom = LocalDate.parse(it.opptjeningsperiodeTom)
                        }

                        val inntekt = Inntektslinje(it.inntektType!!,
                                it.beloep.toDouble(),
                                YearMonth.parse(it.utbetaltIMaaned),
                                fom, tom
                        )
                        inntekterTotalt.add(inntekt)
                    }
        }
        return inntekterTotalt
    }

    private fun hentHttpHeaders(): HttpEntity<HttpHeaders> {
        val httpHeaders = HttpHeaders()
        httpHeaders.set("Nav-Consumer-Id", consumerId)
        httpHeaders["Nav-Call-Id"] = UUID.randomUUID().toString()
        return HttpEntity(httpHeaders)
    }

    private fun getUrl(refusjonsberegningRequest: RefusjonsberegningRequest): URI {
        val periodeStart = LocalDate.parse(refusjonsberegningRequest.refusjonFraDato)
        val periodeSlutt =  LocalDate.parse(refusjonsberegningRequest.refusjonTilDato)
        return UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("ident", refusjonsberegningRequest.fnr)
                .queryParam("maanedFom", (YearMonth.of(periodeStart.year, periodeStart.month)))
                .queryParam("maanedTom", (YearMonth.of(periodeSlutt.year, periodeSlutt.month)))
                .queryParam("ainntektsfilter", ainntektsfilter)
                .build()
                .toUri()
    }

}