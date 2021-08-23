package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.arbeidsgiver.tiltakrefusjon.RessursFinnesIkkeException
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.AltinnTilgangsstyringService
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.Organisasjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull

data class InnloggetArbeidsgiver(
        val identifikator: String,
        @JsonIgnore val altinnTilgangsstyringService: AltinnTilgangsstyringService,
        @JsonIgnore val refusjonRepository: RefusjonRepository,
        @JsonIgnore val refusjonService: RefusjonService

) {

    @JsonIgnore
    val log: Logger = LoggerFactory.getLogger(javaClass)

    val organisasjoner: Set<Organisasjon> = altinnTilgangsstyringService.hentTilganger(identifikator)

    fun finnAlleMedBedriftnummer(bedriftnummer: String): List<Refusjon> {
        sjekkHarTilgangTilRefusjonerForBedrift(bedriftnummer)
        return refusjonRepository.findAllByBedriftNr(bedriftnummer).filter { it.status != RefusjonStatus.MANUELL_KORREKSJON }
    }

    fun godkjenn(refusjonId: String) {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(refusjonId) ?: throw RessursFinnesIkkeException()
        sjekkHarTilgangTilRefusjonerForBedrift(refusjon.bedriftNr)
        refusjonService.godkjennForArbeidsgiver(refusjon)
    }

    fun finnRefusjon(id: String): Refusjon {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkHarTilgangTilRefusjonerForBedrift(refusjon.bedriftNr)
        if (refusjon.status == RefusjonStatus.FOR_TIDLIG || refusjon.status == RefusjonStatus.KLAR_FOR_INNSENDING) {
            try {
                refusjonService.gjørBedriftKontonummeroppslag(refusjon)
            } catch (e: Exception) {
                log.error("Feil ved henting av kontonummer fra ${refusjon.id}", e)
            }
        }
        if (refusjon.status == RefusjonStatus.KLAR_FOR_INNSENDING || refusjon.status == RefusjonStatus.MANUELL_KORREKSJON) {
            try {
                refusjonService.gjørInntektsoppslag(refusjon)
            } catch (e: Exception) {
                log.error("Feil ved henting av inntektoppslag fra ${refusjon.id}", e)
            }
        }
        return refusjon
    }

    private fun sjekkHarTilgangTilRefusjonerForBedrift(bedriftsnummer: String): Boolean {
        if (!organisasjoner.any { it.organizationNumber == bedriftsnummer }) {
            throw TilgangskontrollException()
        }
        return true
    }

    fun finnTidligereRefusjoner(refusjonId: String): List<Refusjon> {
        val refusjon = refusjonRepository.findByIdOrNull(refusjonId) ?: throw TilgangskontrollException()
        val refusjonerMedSammeAvtaleId = refusjonRepository.findAllByTilskuddsgrunnlag_AvtaleIdAndGodkjentAvArbeidsgiverIsNotNull(refusjon.tilskuddsgrunnlag.avtaleId)
        refusjonerMedSammeAvtaleId.forEach { sjekkHarTilgangTilRefusjonerForBedrift(it.bedriftNr) }
        return refusjonerMedSammeAvtaleId.filter { it.id != refusjon.id }
    }
}