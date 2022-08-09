package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.query.Param

interface RefusjonRepositoryCustom {
    fun findAllByBedriftNrAndStatusSorted(
        @Param("bedriftNr") bedrift_nr: List<String>,
        @Param("status") status: RefusjonStatus?,
        @Param("tiltakstype") tiltakstype: Tiltakstype?,
        @Param("sortingOrder") sortingOrder: String?,
        pageable: Pageable
    ): Page<Refusjon>
}