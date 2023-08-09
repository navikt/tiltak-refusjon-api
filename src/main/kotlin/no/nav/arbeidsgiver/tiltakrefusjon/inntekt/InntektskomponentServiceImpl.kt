package no.nav.arbeidsgiver.tiltakrefusjon.inntekt

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.micrometer.observation.annotation.Observed
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.request.Aktør
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.request.InntektRequest
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.response.ArbeidsInntektMaaned
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.response.InntektListe
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.response.InntektResponse
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Inntektslinje
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
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
@Observed
@ConditionalOnProperty("tiltak-refusjon.inntektskomponenten.fake", havingValue = "false")
class InntektskomponentServiceImpl(
    val inntektskomponentProperties: InntektskomponentProperties,
    @Qualifier("anonymProxyRestTemplate") val restTemplate: RestTemplate,
) : InntektskomponentService {

    private val log = LoggerFactory.getLogger(javaClass)

    private val objectMapper = run {
        val mapper = ObjectMapper()
        mapper.registerModule(JavaTimeModule())
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        mapper.registerModule(KotlinModule())
        mapper
    }

    override fun hentInntekter(
        fnr: String,
        bedriftnummerDetSøkesPå: String,
        datoFra: LocalDate,
        datoTil: LocalDate,
    ): Pair<List<Inntektslinje>, String> {
        val requestEntity = lagRequest(fnr, YearMonth.from(datoFra), YearMonth.from(datoTil))
        val responseMedInntekterForDeltaker =
            restTemplate.exchange<String>(
                inntektskomponentProperties.uri,
                HttpMethod.POST,
                requestEntity
            ).body

        val mappedResponse = objectMapper.readValue<InntektResponse>(responseMedInntekterForDeltaker!!)

        val inntekter = mappedResponse.arbeidsInntektMaaned
        return Pair(
            inntekterForBedrift(inntekter, bedriftnummerDetSøkesPå),
            responseMedInntekterForDeltaker
        )
    }

    private fun inntekterForBedrift(
        månedsInntektList: List<ArbeidsInntektMaaned>?,
        bedriftnummerDetSøkesPå: String,
    ): List<Inntektslinje> {
        if (månedsInntektList.isNullOrEmpty()) {
            return emptyList();
        }
        val inntekterTotalt = mutableListOf<Inntektslinje>()

        månedsInntektList.forEach { inntektMaaned ->
            val arbeidsinntektListe: List<InntektListe>? = inntektMaaned.arbeidsInntektInformasjon.inntektListe
            arbeidsinntektListe?.filter { it.virksomhet?.identifikator.toString() == bedriftnummerDetSøkesPå }
                ?.forEach {
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
                            it.beskrivelse,
                            it.beloep.toDouble(),
                            YearMonth.parse(it.utbetaltIMaaned),
                            dateFraOpptjenningsperiode,
                            datoTilOpptjenningsperiode
                        )
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