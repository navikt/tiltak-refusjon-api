package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.nydatamodell

import org.springframework.data.jpa.repository.JpaRepository

interface RefusjonsakRepository : JpaRepository<Refusjonsak, String> {
    fun findAllByDeltakerFnr(deltakerFnr: String): List<Refusjonsak>
    fun findAllByBedriftNr(bedriftNr: String): List<Refusjonsak>
}