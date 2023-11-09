package no.nav.arbeidsgiver.tiltakrefusjon.dokgen

import no.nav.arbeidsgiver.tiltakrefusjon.pdf.RefusjonTilPDF
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonStatus
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object RefusjonTilPDFMapper {
    fun tilPDFdata(refusjon : Refusjon) : RefusjonTilPDF {
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

        if(refusjon.refusjonsgrunnlag.beregning == null){
            if(refusjon.status != RefusjonStatus.GODKJENT_NULLBELØP) {
                throw RuntimeException("Beregning er null")
            }
            return RefusjonTilPDF(
                type = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tiltakstype,
                avtaleNr = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr.toString() + "-" + refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.løpenummer,
                deltakerFornavn = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.deltakerFornavn,
                deltakerEtternavn = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.deltakerEtternavn,
                arbeidsgiverFornavn = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.arbeidsgiverFornavn,
                arbeidsgiverEtternavn = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.arbeidsgiverEtternavn,
                arbeidsgiverTlf = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.arbeidsgiverTlf,
                sendtKravDato = godkjentArbeidsgiverDato,
                utbetaltKravDato = utbetaltDato,
                tilskuddFom = tilskuddFom,
                tilskuddTom = tilskuddTom,
                kontonummer = refusjon.refusjonsgrunnlag.bedriftKontonummer!!,
                bedriftKid = bedriftKid,
                lønn = 0,
                feriepengerSats = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag!!.feriepengerSats,
                feriepenger = 0,
                otpSats = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag!!.otpSats,
                tjenestepensjon = 0,
                arbeidsgiveravgiftSats = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.arbeidsgiveravgiftSats,
                arbeidsgiveravgift = 0,
                lønnstilskuddsprosent = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.lønnstilskuddsprosent,
                refusjonsbeløp = 0,
                beregnetBeløp = 0,
                overTilskuddsbeløp = false,
                sumUtgifter = 0,
                tidligereUtbetalt = 0,
                fratrekkLønnFerie = 0,
                lønnFratrukketFerie = 0,
                tidligereRefundertBeløp = 0,
                tilskuddsbeløp = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddsbeløp,
                forrigeRefusjonMinusBeløp = refusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp,
                forrigeRefusjonsnummer = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr.toString() + "-" + (refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.løpenummer -1),
                sumUtgifterFratrukketRefundertBeløp = 0
            )
        }

        return RefusjonTilPDF(
            type = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tiltakstype,
            avtaleNr = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr.toString() + "-" + refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.løpenummer,
            deltakerFornavn = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.deltakerFornavn,
            deltakerEtternavn = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.deltakerEtternavn,
            arbeidsgiverFornavn = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.arbeidsgiverFornavn,
            arbeidsgiverEtternavn = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.arbeidsgiverEtternavn,
            arbeidsgiverTlf = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.arbeidsgiverTlf,
            sendtKravDato = godkjentArbeidsgiverDato,
            utbetaltKravDato = utbetaltDato,
            tilskuddFom = tilskuddFom,
            tilskuddTom = tilskuddTom,
            kontonummer = refusjon.refusjonsgrunnlag.bedriftKontonummer!!,
            bedriftKid = bedriftKid,
            lønn = refusjon.refusjonsgrunnlag.beregning!!.lønn,
            feriepengerSats = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag!!.feriepengerSats,
            feriepenger = refusjon.refusjonsgrunnlag.beregning!!.feriepenger,
            otpSats = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag!!.otpSats,
            tjenestepensjon = refusjon.refusjonsgrunnlag.beregning!!.tjenestepensjon,
            arbeidsgiveravgiftSats = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.arbeidsgiveravgiftSats,
            arbeidsgiveravgift = refusjon.refusjonsgrunnlag.beregning!!.arbeidsgiveravgift,
            lønnstilskuddsprosent = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.lønnstilskuddsprosent,
            refusjonsbeløp = refusjon.refusjonsgrunnlag.beregning!!.refusjonsbeløp,
            beregnetBeløp = refusjon.refusjonsgrunnlag.beregning!!.beregnetBeløp,
            overTilskuddsbeløp = refusjon.refusjonsgrunnlag.beregning!!.overTilskuddsbeløp,
            sumUtgifter = refusjon.refusjonsgrunnlag.beregning!!.sumUtgifter,
            tidligereUtbetalt = refusjon.refusjonsgrunnlag.beregning!!.tidligereUtbetalt,
            fratrekkLønnFerie = refusjon.refusjonsgrunnlag.beregning!!.fratrekkLønnFerie,
            lønnFratrukketFerie = refusjon.refusjonsgrunnlag.beregning!!.lønnFratrukketFerie,
            tidligereRefundertBeløp = refusjon.refusjonsgrunnlag.beregning!!.tidligereRefundertBeløp,
            tilskuddsbeløp = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddsbeløp,
            forrigeRefusjonMinusBeløp = refusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp,
            forrigeRefusjonsnummer = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr.toString() + "-" + (refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.løpenummer -1),
            sumUtgifterFratrukketRefundertBeløp = refusjon.refusjonsgrunnlag.beregning!!.sumUtgifterFratrukketRefundertBeløp
        )
    }
}
