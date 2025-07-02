package no.nav.arbeidsgiver.tiltakrefusjon.rapport

import no.nav.arbeidsgiver.tiltakrefusjon.leader.LeaderPodCheck
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import kotlin.collections.count
import kotlin.collections.isNotEmpty

@Component
class UbetaltRefusjonRapport(private val leaderPodCheck: LeaderPodCheck, private val refusjonRepository: RefusjonRepository) {

    val log = LoggerFactory.getLogger(javaClass)


    @Scheduled(cron = "0 30 8 * * Mon-Fri")
    fun loggUbetalteRefusjoner() {
        if (!leaderPodCheck.isLeaderPod) {
            return
        }

        val ubetalteRefusjoner = refusjonRepository.hentRefusjonerSomIkkeErBetalt().map { UbetaltFaktura.fraRefusjon(it) }

        if (ubetalteRefusjoner.isNotEmpty()) {
            val loggmelding = StringBuilder("Det fins ${ubetalteRefusjoner.count()} refusjoner som ikke har blitt utbetalt innen 7 dager:")
            loggmelding.appendLine()

            ubetalteRefusjoner.forEach {
                loggmelding.append("${it.refusjonsnummer} - status: ${it.behandlingsstatus},  tilskuddsperiode-id: ${it.tilskuddsperiodeId}, sendt ${it.sendtTidspunkt}")
                loggmelding.appendLine()
            }

            log.error(loggmelding.toString())
        }
    }
}
