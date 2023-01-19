package no.nav.arbeidsgiver.tiltakrefusjon.dokgen

import no.nav.arbeidsgiver.tiltakrefusjon.pdf.RefusjonTilPDF
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object RefusjonTilPDFMapper {
    fun tilPDFdata(refusjon : Refusjon) : RefusjonTilPDF {

        if(refusjon.refusjonsgrunnlag.beregning == null){
            throw RuntimeException("refusjonsgrunnlag er null")
        }

        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.YYYY")
        var godkjentArbeidsgiverDato: String =  ""
        var utbetaltDato:String = ""

        if(refusjon.godkjentAvArbeidsgiver != null)   godkjentArbeidsgiverDato = formatter.format(LocalDate.ofInstant(refusjon.godkjentAvArbeidsgiver,ZoneId.systemDefault()))
        if(refusjon.utbetaltTidspunkt != null)   utbetaltDato =
            formatter.format(LocalDate.ofInstant(refusjon.utbetaltTidspunkt,ZoneId.systemDefault()))

        val tilskuddFom =
            formatter.format(refusjon.tilskuddsgrunnlag.tilskuddFom)

        val tilskuddTom =
            formatter.format( refusjon.tilskuddsgrunnlag.tilskuddTom)

        return RefusjonTilPDF(
            refusjon.tilskuddsgrunnlag.tiltakstype,
            refusjon.tilskuddsgrunnlag.avtaleNr.toString() + "-" + refusjon.tilskuddsgrunnlag.løpenummer,
            refusjon.tilskuddsgrunnlag.deltakerFornavn,
            refusjon.tilskuddsgrunnlag.deltakerEtternavn,
            refusjon.tilskuddsgrunnlag.arbeidsgiverFornavn,
            refusjon.tilskuddsgrunnlag.arbeidsgiverEtternavn,
            refusjon.tilskuddsgrunnlag.arbeidsgiverTlf,
            godkjentArbeidsgiverDato,
            utbetaltDato,
            tilskuddFom,
            tilskuddTom,
            refusjon.refusjonsgrunnlag.bedriftKontonummer!!,
            refusjon.refusjonsgrunnlag.beregning!!.lønn,
            refusjon.refusjonsgrunnlag.tilskuddsgrunnlag!!.feriepengerSats,
            refusjon.refusjonsgrunnlag.beregning!!.feriepenger,
            refusjon.tilskuddsgrunnlag.otpSats,
            refusjon.refusjonsgrunnlag.beregning!!.tjenestepensjon,
            refusjon.tilskuddsgrunnlag.arbeidsgiveravgiftSats,
            refusjon.refusjonsgrunnlag.beregning!!.arbeidsgiveravgift,
            refusjon.tilskuddsgrunnlag.lønnstilskuddsprosent,
            refusjon.refusjonsgrunnlag.beregning!!.refusjonsbeløp,
            refusjon.refusjonsgrunnlag.beregning!!.beregnetBeløp,
            refusjon.refusjonsgrunnlag.beregning!!.overTilskuddsbeløp,
            refusjon.refusjonsgrunnlag.beregning!!.sumUtgifter,
            refusjon.refusjonsgrunnlag.beregning!!.tidligereUtbetalt,
            refusjon.refusjonsgrunnlag.beregning!!.fratrekkLønnFerie,
            refusjon.refusjonsgrunnlag.beregning!!.lønnFratrukketFerie,
            refusjon.refusjonsgrunnlag.beregning!!.tidligereRefundertBeløp,
            refusjon.refusjonsgrunnlag.beregning!!.sumUtgifterFratrukketRefundertBeløp,
            refusjon.tilskuddsgrunnlag.tilskuddsbeløp
        )
    }
}
