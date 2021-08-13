@file:Suppress("FunctionName")

package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.RessursFinnesIkkeException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.findByIdOrNull

interface RefusjonRepository : JpaRepository<Refusjon, String>, JpaSpecificationExecutor<Refusjon> {
    fun findAllByDeltakerFnr(deltakerFnr: String): List<Refusjon>
    fun findAllByBedriftNr(bedriftNr: String): List<Refusjon>
    fun findAllByTilskuddsgrunnlag_Enhet(enhet: String): List<Refusjon>
    fun findAllByTilskuddsgrunnlag_VeilederNavIdent(veilederNavIdent: String): List<Refusjon>
    fun findAllByTilskuddsgrunnlag_AvtaleIdAndGodkjentAvArbeidsgiverIsNotNull(avtaleId: String): List<Refusjon>
    fun findByTilskuddsgrunnlag_TilskuddsperiodeId(tilskuddsperiodeId: String): Refusjon?
    fun findAllByStatus(status: RefusjonStatus): List<Refusjon>
    fun findAllById(refusjonsId: String): List<Refusjon>
}