package no.nav.arbeidsgiver.tiltakrefusjon.utregning

import no.nav.arbeidsgiver.tiltakrefusjon.alleGrunnbelopMap
import no.nav.arbeidsgiver.tiltakrefusjon.enRefusjon
import no.nav.arbeidsgiver.tiltakrefusjon.etTilskuddsgrunnlag
import no.nav.arbeidsgiver.tiltakrefusjon.medBeregning
import no.nav.arbeidsgiver.tiltakrefusjon.medInntekterKunFraTiltaket
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Inntektsgrunnlag
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Inntektslinje
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Korreksjonsgrunn
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refundering
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonStatus
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Tiltakstype
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtgaarGrunn.OVER_AVTALT_TILSKUDD
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtgaarGrunn.OVER_FEM_GRUNNBELOP
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.ARBEIDSGIVERAVGIFT
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.AVTALT_BELOP
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.AVTALT_BELOP_REST_5G
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.BEREGNET_BELOP
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.BEREGNET_BELOP_ETTER_RESTTREKK
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.BRUTTOLONN_I_PERIODEN
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.FERIEPENGER
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.FERIETREKK
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.OBLIGATORISK_TJENESTEPENSJON
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.REDUKSJON_FOR_DELVIS_PERIODE
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.REFUSJONSBELOP_TIL_UTBETALING
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.REFUSJONSGRUNNLAG
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.RESTERENDE_FRATREKK_FOR_FERIE_FRA_TIDLIGERE_REFUSJONER
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.SUM_TILSKUDD_FOR_EN_MND
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.TIDLIGERE_UTBETALT
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.TILSKUDDSPROSENT
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.TIMELONN_X_TIMER
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.time.LocalDate

class UtregningTest {
    /**
     * 5G avhenger av når i året testen kjører, så datoen låses til en dag etter
     * grunnbeløpsjusteringen 2026-05-01. Da treffer alle forventede beløp.
     */
    @BeforeEach
    fun `lås dato`() {
        Now.fixedDate(LocalDate.of(2026, 6, 1))
    }

    @AfterEach
    fun `fjern låst dato`() {
        Now.resetClock()
    }

    @Test
    fun `en utregning med positivt resultat`() {
        val refusjon = refusjon(
            tiltakstype = Tiltakstype.SOMMERJOBB
        )
            .medInntekter(7777)
            .medBeregning()

        val forventetResultat = utregning(
            gruppe(
                BRUTTOLONN_I_PERIODEN tilsvarer 7777,
                FERIEPENGER leggerTil 933 ogErUtledetAv 0.12.prosent,
                OBLIGATORISK_TJENESTEPENSJON leggerTil 174 ogErUtledetAv 0.02.prosent,
                ARBEIDSGIVERAVGIFT leggerTil 1253 ogErUtledetAv 0.141.prosent
            ),
            gruppe(
                REFUSJONSGRUNNLAG erLik 10137,
                TILSKUDDSPROSENT multiplisererMed 0.4.prosent
            ),
            gruppe(
                REFUSJONSBELOP_TIL_UTBETALING erLik 4055,
            )
        )


        assertEquals(
            forventetResultat, Utregning.from(refusjon)
        )
    }

    @Test
    fun `en over tilskuddsbelop`() {
        val refusjon = refusjon(
            tilskuddsbelop = 13_579
        )
            .medInntekter(300_000)
            .medBeregning()

        val forventetResultat = utregning(
            gruppe(
                BRUTTOLONN_I_PERIODEN tilsvarer 300_000,
                FERIEPENGER leggerTil 36_000 ogErUtledetAv 0.12.prosent,
                OBLIGATORISK_TJENESTEPENSJON leggerTil 6_720 ogErUtledetAv 0.02.prosent,
                ARBEIDSGIVERAVGIFT leggerTil 48_324 ogErUtledetAv 0.141.prosent
            ),
            gruppe(
                REFUSJONSGRUNNLAG erLik 391_044,
                TILSKUDDSPROSENT multiplisererMed 0.4.prosent

            ),
            gruppe(
                BEREGNET_BELOP erLik 156_417 utgårGrunnet OVER_AVTALT_TILSKUDD,
                AVTALT_BELOP tilsvarer 13_579
            ),
            gruppe(
                REFUSJONSBELOP_TIL_UTBETALING erLik 13_579
            )
        )

        assertEquals(
            forventetResultat, Utregning.from(refusjon)
        )
    }


