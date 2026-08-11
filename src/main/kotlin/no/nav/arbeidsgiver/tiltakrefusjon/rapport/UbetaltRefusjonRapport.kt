package no.nav.arbeidsgiver.tiltakrefusjon.rapport

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class UbetaltRefusjonRapport(private val refusjonRepository: RefusjonRepository) {

    val log = LoggerFactory.getLogger(javaClass)


    @Scheduled(cron = "0 30 8 * * Mon-Fri")
    @SchedulerLock(
        name = "tiltak-refusjon-api-ubetalte-refusjoner",
        lockAtMostFor = "PT2H",
        lockAtLeastFor = "PT1M"
    )
    fun loggUbetalteRefusjoner() {
        val ubetalteRefusjoner = refusjonRepository.hentRefusjonerSomIkkeErBetalt().map { UbetaltFaktura.fraRefusjon(it) }

        if (ubetalteRefusjoner.isNotEmpty()) {
            val loggmelding = StringBuilder("Det fins ${ubetalteRefusjoner.count()} refusjoner som ikke har blitt utbetalt innen 7 dager:")
            loggmelding.appendLine()

            ubetalteRefusjoner.forEach {
                loggmelding.append("${it.refusjonsnummer} - status: ${it.behandlingsstatus},  tilskuddsperiode-id: ${it.tilskuddsperiodeId}, sendt ${it.sendtTidspunkt}")
                loggmelding.appendLine()
            }

            log.warn(loggmelding.toString())
        }
    }
}
