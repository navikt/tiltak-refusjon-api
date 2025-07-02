@file:Suppress("FunctionName")

package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface RefusjonRepository : JpaRepository<Refusjon, String> {
    fun findAllByBedriftNr(bedriftNr: String): List<Refusjon>
    fun findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_TilskuddsperiodeId(tilskuddsperiodeId: String): List<Refusjon>
    fun findAllByStatus(status: RefusjonStatus): List<Refusjon>
    fun findAllByStatus(status: RefusjonStatus, paging: Pageable): Page<Refusjon>
    fun findAllByFristForGodkjenningBeforeAndStatus(fristForGodkjenning: LocalDate, status: RefusjonStatus): List<Refusjon>

    // veilederIdent
    fun findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_VeilederNavIdentAndStatusInAndRefusjonsgrunnlag_Tilskuddsgrunnlag_TiltakstypeIn(
        veilederNavIdent: String,
        status: List<RefusjonStatus>,
        tiltakstype: List<Tiltakstype>,
        paging: Pageable
    ): Page<Refusjon>
    // DeltakerFnr
    fun findAllByDeltakerFnrAndStatusInAndRefusjonsgrunnlag_Tilskuddsgrunnlag_TiltakstypeIn(
        deltakerFnr: String,
        status: List<RefusjonStatus>,
        tiltakstype: List<Tiltakstype>,
        paging: Pageable
    ): Page<Refusjon>
    // BedriftNr
    fun findAllByBedriftNrAndStatusInAndRefusjonsgrunnlag_Tilskuddsgrunnlag_TiltakstypeIn(
        bedriftNr: String,
        status: List<RefusjonStatus>,
        tiltakstype: List<Tiltakstype>,
        paging: Pageable
    ): Page<Refusjon>
    // Enhet
    fun findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_EnhetAndStatusInAndRefusjonsgrunnlag_Tilskuddsgrunnlag_TiltakstypeIn(
        enhet: String,
        status: List<RefusjonStatus>,
        tiltakstype: List<Tiltakstype>,
        paging: Pageable
    ): Page<Refusjon>
    // AvtaleNr
    fun findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_AvtaleNrAndStatusInAndRefusjonsgrunnlag_Tilskuddsgrunnlag_TiltakstypeIn(
        avtaleNr: Int,
        status: List<RefusjonStatus>,
        tiltakstype: List<Tiltakstype>,
        paging: Pageable
    ): Page<Refusjon>

    fun findAllByDeltakerFnrAndBedriftNrAndStatusInAndRefusjonsgrunnlag_Tilskuddsgrunnlag_Tiltakstype(deltakerFnr: String, bedriftNr: String, status: List<RefusjonStatus>, tiltakstype: Tiltakstype): List<Refusjon>

    @Query("select r from Refusjon r where r.bedriftNr in (:bedriftNr) and (:status is null or r.status = :status) " +
            "and (:tiltakstype is null or r.refusjonsgrunnlag.tilskuddsgrunnlag.tiltakstype = :tiltakstype) " +
            "order by (CASE WHEN r.status = 'KLAR_FOR_INNSENDING' THEN 0 else 1 END)," +
            "r.refusjonsgrunnlag.tilskuddsgrunnlag.deltakerFornavn" +", r.id")
    fun findAllByBedriftNrAndStatusDefaultSort(
        @Param("bedriftNr") bedriftNr: List<String>,
        @Param("status") status: RefusjonStatus?,
        @Param("tiltakstype") tiltakstype: Tiltakstype?,
        pageable: Pageable
    ): Page<Refusjon>

    @Query("select r from Refusjon r where r.bedriftNr in (:bedriftNr) and (:status is null or r.status = :status) " +
            "and (:tiltakstype is null or r.refusjonsgrunnlag.tilskuddsgrunnlag.tiltakstype = :tiltakstype)")
    fun findAllByBedriftNrAndStatusDefinedSort(
        @Param("bedriftNr") bedriftNr: List<String>,
        @Param("status") status: RefusjonStatus?,
        @Param("tiltakstype") tiltakstype: Tiltakstype?,
        pageable: Pageable
    ): Page<Refusjon>

    fun findAllByStatusAndRefusjonsgrunnlagTilskuddsgrunnlagTiltakstypeIn(status: RefusjonStatus, tiltakstype: Collection<Tiltakstype>): List<Refusjon>
    fun findAllByStatusAndRefusjonsgrunnlagTilskuddsgrunnlagTiltakstypeNotIn(status: RefusjonStatus, tiltakstype: Collection<Tiltakstype>): List<Refusjon>

    fun findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_AvtaleNrAndStatusIn(avtaleNr: Int, status: List<RefusjonStatus>): List<Refusjon>

    @Query("""
        select r from Refusjon r
        where r.godkjentAvArbeidsgiver between (current_date - 1 year) and (current_date - 7 day) and r.status = 'SENDT_KRAV'
    """)
    fun hentRefusjonerSomIkkeErBetalt(): List<Refusjon>

}
