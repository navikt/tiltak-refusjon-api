package no.nav.arbeidsgiver.tiltakrefusjon.hendelseslogg

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.BrukerRolle
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.erGyldigFnr
import java.time.LocalDateTime

data class HendelsesloggDTO(
    val refusjonId: String,
    val korreksjonId: String?,
    val utførtAv: String,
    val event: String,
    val metadata: HendelseMetadata? = null,
    val tidspunkt: LocalDateTime,
) {
    constructor(hendelseslogg: Hendelseslogg) : this(
        refusjonId = hendelseslogg.refusjonId,
        korreksjonId = hendelseslogg.korreksjonId,
        utførtAv = utførtAv(hendelseslogg),
        event = hendelseslogg.event,
        metadata = hendelseslogg.metadata,
        tidspunkt = hendelseslogg.tidspunkt,
    )
}

private val RollerSomSkalAnonymiseres = setOf(
    BrukerRolle.ARBEIDSGIVER,
    BrukerRolle.SYSTEM
)

private fun hendelseSkalAnonymiseres(hendelseslogg: Hendelseslogg) =
    RollerSomSkalAnonymiseres.contains(hendelseslogg.utførtRolle)

private fun utførtAv(hendelseslogg: Hendelseslogg) =
    if (hendelseSkalAnonymiseres(hendelseslogg)) {
        hendelseslogg.utførtRolle!!.name
    } else if (erGyldigFnr(hendelseslogg.utførtAv)) {
        // rolle mangler på eldre events
        ""
    } else {
        hendelseslogg.utførtAv
    }
