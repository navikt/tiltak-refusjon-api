package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface RefusjonRepository : JpaRepository<Refusjon, String> {
    fun findByBedriftnummer(bedriftnummer: String): List<Refusjon>
    fun findOneByDeltakerFnrAndBedriftnummerAndFraDatoGreaterThanEqualAndTilDatoLessThanEqual(deltakerFnr: String, bedriftnummer: String, fraDato: LocalDate, tilDato: LocalDate): Refusjon?
    fun findByTilskuddPeriodeId(tilskuddPeriodeId: String) : Refusjon?
}

