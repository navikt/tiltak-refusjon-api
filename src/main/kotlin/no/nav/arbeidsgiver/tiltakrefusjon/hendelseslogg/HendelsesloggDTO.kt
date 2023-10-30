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
        utførtAv = if (hendelseslogg.utførtRolle == BrukerRolle.ARBEIDSGIVER) {
            hendelseslogg.utførtRolle.name
        } else if (erGyldigFnr(hendelseslogg.utførtAv)) {
            // rolle mangler på eldre events
            ""
        } else {
            hendelseslogg.utførtAv
        },
        event = hendelseslogg.event,
        metadata = hendelseslogg.metadata,
        tidspunkt = hendelseslogg.tidspunkt,
    )
}
