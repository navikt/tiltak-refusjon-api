package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface KorreksjonRepository : JpaRepository<Korreksjon, String> {

    fun findAllByDeltakerFnrAndBedriftNrAndStatusInAndRefusjonsgrunnlag_Tilskuddsgrunnlag_Tiltakstype(
        deltakerFnr: String,
        bedriftNr: String,
        status: List<Korreksjonstype>,
        tiltakstype: Tiltakstype
    ): List<Korreksjon>

    @Query(
        """
        select k from Korreksjon k
        where k.deltakerFnr = :deltakerFnr and k.bedriftNr = :bedriftNr
          and k.refusjonsgrunnlag.tilskuddsgrunnlag.tiltakstype = :tiltakstype
          and k.status in (:status)
          and k.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom >= :periodeStart
          and k.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddTom <= :periodeSlutt
    """
    )
    fun hentDeltakersKorreksjoner(
        deltakerFnr: String,
        bedriftNr: String,
        tiltakstype: Tiltakstype,
        status: List<Korreksjonstype>,
        periodeStart: LocalDate,
        periodeSlutt: LocalDate
    ): List<Korreksjon>
}
