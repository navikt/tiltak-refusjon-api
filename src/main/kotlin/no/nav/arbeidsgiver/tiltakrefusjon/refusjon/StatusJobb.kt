package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.automatisk_utbetaling.AutomatiskInnsendingService
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class StatusJobb(
    val refusjonRepository: RefusjonRepository,
    private val automatiskInnsendingService: AutomatiskInnsendingService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // Kjør to ganger på natten, kl 1 og kl 3
    @Scheduled(cron = "0 0 1,3 * * *")
    @SchedulerLock(
        name = "tiltak-refusjon-api-status-endring",
        lockAtMostFor = "PT110M",
        lockAtLeastFor = "PT1M"
    )
    fun sjekkForStatusEndring() {
        settForTidligTilKlarForInnsendingHvisMulig()
        settTilUtgaattHvisMulig(
            refusjonerSomKanSettesTilUtgaatt()
        )
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

    fun settTilUtgaattHvisMulig(refusjoner: List<Refusjon>) {
        var antallEndretTilUtgaatt = 0
        refusjoner.forEach {
            try {
                if (it.settTilUtgåttHvisMulig()) {
                    antallEndretTilUtgaatt++
                    refusjonRepository.save(it)
                }
            } catch (e: Exception) {
                logger.error("Kunne ikke endre status til UTGÅTT for refusjon ${it.id}", e)
            }
        }
        logger.info("Endret status til UTGÅTT på $antallEndretTilUtgaatt refusjoner")
    }

    fun refusjonerSomKanSettesTilUtgaatt(inkluderForTidlig: Boolean = false): List<Refusjon> {
        val refusjoner = if (inkluderForTidlig) {
            refusjonRepository.findAllByStatusIn(
                setOf(
                    RefusjonStatus.KLAR_FOR_INNSENDING,
                    RefusjonStatus.FOR_TIDLIG
                )
            )
        } else refusjonRepository.findAllByStatus(RefusjonStatus.KLAR_FOR_INNSENDING)

        return refusjoner.filter { it.kanSettesTilUtgaatt() }
    }
}
