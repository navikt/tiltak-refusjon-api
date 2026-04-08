package no.nav.arbeidsgiver.tiltakrefusjon.automatisk_utbetaling

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonStatus
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Tiltakstype
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AutomatiskInnsendingService(
    private val refusjonRepository: RefusjonRepository,
    private val refusjonService: RefusjonService
) {
    private val log: Logger = LoggerFactory.getLogger(AutomatiskInnsendingService::class.java.name)

    val tiltakstyperSomKanSendesInnAutomatisk = Tiltakstype.entries.filter { it.utbetalesAutomatisk() }.toSet()

    fun utførAutomatiskInnsendingHvisMulig() {
        refusjonRepository.findAllByStatusAndRefusjonsgrunnlagTilskuddsgrunnlagTiltakstypeIn(
            RefusjonStatus.FOR_TIDLIG,
            tiltakstyperSomKanSendesInnAutomatisk
        )
            .forEach { refusjon ->
                try {
                    refusjonService.utførAutomatiskInnsendingHvisMulig(refusjon)
                } catch (e: Exception) {
                    log.warn("Kunne ikke utføre automatisk innsending på ${refusjon.id}, (status: ${refusjon.status})", e)
                }
            }
    }
}
