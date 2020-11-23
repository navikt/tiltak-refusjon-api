package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface RefusjonRepository : JpaRepository<Refusjon, String> {
    fun findByBedriftnummer(Bedriftnummer: String): List<Refusjon>

    @Query(value = "from Refusjon refusjon where refusjon.deltakerFnr = ?1 and refusjon.bedriftnummer = ?2 and refusjon.fraDato <= ?3 and refusjon.tilDato >= ?4")
    fun findByDeltakerBedriftOgPeriode(DeltakerFnr: String, Bedriftnummer: String, fraDato: LocalDate, tilDato: LocalDate): Refusjon?
}

