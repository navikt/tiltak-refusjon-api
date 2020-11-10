package no.nav.arbeidsgiver.tiltakrefusjon.inntekt

import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.response.ArbeidsInntektMaaned
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.response.InntektResponse
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Inntektslinje
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
class InntektskomponentConsumer {
    private val AINNTEKT_FILTER = "KontrollArbeidsmarkedstiltakA-inntekt"
    private val CONSUMER_ID_VAL = "tiltak-refusjon-api"
    private val CALL_ID_VAL = "halo01"

    private val restTemplate: RestTemplate = RestTemplate()

    private val url:String = "http://localhost:8090/inntekskomponenten/hentinntektliste"

    fun hentInntekter(fnr: String, bedriftnummer:String,periodeStart: LocalDate, periodeSlutt: LocalDate): List<Inntektslinje> {
        val response = restTemplate.exchange<InntektResponse>(getUrl(fnr, periodeStart, periodeSlutt), HttpMethod.POST, hentHttpHeaders())
        val arbeidsInntektMaaned = response.body!!.arbeidsInntektMaaned?.first()
        return inntekterForBedrift(arbeidsInntektMaaned, bedriftnummer) ?: emptyList()
    }

    private fun inntekterForBedrift(månedsInntektList: ArbeidsInntektMaaned?,bedriftnummer:String): List<Inntektslinje>? {
        val listeMedInntekter =  månedsInntektList!!
                .arbeidsInntektInformasjon?.inntektListe?.filter{ it.virksomhet?.identifikator?.toString().equals(bedriftnummer) }
                ?.map {
                   Inntektslinje(it.inntektType!!,
                            it.beloep.toDouble(),
                            YearMonth.parse(it.utbetaltIMaaned),
                            LocalDate.parse(it.opptjeningsperiodeFom),
                            LocalDate.parse(it.opptjeningsperiodeTom))
                }

        return listeMedInntekter
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