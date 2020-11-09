package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface RefusjonRepository: JpaRepository<Refusjon, String>{
    fun findByBedriftnummer(Bedriftnummer:String):List<Refusjon>
    fun findByDeltakerFnrAndBedriftnummerAndFraDatoGreaterThanEqualAndTilDatoLessThanEqual(DeltakerFnr:String, Bedriftnummer:String, fraDato:LocalDate, tilDato:LocalDate):List<Refusjon>
}

