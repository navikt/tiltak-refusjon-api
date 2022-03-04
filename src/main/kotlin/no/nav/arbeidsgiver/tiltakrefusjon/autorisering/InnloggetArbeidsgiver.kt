package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.arbeidsgiver.tiltakrefusjon.RessursFinnesIkkeException
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.AltinnTilgangsstyringService
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.Organisasjon
import no.nav.arbeidsgiver.tiltakrefusjon.organisasjon.EregClient
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Korreksjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.KorreksjonRepository
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonStatus
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Tiltakstype
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull


data class InnloggetArbeidsgiver(
    val identifikator: String,
    @JsonIgnore val altinnTilgangsstyringService: AltinnTilgangsstyringService,
    @JsonIgnore val refusjonRepository: RefusjonRepository,
    @JsonIgnore val korreksjonRepository: KorreksjonRepository,
    @JsonIgnore val refusjonService: RefusjonService,
    @JsonIgnore val eregClient: EregClient
) {

    @JsonIgnore
    val log: Logger = LoggerFactory.getLogger(javaClass)

    val organisasjoner: Set<Organisasjon> = altinnTilgangsstyringService.hentTilganger(identifikator)

    fun finnAlleMedBedriftnummer(bedriftnummer: String): List<Refusjon> {
        sjekkHarTilgangTilRefusjonerForBedrift(bedriftnummer)
        return refusjonRepository.findAllByBedriftNr(bedriftnummer)
    }

    fun finnAlleUnderenheterTilArbeidsgiver() = this.organisasjoner
        .filter { org -> org.type != "Enterprise" && org.organizationForm != "FLI" && org.organizationForm != "AS" }
        .map { organisasjon -> organisasjon.organizationNumber }

    fun finnAlleForGittArbeidsgiver(bedrifter: String?, status: RefusjonStatus?, tiltakstype: Tiltakstype?, page: Int, size: Int): Page<Refusjon> {
        val paging: Pageable = PageRequest.of(page, size, Sort.by("bedriftNr"))
        if(bedrifter != null) {
            if (bedrifter != "ALLEBEDRIFTER") {
                return refusjonRepository.findAllByBedriftNrAndStatus(
                    bedrifter.split(",")
                        .filter { org -> this.organisasjoner.any { it.organizationNumber == org } }, status, tiltakstype, paging
                )
            }
        }
        return refusjonRepository.findAllByBedriftNrAndStatus(finnAlleUnderenheterTilArbeidsgiver(), status, tiltakstype, paging)
    }

    fun godkjenn(refusjonId: String) {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(refusjonId) ?: throw RessursFinnesIkkeException()
        sjekkHarTilgangTilRefusjonerForBedrift(refusjon.bedriftNr)
        refusjonService.godkjennForArbeidsgiver(refusjon, this.identifikator)
    }

    fun finnRefusjon(id: String): Refusjon {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkHarTilgangTilRefusjonerForBedrift(refusjon.bedriftNr)
        refusjonService.gjørBedriftKontonummeroppslag(refusjon)
        refusjonService.gjørInntektsoppslag(refusjon)
        return refusjon
    }

    fun finnKorreksjon(id: String): Korreksjon {
        val korreksjon = korreksjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkHarTilgangTilRefusjonerForBedrift(korreksjon.bedriftNr)
        return korreksjon
    }

    private fun sjekkHarTilgangTilRefusjonerForBedrift(bedriftsnummer: String): Boolean {
        if (organisasjoner.none { it.organizationNumber == bedriftsnummer }) {
            throw TilgangskontrollException()
        }
        return true
    }

    fun endreBruttolønn(id: String, inntekterKunFraTiltaket: Boolean?, bruttoLønn: Int?) {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkHarTilgangTilRefusjonerForBedrift(refusjon.bedriftNr)
        refusjon.endreBruttolønn(inntekterKunFraTiltaket, bruttoLønn)
        refusjonRepository.save(refusjon)
    }

    fun toggleSkalRefundereInntektslinje(id: String, inntekslinjeId: String) {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkHarTilgangTilRefusjonerForBedrift(refusjon.bedriftNr)
        refusjon.toggleIntekslinje(inntekslinjeId)
        refusjonRepository.save(refusjon)
    }

}