    @Test
    fun `en utregning med negativt resultat og manuelle utfyllinger og minusbelop`() {
        val forventetResultat = Utregning(
            negativInntekt +
                    listOf(
                        gruppe(
                            BEREGNET_BELOP erLik -17_727,
                            RESTERENDE_FRATREKK_FOR_FERIE_FRA_TIDLIGERE_REFUSJONER leggerTil -500
                        ),
                        gruppe(
                            REFUSJONSBELOP_TIL_UTBETALING erLik -18_227
                        )
                    )
        )

        val refusjon = refusjon()
            .medInntekter(7777)
            .medFerietrekk(35_000)
            .medEndretBruttolonn(1000)
            .medMinusbelop(500)
            .medBeregning()

        assertEquals(forventetResultat, Utregning.from(refusjon))
    }

    @Test
    fun `en mentoravtale uten delvis periode`() {

        val forventetResultat = utregning(
            gruppe(
                TIMELONN_X_TIMER tilsvarer 3750 ogErUtledetAv Timepris(kronerPerTime = 500, timer = 7.5),
                FERIEPENGER leggerTil 450 ogErUtledetAv 0.12.prosent,
                OBLIGATORISK_TJENESTEPENSJON leggerTil 84 ogErUtledetAv 0.02.prosent,
                ARBEIDSGIVERAVGIFT leggerTil 604 ogErUtledetAv 0.141.prosent
            ),
            gruppe(
                REFUSJONSBELOP_TIL_UTBETALING erLik 4_888
            )
        )

        val refusjon = enRefusjon(
            etTilskuddsgrunnlag(tiltakstype = Tiltakstype.MENTOR).copy(
                tilskuddsbeløp = 4888,
                mentorTimelonn = 500,
                mentorAntallTimer = 7.5
            )
        ).medBeregning()

        assertEquals(forventetResultat, Utregning.from(refusjon))
    }

    @Test
    fun `en mentoravtale med delvis periode`() {

        val forventetResultat = utregning(
            gruppe(
                TIMELONN_X_TIMER tilsvarer 3_750 ogErUtledetAv Timepris(kronerPerTime = 500, timer = 7.5),
                FERIEPENGER leggerTil 450 ogErUtledetAv 0.12.prosent,
                OBLIGATORISK_TJENESTEPENSJON leggerTil 84 ogErUtledetAv 0.02.prosent,
                ARBEIDSGIVERAVGIFT leggerTil 604 ogErUtledetAv 0.141.prosent
            ),
            gruppe(
                SUM_TILSKUDD_FOR_EN_MND erLik 4_888,
                REDUKSJON_FOR_DELVIS_PERIODE leggerTil -888
            ),
            gruppe(
                REFUSJONSBELOP_TIL_UTBETALING erLik 4_000
            )
        )

        val refusjon = enRefusjon(
            etTilskuddsgrunnlag(tiltakstype = Tiltakstype.MENTOR).copy(
                tilskuddsbeløp = 4000,
                mentorTimelonn = 500,
                mentorAntallTimer = 7.5
            )
        ).medBeregning()

        assertEquals(forventetResultat, Utregning.from(refusjon))
    }

    @Test
    fun `vtao har ikke utregning`() {
        val refusjon = enRefusjon(
            etTilskuddsgrunnlag(tiltakstype = Tiltakstype.VTAO).copy(
                tilskuddsbeløp = 4000
            )
        ).medBeregning()

        assertNull(Utregning.from(refusjon))
    }

