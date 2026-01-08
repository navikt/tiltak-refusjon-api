package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsgiver.tiltakrefusjon.Topics.REFUSJON_ENDRET_BETALINGSSTATUS
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.repository.findByIdOrNull
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("tiltak-refusjon.kafka.enabled")
class BetalingStatusKafkaLytter(
    private val refusjonRepository: RefusjonRepository,
    private val korreksjonRepository: KorreksjonRepository,
    private val objectMapper: ObjectMapper
) {

    val log: Logger = LoggerFactory.getLogger(javaClass)

    fun behandleRefusjon(betalingsstatus: BetalingStatusEndringMelding) {
        val refusjon = betalingsstatus.refusjonId?.let { refusjonRepository.findByIdOrNull(it) }
        if (refusjon == null) {
            log.error("Mottatt en betaling status for en ukjent refusjon ${betalingsstatus.refusjonId}")
            return
        }
        if (betalingsstatus.erUtbetalt()) {
            refusjon.utbetalingVellykket()
        } else {
            refusjon.utbetalingMislykket()
        }

        refusjonRepository.save(refusjon)
    }

    fun behandleKorreksjon(betalingsstatus: BetalingStatusEndringMelding) {
        val korreksjon: Korreksjon? = betalingsstatus.korreksjonId?.let { korreksjonRepository.findByIdOrNull(it) }
        if (korreksjon == null) {
            log.error("Mottatt en betaling status for en ukjent korreksjon ${betalingsstatus.korreksjonId}")
            return
        }
        if (betalingsstatus.erUtbetalt()) {
            korreksjon.utbetalingVellykket()
        } else {
            korreksjon.utbetalingMislykket()
        }

        korreksjonRepository.save(korreksjon)
    }

    @KafkaListener(topics = [REFUSJON_ENDRET_BETALINGSSTATUS])
    fun oppdaterKorreksjonEllerRefusjonStatusBasertPåBetalingStatusFraØkonomi(event: String) {
        val betalingsstatus = objectMapper.readValue(event, BetalingStatusEndringMelding::class.java)
        if (betalingsstatus.erForRefusjon() && betalingsstatus.erForKorreksjon()) {
            log.error(
                "Betalingsstatus for både korreksjon og refusjon mottatt! Id: {}, refusjonId: {}, korreksjonId: {}",
                betalingsstatus.id,
                betalingsstatus.refusjonId,
                betalingsstatus.korreksjonId
            )
        } else if (betalingsstatus.erForRefusjon()) {
            behandleRefusjon(betalingsstatus)
        } else if (betalingsstatus.erForKorreksjon()) {
            behandleKorreksjon(betalingsstatus)
        } else {
            log.error("Mottok betalingsstatus for hverken korreksjon eller refusjon! Id: {}", betalingsstatus.id)
        }
    }
}