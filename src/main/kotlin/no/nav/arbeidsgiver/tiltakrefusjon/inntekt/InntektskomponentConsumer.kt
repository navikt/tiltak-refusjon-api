package no.nav.arbeidsgiver.tiltakrefusjon.inntekt

import InntektListe
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.request.Aktør
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.request.InntektRequest
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
import java.util.*


@Service
class InntektskomponentConsumer(
        @Value("\${tiltak-refusjon.inntektskomponenten.uri}") val url: String,
        @Value("\${tiltak-refusjon.inntektskomponenten.filter}") val ainntektsfilter: String,
        @Value("\${tiltak-refusjon.inntektskomponenten.consumer-id}") val consumerId: String,
        @Qualifier("anonymProxyRestTemplate") val restTemplate: RestTemplate
) {

    private val log = LoggerFactory.getLogger(InntektskomponentConsumer::class.java)

    fun hentInntekter(fnr: String, bedriftnummerDetSøkesPå: String, datoFra: LocalDate, datoTil: LocalDate): List<Inntektslinje> {
        try {
            val requestEntity = lagRequest(fnr, YearMonth.from(datoFra), YearMonth.from(datoTil))
            val responseMedInntekterForDeltaker = restTemplate.exchange<InntektResponse>(getUrl(fnr, datoFra, datoTil), HttpMethod.POST, requestEntity).body
            val inntekter = responseMedInntekterForDeltaker?.arbeidsInntektMaaned ?: throw HentingAvInntektException()
            return inntekterForBedrift(inntekter, bedriftnummerDetSøkesPå)
        } catch (ex: Exception) {
            log.warn("Kall til Inntektskomponenten feilet: {}", ex.message)
            throw HentingAvInntektException()
        }
    }

    private fun inntekterForBedrift(månedsInntektList: List<ArbeidsInntektMaaned>?, bedriftnummerDetSøkesPå: String): List<Inntektslinje> {
        val inntekterTotalt = mutableListOf<Inntektslinje>()
        månedsInntektList?.forEach { inntektMaaned ->
            val arbeidsinntektListe: List<InntektListe>? = inntektMaaned.arbeidsInntektInformasjon?.inntektListe
            arbeidsinntektListe?.filter { it.virksomhet?.identifikator.toString() == bedriftnummerDetSøkesPå }?.forEach {
                var dateFraOpptjenningsperiode: LocalDate? = null
                var datoTilOpptjenningsperiode: LocalDate? = null
                if (!it.opptjeningsperiodeFom.isNullOrEmpty()) {
                    dateFraOpptjenningsperiode = LocalDate.parse(it.opptjeningsperiodeFom)
                }
                if (!it.opptjeningsperiodeTom.isNullOrEmpty()) {
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

    private fun lagRequest(fnr: String, månedFom: YearMonth, månedTom: YearMonth): HttpEntity<InntektRequest> {
        val headers = HttpHeaders()
        headers["Nav-Consumer-Id"] = consumerId
        headers["Nav-Call-Id"] = UUID.randomUUID().toString()
        val body = InntektRequest(Aktør(fnr), månedFom, månedTom, ainntektsfilter)
        return HttpEntity(body, headers)
    }

    // TODO: Fjerne denne metoden som legger på query-parametre. Inntektkomponenten bruker ikke dette, men må til p.t. for å ikke gjøre om alle Wiremock-mappingene
    private fun getUrl(fnr: String, datoFra: LocalDate, datoTil: LocalDate): URI {
        return UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("ident", fnr)
                .queryParam("maanedFom", (YearMonth.of(datoFra.year, datoFra.month)))
                .queryParam("maanedTom", (YearMonth.of(datoTil.year, datoTil.month)))
                .queryParam("ainntektsfilter", ainntektsfilter)
                .build()
                .toUri()
    }

}