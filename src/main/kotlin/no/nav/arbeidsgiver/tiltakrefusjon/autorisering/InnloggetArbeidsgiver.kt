package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.AltinnTilgangsstyringService
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.Organisasjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import org.springframework.http.HttpStatus

data class InnloggetArbeidsgiver(
        val identifikator: String,
        @JsonIgnore val altinnTilgangsstyringService: AltinnTilgangsstyringService,
        @JsonIgnore val refusjonRepository: RefusjonRepository) : InnloggetBruker() {

    val organisasjoner:Set<Organisasjon>

    init {
        organisasjoner = altinnTilgangsstyringService.hentTilganger(identifikator)
    }

    override fun finnAlle(): List<Refusjon> {
        throw TilgangskontrollException(HttpStatus.UNAUTHORIZED)
    }

    override fun finnAlleMedBedriftnummer(bedriftnummer: String): List<Refusjon> {
        sjekkHarTilgangTilRefusjonerForBedrift(bedriftnummer)
        return refusjonRepository.findByBedriftnummer(bedriftnummer)
    }

    override fun finnRefusjon(id: String): Refusjon? {
        val refusjon : Refusjon? = refusjonRepository.findById(id).get()
        refusjon?.let { sjekkHarTilgangTilRefusjonerForBedrift(refusjon.bedriftnummer) }
        return refusjon;
    }

    fun hentTilganger(personIdent: String): Set<Organisasjon> {
        return altinnTilgangsstyringService.hentTilganger(personIdent)
    }

    fun hentTilgangerForPaloggetbruker(): Set<Organisasjon>? {
        return hentTilganger(identifikator)
    }

    fun sjekkHarTilgangTilRefusjonerForBedrift(bedriftsnummer: String): Boolean {
        if (!hentTilgangerForPaloggetbruker()?.any { it.organizationNumber == bedriftsnummer }!!) {
            throw TilgangskontrollException(HttpStatus.UNAUTHORIZED)
        }
        return true
    }
}

