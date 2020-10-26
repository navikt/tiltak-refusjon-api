package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import org.springframework.data.jpa.repository.JpaRepository

interface RefusjonRepository: JpaRepository<Refusjon, String>{
    fun findByBedriftnummer(Bedriftnummer:String):List<Refusjon>
}

