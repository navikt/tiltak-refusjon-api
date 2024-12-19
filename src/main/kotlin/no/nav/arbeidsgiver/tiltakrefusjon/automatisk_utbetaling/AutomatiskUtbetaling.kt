package no.nav.arbeidsgiver.tiltakrefusjon.automatisk_utbetaling

import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.SYSTEM_BRUKER
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonStatus
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Tiltakstype
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class AutomatiskUtbetaling(
    private val refusjonRepository: RefusjonRepository,
    private val refusjonService: RefusjonService
) {
    val log = LoggerFactory.getLogger(AutomatiskUtbetaling::class.java.name)

    fun utførAutomatiskUtbetaling() {
        refusjonRepository.findAllByStatusAndRefusjonsgrunnlag_Tilskuddsgrunnlag_TiltakstypeIn(
            RefusjonStatus.FOR_TIDLIG,
            Tiltakstype.somUtbetalesAutomatisk()
        )
            .forEach { refusjon ->
                refusjon.gjørKlarTilInnsending()
                if (refusjon.status == RefusjonStatus.KLAR_FOR_INNSENDING) {
                    utførAutomatiskUtbetaling(refusjon)
                }
            }
    }

    fun utførAutomatiskUtbetaling(refusjon: Refusjon) {
        val refusjonensTiltaktstype = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tiltakstype
        if (!Tiltakstype.somUtbetalesAutomatisk().contains(refusjonensTiltaktstype)) {
            throw IllegalStateException("Refusjon ${refusjon.id} hadde ikke riktig tiltakstype (${refusjonensTiltaktstype})")
        }
        log.info(
            "Utfører automatisk utbetaling for refusjon {}-{} ({})",
            refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr,
            refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.løpenummer,
            refusjon.id
        )
        refusjonService.gjørBeregning(refusjon, SYSTEM_BRUKER)
        refusjon.godkjennForArbeidsgiver(utførtAv = SYSTEM_BRUKER)
        refusjonRepository.save(refusjon)
    }
}
