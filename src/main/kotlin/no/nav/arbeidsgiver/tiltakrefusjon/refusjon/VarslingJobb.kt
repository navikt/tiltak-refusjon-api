package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.leader.LeaderPodCheck
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

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
    fun sjekkForVarsing() {
        if (!leaderPodCheck.isLeaderPod()) {
            logger.info("Pod er ikke leader, så kjører ikke jobb for å finne refusjoner med statusendring")
            return
        }

        val refusjoner = refusjonRepository.findAllByStatus(RefusjonStatus.KLAR_FOR_INNSENDING)
        refusjoner.forEach { refusjon ->
            val varslerForRefusjon = varslingRepository.findAllByRefusjonId(refusjon.id)
            val reVarslerForEfsujon = varslerForRefusjon.filter { it.varselType === VarselType.REVARSEL }

            if (varslerForRefusjon.isEmpty()) {
                // Første varsel
                refusjonVarselProducer.sendVarsel(refusjon, VarselType.KLAR)
            }

            if (refusjon.fristForGodkjenning.isBefore(Now.localDate().plusWeeks(2)) && reVarslerForEfsujon.isEmpty()) {
                // Under 2 uker til frist
                val nyesteVarsling = varslerForRefusjon.maxByOrNull { it.varselTidspunkt }

                val finnesIngenFerskeVarsling =
                    nyesteVarsling != null && nyesteVarsling.varselTidspunkt.isBefore(Now.localDateTime().minusDays(3))
                if (finnesIngenFerskeVarsling) {
                    refusjonVarselProducer.sendVarsel(refusjon, VarselType.REVARSEL)
                }
            }
        }
    }

}