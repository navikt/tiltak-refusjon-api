package no.nav.arbeidsgiver.tiltakrefusjon.inntekt

import InntektListe
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.request.Aktør
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.request.InntektRequest
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.response.ArbeidsInntektMaaned
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.response.InntektResponse
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Inntektslinje
import no.nav.arbeidsgiver.tiltakrefusjon.utils.ConditionalOnPropertyNotEmpty
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import java.time.LocalDate
import java.time.YearMonth
import java.util.*


@Service
@ConditionalOnPropertyNotEmpty("tiltak-refusjon.inntektskomponenten.uri")
class InntektskomponentServiceImpl(
        val inntektskomponentProperties: InntektskomponentProperties,
        @Qualifier("anonymProxyRestTemplate") val restTemplate: RestTemplate
) : InntektskomponentService {

    private val log = LoggerFactory.getLogger(InntektskomponentServiceImpl::class.java)

    override fun hentInntekter(fnr: String, bedriftnummerDetSøkesPå: String, datoFra: LocalDate, datoTil: LocalDate): List<Inntektslinje> {
        try {
            val requestEntity = lagRequest(fnr, YearMonth.from(datoFra), YearMonth.from(datoTil))
            val responseMedInntekterForDeltaker = restTemplate.exchange<InntektResponse>(inntektskomponentProperties.uri, HttpMethod.POST, requestEntity).body
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
                                dateFraOpptjenningsperiode,
                                datoTilOpptjenningsperiode)
                )
            }
        }
        return inntekterTotalt
    }

    private fun lagRequest(fnr: String, månedFom: YearMonth, månedTom: YearMonth): HttpEntity<InntektRequest> {
        val headers = HttpHeaders()
        headers["Nav-Consumer-Id"] = inntektskomponentProperties.consumerId
        headers["Nav-Call-Id"] = UUID.randomUUID().toString()
        val body = InntektRequest(Aktør(fnr), månedFom, månedTom, inntektskomponentProperties.filter)
        return HttpEntity(body, headers)
    }
}