package no.nav.arbeidsgiver.tiltakrefusjon.utregning

import no.nav.arbeidsgiver.tiltakrefusjon.alleGrunnbelopMap
import no.nav.arbeidsgiver.tiltakrefusjon.enBeregningskontekst
import no.nav.arbeidsgiver.tiltakrefusjon.enRefusjon
import no.nav.arbeidsgiver.tiltakrefusjon.etInntektsgrunnlag
import no.nav.arbeidsgiver.tiltakrefusjon.etTilskuddsgrunnlag
import no.nav.arbeidsgiver.tiltakrefusjon.medInntektsgrunnlag
import no.nav.arbeidsgiver.tiltakrefusjon.minusbelop5Gsen
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Inntektsgrunnlag
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Inntektslinje
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Korreksjonsgrunn
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refundering
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonStatus
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Tiltakstype
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.beregn
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
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
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.REFUSJONSBELØP_TIL_UTBETALING
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.REFUSJONSGRUNNLAG
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.RESTERENDE_FRATREKK_FOR_FERIE_FRA_TIDLIGERE_REFUSJONER
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.SUM_TILSKUDD_FOR_EN_MND
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.TIDLIGERE_UTBETALT
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.TILSKUDDSPROSENT
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.TIMELONN_X_TIMER
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull

class UtregningTest {
    @Test
    fun `en utregning med positivt resultat`() {
        val refusjon = enRefusjon(etTilskuddsgrunnlag(tiltakstype = Tiltakstype.SOMMERJOBB))
            .medInntektsgrunnlag(Now.yearMonth(), etInntektsgrunnlag(Now.yearMonth(), true))
        refusjon.refusjonsgrunnlag.inntekterKunFraTiltaket = true
        refusjon.refusjonsgrunnlag.beregning =
            beregn(
                enBeregningskontekst(), refusjon
            )

        val forventetResultat = utregning(
            gruppe(
                BRUTTOLONN_I_PERIODEN tilsvarer 7777.kroner,
                FERIEPENGER medSats 0.12.prosent pluss 933.kroner,
                OBLIGATORISK_TJENESTEPENSJON medSats 0.02.prosent pluss 174.kroner,
                ARBEIDSGIVERAVGIFT medSats 0.141.prosent pluss 1253.kroner
            ),
            gruppe(
                REFUSJONSGRUNNLAG erLik 10137.kroner,
                TILSKUDDSPROSENT multiplisert 0.4.prosent
            ),
            gruppe(
                REFUSJONSBELØP_TIL_UTBETALING erLik 4055.kroner,
            )
        )


        assertEquals(
            forventetResultat, Utregning.from(refusjon)
        )
    }

    @Test
    fun `en over tilskuddsbelop`() {
        val refusjon = enRefusjon(etTilskuddsgrunnlag(tiltakstype = Tiltakstype.SOMMERJOBB))
            .medInntektsgrunnlag(
                Now.yearMonth(), Inntektsgrunnlag(
                    inntekter = listOf(
                        Inntektslinje(
                            inntektType = "LOENNSINNTEKT",
                            beskrivelse = "timeloenn",
                            måned = Now.yearMonth(),
                            beløp = 300000.0,
                            opptjeningsperiodeTom = null,
                            opptjeningsperiodeFom = null,
                            erOpptjentIPeriode = true
                        )
                    ),
                    respons = ""
                )
            )
        refusjon.refusjonsgrunnlag.inntekterKunFraTiltaket = true
        refusjon.refusjonsgrunnlag.beregning =
            beregn(
                enBeregningskontekst(), refusjon
            )

        val forventetResultat = utregning(
            gruppe(
                BRUTTOLONN_I_PERIODEN tilsvarer 300_000.kroner,
                FERIEPENGER medSats 0.12.prosent pluss 36_000.kroner,
                OBLIGATORISK_TJENESTEPENSJON medSats 0.02.prosent pluss 6_720.kroner,
                ARBEIDSGIVERAVGIFT medSats 0.141.prosent pluss 48_324.kroner
            ),
            gruppe(
                REFUSJONSGRUNNLAG erLik 391_044.kroner,
                TILSKUDDSPROSENT multiplisert 0.4.prosent

            ),
            gruppe(
                (BEREGNET_BELOP erLik 156_417.kroner).apply { this.utgårHvis(true) },
                AVTALT_BELOP tilsvarer 13_579.kroner
            ),
            gruppe(
                REFUSJONSBELØP_TIL_UTBETALING erLik 13_579.kroner
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
                            BEREGNET_BELOP erLik (-17727).kroner,
                            RESTERENDE_FRATREKK_FOR_FERIE_FRA_TIDLIGERE_REFUSJONER minus 500.kroner
                        ),
                        gruppe(
                            REFUSJONSBELØP_TIL_UTBETALING erLik (-18227).kroner
                        )
                    )
        )

