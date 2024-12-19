package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.automatisk_utbetaling.AutomatiskUtbetaling
import no.nav.arbeidsgiver.tiltakrefusjon.leader.LeaderPodCheck
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class StatusJobb(
    val refusjonRepository: RefusjonRepository,
    val leaderPodCheck: LeaderPodCheck,
    private val automatiskUtbetaling: AutomatiskUtbetaling,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // Kjør to ganger på natten, kl 1 og kl 3
    @Scheduled(cron = "0 0 1,3 * * *")
    fun sjekkForStatusEndring() {
        if (!leaderPodCheck.isLeaderPod()) {
            logger.info("Pod er ikke leader, så kjører ikke jobb for å finne refusjoner med statusendring")
            return
        }
        fraKlarForInnsendingTilUtgått()
        fraForTidligTilKlarForInnsending()
        automatiskUtbetaling.utførAutomatiskUtbetaling()
    }

    fun fraForTidligTilKlarForInnsending() {
        logger.info("Sjekker for tidliger refusjoner som skal settes til KLAR_FOR_INNSENDING")
        val refusjoner = refusjonRepository.findAllByStatusAndRefusjonsgrunnlag_Tilskuddsgrunnlag_TiltakstypeNotIn(
            RefusjonStatus.FOR_TIDLIG,
            Tiltakstype.somUtbetalesAutomatisk()
        )
        var antallEndretTilKlarForInnsending = 0;
        refusjoner.forEach {
            try {
                it.gjørKlarTilInnsending()
                if (it.status == RefusjonStatus.KLAR_FOR_INNSENDING) {
                    antallEndretTilKlarForInnsending++
                    refusjonRepository.save(it)
                }
            } catch (e: Exception) {
                logger.error("Kunne ikke endre status til KLAR_FOR_INNSENDING for refusjon ${it.id}", e)
            }

        }
        logger.info("Endret til KLAR_FOR_INNSENDING på $antallEndretTilKlarForInnsending refusjoner")
    }

    fun fraKlarForInnsendingTilUtgått() {
        val refusjoner = refusjonRepository.findAllByStatus(RefusjonStatus.KLAR_FOR_INNSENDING)
        var antallEndretTilUtgått: Int = 0
        refusjoner.forEach {
            try {
                it.gjørRefusjonUtgått()
                if (it.status == RefusjonStatus.UTGÅTT) {
                    it.gjørRefusjonUtgått()
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
