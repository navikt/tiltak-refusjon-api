package no.nav.arbeidsgiver.tiltakrefusjon

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.*
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeForkortetMelding
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeGodkjentMelding
import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController("/admin")
class AdminController(
    val service: RefusjonService,
    val refusjonRepository: RefusjonRepository,
    val korreksjonRepository: KorreksjonRepository,
    val refusjonEndretStatusKafkaTemplate: KafkaTemplate<String, RefusjonEndretStatusMelding>
) {
    val logger = LoggerFactory.getLogger(javaClass)

    @Unprotected
    @PostMapping("opprett-refusjon")
    fun opprettRefusjon(@RequestBody jsonMelding: TilskuddsperiodeGodkjentMelding): Refusjon? {
        logger.info(
            "Bruker AdminController for å opprette refusjon med tilskuddsperiodeId {}",
            jsonMelding.tilskuddsperiodeId
        )
        return service.opprettRefusjon(jsonMelding)
    }

    @Unprotected
    @PostMapping("opprett-refusjoner")
    fun opprettRefusjoner(@RequestBody jsonMeldinger: List<TilskuddsperiodeGodkjentMelding>) {
        jsonMeldinger.forEach {
            logger.info(
                "Bruker AdminController for å opprette refusjon med tilskuddsperiodeId {}",
                it.tilskuddsperiodeId
            )
            service.opprettRefusjon(it)
        }
    }

    @Unprotected
    @PostMapping("forkort-tilskuddsperiode")
    fun forkortTilskuddsperiode(@RequestBody jsonMelding: TilskuddsperiodeForkortetMelding) {
        logger.info(
            "Bruker AdminController for å forkorte tilskuddsperiode med tilskuddsperiodeId {}",
            jsonMelding.tilskuddsperiodeId
        )
        service.forkortRefusjon(jsonMelding)
    }

    @Unprotected
    @PostMapping("lag-korreksjoner")
    fun lagKorreksjoner(@RequestBody korreksjonRequest: KorreksjonRequest): List<String> {
        logger.info(
            "Bruker AdminController for å opprette korreksjon på {} refusjoner",
            korreksjonRequest.refusjonIder.size
        )
        val korreksjoner = mutableListOf<String>()
        for (id in korreksjonRequest.refusjonIder) {
            val refusjon =
                refusjonRepository.findByIdOrNull(id) ?: throw RuntimeException("Finner ikke refusjon med id=$id")
            service.opprettKorreksjonsutkast(refusjon, korreksjonRequest.korreksjonsgrunner)
            // korreksjoner.add(korreksjon.id)
        }
        return korreksjoner
    }

    // Kanskje ikke behov. Ble brukt ved migrering til ny datamodell
    // @Unprotected
    // @PostMapping("slett-korreksjoner")
    // fun slettKorreksjoner(@RequestBody refusjonIder: List<String>) {
    //     logger.info("Bruker AdminController for å slette korreksjon på {} refusjoner", refusjonIder.size)
    //     for (id in refusjonIder) {
    //         val refusjon =
    //             refusjonRepository.findByIdOrNull(id) ?: throw RuntimeException("Finner ikke refusjon med id=$id")
    //         service.slettKorreksjonsutkast(refusjon)
    //     }
    // }

    @Unprotected
    @PostMapping("forleng-frister")
    fun forlengFrister(@RequestBody request: ForlengFristerRequest) {
        logger.info(
            "Bruker AdminController for å forlenge frister på {} refusjoner",
            request.refusjonIder.size
        )
        for (id in request.refusjonIder) {
            val refusjon =
                refusjonRepository.findByIdOrNull(id) ?: throw RuntimeException("Finner ikke refusjon med id=$id")
            refusjon.forlengFrist(request.nyFrist, request.årsak, "admin")
            refusjonRepository.save(refusjon)
        }
    }

    @Unprotected
    @PostMapping("annuller-tilskuddsperioder-manuelt")
    fun annullerTilskuddsperioderIRefusjonManuelt(@RequestBody request: AnnullerTilskuddsperioderRequest) {
        logger.info("Bruker AdminController for å annullere tilskuddsperioder i {} refusjoner", request.refusjonIder.size)
        for (id in request.refusjonIder) {
            val refusjon =
                refusjonRepository.findByIdOrNull(id) ?: throw RuntimeException("Finner ikke refusjon med id=$id")
            refusjon.annullerTilskuddsperioderIRefusjon(request.utførtAv, request.årsak)
            refusjonRepository.save(refusjon)
        }
    }
    @Unprotected
    @PostMapping("annuller-tilskuddsperioder-manuelt-i-utgåtte-refusjoner")
    fun annullerTilskuddsperioderIUtgåtteRefusjonManuelt(@RequestBody request: AnnullerTilskuddsperioderIUtgåtteRefusjonerRequest) {
        val utgåtteRefusjoner = refusjonRepository.findAllByStatus(RefusjonStatus.UTGÅTT)
        logger.info("Bruker AdminController for å annullere tilskuddsperioder i {} utgåtte refusjoner", utgåtteRefusjoner.size)
        utgåtteRefusjoner.forEach {
            it.annullerTilskuddsperioderIRefusjon(request.utførtAv, request.årsak)
            refusjonRepository.save(it)
        }
    }
    @Unprotected
    @PostMapping("send-statuser-til-kafka-topic")
    fun sendStatuserTilKafkaTopic() {
        logger.info("Sender status for alle refusjoner til kafka-topic")
        val refusjoner = refusjonRepository.findAll()
        var antallStatuserSendt = refusjoner.size
        refusjoner.forEach { refusjon ->
            val melding = RefusjonEndretStatusMelding(
                refusjonId = refusjon.id,
                bedriftNr = refusjon.bedriftNr,
                avtaleId = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleId,
                status = refusjon.status
            )
            refusjonEndretStatusKafkaTemplate.send(
                Topics.REFUSJON_ENDRET_STATUS,
                refusjon.id,
                melding
            ).addCallback({
                logger.info("Melding med id {} sendt til Kafka topic {}", it?.producerRecord?.key(), it?.recordMetadata?.topic())
            }, {
                logger.warn("Feil ved sending av refusjon status på Kafka", it)
            })
        }
        logger.info("Sendt totalt $antallStatuserSendt statuser til kafka" )
    }
}

data class KorreksjonRequest(val refusjonIder: List<String>, val korreksjonsgrunner: Set<Korreksjonsgrunn>)

data class ForlengFristerRequest(val refusjonIder: List<String>, val nyFrist: LocalDate, val årsak: String)

data class AnnullerTilskuddsperioderRequest(val refusjonIder: List<String>, val utførtAv: String, val årsak: String)
data class AnnullerTilskuddsperioderIUtgåtteRefusjonerRequest(val utførtAv: String, val årsak: String)