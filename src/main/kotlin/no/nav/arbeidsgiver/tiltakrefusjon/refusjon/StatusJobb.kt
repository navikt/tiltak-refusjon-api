package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.automatisk_utbetaling.AutomatiskInnsendingService
import no.nav.arbeidsgiver.tiltakrefusjon.leader.LeaderPodCheck
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class StatusJobb(
    val refusjonRepository: RefusjonRepository,
    val leaderPodCheck: LeaderPodCheck,
    private val automatiskInnsendingService: AutomatiskInnsendingService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // Kjør to ganger på natten, kl 1 og kl 3
    @Scheduled(cron = "0 0 1,3 * * *")
    fun sjekkForStatusEndring() {
        if (!leaderPodCheck.isLeaderPod()) {
            logger.info("Pod er ikke leader, så kjører ikke jobb for å finne refusjoner med statusendring")
            return
        }
        settForTidligTilKlarForInnsendingHvisMulig()
        settKlarForInnsendingTilUtgåttHvisMulig()
        automatiskInnsendingService.utførAutomatiskInnsendingHvisMulig()
    }

    fun settForTidligTilKlarForInnsendingHvisMulig() {
        logger.info("Sjekker for tidliger refusjoner som skal settes til KLAR_FOR_INNSENDING")
        val refusjoner = refusjonRepository.findAllByStatusAndRefusjonsgrunnlagTilskuddsgrunnlagTiltakstypeNotIn(
            RefusjonStatus.FOR_TIDLIG,
            automatiskInnsendingService.tiltakstyperSomKanSendesInnAutomatisk
        )
        var antallEndretTilKlarForInnsending = 0;
        refusjoner.forEach {
            try {
                if (it.settKlarTilInnsendingHvisMulig()) {
                    antallEndretTilKlarForInnsending++
                    refusjonRepository.save(it)
                }
            } catch (e: Exception) {
                logger.error("Kunne ikke endre status til KLAR_FOR_INNSENDING for refusjon ${it.id}", e)
            }

        }
        logger.info("Endret til KLAR_FOR_INNSENDING på $antallEndretTilKlarForInnsending refusjoner")
    }

    fun settKlarForInnsendingTilUtgåttHvisMulig() {
        val refusjoner = refusjonRepository.findAllByStatus(RefusjonStatus.KLAR_FOR_INNSENDING)
        var antallEndretTilUtgått = 0
        refusjoner.forEach {
            try {
                if (it.settTilUtgåttHvisMulig()) {
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
