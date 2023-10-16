package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException
import no.nav.arbeidsgiver.tiltakrefusjon.RessursFinnesIkkeException
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.AltinnTilgangsstyringService
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.Organisasjon
import no.nav.arbeidsgiver.tiltakrefusjon.organisasjon.EregClient
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
import java.time.temporal.ChronoUnit

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

    private fun getQueryMethodForFinnAlleForGittArbeidsgiver(bedriftNr: List<String>, status: RefusjonStatus?, tiltakstype: Tiltakstype?, sortingOrder: SortingOrder?, page: Int, size: Int): Page<Refusjon> {
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

    fun godkjennNullbeløp(refusjonId: String) {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(refusjonId) ?: throw RessursFinnesIkkeException()
        sjekkHarTilgangTilRefusjonerForBedrift(refusjon.bedriftNr)
        refusjonService.godkjennNullbeløpForArbeidsgiver(refusjon, this.identifikator)
    }

    fun finnRefusjonImmutable(id: String): Refusjon {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkHarTilgangTilRefusjonerForBedrift(refusjon.bedriftNr)
        return refusjon
    }

    fun finnRefusjon(id: String): Refusjon {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkHarTilgangTilRefusjonerForBedrift(refusjon.bedriftNr)

        return refusjon
    }

    fun oppdaterRefusjon(id: String, sistEndret: Instant?): Refusjon {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkHarTilgangTilRefusjonerForBedrift(refusjon.bedriftNr)
        sjekkSistEndret(refusjon, sistEndret)

        if(refusjon.åpnetFørsteGang == null) {
            refusjon.åpnetFørsteGang = Now.instant()
        }
        refusjonService.gjørBedriftKontonummeroppslag(refusjon)
        refusjonService.gjørInntektsoppslag(refusjon)
        refusjonService.oppdaterSistEndret(refusjon)
        refusjonRepository.save(refusjon)
        return refusjon
    }

    fun finnInntekter(id: String, sistEndret: Instant?): Refusjon {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkHarTilgangTilRefusjonerForBedrift(refusjon.bedriftNr)
        sjekkSistEndret(refusjon, sistEndret)
        refusjonService.gjørInntektsoppslag(refusjon)
        refusjonService.oppdaterSistEndret(refusjon)
        return refusjon
    }

    fun finnBedriftkontonummer(id: String, sistEndret: Instant?): Refusjon {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkHarTilgangTilRefusjonerForBedrift(refusjon.bedriftNr)
        sjekkSistEndret(refusjon, sistEndret)
        refusjonService.gjørBedriftKontonummeroppslag(refusjon)
        refusjonService.oppdaterSistEndret(refusjon)
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

    fun endreBruttolønn(id: String, inntekterKunFraTiltaket: Boolean?, bruttoLønn: Int?, sistEndret: Instant?) {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkHarTilgangTilRefusjonerForBedrift(refusjon.bedriftNr)
        sjekkSistEndret(refusjon, sistEndret)
        refusjonService.endreBruttolønn(refusjon, inntekterKunFraTiltaket, bruttoLønn)
        refusjonService.gjørBeregning(refusjon)
        refusjonRepository.save(refusjon)
    }

    fun lagreBedriftKID(id: String, bedriftKID: String?){
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkHarTilgangTilRefusjonerForBedrift(refusjon.bedriftNr)
        refusjon.endreBedriftKID(bedriftKID)
        refusjonRepository.save(refusjon)
    }

    fun setInntektslinjeTilOpptjentIPeriode(refusjonId: String, inntekslinjeId: String, erOpptjentIPeriode: Boolean, sistEndret: Instant?) {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(refusjonId) ?: throw RessursFinnesIkkeException()
        sjekkHarTilgangTilRefusjonerForBedrift(refusjon.bedriftNr)
        sjekkSistEndret(refusjon, sistEndret)
        refusjon.setInntektslinjeTilOpptjentIPeriode(inntekslinjeId, erOpptjentIPeriode)
        refusjonService.gjørBeregning(refusjon)
        refusjonService.oppdaterSistEndret(refusjon)
        refusjonRepository.save(refusjon)
    }

    fun settFratrekkRefunderbarBeløp(id: String, fratrekkRefunderbarBeløp: Boolean, refunderbarBeløp: Int?, sistEndret: Instant?) {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkHarTilgangTilRefusjonerForBedrift(refusjon.bedriftNr)
        sjekkSistEndret(refusjon, sistEndret)
        refusjon.settFratrekkRefunderbarBeløp(fratrekkRefunderbarBeløp, refunderbarBeløp)
        refusjonService.gjørBeregning(refusjon)
        refusjonService.oppdaterSistEndret(refusjon)
        refusjonRepository.save(refusjon)
    }

    fun utsettFristSykepenger(id: String) {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkHarTilgangTilRefusjonerForBedrift(refusjon.bedriftNr)
        log.info("Utsetter frist på refusjon ${refusjon.id} grunnet sykepenger/fravær i perioden")
        val tolvMåneder = antallMånederEtter(refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddTom, 12)
        refusjon.forlengFrist(
            nyFrist = tolvMåneder,
            årsak = "Sykepenger",
            utførtAv = identifikator,
            enforce = true
        );
        refusjonRepository.save(refusjon)
    }

    fun merkForHentInntekterFrem(id: String, merking: Boolean) {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkHarTilgangTilRefusjonerForBedrift(refusjon.bedriftNr)
        refusjon.merkForHentInntekterFrem(merking, identifikator)
        log.info("Merket refusjon ${refusjon.id} for henting av inntekter fremover")
        refusjonRepository.save(refusjon)
    }

    private fun sjekkSistEndret(refusjon: Refusjon, sistEndret: Instant?) {
        if ( refusjon.sistEndret != null && sistEndret != null) {
            if (sistEndret.truncatedTo(ChronoUnit.MILLIS).isBefore(refusjon.sistEndret!!.truncatedTo(ChronoUnit.MILLIS))) {
                println("OH NOES")
                println("Sistendret ${sistEndret.truncatedTo(ChronoUnit.MILLIS)} er før ${refusjon.sistEndret}")
                throw FeilkodeException(Feilkode.SAMTIDIGE_ENDRINGER);
            }
        }
    }

}