        val refusjon = enRefusjon(etTilskuddsgrunnlag(tiltakstype = Tiltakstype.SOMMERJOBB))
            .medInntektsgrunnlag(Now.yearMonth(), etInntektsgrunnlag(Now.yearMonth(), true))
            .medFerietrekk()
        refusjon.refusjonsgrunnlag.inntekterKunFraTiltaket = false
        refusjon.refusjonsgrunnlag.endretBruttoLønn = 1000
        refusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp = -500
        refusjon.refusjonsgrunnlag.beregning =
            beregn(
                enBeregningskontekst(), refusjon
            )

        assertEquals(forventetResultat, Utregning.from(refusjon))
    }

    @Test
    fun `en mentoravtale uten delvis periode`() {

        val forventetResultat = utregning(
            gruppe(
                TIMELONN_X_TIMER medSats Timelonn(7.5, 500) tilsvarer 3750.kroner,
                FERIEPENGER medSats 0.12.prosent pluss 450.kroner,
                OBLIGATORISK_TJENESTEPENSJON medSats 0.02.prosent pluss 84.kroner,
                ARBEIDSGIVERAVGIFT medSats 0.141.prosent pluss 604.kroner
            ),
            gruppe(
                REFUSJONSBELØP_TIL_UTBETALING erLik 4_888.kroner
            )
        )

        val refusjon = enRefusjon(
            etTilskuddsgrunnlag(tiltakstype = Tiltakstype.MENTOR).copy(
                tilskuddsbeløp = 4888,
                mentorTimelonn = 500,
                mentorAntallTimer = 7.5
            )
        )
        refusjon.refusjonsgrunnlag.beregning =
            beregn(enBeregningskontekst(), refusjon)

        assertEquals(forventetResultat, Utregning.from(refusjon))
    }

    @Test
    fun `en mentoravtale med delvis periode`() {

        val forventetResultat = utregning(
            gruppe(
                TIMELONN_X_TIMER medSats Timelonn(7.5, 500) tilsvarer 3750.kroner,
                FERIEPENGER medSats 0.12.prosent pluss 450.kroner,
                OBLIGATORISK_TJENESTEPENSJON medSats 0.02.prosent pluss 84.kroner,
                ARBEIDSGIVERAVGIFT medSats 0.141.prosent pluss 604.kroner
            ),
            gruppe(
                SUM_TILSKUDD_FOR_EN_MND erLik 4888.kroner,
                REDUKSJON_FOR_DELVIS_PERIODE minus 888.kroner
            ),
            gruppe(
                REFUSJONSBELØP_TIL_UTBETALING erLik 4000.kroner
            )
        )

        val refusjon = enRefusjon(
            etTilskuddsgrunnlag(tiltakstype = Tiltakstype.MENTOR).copy(
                tilskuddsbeløp = 4000,
                mentorTimelonn = 500,
                mentorAntallTimer = 7.5
            )
        )
        refusjon.refusjonsgrunnlag.beregning =
            beregn(enBeregningskontekst(), refusjon)

        assertEquals(forventetResultat, Utregning.from(refusjon))
    }

    @Test
    fun `vtao har ikke utregning`() {
        val refusjon = enRefusjon(
            etTilskuddsgrunnlag(tiltakstype = Tiltakstype.VTAO).copy(
                tilskuddsbeløp = 4000
            )
        )
        refusjon.refusjonsgrunnlag.beregning =
            beregn(enBeregningskontekst(), refusjon)

        assertNull(Utregning.from(refusjon))
    }

    @Test
    fun `en korreksjonsutregning`() {
        val mnd = Now.yearMonth()
        val inntektsgrunnlag = Inntektsgrunnlag(
            inntekter = listOf(
                Inntektslinje(
                    inntektType = "LOENNSINNTEKT",
                    beskrivelse = "timeloenn",
                    måned = Now.yearMonth(),
                    beløp = 300000.0,
                    opptjeningsperiodeTom = null,
                    opptjeningsperiodeFom = null,
                    erOpptjentIPeriode = true
                )
            ),
            respons = ""
        )
        val refusjon = enRefusjon(etTilskuddsgrunnlag(tiltakstype = Tiltakstype.SOMMERJOBB))
            .medInntektsgrunnlag(mnd, inntektsgrunnlag)
        refusjon.refusjonsgrunnlag.inntekterKunFraTiltaket = true
        refusjon.refusjonsgrunnlag.beregning =
            beregn(
                enBeregningskontekst(), refusjon
            )
        refusjon.status = RefusjonStatus.UTBETALT

        val korreksjonsutkast = refusjon.opprettKorreksjonsutkast(
            setOf(Korreksjonsgrunn.DELTAKER_HAR_IKKE_VÆRT_TILSTEDE_I_PERIODEN),
            null,
            0,
            null
        ).medInntektsgrunnlag(mnd, inntektsgrunnlag)

        korreksjonsutkast.refusjonsgrunnlag.beregning = beregn(enBeregningskontekst(), korreksjonsutkast)

        val forventetResultat = utregning(
            gruppe(
                BRUTTOLONN_I_PERIODEN tilsvarer 300_000.kroner,
                FERIEPENGER medSats 0.12.prosent pluss 36_000.kroner,
                OBLIGATORISK_TJENESTEPENSJON medSats 0.02.prosent pluss 6_720.kroner,
                ARBEIDSGIVERAVGIFT medSats 0.141.prosent pluss 48_324.kroner
            ),
            gruppe(
                REFUSJONSGRUNNLAG erLik 391_044.kroner,
                TILSKUDDSPROSENT multiplisert 0.4.prosent

            ),
            gruppe(
                (BEREGNET_BELOP erLik 156_417.kroner).apply { this.utgårHvis(true) },
                AVTALT_BELOP tilsvarer 13_579.kroner,
                TIDLIGERE_UTBETALT minus 13_579.kroner
            ),
            gruppe(
                REFUSJONSBELØP_TIL_UTBETALING erLik 0.kroner
            )
        )

        assertEquals(forventetResultat, Utregning.from(korreksjonsutkast))
    }

    @Test
    fun testMedResterendeFratrekkOg5G() {
        val mnd = Now.yearMonth()
        val inntektsgrunnlag = Inntektsgrunnlag(
            inntekter = listOf(
                Inntektslinje(
                    inntektType = "LOENNSINNTEKT",
                    beskrivelse = "timeloenn",
                    måned = Now.yearMonth(),
                    beløp = 22_423.0,
                    opptjeningsperiodeTom = null,
                    opptjeningsperiodeFom = null,
                    erOpptjentIPeriode = true
                ),
                Inntektslinje(
                    inntektType = "LOENNSINNTEKT",
                    beskrivelse = "trekkILoennForFerie",
                    måned = Now.yearMonth().minusMonths(1),
                    beløp = -1_200.0,
                    opptjeningsperiodeTom = null,
                    opptjeningsperiodeFom = null,
                    erOpptjentIPeriode = true
                )
            ),
            respons = ""
        )

        val refusjon = minusbelop5Gsen().medInntektsgrunnlag(mnd, inntektsgrunnlag)
        refusjon.refusjonsgrunnlag.inntekterKunFraTiltaket = true
        refusjon.refusjonsgrunnlag.beregning =
            beregn(
                enBeregningskontekst(), refusjon
            )

        val forventetResultat = utregning(
            gruppe(
                BRUTTOLONN_I_PERIODEN tilsvarer 22_423.kroner,
                FERIETREKK minus 1_200.kroner,
                FERIEPENGER medSats 0.12.prosent pluss 2_547.kroner,
                OBLIGATORISK_TJENESTEPENSJON medSats 0.02.prosent pluss 475.kroner,
                ARBEIDSGIVERAVGIFT medSats 0.141.prosent pluss 3_419.kroner
            ),
            gruppe(
                REFUSJONSGRUNNLAG erLik 27_664.kroner,
                TILSKUDDSPROSENT multiplisert 0.4.prosent
            ),
            gruppe(
                BEREGNET_BELOP erLik 11_065.kroner,
                RESTERENDE_FRATREKK_FOR_FERIE_FRA_TIDLIGERE_REFUSJONER minus 5_000.kroner,
                (BEREGNET_BELOP_ETTER_RESTTREKK erLik 6_065.kroner).apply {utgår = true },
                AVTALT_BELOP_REST_5G tilsvarer 2_000.kroner
            ),
            gruppe(
                REFUSJONSBELØP_TIL_UTBETALING erLik 2_000.kroner
            )
        )

        assertEquals(forventetResultat, Utregning.from(refusjon))

        refusjon.status = RefusjonStatus.UTBETALT
        val korreksjonsutkast = refusjon.opprettKorreksjonsutkast(
            setOf(Korreksjonsgrunn.DELTAKER_HAR_IKKE_VÆRT_TILSTEDE_I_PERIODEN),
            null,
            refusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp,
            null
        ).medInntektsgrunnlag(mnd, inntektsgrunnlag)

        korreksjonsutkast.refusjonsgrunnlag.sumUtbetaltVarig = (alleGrunnbelopMap.floorEntry(Now.localDate()).component2() * 5) - 2_000
        korreksjonsutkast.refusjonsgrunnlag.beregning = beregn(enBeregningskontekst(), korreksjonsutkast)

        val forventetKorreksjonsresultat = utregning(
            gruppe(
                BRUTTOLONN_I_PERIODEN tilsvarer 22_423.kroner,
                FERIETREKK minus 1_200.kroner,
                FERIEPENGER medSats 0.12.prosent pluss 2_547.kroner,
                OBLIGATORISK_TJENESTEPENSJON medSats 0.02.prosent pluss 475.kroner,
                ARBEIDSGIVERAVGIFT medSats 0.141.prosent pluss 3_419.kroner
            ),
            gruppe(
                REFUSJONSGRUNNLAG erLik 27_664.kroner,
                TILSKUDDSPROSENT multiplisert 0.4.prosent
            ),
            gruppe(
                BEREGNET_BELOP erLik 11_065.kroner,
                RESTERENDE_FRATREKK_FOR_FERIE_FRA_TIDLIGERE_REFUSJONER minus 5_000.kroner,
                (BEREGNET_BELOP_ETTER_RESTTREKK erLik 6_065.kroner).apply {utgår = true },
                AVTALT_BELOP_REST_5G tilsvarer 2_000.kroner,
                TIDLIGERE_UTBETALT minus 2_000.kroner
            ),
            gruppe(
                REFUSJONSBELØP_TIL_UTBETALING erLik 0.kroner
            )
        )

        assertEquals(forventetKorreksjonsresultat, Utregning.from(korreksjonsutkast))
    }
}

