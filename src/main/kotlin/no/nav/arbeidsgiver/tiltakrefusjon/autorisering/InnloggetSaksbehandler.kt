package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.AbacTilgangsstyringService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

class InnloggetSaksbehandler(val identifikator: NavIdent, val abacTilgangsstyringService: AbacTilgangsstyringService, val refusjonRepository: RefusjonRepository ) : InnloggetBruker() {

    fun finnAlle(): List<Refusjon> {
        return medLesetilgang(refusjonRepository.findAll())
    }

    override fun finnAlleMedBedriftnummer(bedriftnummer: String): List<Refusjon> {
        return medLesetilgang(refusjonRepository.findByBedriftnummer(bedriftnummer))
    }

    override fun finnRefusjon(id: String): Refusjon? {
       val refusjon = refusjonRepository.findByIdOrNull(id);
        return refusjon?.let { hvisLesetilgang(it) }
    }

    private fun medLesetilgang(refusjoner:List<Refusjon>):List<Refusjon>{
        return refusjoner
                .filter { refusjon: Refusjon -> abacTilgangsstyringService.harLeseTilgang(identifikator, refusjon.deltakerFnr)
        }
    }

    private fun hvisLesetilgang(refusjon :Refusjon):Refusjon{
        if( abacTilgangsstyringService.harLeseTilgang(identifikator, refusjon.deltakerFnr)){
            return refusjon
        }
        throw HttpClientErrorException(HttpStatus.UNAUTHORIZED)
    }
}

//TODO Test ingen treff her og fro arb.giver