    @Test
    fun `en korreksjonsutregning`() {
        val refusjon = refusjon(
            tilskuddsbelop = 13_579
        )
            .medInntekter(300_000)
            .medBeregning()
        refusjon.status = RefusjonStatus.UTBETALT

        val korreksjonsutkast = refusjon.opprettKorreksjonsutkast(
            setOf(Korreksjonsgrunn.DELTAKER_HAR_IKKE_VÆRT_TILSTEDE_I_PERIODEN),
            null,
            null
        )
            .medInntekter(300_000)
            .medBeregning()

        val forventetResultat = utregning(
            gruppe(
                BRUTTOLONN_I_PERIODEN tilsvarer 300_000,
                FERIEPENGER leggerTil 36_000 ogErUtledetAv 0.12.prosent,
                OBLIGATORISK_TJENESTEPENSJON leggerTil 6_720 ogErUtledetAv 0.02.prosent,
                ARBEIDSGIVERAVGIFT leggerTil 48_324 ogErUtledetAv 0.141.prosent
            ),
            gruppe(
                REFUSJONSGRUNNLAG erLik 391_044,
                TILSKUDDSPROSENT multiplisererMed 0.4.prosent

            ),
            gruppe(
                BEREGNET_BELOP erLik 156_417 utgårGrunnet OVER_AVTALT_TILSKUDD,
                AVTALT_BELOP tilsvarer 13_579,
                TIDLIGERE_UTBETALT leggerTil -13_579
            ),
            gruppe(
                REFUSJONSBELOP_TIL_UTBETALING erLik 0
            )
        )

        assertEquals(forventetResultat, Utregning.from(korreksjonsutkast))
    }

    @Test
    fun `over 5G med resterende fratrekk`() {
        val totaltUtbetaltForTiltaket = (alleGrunnbelopMap.floorEntry(Now.localDate()).component2() * 5) - 2_000
        val refusjon = refusjon(
            tiltakstype = Tiltakstype.VARIG_LONNSTILSKUDD,
            tilskuddsbelop = 55000
        )
            .medInntekter(150_000)
            .medFerietrekk(1_200)
            .medMinusbelop(5_000)
            .medTotaltUtbetaltForTiltak(totaltUtbetaltForTiltaket)
            .medBeregning()

        val forventetResultat = utregning(
            gruppe(
                BRUTTOLONN_I_PERIODEN tilsvarer 150_000,
                FERIETREKK leggerTil -1_200,
                FERIEPENGER leggerTil 17_856 ogErUtledetAv 0.12.prosent,
                OBLIGATORISK_TJENESTEPENSJON leggerTil 3_333 ogErUtledetAv 0.02.prosent,
                ARBEIDSGIVERAVGIFT leggerTil 23_968 ogErUtledetAv 0.141.prosent
            ),
            gruppe(
                REFUSJONSGRUNNLAG erLik 193_958,
                TILSKUDDSPROSENT multiplisererMed 0.4.prosent
            ),
            gruppe(
                BEREGNET_BELOP erLik 77_583 utgårGrunnet OVER_AVTALT_TILSKUDD,
                AVTALT_BELOP tilsvarer 55_000,
                RESTERENDE_FRATREKK_FOR_FERIE_FRA_TIDLIGERE_REFUSJONER leggerTil -5_000,
                BEREGNET_BELOP_ETTER_RESTTREKK erLik 50_000 utgårGrunnet OVER_FEM_GRUNNBELOP,
                AVTALT_BELOP_REST_5G tilsvarer 2_000
            ),
            gruppe(
                REFUSJONSBELOP_TIL_UTBETALING erLik 2_000
            )
        )

        assertEquals(forventetResultat, Utregning.from(refusjon))

        refusjon.status = RefusjonStatus.UTBETALT
        val korreksjonsutkast = refusjon.opprettKorreksjonsutkast(
            setOf(Korreksjonsgrunn.DELTAKER_HAR_IKKE_VÆRT_TILSTEDE_I_PERIODEN),
            null,
            null
        )
            .medInntekter(150_000)
            .medFerietrekk(1_200)
            .medTotaltUtbetaltForTiltak(totaltUtbetaltForTiltaket)
            .medBeregning()

        val forventetKorreksjonsresultat = utregning(
            gruppe(
                BRUTTOLONN_I_PERIODEN tilsvarer 150_000,
                FERIETREKK leggerTil -1_200,
                FERIEPENGER leggerTil 17_856 ogErUtledetAv 0.12.prosent,
                OBLIGATORISK_TJENESTEPENSJON leggerTil 3_333 ogErUtledetAv 0.02.prosent,
                ARBEIDSGIVERAVGIFT leggerTil 23_968 ogErUtledetAv 0.141.prosent
            ),
            gruppe(
                REFUSJONSGRUNNLAG erLik 193_958,
                TILSKUDDSPROSENT multiplisererMed 0.4.prosent
            ),
            gruppe(
                BEREGNET_BELOP erLik 77_583 utgårGrunnet OVER_AVTALT_TILSKUDD,
                AVTALT_BELOP tilsvarer 55_000,
                RESTERENDE_FRATREKK_FOR_FERIE_FRA_TIDLIGERE_REFUSJONER leggerTil -5_000,
                BEREGNET_BELOP_ETTER_RESTTREKK erLik 50_000 utgårGrunnet OVER_FEM_GRUNNBELOP,
                AVTALT_BELOP_REST_5G tilsvarer 2_000,
                TIDLIGERE_UTBETALT leggerTil -2_000
            ),
            gruppe(
                REFUSJONSBELOP_TIL_UTBETALING erLik 0
            )
        )

        assertEquals(forventetKorreksjonsresultat, Utregning.from(korreksjonsutkast))
    }

