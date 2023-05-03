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
        val resendingsnummer: Int? = null,
        val bedriftKontonummer: String,
        val bedriftKid: String? = null)
        {
        companion object{
                @JvmStatic
                fun create(refusjon: Refusjon): RefusjonGodkjentMelding {
                        return RefusjonGodkjentMelding(refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleId, refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddsperiodeId,
                                refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tiltakstype,refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.deltakerFornavn,
                                refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.deltakerEtternavn,refusjon.deltakerFnr,
                                refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.veilederNavIdent,
                                refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.bedriftNavn,
                                refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.bedriftNr,
                                refusjon.refusjonsgrunnlag.beregning!!.refusjonsbeløp,
                                refusjon.id,
                                refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom,
                                refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddTom,
                                refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.feriepengerSats,
                                refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.otpSats,
                                refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.arbeidsgiveravgiftSats,
                                refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.lønnstilskuddsprosent,
                                refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr,
                                refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.løpenummer,
                                refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.resendingsnummer,
                                refusjon.refusjonsgrunnlag.bedriftKontonummer!!,
                                refusjon.refusjonsgrunnlag.bedriftKid
                        )
                }
        }
}