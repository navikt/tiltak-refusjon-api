package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.HentSaksbehandlerRefusjonerQueryParametre
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

data class InnloggetSaksbehandler(
        val ident: String,
        val navn: String,
        @JsonIgnore val abacTilgangsstyringService: AbacTilgangsstyringService,
        @JsonIgnore val refusjonRepository: RefusjonRepository
) {

    fun finnAlle(queryParametre: HentSaksbehandlerRefusjonerQueryParametre): List<Refusjon> {
        var liste = refusjonRepository.findAll()
        if (queryParametre.bedriftNr != null) {
            liste = liste.filter { queryParametre.bedriftNr == it.bedriftNr }
        }
        if (queryParametre.status != null) {
            liste = liste.filter { queryParametre.status == it.status }
        }
        if (queryParametre.tiltakstype != null) {
            liste = liste.filter { queryParametre.tiltakstype == it.tilskuddsgrunnlag.tiltakstype }
        }
        return medLesetilgang(liste)
    }

    fun finnRefusjon(id: String): Refusjon? {
        val refusjon = refusjonRepository.findByIdOrNull(id)
        return refusjon?.let { hvisLesetilgang(it) }
    }

    private fun medLesetilgang(refusjoner: List<Refusjon>): List<Refusjon> {
        return refusjoner
                .filter {
                    abacTilgangsstyringService.harLeseTilgang(ident, it.deltakerFnr)
                }
    }

    private fun hvisLesetilgang(refusjon: Refusjon): Refusjon {
        if (abacTilgangsstyringService.harLeseTilgang(ident, refusjon.deltakerFnr)) {
            return refusjon
        }
        throw HttpClientErrorException(HttpStatus.UNAUTHORIZED)
    }
}