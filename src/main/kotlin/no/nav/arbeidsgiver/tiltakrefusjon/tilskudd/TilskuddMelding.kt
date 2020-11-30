package no.nav.arbeidsgiver.tiltakrefusjon.tilskudd

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Tiltakstype
import java.time.LocalDate

data class TilskuddMelding(
        val avtaleId: String,
        val tilskuddPeriodeId: String,
        val avtaleInnholdId: String,
        val tiltakstype: Tiltakstype,
        val deltakerFornavn: String,
        val deltakerEtternavn: String,
        val deltakerFnr: String,
        val veilederNavIdent: String,
        val bedriftNavn: String,
        val bedriftNr: String,
        val tilskuddBeløp: Int,
        val tilskuddFraDato: LocalDate,
        val tilskuddTilDato: LocalDate
)