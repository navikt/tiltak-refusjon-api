package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface RefusjonRepository : JpaRepository<Refusjon, String>, JpaSpecificationExecutor<Refusjon> {
    fun findAllByDeltakerFnr(deltakerFnr: String): List<Refusjon>
    fun findAllByBedriftNr(bedriftNr: String): List<Refusjon>
}