package no.nav.arbeidsgiver.tiltakrefusjon.tilskudd

import java.time.LocalDate
import java.util.UUID

data class TilskuddMelding (
    val avtaleId: UUID? = null,
    val tilskuddPeriodeId: UUID? = null,
    val avtaleInnholdId: UUID? = null,
    val tiltakstype: String? = null,
    val deltakerFornavn: String? = null,
    val deltakerEtternavn: String? = null,
    val deltakerFnr: String? = null,
    val veilederNavIdent: String? = null,
    val bedriftNavn: String? = null,
    val bedriftnummer: String? = null,
    val tilskuddBeløp: Int? = null,
    val tilskuddFraDato: LocalDate? = null,
    val tilskuddTilDato: LocalDate? = null
)