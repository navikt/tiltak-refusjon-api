package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.nydatamodell

import org.springframework.data.jpa.repository.JpaRepository

interface RefusjonsakRepository : JpaRepository<Refusjonsak, String> {
    fun findAllByDeltakerFnr(deltaker: String): List<Refusjonsak>
}