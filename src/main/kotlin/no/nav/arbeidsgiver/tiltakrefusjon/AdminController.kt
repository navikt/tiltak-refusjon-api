package no.nav.arbeidsgiver.tiltakrefusjon

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Korreksjonsgrunn
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonService
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeForkortetMelding
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeGodkjentMelding
import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

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
    @PostMapping("opprett-refusjoner")
    fun opprettRefusjoner(@RequestBody jsonMeldinger: List<TilskuddsperiodeGodkjentMelding>) {
        jsonMeldinger.forEach {
            logger.info("Bruker AdminController for å opprette refusjon med tilskuddsperiodeId {}",
                it.tilskuddsperiodeId)
            service.opprettRefusjon(it)
        }
    }

    @Unprotected
    @PostMapping("forkort-tilskuddsperiode")
    fun forkortTilskuddsperiode(@RequestBody jsonMelding: TilskuddsperiodeForkortetMelding) {
        logger.info("Bruker AdminController for å forkorte tilskuddsperiode med tilskuddsperiodeId {}",
            jsonMelding.tilskuddsperiodeId)
        service.forkortRefusjon(jsonMelding)
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

    @Unprotected
    @PostMapping("forleng-frister")
    fun forlengFrister(@RequestBody request: ForlengFristerRequest) {
        logger.info("Bruker AdminController for å forlenge frister på {} refusjoner",
            request.refusjonIder.size)
        for (id in request.refusjonIder) {
            val refusjon =
                refusjonRepository.findByIdOrNull(id) ?: throw RuntimeException("Finner ikke refusjon med id=$id")
            refusjon.forlengFrist(request.nyFrist, request.årsak, "admin")
            refusjonRepository.save(refusjon)
        }
    }
}

data class KorreksjonRequest(val refusjonIder: List<String>, val korreksjonsgrunner: Set<Korreksjonsgrunn>)

data class ForlengFristerRequest(val refusjonIder: List<String>, val nyFrist: LocalDate, val årsak: String)