    @Test
    fun `over 5G uten resterende fratrekk`() {
        val totaltUtbetaltForTiltaket = (alleGrunnbelopMap.floorEntry(Now.localDate()).component2() * 5) - 2_000
        val refusjon = refusjon(
            tiltakstype = Tiltakstype.VARIG_LONNSTILSKUDD,
            tilskuddsbelop = 55000
        )
            .medInntekter(150_000)
            .medFerietrekk(1_200)
            .medTotaltUtbetaltForTiltak(totaltUtbetaltForTiltaket)
            .medBeregning()

        val forventetResultat = utregning(
            gruppe(
                BRUTTOLONN_I_PERIODEN tilsvarer 150_000,
                FERIETREKK leggerTil -1_200,
                FERIEPENGER leggerTil 17_856 ogErUtledetAv 0.12.prosent,
                OBLIGATORISK_TJENESTEPENSJON leggerTil 3_333 ogErUtledetAv 0.02.prosent,
                ARBEIDSGIVERAVGIFT leggerTil 23_968 ogErUtledetAv 0.141.prosent
            ),
            gruppe(
                REFUSJONSGRUNNLAG erLik 193_958,
                TILSKUDDSPROSENT multiplisererMed 0.4.prosent
            ),
            gruppe(
                BEREGNET_BELOP erLik 77_583 utgårGrunnet OVER_FEM_GRUNNBELOP,
                AVTALT_BELOP_REST_5G tilsvarer 2_000
            ),
            gruppe(
                REFUSJONSBELOP_TIL_UTBETALING erLik 2_000
            )
        )

        assertEquals(forventetResultat, Utregning.from(refusjon))

        refusjon.status = RefusjonStatus.UTBETALT
        val korreksjonsutkast = refusjon.opprettKorreksjonsutkast(
            setOf(Korreksjonsgrunn.DELTAKER_HAR_IKKE_VÆRT_TILSTEDE_I_PERIODEN),
            null,
            null
        )
            .medInntekter(150_000)
            .medFerietrekk(1_200)
            .medTotaltUtbetaltForTiltak(totaltUtbetaltForTiltaket)
            .medBeregning()

        val forventetKorreksjonsresultat = utregning(
            gruppe(
                BRUTTOLONN_I_PERIODEN tilsvarer 150_000,
                FERIETREKK leggerTil -1_200,
                FERIEPENGER leggerTil 17_856 ogErUtledetAv 0.12.prosent,
                OBLIGATORISK_TJENESTEPENSJON leggerTil 3_333 ogErUtledetAv 0.02.prosent,
                ARBEIDSGIVERAVGIFT leggerTil 23_968 ogErUtledetAv 0.141.prosent
            ),
            gruppe(
                REFUSJONSGRUNNLAG erLik 193_958,
                TILSKUDDSPROSENT multiplisererMed 0.4.prosent
            ),
            gruppe(
                BEREGNET_BELOP erLik 77_583 utgårGrunnet OVER_FEM_GRUNNBELOP,
                AVTALT_BELOP_REST_5G tilsvarer 2_000,
                TIDLIGERE_UTBETALT leggerTil -2_000
            ),
            gruppe(
                REFUSJONSBELOP_TIL_UTBETALING erLik 0
            )
        )

        assertEquals(forventetKorreksjonsresultat, Utregning.from(korreksjonsutkast))
    }
}

