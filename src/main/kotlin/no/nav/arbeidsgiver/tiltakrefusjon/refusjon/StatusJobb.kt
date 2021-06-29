package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.leader.LeaderPodCheck
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@EnableScheduling
class StatusJobb(val refusjonRepository: RefusjonRepository, val leaderPodCheck: LeaderPodCheck) {
    private val logger = LoggerFactory.getLogger(javaClass)


    @Scheduled(fixedDelayString = "\${tiltak-refusjon.statusjobb.fixed-delay}")
    fun sjekkForStatusEndring() {
        if (!leaderPodCheck.isLeaderPod()) {
            logger.info("Pod er ikke leader, så kjører ikke jobb for å finne refusjoner med statusendring")
            return
        }
        sjekkOmUtgått()
        sjekkOmKlarForInnsending()
    }


    fun sjekkOmKlarForInnsending() {
        val refusjoner = refusjonRepository.findAllByStatus(RefusjonStatus.FOR_TIDLIG)
        var antallEndretTilForTidlig: Int = 0;
        refusjoner.forEach {
            try {
                if (Now.localDate().isAfter(it.tilskuddsgrunnlag.tilskuddTom)) {
                    it.status = RefusjonStatus.KLAR_FOR_INNSENDING
                    antallEndretTilForTidlig++
                    it.refusjonKlar()
                    refusjonRepository.save(it)
                }
            } catch (e: Exception) {
                logger.error("Kunne ikke endre status til KLAR_FOR_INNSENDING for refusjon ${it.id}", e)
            }

        }
        logger.info("Endret til KLAR_FOR_INNSENDING på $antallEndretTilForTidlig refusjoner")
    }

    fun sjekkOmUtgått() {
        val refusjoner = refusjonRepository.findAllByStatus(RefusjonStatus.KLAR_FOR_INNSENDING)
        var antallEndretTilUtgått: Int = 0;
        refusjoner.forEach {
            try {
                if (Now.localDate().isAfter(it.fristForGodkjenning)) {
                    it.status = RefusjonStatus.UTGÅTT
                    antallEndretTilUtgått++
                    refusjonRepository.save(it)
                }
            } catch (e: Exception) {
                logger.error("Kunne ikke endre status til UTGÅTT for refusjon ${it.id}", e)
            }
        }
        logger.info("Endret status til UTGÅTT på $antallEndretTilUtgått refusjoner")
    }

}