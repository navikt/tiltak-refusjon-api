package no.nav.arbeidsgiver.tiltakrefusjon.dokgen

import no.nav.arbeidsgiver.tiltakrefusjon.pdf.RefusjonTilPDF
import no.nav.arbeidsgiver.tiltakrefusjon.rapport.lagRefusjonsnummer
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonStatus
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object RefusjonTilPDFMapper {
    fun tilPDFdata(refusjon: Refusjon): RefusjonTilPDF {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

        var godkjentArbeidsgiverDato = ""
        var utbetaltDato = ""
        var bedriftKid = ""
        val tilskuddFom = formatter.format(refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom)
        val tilskuddTom = formatter.format(refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddTom)

        if (refusjon.godkjentAvArbeidsgiver != null) godkjentArbeidsgiverDato =
            formatter.format(LocalDate.ofInstant(refusjon.godkjentAvArbeidsgiver, ZoneId.systemDefault()))
        if (refusjon.utbetaltTidspunkt != null) utbetaltDato =
            formatter.format(LocalDate.ofInstant(refusjon.utbetaltTidspunkt, ZoneId.systemDefault()))

        if (refusjon.refusjonsgrunnlag.bedriftKid != null) {
            bedriftKid = refusjon.refusjonsgrunnlag.bedriftKid!!
        }
        val beregning = refusjon.refusjonsgrunnlag.beregning
        val tilskuddsgrunnlag = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag

        if (beregning == null) {
            if (refusjon.status != RefusjonStatus.GODKJENT_NULLBELØP) {
                throw RuntimeException("Beregning er null")
            }

            return RefusjonTilPDF(
                type = tilskuddsgrunnlag.tiltakstype,
                avtaleNr = lagRefusjonsnummer(refusjon),
                deltakerFornavn = tilskuddsgrunnlag.deltakerFornavn,
                deltakerEtternavn = tilskuddsgrunnlag.deltakerEtternavn,
                arbeidsgiverFornavn = tilskuddsgrunnlag.arbeidsgiverFornavn,
                arbeidsgiverEtternavn = tilskuddsgrunnlag.arbeidsgiverEtternavn,
                arbeidsgiverTlf = tilskuddsgrunnlag.arbeidsgiverTlf,
                sendtKravDato = godkjentArbeidsgiverDato,
                utbetaltKravDato = utbetaltDato,
                tilskuddFom = tilskuddFom,
                tilskuddTom = tilskuddTom,
                kontonummer = refusjon.refusjonsgrunnlag.bedriftKontonummer!!,
                bedriftKid = bedriftKid,
                lønn = 0,
                feriepengerSats = tilskuddsgrunnlag.feriepengerSats,
                feriepenger = 0,
                otpSats = tilskuddsgrunnlag.otpSats,
                tjenestepensjon = 0,
                arbeidsgiveravgiftSats = tilskuddsgrunnlag.arbeidsgiveravgiftSats,
                arbeidsgiveravgift = 0,
                lønnstilskuddsprosent = tilskuddsgrunnlag.lønnstilskuddsprosent,
                refusjonsbeløp = 0,
                beregnetBeløp = 0,
                overTilskuddsbeløp = false,
                overFemGrunnbeløp = false,
                sumUtgifter = 0,
                tidligereUtbetalt = 0,
                fratrekkLønnFerie = 0,
                lønnFratrukketFerie = 0,
                tidligereRefundertBeløp = 0,
                tilskuddsbeløp = tilskuddsgrunnlag.tilskuddsbeløp,
                forrigeRefusjonMinusBeløp = refusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp,
                sumUtgifterFratrukketRefundertBeløp = 0,
                mentorTimelonn = 0,
                mentorAntallTimer = 0.0,
                reduksjonForDelvisPeriode = 0
            )
        }

        return RefusjonTilPDF(
            type = tilskuddsgrunnlag.tiltakstype,
            avtaleNr = lagRefusjonsnummer(refusjon),
            deltakerFornavn = tilskuddsgrunnlag.deltakerFornavn,
            deltakerEtternavn = tilskuddsgrunnlag.deltakerEtternavn,
            arbeidsgiverFornavn = tilskuddsgrunnlag.arbeidsgiverFornavn,
            arbeidsgiverEtternavn = tilskuddsgrunnlag.arbeidsgiverEtternavn,
            arbeidsgiverTlf = tilskuddsgrunnlag.arbeidsgiverTlf,
            sendtKravDato = godkjentArbeidsgiverDato,
            utbetaltKravDato = utbetaltDato,
            tilskuddFom = tilskuddFom,
            tilskuddTom = tilskuddTom,
            kontonummer = refusjon.refusjonsgrunnlag.bedriftKontonummer!!,
            bedriftKid = bedriftKid,
            lønn = beregning.lønn,
            feriepengerSats = tilskuddsgrunnlag.feriepengerSats,
            feriepenger = beregning.feriepenger,
            otpSats = tilskuddsgrunnlag.otpSats,
            tjenestepensjon = beregning.tjenestepensjon,
            arbeidsgiveravgiftSats = tilskuddsgrunnlag.arbeidsgiveravgiftSats,
            arbeidsgiveravgift = beregning.arbeidsgiveravgift,
            lønnstilskuddsprosent = tilskuddsgrunnlag.lønnstilskuddsprosent,
            refusjonsbeløp = beregning.refusjonsbeløp,
            beregnetBeløp = beregning.beregnetBeløp,
            overTilskuddsbeløp = beregning.overTilskuddsbeløp,
            overFemGrunnbeløp = beregning.overFemGrunnbeløp ?: false,
            sumUtgifter = beregning.sumUtgifter,
            tidligereUtbetalt = beregning.tidligereUtbetalt,
            fratrekkLønnFerie = beregning.fratrekkLønnFerie,
            lønnFratrukketFerie = beregning.lønnFratrukketFerie,
            tidligereRefundertBeløp = beregning.tidligereRefundertBeløp,
            tilskuddsbeløp = tilskuddsgrunnlag.tilskuddsbeløp,
            forrigeRefusjonMinusBeløp = refusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp,
            sumUtgifterFratrukketRefundertBeløp = beregning.sumUtgifterFratrukketRefundertBeløp,
            mentorTimelonn = tilskuddsgrunnlag.mentorTimelonn,
            mentorAntallTimer = tilskuddsgrunnlag.mentorAntallTimer,
            reduksjonForDelvisPeriode = (beregning.sumUtgifter - beregning.refusjonsbeløp)
        )
    }
}
