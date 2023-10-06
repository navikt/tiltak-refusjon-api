package no.nav.arbeidsgiver.tiltakrefusjon.dokgen

import no.nav.arbeidsgiver.tiltakrefusjon.pdf.RefusjonTilPDF
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object RefusjonTilPDFMapper {
    fun tilPDFdata(refusjon : Refusjon) : RefusjonTilPDF {

        if(refusjon.refusjonsgrunnlag.beregning == null){
            throw RuntimeException("Beregning er null")
        }

        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.YYYY")
        var godkjentArbeidsgiverDato: String =  ""
        var utbetaltDato:String = ""
        var bedriftKid:String = ""

        if(refusjon.godkjentAvArbeidsgiver != null)   godkjentArbeidsgiverDato = formatter.format(LocalDate.ofInstant(refusjon.godkjentAvArbeidsgiver,ZoneId.systemDefault()))
        if(refusjon.utbetaltTidspunkt != null)   utbetaltDato =
            formatter.format(LocalDate.ofInstant(refusjon.utbetaltTidspunkt,ZoneId.systemDefault()))

        if(refusjon.refusjonsgrunnlag.bedriftKid != null){
            bedriftKid = refusjon.refusjonsgrunnlag.bedriftKid!!
        }



        val tilskuddFom =
            formatter.format(refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom)

        val tilskuddTom =
            formatter.format( refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddTom)

        return RefusjonTilPDF(
            refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tiltakstype,
            refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr.toString() + "-" + refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.løpenummer,
            refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.deltakerFornavn,
            refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.deltakerEtternavn,
            refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.arbeidsgiverFornavn,
            refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.arbeidsgiverEtternavn,
            refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.arbeidsgiverTlf,
            godkjentArbeidsgiverDato,
            utbetaltDato,
            tilskuddFom,
            tilskuddTom,
            refusjon.refusjonsgrunnlag.bedriftKontonummer!!,
            bedriftKid,
            refusjon.refusjonsgrunnlag.beregning!!.lønn,
            refusjon.refusjonsgrunnlag.tilskuddsgrunnlag!!.feriepengerSats,
            refusjon.refusjonsgrunnlag.beregning!!.feriepenger,
            refusjon.refusjonsgrunnlag.tilskuddsgrunnlag!!.otpSats,
            refusjon.refusjonsgrunnlag.beregning!!.tjenestepensjon,
            refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.arbeidsgiveravgiftSats,
            refusjon.refusjonsgrunnlag.beregning!!.arbeidsgiveravgift,
            refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.lønnstilskuddsprosent,
            refusjon.refusjonsgrunnlag.beregning!!.refusjonsbeløp,
            refusjon.refusjonsgrunnlag.beregning!!.beregnetBeløp,
            refusjon.refusjonsgrunnlag.beregning!!.overTilskuddsbeløp,
            refusjon.refusjonsgrunnlag.beregning!!.sumUtgifter,
            refusjon.refusjonsgrunnlag.beregning!!.tidligereUtbetalt,
            refusjon.refusjonsgrunnlag.beregning!!.fratrekkLønnFerie,
            refusjon.refusjonsgrunnlag.beregning!!.lønnFratrukketFerie,
            refusjon.refusjonsgrunnlag.beregning!!.tidligereRefundertBeløp,
            refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddsbeløp,
            refusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp,
            refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr.toString() + "-" + (refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.løpenummer -1),
            refusjon.refusjonsgrunnlag.beregning!!.sumUtgifterFratrukketRefundertBeløp
        )
    }
}
