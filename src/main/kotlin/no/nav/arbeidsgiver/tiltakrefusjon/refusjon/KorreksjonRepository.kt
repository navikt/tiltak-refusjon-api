package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import org.springframework.data.jpa.repository.JpaRepository

interface KorreksjonRepository : JpaRepository<Korreksjon, String> {

    fun findAllByDeltakerFnrAndBedriftNrAndStatusInAndRefusjonsgrunnlag_Tilskuddsgrunnlag_Tiltakstype(
        deltakerFnr: String,
        bedriftNr: String,
        status: List<Korreksjonstype>,
        tiltakstype: Tiltakstype
    ): List<Korreksjon>
}
