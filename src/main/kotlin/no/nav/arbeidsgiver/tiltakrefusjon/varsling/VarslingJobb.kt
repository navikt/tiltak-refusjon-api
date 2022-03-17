package no.nav.arbeidsgiver.tiltakrefusjon.varsling

import no.nav.arbeidsgiver.tiltakrefusjon.leader.LeaderPodCheck
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonStatus
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import org.joda.time.LocalDate
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration

@Component
@EnableScheduling
@ConditionalOnProperty("tiltak-refusjon.kafka.enabled")
class VarslingJobb(
    val refusjonRepository: RefusjonRepository,
    val varslingRepository: VarslingRepository,
    val refusjonVarselProducer: RefusjonVarselProducer,
    val leaderPodCheck: LeaderPodCheck,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelayString = "\${tiltak-refusjon.varslingsjobb.fixed-delay}")
    fun sjekkForRevarsling() {

        if (!leaderPodCheck.isLeaderPod()) {
            logger.info("Pod er ikke leader, så kjører ikke jobb for å finne refusjoner som skal varsles")
            return
        }

        val refusjoner = refusjonRepository.findAllByStatus(RefusjonStatus.KLAR_FOR_INNSENDING)
        for (refusjon in refusjoner) {

            val varslerForRefusjon = varslingRepository.findAllByRefusjonId(refusjon.id)
            val kortTidTilRefusjonenGårUt = refusjon.fristForGodkjenning.isBefore(Now.localDate().plusWeeks(2))
            val finnesIngenRevarslerForRefusjon = varslerForRefusjon.none { it.varselType === VarselType.REVARSEL }
            val finnesIngenFerskVarsling = dagerSidenForrigeVarsel(varslerForRefusjon) > 3

            if (kortTidTilRefusjonenGårUt && finnesIngenRevarslerForRefusjon && finnesIngenFerskVarsling) {
                refusjonVarselProducer.sendVarsel(VarselType.REVARSEL, refusjon.id, refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddsperiodeId, refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleId, refusjon.fristForGodkjenning)
            }
        }
    }

    // Cronjobb kjører kl 07:00 den 5 hver måned.
    @Scheduled(cron = "\${tiltak-refusjon.varsling.varsling-klar-cron}")
    fun sjekkForVarslingKlar() {

        logger.info("Cronjobb kjører")

        if (!leaderPodCheck.isLeaderPod()) {
            logger.info("Pod er ikke leader, så kjører ikke jobb for å finne refusjoner som skal varsles")
            return
        }

        val forrigeMåned = LocalDate.now().minusMonths(1).monthOfYear;
        val refusjoner = refusjonRepository.findAllByStatus(RefusjonStatus.KLAR_FOR_INNSENDING)
        var antallSendteVarsler = 0
        for (refusjon in refusjoner) {
            val varslerForRefusjon = varslingRepository.findAllByRefusjonId(refusjon.id)

            if (varslerForRefusjon.none { it.varselType === VarselType.KLAR} && refusjon.tilskuddsgrunnlag.tilskuddTom.monthValue == forrigeMåned) {
                refusjonVarselProducer.sendVarsel(VarselType.KLAR, refusjon.id, refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddsperiodeId, refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleId, refusjon.fristForGodkjenning)
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