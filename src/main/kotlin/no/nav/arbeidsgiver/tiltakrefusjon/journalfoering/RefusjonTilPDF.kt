package no.nav.arbeidsgiver.tiltakrefusjon.journalfoering

import java.time.LocalDate

data class RefusjonTilPDF(
    val id: String,
    val deltakerFornavn: String,
    val deltakerEtternavn: String,
    val tilskuddFom: LocalDate,
    val tilskuddTom: LocalDate,
    val kontonummer: String,
    val lønn: Int,
    val arbeidsgiveravgift: Int,
    val feriepengerSats:Double,
    val feriepenger: Int,
    val refusjonsbeløp: Int,
    val beregnetBeløp: Int,
    val overTilskuddsbeløp: Boolean,
    val sumUtgifter: Int,
    val tjenestepensjon: Int,
    val tidligereUtbetalt: Int,
    val fratrekkLønnFerie: Int,
    val lønnFratrukketFerie: Int,
    val tidligereRefundertBeløp: Int,
    val sumUtgifterFratrukketRefundertBeløp: Int,
    val lønnstilskuddsprosent: Int
)

