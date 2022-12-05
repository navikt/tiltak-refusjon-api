@file:Suppress("FunctionName")

package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param


interface RefusjonRepository : JpaRepository<Refusjon, String> {
    fun findAllByDeltakerFnr(deltakerFnr: String): List<Refusjon>
    fun findAllByBedriftNr(bedriftNr: String): List<Refusjon>
    fun findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_Enhet(enhet: String): List<Refusjon>
    fun findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_VeilederNavIdent(veilederNavIdent: String): List<Refusjon>
    fun findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_AvtaleIdAndGodkjentAvArbeidsgiverIsNotNull(avtaleId: String): List<Refusjon>
    fun findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_TilskuddsperiodeId(tilskuddsperiodeId: String): List<Refusjon>
    fun findAllByStatus(status: RefusjonStatus): List<Refusjon>
    fun findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_AvtaleNr(avtaleNr: Int): List<Refusjon>
    @Query("select r from Refusjon r where r.bedriftNr = :bedriftNr and (r.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr = :avtaleNr) and (:tiltakstype is null or r.refusjonsgrunnlag.tilskuddsgrunnlag.tiltakstype = :tiltakstype) and (:status is null or r.status = :status) " +
            "and :nåværendeLøpenummer not in (1) and r.refusjonsgrunnlag.tilskuddsgrunnlag.løpenummer = (:nåværendeLøpenummer - 1 ) and  r.refusjonsgrunnlag.beregning is not null and r.refusjonsgrunnlag.beregning.refusjonsbeløp <= 0 ")
    fun finnRefusjonSomSkalSendesMedMinusBeløpFørDenne(
        @Param("bedriftNr") bedriftNr: String,
        @Param("avtaleNr") avtaleNr: Int,
        @Param("tiltakstype") tiltakstype: Tiltakstype,
        @Param("status") status: RefusjonStatus,
        @Param("nåværendeLøpenummer") nåværendeLøpenummer: Int
    ): Refusjon?

    @Query("from Refusjon r where r.bedriftNr = :bedriftNr and (r.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr = :avtaleNr)  and (:tiltakstype is null or r.refusjonsgrunnlag.tilskuddsgrunnlag.tiltakstype = :tiltakstype) and (:status is null or r.status = :status) " +
            "and :nåværendeLøpenummer not in (1) and r.refusjonsgrunnlag.tilskuddsgrunnlag.løpenummer = (:nåværendeLøpenummer - 1)"
    )
    fun finnRefusjonSomSkalSendesFørDenne(
        @Param("bedriftNr") bedriftNr: String,
        @Param("avtaleNr") avtaleNr: Int,
        @Param("tiltakstype") tiltakstype: Tiltakstype,
        @Param("status") status: RefusjonStatus,
        @Param("nåværendeLøpenummer") nåværendeLøpenummer: Int
    ): Refusjon?
    @Query("select r from Refusjon r where r.bedriftNr in (:bedriftNr) and (:status is null or r.status = :status) " +
            "and (:tiltakstype is null or r.refusjonsgrunnlag.tilskuddsgrunnlag.tiltakstype = :tiltakstype) " +
            "order by (CASE WHEN r.status = 'KLAR_FOR_INNSENDING' THEN 0 else 1 END)")
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

    @Query("select r from Refusjon r where r.bedriftNr in (:bedriftNr)")
    fun findAllByBedriftNr(@Param("bedriftNr") bedriftNr: List<String>): List<Refusjon>
}