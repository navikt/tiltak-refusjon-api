package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.arbeidsgiver.tiltakrefusjon.RessursFinnesIkkeException
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.AltinnTilgangsstyringService
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.Organisasjon
import no.nav.arbeidsgiver.tiltakrefusjon.organisasjon.EregClient
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.*
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
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
    @JsonIgnore val eregClient: EregClient,
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

    fun getSortingOrderForPageable(sortingOrder: SortingOrder): Sort.Order {
        when (sortingOrder) {
            SortingOrder.DELTAKER_ASC -> return Sort.Order.asc("refusjonsgrunnlag.tilskuddsgrunnlag.deltakerFornavn")
            SortingOrder.DELTAKER_DESC -> return Sort.Order.desc("refusjonsgrunnlag.tilskuddsgrunnlag.deltakerFornavn")
            SortingOrder.PERIODE_ASC -> return Sort.Order.asc("refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddTom")
            SortingOrder.PERIODE_DESC -> return Sort.Order.desc("refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddTom")
            SortingOrder.STATUS_DESC -> return Sort.Order.desc("status")
            SortingOrder.FRISTFORGODKJENNING_ASC -> return Sort.Order.asc("fristForGodkjenning")
            SortingOrder.FRISTFORGODKJENNING_DESC -> return Sort.Order.desc("fristForGodkjenning")
            else -> return Sort.Order.asc("status")
        }
    }

    fun getQueryMethodForFinnAlleForGittArbeidsgiver(bedriftNr: List<String>, status: RefusjonStatus?, tiltakstype: Tiltakstype?,  sortingOrder: SortingOrder?, page: Int, size: Int): Page<Refusjon> {
        val paging: Pageable = PageRequest.of(page, size)
        if(sortingOrder != null && sortingOrder != SortingOrder.STATUS_ASC) {
            return refusjonRepository.findAllByBedriftNrAndStatusDefinedSort(bedriftNr, status, tiltakstype, PageRequest.of(page, size, Sort.by(getSortingOrderForPageable(sortingOrder))))
        }
        return refusjonRepository.findAllByBedriftNrAndStatusDefaultSort(bedriftNr, status, tiltakstype, paging)

    }

    fun finnAlleForGittArbeidsgiver(bedrifter: String?, status: RefusjonStatus?, tiltakstype: Tiltakstype?,  sortingOrder: SortingOrder?, page: Int, size: Int): Page<Refusjon> {
        if(bedrifter != null) {
            if (bedrifter != "ALLEBEDRIFTER") {
                return getQueryMethodForFinnAlleForGittArbeidsgiver(
                    bedrifter.split(",")
                        .filter { org -> this.organisasjoner.any { it.organizationNumber == org } }, status, tiltakstype, sortingOrder, page, size
                )
            }
        }
        return getQueryMethodForFinnAlleForGittArbeidsgiver(finnAlleUnderenheterTilArbeidsgiver(), status, tiltakstype, sortingOrder, page, size)
    }

    fun godkjenn(refusjonId: String) {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(refusjonId) ?: throw RessursFinnesIkkeException()
        sjekkHarTilgangTilRefusjonerForBedrift(refusjon.bedriftNr)
        refusjonService.godkjennForArbeidsgiver(refusjon, this.identifikator)
    }

    fun finnRefusjon(id: String): Refusjon {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        settMinusBeløpOmFratrukketFerieGirMinusForForrigeRefusjonOmDenFinnes(refusjon)
        settOmForrigeRefusjonMåSendesFørst(refusjon)
        sjekkHarTilgangTilRefusjonerForBedrift(refusjon.bedriftNr)
        if(refusjon.åpnetFørsteGang == null) {
            refusjon.åpnetFørsteGang = Now.instant()
        }
        refusjonService.gjørBedriftKontonummeroppslag(refusjon)
        refusjonService.gjørInntektsoppslag(refusjon)
        return refusjon
    }

    /**
     * Avklart med resultatseksjonen og virkemiddelseksjonen at ved minusbeløp en måned vil det ikke bli noen refusjon den måneden,
     * men at vi overfører minusbeløp til neste måned dersom tiltaket fortsetter måneden etter. Hvis tiltaket avsluttes den samme måneden hvor det går i minus,
     * så går refusjonen bare i 0,-.
     */
    private fun settMinusBeløpOmFratrukketFerieGirMinusForForrigeRefusjonOmDenFinnes(denneRefusjon: Refusjon) {
        val tidligereRefusjonMedMinusBeløpEtterFratrukketFerie: Refusjon =
            refusjonRepository.finnRefusjonSomSkalSendesMedMinusBeløpEtterFratrukketFerieFørDenne(
                denneRefusjon.bedriftNr,
                denneRefusjon.tilskuddsgrunnlag.avtaleNr,
                denneRefusjon.tilskuddsgrunnlag.tiltakstype,
                RefusjonStatus.GODKJENT_MINUSBELØP,
                denneRefusjon.tilskuddsgrunnlag.løpenummer
            ) ?: return

        val beløpFraForrigeRefusjon = if (tidligereRefusjonMedMinusBeløpEtterFratrukketFerie.beregning!!.lønnFratrukketFerie <= 0)  tidligereRefusjonMedMinusBeløpEtterFratrukketFerie.beregning!!.lønnFratrukketFerie else tidligereRefusjonMedMinusBeløpEtterFratrukketFerie.beregning!!.refusjonsbeløp
        denneRefusjon.refusjonsgrunnlag.oppgiForrigeRefusjonsbeløp(beløpFraForrigeRefusjon)
    }

    private fun settOmForrigeRefusjonMåSendesFørst(refusjon: Refusjon){
        if(refusjon.status != RefusjonStatus.KLAR_FOR_INNSENDING) return
        val forrigeRefusjonSomMåSendesInnFørst: Refusjon = refusjonRepository.finnRefusjonSomSkalSendesFørDenne(refusjon.bedriftNr,refusjon.tilskuddsgrunnlag.avtaleNr,refusjon.tilskuddsgrunnlag.tiltakstype, RefusjonStatus.KLAR_FOR_INNSENDING, refusjon.tilskuddsgrunnlag.løpenummer).firstOrNull()
            ?: return
        if(forrigeRefusjonSomMåSendesInnFørst != refusjon) refusjon.angiRefusjonSomMåSendesFørst(forrigeRefusjonSomMåSendesInnFørst)
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

    fun setInntektslinjeTilOpptjentIPeriode(refusjonId: String, inntekslinjeId: String, erOpptjentIPeriode: Boolean) {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(refusjonId) ?: throw RessursFinnesIkkeException()
        sjekkHarTilgangTilRefusjonerForBedrift(refusjon.bedriftNr)
        refusjon.setInntektslinjeTilOpptjentIPeriode(inntekslinjeId, erOpptjentIPeriode)
        refusjonRepository.save(refusjon)
    }

    fun settFratrekkRefunderbarBeløp(id: String, fratrekkRefunderbarBeløp: Boolean, refunderbarBeløp: Int?) {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkHarTilgangTilRefusjonerForBedrift(refusjon.bedriftNr)
        refusjon.settFratrekkRefunderbarBeløp(fratrekkRefunderbarBeløp, refunderbarBeløp)
        refusjonRepository.save(refusjon)
    }

}
