package no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsgiver.tiltakrefusjon.Topics
import no.nav.arbeidsgiver.tiltakrefusjon.featuretoggles.FeatureToggle
import no.nav.arbeidsgiver.tiltakrefusjon.featuretoggles.FeatureToggleService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Tiltakstype
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@ConditionalOnProperty("tiltak-refusjon.kafka.enabled")
@Component
class TilskuddsperiodeKafkaLytter(
    val service: RefusjonService,
    val objectMapper: ObjectMapper,
    val featureToggleService: FeatureToggleService
) {

    val log: Logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = [Topics.TILSKUDDSPERIODE_GODKJENT]
    )
    fun tilskuddsperiodeGodkjent(tilskuddMelding: String) {
        val godkjentMelding = objectMapper.readValue(tilskuddMelding, TilskuddsperiodeGodkjentMelding::class.java)
        if (godkjentMelding.tiltakstype == Tiltakstype.MENTOR && !featureToggleService.isEnabled(
                FeatureToggle.MENTOR_TILSKUDD,
                godkjentMelding.veilederNavIdent
            )
        ) {
            log.info("mentorFeatureToggle er ikke på. Mentor avtale ${godkjentMelding.avtaleNr} ignorert")
            return
        }
        service.opprettRefusjon(godkjentMelding)
    }

    @KafkaListener(
        topics = [Topics.TILSKUDDSPERIODE_ANNULLERT],
    )
    fun tilskuddsperiodeAnnullert(tilskuddMelding: String) {
        val annullertMelding = objectMapper.readValue(tilskuddMelding, TilskuddsperiodeAnnullertMelding::class.java)
        service.annullerRefusjon(annullertMelding)
    }

    @KafkaListener(
        topics = [Topics.TILSKUDDSPERIODE_FORKORTET],
    )
    fun tilskuddsperiodeForkortet(tilskuddMelding: String) {
        val forkortetMelding = objectMapper.readValue(tilskuddMelding, TilskuddsperiodeForkortetMelding::class.java)
        service.forkortRefusjon(forkortetMelding)
    }
}
