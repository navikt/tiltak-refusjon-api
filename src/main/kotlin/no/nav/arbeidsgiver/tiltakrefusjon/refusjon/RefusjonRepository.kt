@file:Suppress("FunctionName")

package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface RefusjonRepository : JpaRepository<Refusjon, String>, JpaSpecificationExecutor<Refusjon> {
    fun findAllByDeltakerFnr(deltakerFnr: String): List<Refusjon>
    fun findAllByBedriftNr(bedriftNr: String): List<Refusjon>
    fun findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_Enhet(enhet: String): List<Refusjon>
    fun findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_VeilederNavIdent(veilederNavIdent: String): List<Refusjon>
    fun findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_AvtaleIdAndGodkjentAvArbeidsgiverIsNotNull(avtaleId: String): List<Refusjon>
    fun findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_TilskuddsperiodeId(tilskuddsperiodeId: String): List<Refusjon>
    fun findAllByStatus(status: RefusjonStatus): List<Refusjon>
    fun findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_AvtaleNr(avtaleNr: Int): List<Refusjon>
}