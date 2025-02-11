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
        refusjonRepository.findAllByStatusInAndRefusjonsgrunnlagTilskuddsgrunnlagTiltakstypeIn(
            listOf(RefusjonStatus.FOR_TIDLIG, RefusjonStatus.KLAR_FOR_INNSENDING),
            tiltakstyperSomKanSendesInnAutomatisk
        )
            .forEach { refusjon ->
                if (refusjon.status == RefusjonStatus.KLAR_FOR_INNSENDING || refusjon.settKlarTilInnsendingHvisMulig()) {
                    utførAutomatiskInnsendingHvisMulig(refusjon)
                }
            }
    }

    fun utførAutomatiskInnsendingHvisMulig(refusjon: Refusjon) {
        val refusjonensTiltaktstype = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tiltakstype
        if (!refusjonensTiltaktstype.utbetalesAutomatisk()) {
            throw IllegalStateException("Refusjon ${refusjon.id} kan ikke sendes inn automatisk (tiltakstype ${refusjonensTiltaktstype})")
        }
        log.info(
            "Utfører automatisk innsending av refusjon {}-{} ({})",
            refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr,
            refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.løpenummer,
            refusjon.id
        )
        refusjonService.gjørBeregning(refusjon, SYSTEM_BRUKER)
        refusjon.godkjennForArbeidsgiver(utførtAv = SYSTEM_BRUKER)
        refusjonRepository.save(refusjon)
    }
}
