package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException
import no.nav.arbeidsgiver.tiltakrefusjon.RessursFinnesIkkeException
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDate

data class InnloggetSaksbehandler(
    val identifikator: String,
    val navn: String,
    @JsonIgnore val abacTilgangsstyringService: AbacTilgangsstyringService,
    @JsonIgnore val refusjonRepository: RefusjonRepository,
    @JsonIgnore val refusjonService: RefusjonService,
) {
    @JsonIgnore
    val log: Logger = LoggerFactory.getLogger(javaClass)

    fun finnAlle(queryParametre: HentSaksbehandlerRefusjonerQueryParametre): List<Refusjon> {
        var liste =
            if (!queryParametre.bedriftNr.isNullOrBlank()) {
                refusjonRepository.findAllByBedriftNr(queryParametre.bedriftNr)
            } else if (!queryParametre.veilederNavIdent.isNullOrBlank()) {
                refusjonRepository.findAllByTilskuddsgrunnlag_VeilederNavIdent(queryParametre.veilederNavIdent)
            } else if (!queryParametre.deltakerFnr.isNullOrBlank()) {
                refusjonRepository.findAllByDeltakerFnr(queryParametre.deltakerFnr)
            } else if (!queryParametre.enhet.isNullOrBlank()) {
                refusjonRepository.findAllByTilskuddsgrunnlag_Enhet(queryParametre.enhet)
            } else if (queryParametre.avtaleNr !== null) {
                refusjonRepository.findAllByTilskuddsgrunnlag_AvtaleNr(queryParametre.avtaleNr)
            } else {
                emptyList()
            }

        if (queryParametre.status != null) {
            liste = liste.filter { queryParametre.status == it.status }
        }
        if (queryParametre.tiltakstype != null) {
            liste = liste.filter { queryParametre.tiltakstype == it.tilskuddsgrunnlag.tiltakstype }
        }
        return medLesetilgang(liste)
    }

    fun finnRefusjon(id: String): Refusjon {
        val refusjon = refusjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkLesetilgang(refusjon)
        if (refusjon.status == RefusjonStatus.KORREKSJON_UTKAST && refusjon.korreksjonsgrunner.contains(
                Korreksjonsgrunn.HENT_INNTEKTER_PÅ_NYTT)
        ) {
            try {
                refusjonService.gjørInntektsoppslag(refusjon)
            } catch (e: Exception) {
                log.error("Feil ved henting av inntekt for ${refusjon.id}", e)
            }
        }
        return refusjon
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

    fun opprettKorreksjonsutkast(id: String, korreksjonsgrunner: Set<Korreksjonsgrunn>): Refusjon {
        val gammel = finnRefusjon(id)
        refusjonService.opprettKorreksjonsutkast(gammel, korreksjonsgrunner)
        return gammel
    }

    fun slettKorreksjonsutkast(id: String): Refusjon {
        val korreksjon = finnRefusjon(id)
        if (!korreksjon.kanSlettes()) {
            throw FeilkodeException(Feilkode.UGYLDIG_STATUS)
        }
        val opprinneligRefusjon = refusjonRepository.findByIdOrNull(korreksjon.korreksjonAvId)!!
        opprinneligRefusjon.korrigeresAvId = null
        refusjonRepository.save(opprinneligRefusjon)
        refusjonRepository.delete(korreksjon)
        return korreksjon
    }

    fun utbetalKorreksjon(id: String, beslutterNavIdent: String, kostnadssted: String): Refusjon {
        val korreksjon = finnRefusjon(id)
        sjekkLesetilgang(korreksjon)
        korreksjon.utbetalKorreksjon(this.identifikator, beslutterNavIdent, kostnadssted)
        refusjonRepository.save(korreksjon)
        return korreksjon
    }

    fun fullførKorreksjonVedOppgjort(id: String) {
        val korreksjon = finnRefusjon(id)
        sjekkLesetilgang(korreksjon)
        korreksjon.fullførKorreksjonVedOppgjort(this.identifikator)
        refusjonRepository.save(korreksjon)
    }

    fun fullførKorreksjonVedTilbakekreving(id: String) {
        val korreksjon = finnRefusjon(id)
        sjekkLesetilgang(korreksjon)
        korreksjon.fullførKorreksjonVedTilbakekreving(this.identifikator)
        refusjonRepository.save(korreksjon)
    }

    fun endreBruttolønn(id: String, inntekterKunFraTiltaket: Boolean, endretBruttoLønn: Int?) {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkLesetilgang(refusjon)
        if (refusjon.korreksjonAvId == null) {
            // Saksbehandler kan kun oppgi bruttolønn ved korreksjon
            throw FeilkodeException(Feilkode.SAKSBEHANDLER_SVARER_PÅ_INNTEKTSPØRSMÅL)
        }
        refusjonService.endreBruttolønn(refusjon, inntekterKunFraTiltaket, endretBruttoLønn)
    }

    fun forlengFrist(id: String, nyFrist: LocalDate, årsak: String): Refusjon {
        val refusjon = finnRefusjon(id)
        refusjon.forlengFrist(nyFrist, årsak, identifikator)
        refusjonRepository.save(refusjon)
        return refusjon
    }
}