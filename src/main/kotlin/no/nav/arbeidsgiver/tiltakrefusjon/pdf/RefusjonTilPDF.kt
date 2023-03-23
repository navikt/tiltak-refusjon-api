package no.nav.arbeidsgiver.tiltakrefusjon.pdf

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Tiltakstype

data class RefusjonTilPDF(
    val type: Tiltakstype,
    val avtaleNr: String,
    val deltakerFornavn: String,
    val deltakerEtternavn: String,
    val arbeidsgiverFornavn: String?,
    val arbeidsgiverEtternavn: String?,
    val arbeidsgiverTlf: String?,
    val sendtKravDato: String,
    val utbetaltKravDato: String,
    val tilskuddFom: String,
    val tilskuddTom: String,
    val kontonummer: String,
    val lønn: Int,
    var feriepengerSats:Double,
    val feriepenger: Int,
    var otpSats: Double,
    val tjenestepensjon: Int,
    var arbeidsgiveravgiftSats: Double,
    val arbeidsgiveravgift: Int,
    val lønnstilskuddsprosent: Int,
    val refusjonsbeløp: Int,
    val beregnetBeløp: Int,
    val overTilskuddsbeløp: Boolean,
    val sumUtgifter: Int,
    val tidligereUtbetalt: Int,
    val fratrekkLønnFerie: Int,
    val lønnFratrukketFerie: Int,
    val tidligereRefundertBeløp: Int,
    val tilskuddsbeløp: Int,
    val forrigeRefusjonMinusBeløp: Int,
    val forrigeRefusjonsnummer: String,
    val sumUtgifterFratrukketRefundertBeløp: Int
)

