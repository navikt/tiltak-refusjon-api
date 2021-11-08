package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.arbeidsgiver.tiltakrefusjon.RessursFinnesIkkeException
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.InntektskomponentService
import no.nav.arbeidsgiver.tiltakrefusjon.okonomi.KontoregisterService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.HentSaksbehandlerRefusjonerQueryParametre
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Inntektsgrunnlag
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Korreksjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.KorreksjonRepository
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Korreksjonsgrunn
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDate

data class InnloggetSaksbehandler(
    val identifikator: String,
    val navn: String,
    @JsonIgnore val abacTilgangsstyringService: AbacTilgangsstyringService,
    @JsonIgnore val refusjonRepository: RefusjonRepository,
    @JsonIgnore val korreksjonRepository: KorreksjonRepository,
    @JsonIgnore val refusjonService: RefusjonService,
    @JsonIgnore val inntektskomponentService: InntektskomponentService,
    @JsonIgnore val kontoregisterService: KontoregisterService,
) {
    @JsonIgnore
    val log: Logger = LoggerFactory.getLogger(javaClass)

    fun finnAlle(queryParametre: HentSaksbehandlerRefusjonerQueryParametre): List<Refusjon> {
        var liste =
            if (!queryParametre.bedriftNr.isNullOrBlank()) {
                refusjonRepository.findAllByBedriftNr(queryParametre.bedriftNr)
            } else if (!queryParametre.veilederNavIdent.isNullOrBlank()) {
                refusjonRepository.findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_VeilederNavIdent(queryParametre.veilederNavIdent)
            } else if (!queryParametre.deltakerFnr.isNullOrBlank()) {
                refusjonRepository.findAllByDeltakerFnr(queryParametre.deltakerFnr)
            } else if (!queryParametre.enhet.isNullOrBlank()) {
                refusjonRepository.findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_Enhet(queryParametre.enhet)
            } else if (queryParametre.avtaleNr !== null) {
                refusjonRepository.findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_AvtaleNr(queryParametre.avtaleNr)
            } else {
                emptyList()
            }

        if (queryParametre.status != null) {
            liste = liste.filter { queryParametre.status == it.status }
        }
        if (queryParametre.tiltakstype != null) {
            liste = liste.filter { queryParametre.tiltakstype == it.refusjonsgrunnlag.tilskuddsgrunnlag.tiltakstype }
        }
        return medLesetilgang(liste)
    }

    fun finnRefusjon(id: String): Refusjon {
        val refusjon = refusjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkLesetilgang(refusjon)
        return refusjon
    }

    fun finnKorreksjon(id: String): Korreksjon {
        val korreksjon = korreksjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkLesetilgang(korreksjon)
        if (korreksjon.skalGjøreKontonummerOppslag()) {
            val kontonummer = kontoregisterService.hentBankkontonummer(korreksjon.bedriftNr)
            korreksjon.refusjonsgrunnlag.oppgiBedriftKontonummer(kontonummer)
            korreksjonRepository.save(korreksjon)
        }
        if (korreksjon.skalGjøreInntektsoppslag()) {
            val inntektsoppslag = inntektskomponentService.hentInntekter(
                fnr = korreksjon.deltakerFnr,
                bedriftnummerDetSøkesPå = korreksjon.bedriftNr,
                datoFra = korreksjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom,
                datoTil = korreksjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddTom.plusMonths(1) // TODO: Sjekk korreksjonsgrunn, om det skal være 2 mnd etter
            )
            val inntektsgrunnlag = Inntektsgrunnlag(
                inntekter = inntektsoppslag.first,
                respons = inntektsoppslag.second
            )
            korreksjon.refusjonsgrunnlag.oppgiInntektsgrunnlag(inntektsgrunnlag)
            korreksjonRepository.save(korreksjon)
        }
        return korreksjon
    }

    private fun medLesetilgang(refusjoner: List<Refusjon>): List<Refusjon> {
        return refusjoner
            .filter {
                abacTilgangsstyringService.harLeseTilgang(identifikator, it.deltakerFnr)
            }
    }

    private fun sjekkLesetilgang(refusjon: Refusjon) {
        if (!abacTilgangsstyringService.harLeseTilgang(identifikator, refusjon.deltakerFnr)) {
            throw TilgangskontrollException()
        }
    }

    private fun sjekkLesetilgang(korreksjon: Korreksjon) {
        if (!abacTilgangsstyringService.harLeseTilgang(identifikator, korreksjon.deltakerFnr)) {
            throw TilgangskontrollException()
        }
    }

    fun opprettKorreksjonsutkast(id: String, korreksjonsgrunner: Set<Korreksjonsgrunn>): Refusjon {
        val gammel = finnRefusjon(id)
        refusjonService.opprettKorreksjonsutkast(gammel, korreksjonsgrunner)
        return gammel
    }

    fun slettKorreksjonsutkast(id: String) {
        val korreksjon = finnKorreksjon(id)
        sjekkLesetilgang(korreksjon)
        val refusjon = finnRefusjon(korreksjon.korrigererRefusjonId)
        sjekkLesetilgang(refusjon)
        if (korreksjon.kanSlettes()) {
            refusjon.slettKorreksjonsutkast()
            refusjonRepository.save(refusjon)
            korreksjonRepository.delete(korreksjon)
        }
    }

    fun utbetalKorreksjon(id: String, beslutterNavIdent: String, kostnadssted: String) {
        val korreksjon = korreksjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkLesetilgang(korreksjon)
        val refusjon = finnRefusjon(korreksjon.korrigererRefusjonId)
        sjekkLesetilgang(refusjon)

        korreksjon.utbetalKorreksjon(this.identifikator, beslutterNavIdent, kostnadssted)
        refusjon.status = RefusjonStatus.KORRIGERT

        refusjonRepository.save(refusjon)
        korreksjonRepository.save(korreksjon)
    }

    fun fullførKorreksjonVedOppgjort(id: String) {
        val korreksjon = korreksjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkLesetilgang(korreksjon)
        val refusjon = finnRefusjon(korreksjon.korrigererRefusjonId)
        sjekkLesetilgang(refusjon)

        korreksjon.fullførKorreksjonVedOppgjort(this.identifikator)
        refusjon.status = RefusjonStatus.KORRIGERT

        refusjonRepository.save(refusjon)
        korreksjonRepository.save(korreksjon)
    }

    fun fullførKorreksjonVedTilbakekreving(id: String) {
        val korreksjon = korreksjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkLesetilgang(korreksjon)
        val refusjon = finnRefusjon(korreksjon.korrigererRefusjonId)
        sjekkLesetilgang(refusjon)

        korreksjon.fullførKorreksjonVedTilbakekreving(this.identifikator)
        refusjon.status = RefusjonStatus.KORRIGERT

        refusjonRepository.save(refusjon)
        korreksjonRepository.save(korreksjon)
    }

    fun endreBruttolønn(id: String, inntekterKunFraTiltaket: Boolean, endretBruttoLønn: Int?) {
        val korreksjon = korreksjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkLesetilgang(korreksjon)
        korreksjon.endreBruttolønn(inntekterKunFraTiltaket, endretBruttoLønn)
        korreksjonRepository.save(korreksjon)
    }

    fun forlengFrist(id: String, nyFrist: LocalDate, årsak: String): Refusjon {
        val refusjon = finnRefusjon(id)
        refusjon.forlengFrist(nyFrist, årsak, identifikator)
        refusjonRepository.save(refusjon)
        return refusjon
    }
}