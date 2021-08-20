package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import java.time.LocalDate

data class RefusjonGodkjentMelding(
        val avtaleId: String,
        val tilskuddsperiodeId: String,
        val tiltakstype: Tiltakstype,
        val deltakerFornavn: String,
        val deltakerEtternavn: String,
        val deltakerFnr: String,
        val veilederNavIdent: String,
        val bedriftNavn: String,
        val bedriftNr: String,
        val beløp: Int,
        val refusjonId: String,
        val tilskuddFom: LocalDate,
        val tilskuddTom: LocalDate,
        val feriepengerSats: Double,
        val otpSats: Double,
        val arbeidsgiveravgiftSats: Double,
        val tilskuddsprosent: Int,
        val avtaleNr: Int,
        val løpenummer: Int,
        val bedriftKontonummer: String)
        {
        companion object{
                @JvmStatic
                fun create(refusjon: Refusjon): RefusjonGodkjentMelding {
                        return RefusjonGodkjentMelding(refusjon.tilskuddsgrunnlag.avtaleId, refusjon.tilskuddsgrunnlag.tilskuddsperiodeId,
                                refusjon.tilskuddsgrunnlag.tiltakstype,refusjon.tilskuddsgrunnlag.deltakerFornavn,
                                refusjon.tilskuddsgrunnlag.deltakerEtternavn,refusjon.deltakerFnr,
                                refusjon.tilskuddsgrunnlag.veilederNavIdent,
                                refusjon.tilskuddsgrunnlag.bedriftNavn,
                                refusjon.tilskuddsgrunnlag.bedriftNr,
                                refusjon.beregning!!.refusjonsbeløp,
                                refusjon.id,
                                refusjon.tilskuddsgrunnlag.tilskuddFom,
                                refusjon.tilskuddsgrunnlag.tilskuddTom,
                                refusjon.tilskuddsgrunnlag.feriepengerSats,
                                refusjon.tilskuddsgrunnlag.otpSats,
                                refusjon.tilskuddsgrunnlag.arbeidsgiveravgiftSats,
                                refusjon.tilskuddsgrunnlag.lønnstilskuddsprosent,
                                refusjon.tilskuddsgrunnlag.avtaleNr,
                                refusjon.tilskuddsgrunnlag.løpenummer,
                                refusjon.bedriftKontonummer!!
                        )
                }
        }
}