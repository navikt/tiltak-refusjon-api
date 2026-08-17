package no.nav.arbeidsgiver.tiltakrefusjon.varsling

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonStatus
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDate

@Component
@ConditionalOnProperty("tiltak-refusjon.kafka.enabled")
class VarslingJobb(
    val refusjonRepository: RefusjonRepository,
    val varslingRepository: VarslingRepository,
    val refusjonVarselProducer: RefusjonVarselProducer,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // Maks låsetid må være kortere enn to timer, slik at reservekjøringen kan ta over etter en krasj.
    @Scheduled(cron = "0 0 2,4 * * *")
    @SchedulerLock(
        name = "tiltak-refusjon-api-revarsling",
        lockAtMostFor = "PT110M",
        lockAtLeastFor = "PT1M"
    )
    fun sjekkForRevarsling() {
        val refusjoner = refusjonRepository.findAllByStatus(RefusjonStatus.KLAR_FOR_INNSENDING)
        for (refusjon in refusjoner) {
            if (refusjon.tiltakstype().utbetalesAutomatisk()) {
                logger.error("Refusjon ${refusjon.id} av tiltakstype ${refusjon.tiltakstype()} skal ikke ha manuell godkjenning")
                continue;
            }

            val varslerForRefusjon = varslingRepository.findAllByRefusjonId(refusjon.id)
            val kortTidTilRefusjonenGårUt = refusjon.fristForGodkjenning.isBefore(Now.localDate().plusWeeks(2))
            val finnesIngenRevarslerForRefusjon = varslerForRefusjon.none { it.varselType === VarselType.REVARSEL }
            val finnesIngenFerskVarsling = dagerSidenForrigeVarsel(varslerForRefusjon) > 3

            if (kortTidTilRefusjonenGårUt && finnesIngenRevarslerForRefusjon && finnesIngenFerskVarsling) {
                refusjonVarselProducer.sendVarsel(
                    varselType = VarselType.REVARSEL,
                    refusjonId = refusjon.id,
                    tilskuddsperiodeId = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddsperiodeId,
                    avtaleId = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleId,
                    fristForGodkjenning = refusjon.fristForGodkjenning,
                    avtaleNr = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr,
                    løpenummer = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.løpenummer,
                    tilskuddFom = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom,
                    tilskuddTom = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddTom,
                    resendingsnummer = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.resendingsnummer,
                    korreksjonsnummer = null
                )
            }
        }
    }

    // Cronjobb kjører kl 07:00 den 5 hver måned.
    @Scheduled(cron = "\${tiltak-refusjon.varsling.varsling-klar-cron}")
    @SchedulerLock(
        name = "tiltak-refusjon-api-varsling-klar",
        lockAtMostFor = "PT2H",
        lockAtLeastFor = "PT1M"
    )
    fun sjekkForVarslingKlar() {
        val forrigeMåned = LocalDate.now().minusMonths(1).month;
        val refusjoner = refusjonRepository.findAllByStatus(RefusjonStatus.KLAR_FOR_INNSENDING)
        var antallSendteVarsler = 0
        for (refusjon in refusjoner) {
            if (refusjon.tiltakstype().utbetalesAutomatisk()) {
                logger.error("Refusjon ${refusjon.id} av tiltakstype ${refusjon.tiltakstype()} skal ikke ha manuell godkjenning")
                continue;
            }
            val varslerForRefusjon = varslingRepository.findAllByRefusjonId(refusjon.id)

            if (varslerForRefusjon.none { it.varselType === VarselType.KLAR} && forrigeMåned.equals(refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddTom.month)) {
                refusjonVarselProducer.sendVarsel(
                    varselType = VarselType.KLAR,
                    refusjonId = refusjon.id,
                    tilskuddsperiodeId = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddsperiodeId,
                    avtaleId = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleId,
                    fristForGodkjenning = refusjon.fristForGodkjenning,
                    avtaleNr = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr,
                    løpenummer = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.løpenummer,
                    tilskuddFom = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom,
                    tilskuddTom = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddTom,
                    resendingsnummer = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.resendingsnummer,
                    korreksjonsnummer = null
                )
                antallSendteVarsler++
                continue;
            }
        }
        logger.info("Cron jobb ferdig kjørt. Sendt ${antallSendteVarsler} varsler")
    }

    fun dagerSidenForrigeVarsel(varslinger: List<Varsling>): Long {
        val nyesteVarsling = varslinger.maxByOrNull { it.varselTidspunkt } ?: return Long.MAX_VALUE
        return Duration.between(nyesteVarsling.varselTidspunkt, Now.localDateTime()).toDays()
    }

}
