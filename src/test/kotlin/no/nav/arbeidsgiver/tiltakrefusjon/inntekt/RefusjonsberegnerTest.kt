package no.nav.arbeidsgiver.tiltakrefusjon.inntekt

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.*
import no.nav.arbeidsgiver.tiltakrefusjon.utils.ulid
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class RefusjonsberegnerTest {

    lateinit var juni: Inntektslinje
    lateinit var juli: Inntektslinje
    lateinit var september: Inntektslinje
    lateinit var enInntektslinje: Inntektsgrunnlag
    lateinit var juniUregelmessig: Inntektslinje
    lateinit var inntektsliste: List<Inntektslinje>
    lateinit var inntektsgrunnlag: Inntektsgrunnlag
    lateinit var inntektsgrunnlagUregelmessig: Inntektsgrunnlag

    @BeforeEach
    fun init() {
        juni =
            lagEnInntektslinje(20000.00, YearMonth.of(2023, 7), LocalDate.of(2023, 6, 1), LocalDate.of(2023, 6, 30))
        juli =
            lagEnInntektslinje(20000.00, YearMonth.of(2023, 7), LocalDate.of(2023, 7, 1), LocalDate.of(2023, 7, 31))
        september =
            lagEnInntektslinje(16666.00, YearMonth.of(2023, 9), LocalDate.of(2023, 9, 1), LocalDate.of(2023, 9, 30))

        enInntektslinje = Inntektsgrunnlag(listOf(september), "repons fra Inntekt")

        inntektsliste = listOf(juni, juli)
        inntektsgrunnlag = Inntektsgrunnlag(inntektsliste, "repons fra Inntekt")

        juniUregelmessig =
            lagEnInntektslinjeUregelmessigeTillegg(20000.00, YearMonth.of(2023, 7), LocalDate.of(2023, 6, 1), LocalDate.of(2023, 6, 30))
        inntektsgrunnlagUregelmessig = Inntektsgrunnlag(listOf(juniUregelmessig, juni), "repons fra Inntekt")
    }

    fun lagEtTilskuddsgrunnlag(
        tilskuddFom: LocalDate,
        tilskuddTom: LocalDate,
        tiltakstype: Tiltakstype,
        tilskuddsbeløp: Int,
    ): Tilskuddsgrunnlag {
        val tilskuddsgrunnlag = Tilskuddsgrunnlag(
            avtaleId = ulid(),
            tilskuddsperiodeId = ulid(),
            deltakerFornavn = "",
            deltakerEtternavn = "",
            arbeidsgiverFornavn = "Arne",
            arbeidsgiverEtternavn = "Arbeidsgiver",
            arbeidsgiverTlf = "41111111",
            tiltakstype = tiltakstype,
            deltakerFnr = "",
            veilederNavIdent = "",
            bedriftNavn = "Kiwi Majorstuen",
            bedriftNr = "",
            otpSats = 0.02,
            feriepengerSats = 0.102,
            arbeidsgiveravgiftSats = 0.141,
            lønnstilskuddsprosent = 40,
            tilskuddFom = tilskuddFom,
            tilskuddTom = tilskuddTom,
            tilskuddsbeløp = tilskuddsbeløp,
            avtaleNr = 3456,
            resendingsnummer = null,
            løpenummer = 3,
            enhet = "1000",
            godkjentAvBeslutterTidspunkt = LocalDateTime.of(tilskuddTom.year, tilskuddTom.month, tilskuddTom.dayOfMonth, 0, 0 )
        )
        return tilskuddsgrunnlag
    }

    private fun lagEnInntektslinje(
        beløp: Double,
        måned: YearMonth,
        opptjeningsperiodeFom: LocalDate,
        opptjeningsperiodeTom: LocalDate,
    ): Inntektslinje {
        return Inntektslinje(
            inntektType = "LOENNSINNTEKT",
            beskrivelse = "fastloenn",
            beløp = beløp,
            måned = måned,
            opptjeningsperiodeFom = opptjeningsperiodeFom,
            opptjeningsperiodeTom = opptjeningsperiodeTom,
            erOpptjentIPeriode = true
        )
    }

    private fun lagEnInntektslinjeUregelmessigeTillegg(
        beløp: Double,
        måned: YearMonth,
        opptjeningsperiodeFom: LocalDate,
        opptjeningsperiodeTom: LocalDate,
    ): Inntektslinje {
        return Inntektslinje(
            inntektType = "LOENNSINNTEKT",
            beskrivelse = "uregelmessigeTilleggKnyttetTilArbeidetTid",
            beløp = beløp,
            måned = måned,
            opptjeningsperiodeFom = opptjeningsperiodeFom,
            opptjeningsperiodeTom = opptjeningsperiodeTom,
            erOpptjentIPeriode = true
        )
    }

    @Test
    fun `beregning av sommerjobb, skal ikke beregne på dagsats`() {
        val tilskuddsgrunnlagSommerJobb = lagEtTilskuddsgrunnlag(
            LocalDate.of(2023, 6, 1),
            LocalDate.of(2023, 7, 16),
            Tiltakstype.SOMMERJOBB,
            40000
        )
        val beregning = beregnRefusjonsbeløp(
            inntektsgrunnlag.inntekter.toList(),
            tilskuddsgrunnlagSommerJobb,
            0,
            null,
            null,
            tilskuddFom = LocalDate.of(2023,6,1),
            harFerietrekkForSammeMåned = false
        )
        val beregnetBeløpHeleInntektsgrunnlaget = 20520
        assertThat(beregning.refusjonsbeløp).isEqualTo(beregnetBeløpHeleInntektsgrunnlaget)
    }

    @Test
    fun `beregning av lønnstilskudd, skal beregne på dagsats`() {
        val tilskuddsgrunnlagLønnstilskudd = lagEtTilskuddsgrunnlag(
            LocalDate.of(2023, 6, 1),
            LocalDate.of(2023, 6, 30),
            Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            40000
        )
        val beregning = beregnRefusjonsbeløp(
            inntektsgrunnlagUregelmessig.inntekter.toList(),
            tilskuddsgrunnlagLønnstilskudd,
            0,
            null,
            tilskuddFom = LocalDate.of(2023,6,1),
            harFerietrekkForSammeMåned = false
        )
        val beregnetBeløpAvAntallDagerJobbetInnenforInntektsgrunnlaget = 20520
        assertThat(beregning.refusjonsbeløp).isEqualTo(beregnetBeløpAvAntallDagerJobbetInnenforInntektsgrunnlaget)
    }

    @Test
    fun `varig lønnstilskudd, sum over 5g skal capes`() {
        // Seks perioder med 100 000.
        val tilskuddsgrunnlagLønnstilskudd = lagEtTilskuddsgrunnlag(
            LocalDate.of(2023, 6, 1),
            LocalDate.of(2023, 6, 30),
            Tiltakstype.VARIG_LONNSTILSKUDD,
            100000
        )
        val beregning = beregnRefusjonsbeløp(
            inntektsgrunnlagUregelmessig.inntekter.toList(),
            tilskuddsgrunnlagLønnstilskudd,
            0,
            null,
            tilskuddFom = LocalDate.of(2023,6,1),
            sumUtbetaltVarig = 590000,
            harFerietrekkForSammeMåned = false
        )
        val beregning2 = beregnRefusjonsbeløp(
            inntektsgrunnlagUregelmessig.inntekter.toList(),
            tilskuddsgrunnlagLønnstilskudd.copy(tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD),
            0,
            null,
            tilskuddFom = LocalDate.of(2023,6,1),
            sumUtbetaltVarig = 590000,
            harFerietrekkForSammeMåned = false
        )

        // Beregning uten 5G-sjekk skal gi et refusjonsbeløp på 20856
        // Med 590000 allerede utbetalt så vil dette være over tilgjengelig sum
        assertThat(beregning.refusjonsbeløp).isEqualTo(3100)
        assertThat(beregning2.refusjonsbeløp).isEqualTo(20520)

    }

    @Test
    fun `Beregning_av_refusjongrunnlag_sumUtgifter`(){
        val tilskuddsgrunnlagLønnstilskudd = lagEtTilskuddsgrunnlag(
            LocalDate.of(2023, 9, 1),
            LocalDate.of(2023, 9, 30),
            Tiltakstype.VARIG_LONNSTILSKUDD,
            10000
        )

        val beregning = beregnRefusjonsbeløp(
            enInntektslinje.inntekter.toList(),
            tilskuddsgrunnlagLønnstilskudd,
            0,
            null,
            tilskuddFom = LocalDate.of(2023,9,1),
            sumUtbetaltVarig = 16666,
            harFerietrekkForSammeMåned = false
        )

        assertThat(beregning.sumUtgifter).isEqualTo(21375)

    }

    @Test
    fun `desimaler fører ikke til at beregning er overTilskuddsbeløp`(){
        val tilskuddsgrunnlagLønnstilskudd = lagEtTilskuddsgrunnlag(
            LocalDate.of(2023, 9, 1),
            LocalDate.of(2023, 9, 30),
            Tiltakstype.VARIG_LONNSTILSKUDD,
            10000
        )

        val inntektslinje = lagEnInntektslinje(19493.00, YearMonth.of(2023, 9), LocalDate.of(2023, 9, 1), LocalDate.of(2023, 9, 30))

        val beregning = beregnRefusjonsbeløp(
            listOf(inntektslinje),
            tilskuddsgrunnlagLønnstilskudd,
            0,
            null,
            tilskuddFom = LocalDate.of(2023,9,1),
            sumUtbetaltVarig = 16666,
            harFerietrekkForSammeMåned = false
        )

        assertThat(beregning.refusjonsbeløp)
            .isEqualTo(tilskuddsgrunnlagLønnstilskudd.tilskuddsbeløp)
            .describedAs("Refusjonsbeløp og tilskuddsbeløp er like")
        assertThat(beregning.overTilskuddsbeløp)
            .isEqualTo(false)
            .describedAs("Hvis beløpene er like, kan ikke overTilskuddsbeløp være sann. Vil isåfall bety at vi inkluderer desimaler i beregningen")

    }
}
