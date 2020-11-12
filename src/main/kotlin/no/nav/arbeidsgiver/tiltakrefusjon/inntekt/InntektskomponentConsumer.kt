package no.nav.arbeidsgiver.tiltakrefusjon.inntekt

import InntektListe
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.response.ArbeidsInntektMaaned
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.response.InntektResponse
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Inntektslinje
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

    fun hentInntekter(fnr:String, bedriftnummerDetSøkesPå: String, datoFra:LocalDate, datoTil:LocalDate): List<Inntektslinje> {
        try{
            val responseMedInntekterForDeltaker = restTemplate.exchange<InntektResponse>(getUrl(fnr, datoFra,datoTil), HttpMethod.POST, hentHttpHeaders()).body
            val inntekter = responseMedInntekterForDeltaker?.arbeidsInntektMaaned ?: throw HentingAvInntektException()
            return  inntekterForBedrift(inntekter, bedriftnummerDetSøkesPå)
        }catch (ex: Exception){
             log.warn("Kall til Inntektskomponenten feilet: {}", ex.message)
            throw HentingAvInntektException()
        }
    }

    private fun inntekterForBedrift(månedsInntektList: List<ArbeidsInntektMaaned>?, bedriftnummerDetSøkesPå: String): List<Inntektslinje> {
        val inntekterTotalt = mutableListOf<Inntektslinje>()
        månedsInntektList?.forEach {
            val arbeidsinntektListe: List<InntektListe>? = it.arbeidsInntektInformasjon?.inntektListe
            arbeidsinntektListe?.filter{ it.virksomhet?.identifikator.toString() == bedriftnummerDetSøkesPå }?.forEach {
                        var dateFraOpptjenningsperiode: LocalDate? = null
                        var datoTilOpptjenningsperiode: LocalDate? = null
                        if(!it.opptjeningsperiodeFom.isNullOrEmpty()){
                            dateFraOpptjenningsperiode = LocalDate.parse(it.opptjeningsperiodeFom)
                        }
                        if(!it.opptjeningsperiodeTom.isNullOrEmpty()){
                            datoTilOpptjenningsperiode = LocalDate.parse(it.opptjeningsperiodeTom)
                        }

                        inntekterTotalt.add(
                                Inntektslinje(
                                        it.inntektType,
                                        it.beloep.toDouble(),
                                        YearMonth.parse(it.utbetaltIMaaned),
                                        dateFraOpptjenningsperiode, datoTilOpptjenningsperiode)
                        )
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

    private fun getUrl(fnr:String, datoFra:LocalDate, datoTil:LocalDate): URI {
        return UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("ident", fnr)
                .queryParam("maanedFom", (YearMonth.of(datoFra.year, datoFra.month)))
                .queryParam("maanedTom", (YearMonth.of(datoTil.year, datoTil.month)))
                .queryParam("ainntektsfilter", ainntektsfilter)
                .build()
                .toUri()
    }

}