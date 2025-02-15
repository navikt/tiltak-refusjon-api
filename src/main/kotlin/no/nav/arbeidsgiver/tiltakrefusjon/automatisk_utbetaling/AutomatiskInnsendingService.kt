package no.nav.arbeidsgiver.tiltakrefusjon.automatisk_utbetaling

import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.SYSTEM_BRUKER
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonStatus
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Tiltakstype
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AutomatiskInnsendingService(
    private val refusjonRepository: RefusjonRepository,
    private val refusjonService: RefusjonService
) {
    val log = LoggerFactory.getLogger(AutomatiskInnsendingService::class.java.name)

    val tiltakstyperSomKanSendesInnAutomatisk = Tiltakstype.entries.filter { it.utbetalesAutomatisk() }.toSet()

    @Transactional
    fun utførAutomatiskInnsendingHvisMulig() {
        refusjonRepository.findAllByStatusAndRefusjonsgrunnlagTilskuddsgrunnlagTiltakstypeIn(
            RefusjonStatus.FOR_TIDLIG,
            tiltakstyperSomKanSendesInnAutomatisk
        )
            .forEach { refusjon ->
                if (refusjon.settKlarTilInnsendingHvisMulig()) {
                    refusjonService.utførAutomatiskInnsendingHvisMulig(refusjon)
                }
            }
    }
}
