package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException
import no.nav.arbeidsgiver.tiltakrefusjon.RessursFinnesIkkeException
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.AltinnTilgangsstyringService
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.Organisasjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.*
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import no.nav.arbeidsgiver.tiltakrefusjon.utils.antallMånederEtter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import java.time.Instant

data class InnloggetArbeidsgiver(
    override val identifikator: String,
    @JsonIgnore val altinnTilgangsstyringService: AltinnTilgangsstyringService,
    @JsonIgnore val refusjonRepository: RefusjonRepository,
    @JsonIgnore val korreksjonRepository: KorreksjonRepository,
    @JsonIgnore val refusjonService: RefusjonService,
) : InnloggetBruker {

    @JsonIgnore
    val log: Logger = LoggerFactory.getLogger(javaClass)
    override val rolle: BrukerRolle = BrukerRolle.ARBEIDSGIVER

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
            SortingOrder.TILTAKSTYPE_ASC -> return Sort.Order.asc("refusjonsgrunnlag.tilskuddsgrunnlag.tiltakstype")
            SortingOrder.TILTAKSTYPE_DESC -> return Sort.Order.desc("refusjonsgrunnlag.tilskuddsgrunnlag.tiltakstype")
            SortingOrder.BEDRIFT_ASC -> return Sort.Order.asc("refusjonsgrunnlag.tilskuddsgrunnlag.bedriftNavn")
            SortingOrder.BEDRIFT_DESC-> return Sort.Order.desc("refusjonsgrunnlag.tilskuddsgrunnlag.bedriftNavn")
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

    private fun getQueryMethodForFinnAlleForGittArbeidsgiver(bedriftNr: List<String>, status: RefusjonStatus?, tiltakstype: Tiltakstype?, sortingOrder: SortingOrder?, page: Int, size: Int): Page<Refusjon> {
        val paging: Pageable = PageRequest.of(page, size)
        if(sortingOrder != null && sortingOrder != SortingOrder.STATUS_ASC) {
            return refusjonRepository.findAllByBedriftNrAndStatusDefinedSort(bedriftNr, status, tiltakstype, PageRequest.of(page, size, Sort.by(getSortingOrderForPageable(sortingOrder), Sort.Order.asc("id"))))
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

    fun godkjenn(refusjonId: String, sistEndret: Instant?) {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(refusjonId) ?: throw RessursFinnesIkkeException()
        sjekkHarTilgangTilRefusjonerForBedrift(refusjon.bedriftNr)
        sjekkSistEndret(refusjon, sistEndret)
        refusjonService.godkjennForArbeidsgiver(refusjon, this)

    }

    fun godkjennNullbeløp(refusjonId: String, sistEndret: Instant?) {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(refusjonId) ?: throw RessursFinnesIkkeException()
        sjekkHarTilgangTilRefusjonerForBedrift(refusjon.bedriftNr)
        sjekkSistEndret(refusjon, sistEndret)
        refusjonService.godkjennNullbeløpForArbeidsgiver(refusjon, this)

    }

    fun finnRefusjon(id: String): Refusjon {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkHarTilgangTilRefusjonerForBedrift(refusjon.bedriftNr)
        return refusjon
    }

    fun settKontonummerOgInntekterPåRefusjon(id: String, sistEndret: Instant?): Refusjon {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkHarTilgangTilRefusjonerForBedrift(refusjon.bedriftNr)
        if(refusjon.status != RefusjonStatus.KLAR_FOR_INNSENDING) {
            throw FeilkodeException(Feilkode.UGYLDIG_STATUS)
        }
        sjekkSistEndret(refusjon, sistEndret)
        if(refusjon.åpnetFørsteGang == null) {
            refusjon.åpnetFørsteGang = Now.instant()
        }
        refusjonService.gjørBedriftKontonummeroppslag(refusjon)
        refusjonService.gjørInntektsoppslag(refusjon, this)
        refusjonService.oppdaterSistEndret(refusjon)
        return refusjonRepository.save(refusjon)
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

    fun endreBruttolønn(id: String, inntekterKunFraTiltaket: Boolean?, bruttoLønn: Int?, sistEndret: Instant?) {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkHarTilgangTilRefusjonerForBedrift(refusjon.bedriftNr)
        sjekkSistEndret(refusjon, sistEndret)
        refusjonService.endreBruttolønn(refusjon, inntekterKunFraTiltaket, bruttoLønn)
        refusjonService.gjørBeregning(refusjon, this)
        refusjonService.oppdaterSistEndret(refusjon)
        refusjonRepository.save(refusjon)
    }

    fun lagreBedriftKID(id: String, bedriftKID: String?, sistEndret: Instant?){
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkHarTilgangTilRefusjonerForBedrift(refusjon.bedriftNr)
        sjekkSistEndret(refusjon, sistEndret)
        refusjon.endreBedriftKID(bedriftKID)
        refusjonService.oppdaterSistEndret(refusjon)
        refusjonRepository.save(refusjon)
    }

    fun setInntektslinjeTilOpptjentIPeriode(refusjonId: String, inntekslinjeId: String, erOpptjentIPeriode: Boolean, sistEndret: Instant?) {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(refusjonId) ?: throw RessursFinnesIkkeException()
        sjekkHarTilgangTilRefusjonerForBedrift(refusjon.bedriftNr)
        sjekkSistEndret(refusjon, sistEndret)
        refusjon.setInntektslinjeTilOpptjentIPeriode(inntekslinjeId, erOpptjentIPeriode)
        refusjonService.gjørBeregning(refusjon, this)
        refusjonService.oppdaterSistEndret(refusjon)
        refusjonRepository.save(refusjon)
    }

    fun settFratrekkRefunderbarBeløp(id: String, fratrekkRefunderbarBeløp: Boolean, refunderbarBeløp: Int?, sistEndret: Instant?) {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkHarTilgangTilRefusjonerForBedrift(refusjon.bedriftNr)
        sjekkSistEndret(refusjon, sistEndret)
        refusjon.settFratrekkRefunderbarBeløp(fratrekkRefunderbarBeløp, refunderbarBeløp)
        if(fratrekkRefunderbarBeløp) {
            val tolvMåneder = antallMånederEtter(refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddTom, 12)
            refusjon.forlengFristSykepenger(nyFrist = tolvMåneder, årsak = "Sykepenger", utførtAv = this)
        }
        refusjonService.gjørBeregning(refusjon, this)
        refusjonService.oppdaterSistEndret(refusjon)
        refusjonRepository.save(refusjon)
    }

    fun merkForHentInntekterFrem(id: String, merking: Boolean, sistEndret: Instant?) {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkHarTilgangTilRefusjonerForBedrift(refusjon.bedriftNr)
        sjekkSistEndret(refusjon, sistEndret)
        refusjon.merkForHentInntekterFrem(merking, this)
        refusjonService.oppdaterSistEndret(refusjon)
        log.info("Merket refusjon ${refusjon.id} for henting av inntekter fremover")
        refusjonRepository.save(refusjon)
    }

    private fun sjekkSistEndret(refusjon: Refusjon, sistEndret: Instant?) {
        if (refusjon.sistEndret != null && sistEndret != null) {
            if (sistEndret.isBefore(refusjon.sistEndret)) {
                log.warn("Sist endret exception på refusjon ${refusjon.id} (refusjon-tid: ${refusjon.sistEndret}, if-unmodified-since: $sistEndret")
                throw FeilkodeException(Feilkode.SAMTIDIGE_ENDRINGER);
            }
        }
    }

}