private fun gruppe(vararg utregningslinjer: Utregningslinje) = Utregningsgruppe(utregningslinjer.toList())

private fun utregning(vararg grupper: Utregningsgruppe): Utregning = Utregning(
    grupper.toList()
)

private infix operator fun UtregningsradType.minus(kroner: Kroner): Utregningslinje =
    Utregningslinje(this, kroner, Fortegn.MINUS)

private infix fun UtregningsradType.multiplisert(prosent: Prosent): Utregningslinje =
    Utregningslinje(this, prosent, fortegn = Fortegn.MULTIPLISER)

private infix fun Utregningslinje.pluss(kroner: Kroner): Utregningslinje =
    this.copy(fortegn = Fortegn.PLUSS, verdi = kroner)

private infix fun UtregningsradType.medSats(prosent: Verdi) = Utregningslinje(this, 0.kroner, sats = prosent)

private infix fun Utregningslinje.tilsvarer(kroner: Kroner) = this.copy(verdi = kroner)

val negativInntekt = listOf(
    Utregningsgruppe(
        listOf(
            Utregningslinje(BRUTTOLONN_I_PERIODEN, 1000.kroner),
            Utregningslinje(FERIETREKK, 35000.kroner, Fortegn.MINUS),
            Utregningslinje(
                FERIEPENGER,
                4080.kroner,
                Fortegn.MINUS
            ).medSats(0.12.prosent),
            Utregningslinje(
                OBLIGATORISK_TJENESTEPENSJON,
                762.kroner,
                Fortegn.MINUS
            ).medSats(0.02.prosent),
            Utregningslinje(ARBEIDSGIVERAVGIFT, 5477.kroner, Fortegn.MINUS).medSats(
                0.141.prosent
            )
        )
    ), Utregningsgruppe(
        listOf(
            Utregningslinje(REFUSJONSGRUNNLAG, (-44318).kroner, Fortegn.ER_LIK),
            Utregningslinje(
                TILSKUDDSPROSENT,
                verdi = 0.4.prosent,
                fortegn = Fortegn.MULTIPLISER
            )
        )
    )
)

fun Refundering.medFerietrekk(): Refundering {
    this.refusjonsgrunnlag.inntektsgrunnlag?.inntekter += listOf(
        Inntektslinje(
            inntektType = "LOENNSINNTEKT",
            beskrivelse = "trekkILoennForFerie",
            måned = Now.yearMonth().minusMonths(1),
            beløp = -35000.0,
            opptjeningsperiodeTom = Now.localDate(),
            opptjeningsperiodeFom = Now.localDate(),
            erOpptjentIPeriode = true
        )
    )
    return this
}