private fun gruppe(vararg utregningslinjer: Utregningslinje): Utregningsgruppe = utregningslinjer.toList()

private val negativInntekt = listOf(
    gruppe(
        BRUTTOLONN_I_PERIODEN tilsvarer 1000,
        FERIETREKK leggerTil -35_000,
        FERIEPENGER leggerTil -4080 ogErUtledetAv 0.12.prosent,
        OBLIGATORISK_TJENESTEPENSJON leggerTil -762 ogErUtledetAv 0.02.prosent,
        ARBEIDSGIVERAVGIFT leggerTil -5477 ogErUtledetAv 0.141.prosent
    ),
    gruppe(
        REFUSJONSGRUNNLAG erLik -44318,
        TILSKUDDSPROSENT multiplisererMed 0.4.prosent
    )
)


fun refusjon(tiltakstype: Tiltakstype = Tiltakstype.SOMMERJOBB, tilskuddsbelop: Int = 10_000): Refusjon {
    val tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
        tiltakstype = tiltakstype,
        tilskuddsbeløp = tilskuddsbelop
    )
    return enRefusjon(tilskuddsgrunnlag)
        .medInntekterKunFraTiltaket()
}


fun <T : Refundering> T.medInntekter(belop: Number): T {
    this.refusjonsgrunnlag.inntektsgrunnlag = Inntektsgrunnlag(
        inntekter = listOf(
            Inntektslinje(
                inntektType = "LOENNSINNTEKT",
                beskrivelse = "timeloenn",
                måned = Now.yearMonth(),
                beløp = belop.toDouble(),
                opptjeningsperiodeTom = null,
                opptjeningsperiodeFom = null,
                erOpptjentIPeriode = true
            )
        ),
        respons = ""
    )
    return this
}

fun <T : Refundering> T.medEndretBruttolonn(belop: Number): T {
    this.refusjonsgrunnlag.inntekterKunFraTiltaket = false
    this.refusjonsgrunnlag.endretBruttoLønn = belop.toInt()
    return this
}

fun <T : Refundering> T.medMinusbelop(belop: Number): T {
    this.refusjonsgrunnlag.forrigeRefusjonMinusBeløp = belop.toInt() * -1
    return this
}

fun <T : Refundering> T.medFerietrekk(belop: Number): T {
    this.refusjonsgrunnlag.inntektsgrunnlag?.inntekter += listOf(
        Inntektslinje(
            inntektType = "LOENNSINNTEKT",
            beskrivelse = "trekkILoennForFerie",
            måned = Now.yearMonth().minusMonths(1),
            beløp = -belop.toDouble(),
            opptjeningsperiodeTom = Now.localDate(),
            opptjeningsperiodeFom = Now.localDate(),
            erOpptjentIPeriode = true
        )
    )
    return this
}

fun <T : Refundering> T.medTotaltUtbetaltForTiltak(belop: Number): T {
    this.refusjonsgrunnlag.sumUtbetaltVarig = belop.toInt()
    return this
}
