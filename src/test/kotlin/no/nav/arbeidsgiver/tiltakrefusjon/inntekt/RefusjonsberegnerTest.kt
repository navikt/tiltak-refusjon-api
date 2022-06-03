package no.nav.arbeidsgiver.tiltakrefusjon.inntekt

import com.github.guepardoapps.kulid.ULID
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class RefusjonsberegnerTest {

    lateinit var juni: Inntektslinje
    lateinit var juli: Inntektslinje
    lateinit var inntektsliste: List<Inntektslinje>
    lateinit var inntektsgrunnlag: Inntektsgrunnlag

    @BeforeEach
    fun init() {
        juni =
            lagEnInntektslinje(20000.00, YearMonth.of(2021, 7), LocalDate.of(2021, 6, 1), LocalDate.of(2021, 6, 30))
        juli =
            lagEnInntektslinje(20000.00, YearMonth.of(2021, 7), LocalDate.of(2021, 7, 1), LocalDate.of(2021, 7, 31))
        inntektsliste = listOf(juni, juli)
        inntektsgrunnlag = Inntektsgrunnlag(inntektsliste, "repons fra Inntekt")
    }

    fun lagEtTilskuddsgrunnlag(
        tilskuddFom: LocalDate,
        tilskuddTom: LocalDate,
        tiltakstype: Tiltakstype,
        tilskuddsbeløp: Int,
    ): Tilskuddsgrunnlag {
        val tilskuddsgrunnlag = Tilskuddsgrunnlag(
            avtaleId = ULID.random(),
            tilskuddsperiodeId = ULID.random(),
            deltakerFornavn = "",
            deltakerEtternavn = "",
            tiltakstype = tiltakstype,
            deltakerFnr = "",
            veilederNavIdent = "",
            bedriftNavn = "Kiwi Majorstuen",
            bedriftNr = "",
            otpSats = 0.02,
            feriepengerSats = 0.12,
            arbeidsgiveravgiftSats = 0.141,
            lønnstilskuddsprosent = 40,
            tilskuddFom = tilskuddFom,
            tilskuddTom = tilskuddTom,
            tilskuddsbeløp = tilskuddsbeløp,
            avtaleNr = 3456,
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

    @Test
    fun `beregning av sommerjobb, skal ikke beregne på dagsats`() {
        val tilskuddsgrunnlagSommerJobb = lagEtTilskuddsgrunnlag(
            LocalDate.of(2021, 6, 1),
            LocalDate.of(2021, 7, 16),
            Tiltakstype.SOMMERJOBB,
            40000
        )
        val beregning = beregnRefusjonsbeløp(
            inntektsgrunnlag.inntekter.toList(),
            tilskuddsgrunnlagSommerJobb,
            0,
            null,
            null
        )
        val beregnetBeløpHeleInntektsgrunnlaget = 20856
        assertThat(beregning.refusjonsbeløp).isEqualTo(beregnetBeløpHeleInntektsgrunnlaget)
    }

    @Test
    @Disabled("Finne ut hvordan lønnstilskudd skal beregnes")
    fun `beregning av lønnstilskudd, skal beregne på dagsats`() {
        val tilskuddsgrunnlagSommerJobb = lagEtTilskuddsgrunnlag(
            LocalDate.of(2021, 6, 1),
            LocalDate.of(2021, 7, 16),
            Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            40000
        )
        val beregning = beregnRefusjonsbeløp(
            inntektsgrunnlag.inntekter.toList(),
            tilskuddsgrunnlagSommerJobb,
            0,
            null
        )
        val beregnetBeløpAvAntallDagerJobbetInnenforInntektsgrunnlaget = 15810
        assertThat(beregning.refusjonsbeløp).isEqualTo(beregnetBeløpAvAntallDagerJobbetInnenforInntektsgrunnlaget)
    }
}