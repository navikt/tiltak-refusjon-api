package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.AbacTilgangsstyringService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.nydatamodell.Refusjonsak
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.nydatamodell.RefusjonsakRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

data class InnloggetSaksbehandler(
        val ident: String,
        val navn: String,
        @JsonIgnore val abacTilgangsstyringService: AbacTilgangsstyringService,
        @JsonIgnore val refusjonRepository: RefusjonsakRepository
) : InnloggetBruker() {

    override fun finnAlle(): List<Refusjonsak> {
        return medLesetilgang(refusjonRepository.findAll())
    }

    override fun finnAlleMedBedriftnummer(bedriftnummer: String): List<Refusjonsak> {
        return medLesetilgang(refusjonRepository.findAllByBedriftNr(bedriftnummer))
    }

    override fun finnRefusjonsak(id: String): Refusjonsak? {
        val refusjon = refusjonRepository.findByIdOrNull(id)
        return refusjon?.let { hvisLesetilgang(it) }
    }

    private fun medLesetilgang(refusjoner: List<Refusjonsak>): List<Refusjonsak> {
        return refusjoner
                .filter {
                    abacTilgangsstyringService.harLeseTilgang(ident, it.deltakerFnr)
                }
    }

    private fun hvisLesetilgang(refusjon: Refusjonsak): Refusjonsak {
        if (abacTilgangsstyringService.harLeseTilgang(ident, refusjon.deltakerFnr)) {
            return refusjon
        }
        throw HttpClientErrorException(HttpStatus.UNAUTHORIZED)
    }
}