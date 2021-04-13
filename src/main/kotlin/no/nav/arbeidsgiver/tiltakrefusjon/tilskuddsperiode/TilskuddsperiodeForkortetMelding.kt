package no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode

import java.time.LocalDate

data class TilskuddsperiodeForkortetMelding(
    val tilskuddsperiodeId: String,
    val tilskuddsbeløp: Int,
    val tilskuddTom: LocalDate,
)