package no.nav.arbeidsgiver.tiltakrefusjon.inntekt

import InntektListe
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.response.ArbeidsInntektMaaned
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.response.InntektResponse
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Inntektslinje
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonsberegningRequest
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


@Service
class InntektskomponentConsumer(@Value("\${tiltak-refusjon.integrasjon.inntektskomponenten-uri}") val url: String) {
    private val AINNTEKT_FILTER = "KontrollArbeidsmarkedstiltakA-inntekt"
    private val CONSUMER_ID_VAL = "tiltak-refusjon-api"
    private val CALL_ID_VAL = "halo01"

    private val restTemplate: RestTemplate = RestTemplate()

    fun hentInntekter(refusjonsberegningRequest: RefusjonsberegningRequest): List<Inntektslinje> {
        //TODO
        // Feil håndtering
        // Logging
        try{
            val response = restTemplate.exchange<InntektResponse>(getUrl(refusjonsberegningRequest.fnr!!, LocalDate.parse(refusjonsberegningRequest.refusjonFraDato!!), LocalDate.parse(refusjonsberegningRequest.refusjonTilDato!!)), HttpMethod.POST, hentHttpHeaders())
            val arbeidsInntektMaaned = response.body!!.arbeidsInntektMaaned
            return  inntekterForBedrift(arbeidsInntektMaaned, refusjonsberegningRequest.bedriftNr!!) ?: emptyList()
        }catch (ex: Exception){
            throw HentingAvInntektException()
        }
    }

    private fun inntekterForBedrift(månedsInntektList: List<ArbeidsInntektMaaned>?, bedriftnummerDetSøkesOm: String): List<Inntektslinje>? {
        val inntekterTotalt = mutableListOf<Inntektslinje>()

        månedsInntektList?.forEach {
            var arbeidsinntektListe: List<InntektListe>? = it.arbeidsInntektInformasjon?.inntektListe
            arbeidsinntektListe?.filter{
                it.virksomhet?.identifikator?.toString().equals(bedriftnummerDetSøkesOm)
            }?.forEach {
                        //TODO: Den best måten å håndtere null verdier på her?
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
        httpHeaders.set("Nav-Consumer-Id", CONSUMER_ID_VAL)
        httpHeaders["Nav-Call-Id"] = CALL_ID_VAL
        return HttpEntity(httpHeaders)
    }

    private fun getUrl(fnr: String, periodeStart: LocalDate, periodeSlutt: LocalDate): URI {
        return UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("ident", fnr)
                .queryParam("maanedFom", (YearMonth.of(periodeStart.year, periodeStart.month)))
                .queryParam("maanedTom", (YearMonth.of(periodeSlutt.year, periodeSlutt.month)))
                .queryParam("ainntektsfilter", AINNTEKT_FILTER)
                .build()
                .toUri()
    }

}