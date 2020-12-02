package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.AltinnTilgangsstyringService
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.Organisasjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.nydatamodell.Refusjonsak
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.nydatamodell.RefusjonsakRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus

data class InnloggetArbeidsgiver(
        val identifikator: String,
        @JsonIgnore val altinnTilgangsstyringService: AltinnTilgangsstyringService,
        @JsonIgnore val refusjonsakRepository: RefusjonsakRepository
) : InnloggetBruker() {

    val organisasjoner: Set<Organisasjon> = altinnTilgangsstyringService.hentTilganger(identifikator)

    override fun finnAlle(): List<Refusjonsak> {
        throw TilgangskontrollException(HttpStatus.UNAUTHORIZED)
    }

    override fun finnAlleMedBedriftnummer(bedriftnummer: String): List<Refusjonsak> {
        sjekkHarTilgangTilRefusjonsakerForBedrift(bedriftnummer)
        return refusjonsakRepository.findAllByBedriftNr(bedriftnummer)
    }

    override fun finnRefusjonsak(id: String): Refusjonsak? {
        val refusjon: Refusjonsak? = refusjonsakRepository.findByIdOrNull(id)
        refusjon?.let { sjekkHarTilgangTilRefusjonsakerForBedrift(refusjon.bedriftNr) }
        return refusjon
    }

    private fun sjekkHarTilgangTilRefusjonsakerForBedrift(bedriftsnummer: String): Boolean {
        if (!organisasjoner.any { it.organizationNumber == bedriftsnummer }) {
            throw TilgangskontrollException(HttpStatus.UNAUTHORIZED)
        }
        return true
    }
}

