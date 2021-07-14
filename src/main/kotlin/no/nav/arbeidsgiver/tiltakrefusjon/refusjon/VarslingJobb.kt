package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.leader.LeaderPodCheck
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
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
    fun sjekkForVarsing() {
        if (!leaderPodCheck.isLeaderPod()) {
            logger.info("Pod er ikke leader, så kjører ikke jobb for å finne refusjoner som skal varsles")
            return
        }

        val refusjoner = refusjonRepository.findAllByStatus(RefusjonStatus.KLAR_FOR_INNSENDING)
        refusjoner.forEach { refusjon ->
            val varslerForRefusjon = varslingRepository.findAllByRefusjonId(refusjon.id)

            if (varslerForRefusjon.none { it.varselType === VarselType.KLAR }) {
                // Første varsel
                refusjonVarselProducer.sendVarsel(refusjon, VarselType.KLAR)
            }

            val finnesIngenRevarslerForRefusjon = varslerForRefusjon.none { it.varselType === VarselType.REVARSEL }
            val kortTidTilRefusjonenGårUt = refusjon.fristForGodkjenning.isBefore(Now.localDate().plusWeeks(2))

            if (kortTidTilRefusjonenGårUt && finnesIngenRevarslerForRefusjon) {
                val nyesteVarsling = varslerForRefusjon.maxByOrNull { it.varselTidspunkt }
                val finnesIngenFerskeVarsling = dagerSidenForrigeVarsel(varslerForRefusjon) > 3
                if (finnesIngenFerskeVarsling) {
                    refusjonVarselProducer.sendVarsel(refusjon, VarselType.REVARSEL)
                }
            }
        }
    }

    fun dagerSidenForrigeVarsel(varslinger: List<Varsling>): Long {
        val nyesteVarsling = varslinger.maxByOrNull { it.varselTidspunkt } ?: return Long.MAX_VALUE
        return Duration.between(nyesteVarsling.varselTidspunkt, Now.localDateTime()).toDays()
    }

}