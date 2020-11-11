package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import java.time.LocalDate


data class RefusjonsberegningRequest (
    var fnr: String? = null,
    var bedriftNr: String? = null,
    val refusjonFraDato: LocalDate? = null,
    val refusjonTilDato: LocalDate? = null
)