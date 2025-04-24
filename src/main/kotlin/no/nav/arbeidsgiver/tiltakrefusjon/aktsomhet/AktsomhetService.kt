package no.nav.arbeidsgiver.tiltakrefusjon.aktsomhet

import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBrukerService
import no.nav.arbeidsgiver.tiltakrefusjon.persondata.PersondataService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AktsomhetService(
    val innloggetBrukerService: InnloggetBrukerService,
    val persondataService: PersondataService
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    fun kreverAktsomhet(id: String): Aktsomhet {
        try {
            val (refusjon, isArbeidsgiver) = hentRefusjon(id)
            val diskresjonskode = persondataService.hentDiskresjonskode(refusjon.deltakerFnr)
            return Aktsomhet.av(diskresjonskode, isArbeidsgiver)
        } catch (e: Exception) {
            log.error("Feil ved henting av Aktsomhet på refusjon med id $id", e)
            return Aktsomhet.tom()
        }
    }

    private fun hentRefusjon(id: String): Pair<Refusjon, Boolean> {
        if (innloggetBrukerService.erArbeidsgiver()) {
            val innloggetArbeidsgiver = innloggetBrukerService.hentInnloggetArbeidsgiver()
            return Pair(innloggetArbeidsgiver.finnRefusjon(id), true)
        }

        val innloggetSaksbehandler = innloggetBrukerService.hentInnloggetSaksbehandler()
        return Pair(innloggetSaksbehandler.finnRefusjon(id), false)
    }
}
