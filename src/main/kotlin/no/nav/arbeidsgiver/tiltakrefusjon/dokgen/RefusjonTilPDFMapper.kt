package no.nav.arbeidsgiver.tiltakrefusjon.dokgen

import no.nav.arbeidsgiver.tiltakrefusjon.journalfoering.RefusjonTilPDF
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon

object RefusjonTilPDFMapper {
    fun tilPDFdata(refusjon : Refusjon) : RefusjonTilPDF {

        if(refusjon.refusjonsgrunnlag.beregning == null){
            throw RuntimeException("refusjonsgrunnlag er null")
        }

        return RefusjonTilPDF(
            refusjon.id,
            refusjon.tilskuddsgrunnlag.deltakerFornavn,
            refusjon.tilskuddsgrunnlag.deltakerEtternavn,
            refusjon.tilskuddsgrunnlag.tilskuddFom,
            refusjon.tilskuddsgrunnlag.tilskuddTom,
            refusjon.refusjonsgrunnlag.bedriftKontonummer!!,
            refusjon.refusjonsgrunnlag.beregning!!.lønn,
            refusjon.refusjonsgrunnlag.beregning!!.arbeidsgiveravgift,
            refusjon.refusjonsgrunnlag.tilskuddsgrunnlag!!.feriepengerSats,
            refusjon.refusjonsgrunnlag.beregning!!.feriepenger,
            refusjon.refusjonsgrunnlag.beregning!!.refusjonsbeløp,
            refusjon.refusjonsgrunnlag.beregning!!.beregnetBeløp,
            refusjon.refusjonsgrunnlag.beregning!!.overTilskuddsbeløp,
            refusjon.refusjonsgrunnlag.beregning!!.sumUtgifter,
            refusjon.refusjonsgrunnlag.beregning!!.tjenestepensjon,
            refusjon.refusjonsgrunnlag.beregning!!.tidligereUtbetalt,
            refusjon.refusjonsgrunnlag.beregning!!.fratrekkLønnFerie,
            refusjon.refusjonsgrunnlag.beregning!!.lønnFratrukketFerie,
            refusjon.refusjonsgrunnlag.beregning!!.tidligereRefundertBeløp,
            refusjon.refusjonsgrunnlag.beregning!!.sumUtgifterFratrukketRefundertBeløp,
            refusjon.tilskuddsgrunnlag.lønnstilskuddsprosent,
            refusjon.tilskuddsgrunnlag.tilskuddsbeløp
        )

    }
}
