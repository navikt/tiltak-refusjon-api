package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface VarslingRepository : JpaRepository<Varsling, String>, JpaSpecificationExecutor<Varsling> {
    fun findAllByRefusjonId(refusjonId: String): List<Varsling>
    fun findAllByRefusjonIdAndVarselType(refusjonId: String, varselType: VarselType): List<Varsling>
}