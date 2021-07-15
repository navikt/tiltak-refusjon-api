package no.nav.arbeidsgiver.tiltakrefusjon.varsling

import no.nav.arbeidsgiver.tiltakrefusjon.leader.LeaderPodCheck
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonStatus
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
    val varslingProperties: VarslingProperties,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelayString = "\${tiltak-refusjon.varslingsjobb.fixed-delay}")
    fun sjekkForVarsling() {
        if (!leaderPodCheck.isLeaderPod()) {
            logger.info("Pod er ikke leader, så kjører ikke jobb for å finne refusjoner som skal varsles")
            return
        }

        val refusjoner = refusjonRepository.findAllByStatus(RefusjonStatus.KLAR_FOR_INNSENDING).filter { varslingProperties.earlyBirds.contains(it.id) }
        refusjoner.forEach { refusjon ->
            val varslerForRefusjon = varslingRepository.findAllByRefusjonId(refusjon.id)

            if (varslerForRefusjon.none { it.varselType === VarselType.KLAR }) {
                refusjonVarselProducer.sendVarsel(refusjon, VarselType.KLAR)
                return
            }

            val kortTidTilRefusjonenGårUt = refusjon.fristForGodkjenning.isBefore(Now.localDate().plusWeeks(2))
            val finnesIngenRevarslerForRefusjon = varslerForRefusjon.none { it.varselType === VarselType.REVARSEL }
            val finnesIngenFerskVarsling = dagerSidenForrigeVarsel(varslerForRefusjon) > 3

            if (kortTidTilRefusjonenGårUt && finnesIngenRevarslerForRefusjon && finnesIngenFerskVarsling) {
                refusjonVarselProducer.sendVarsel(refusjon, VarselType.REVARSEL)
            }
        }
    }

    fun dagerSidenForrigeVarsel(varslinger: List<Varsling>): Long {
        val nyesteVarsling = varslinger.maxByOrNull { it.varselTidspunkt } ?: return Long.MAX_VALUE
        return Duration.between(nyesteVarsling.varselTidspunkt, Now.localDateTime()).toDays()
    }

}