package no.nav.arbeidsgiver.tiltakrefusjon

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Korreksjonsgrunn
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonService
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeGodkjentMelding
import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController("/admin")
class AdminController(val service: RefusjonService, val refusjonRepository: RefusjonRepository) {
    val logger = LoggerFactory.getLogger(javaClass)

    @Unprotected
    @PostMapping("opprett-refusjon")
    fun opprettRefusjon(@RequestBody jsonMelding: TilskuddsperiodeGodkjentMelding): Refusjon? {
        logger.info("Bruker AdminController for å opprette refusjon med tilskuddsperiodeId {}",
            jsonMelding.tilskuddsperiodeId)
        return service.opprettRefusjon(jsonMelding)
    }

    @Unprotected
    @PostMapping("lag-korreksjoner")
    fun lagKorreksjoner(@RequestBody korreksjonRequest: KorreksjonRequest): List<String> {
        logger.info("Bruker AdminController for å opprette korreksjon på {} refusjoner",
            korreksjonRequest.refusjonIder.size)
        val korreksjoner = mutableListOf<String>()
        for (id in korreksjonRequest.refusjonIder) {
            val refusjon =
                refusjonRepository.findByIdOrNull(id) ?: throw RuntimeException("Finner ikke refusjon med id=$id")
            val korreksjon = service.korriger(refusjon, korreksjonRequest.korreksjonsgrunner)
            korreksjoner.add(korreksjon.id)
        }
        return korreksjoner
    }
}

data class KorreksjonRequest(val refusjonIder: List<String>, val korreksjonsgrunner: Set<Korreksjonsgrunn>)