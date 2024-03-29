package no.nav.arbeidsgiver.tiltakrefusjon.varsling

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface VarslingRepository : JpaRepository<Varsling, String>, JpaSpecificationExecutor<Varsling> {
    fun findAllByRefusjonId(refusjonId: String): List<Varsling>
}