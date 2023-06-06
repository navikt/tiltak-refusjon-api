package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.etTilskuddsgrunnlag
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.YearMonth

class RefusjonsgrunnlagTest {

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun oppgiInntektsgrunnlagBeholdGamleID() {
        val originalInntektslinje1 = Inntektslinje(
            inntektType = "LOENNSINNTEKT",
            beskrivelse = null,
            beløp = 25000.0,
            måned = YearMonth.now(),
            opptjeningsperiodeFom = Now.localDate(),
            opptjeningsperiodeTom = Now.localDate().plusDays(30)
        )
        val originalInntektslinje2 = Inntektslinje(
            inntektType = "LOENNSINNTEKT",
            beskrivelse = null,
            beløp = -25000.0,
            måned = YearMonth.now(),
            opptjeningsperiodeFom = Now.localDate(),
            opptjeningsperiodeTom = Now.localDate().plusDays(30)
        )

        val originalRefusjonsgrunnlag = Refusjonsgrunnlag(etTilskuddsgrunnlag())
        val originalInntektsgrunnlag =  Inntektsgrunnlag(listOf(originalInntektslinje1,originalInntektslinje2),"JSON STRING")
        originalRefusjonsgrunnlag.inntektsgrunnlag = originalInntektsgrunnlag

        val inntektslinje1AndreGangKall = Inntektslinje(
            inntektType = "LOENNSINNTEKT",
            beskrivelse = null,
            beløp = 25000.0,
            måned = YearMonth.now(),
            opptjeningsperiodeFom = Now.localDate(),
            opptjeningsperiodeTom = Now.localDate().plusDays(30)
        )
        val inntektslinje2AndreGangKall = Inntektslinje(
            inntektType = "LOENNSINNTEKT",
            beskrivelse = null,
            beløp = -25000.0,
            måned = YearMonth.now(),
            opptjeningsperiodeFom = Now.localDate(),
            opptjeningsperiodeTom = Now.localDate().plusDays(30)
        )


        val nyInntektsgrunnlag =  Inntektsgrunnlag(listOf(inntektslinje1AndreGangKall,inntektslinje2AndreGangKall),"JSON STRING")
        originalRefusjonsgrunnlag.oppgiInntektsgrunnlag(nyInntektsgrunnlag,originalRefusjonsgrunnlag.inntektsgrunnlag)
        assertEquals(originalInntektslinje1.id,originalRefusjonsgrunnlag.inntektsgrunnlag?.inntekter?.first { it.id == originalInntektslinje1.id }?.id )
        assertEquals(originalInntektslinje2.id,originalRefusjonsgrunnlag.inntektsgrunnlag?.inntekter?.first { it.id == originalInntektslinje2.id }?.id )
    }

    @Test
    fun oppgiInntektsgrunnlagFjernEldreOmEldreErSlettet() {
        val originalInntektslinje1 = Inntektslinje(
            inntektType = "LOENNSINNTEKT",
            beskrivelse = null,
            beløp = 25000.0,
            måned = YearMonth.now(),
            opptjeningsperiodeFom = Now.localDate(),
            opptjeningsperiodeTom = Now.localDate().plusDays(30)
        )
        val originalInntektslinje2 = Inntektslinje(
            inntektType = "LOENNSINNTEKT",
            beskrivelse = null,
            beløp = -25000.0,
            måned = YearMonth.now(),
            opptjeningsperiodeFom = Now.localDate(),
            opptjeningsperiodeTom = Now.localDate().plusDays(30)
        )

        val originalRefusjonsgrunnlag = Refusjonsgrunnlag(etTilskuddsgrunnlag())
        val originalInntektsgrunnlag =  Inntektsgrunnlag(listOf(originalInntektslinje1,originalInntektslinje2),"JSON STRING")
        originalRefusjonsgrunnlag.inntektsgrunnlag = originalInntektsgrunnlag

        val inntektslinje1AndreGangKall = Inntektslinje(
            inntektType = "LOENNSINNTEKT",
            beskrivelse = null,
            beløp = 1000.0,
            måned = YearMonth.now(),
            opptjeningsperiodeFom = Now.localDate(),
            opptjeningsperiodeTom = Now.localDate().plusDays(30)
        )
        val inntektslinje2AndreGangKall = Inntektslinje(
            inntektType = "LOENNSINNTEKT",
            beskrivelse = null,
            beløp = -100.0,
            måned = YearMonth.now(),
            opptjeningsperiodeFom = Now.localDate(),
            opptjeningsperiodeTom = Now.localDate().plusDays(30)
        )


        val nyInntektsgrunnlag =  Inntektsgrunnlag(listOf(inntektslinje1AndreGangKall,inntektslinje2AndreGangKall),"JSON STRING")
        originalRefusjonsgrunnlag.oppgiInntektsgrunnlag(nyInntektsgrunnlag,originalRefusjonsgrunnlag.inntektsgrunnlag)
        assertEquals(2,originalRefusjonsgrunnlag.inntektsgrunnlag?.inntekter?.size )
    }

    @Test
    fun oppgiInntektsgrunnlagFjernEldreOmNyttKallErTom() {
        val originalInntektslinje1 = Inntektslinje(
            inntektType = "LOENNSINNTEKT",
            beskrivelse = null,
            beløp = 25000.0,
            måned = YearMonth.now(),
            opptjeningsperiodeFom = Now.localDate(),
            opptjeningsperiodeTom = Now.localDate().plusDays(30)
        )
        val originalInntektslinje2 = Inntektslinje(
            inntektType = "LOENNSINNTEKT",
            beskrivelse = null,
            beløp = -25000.0,
            måned = YearMonth.now(),
            opptjeningsperiodeFom = Now.localDate(),
            opptjeningsperiodeTom = Now.localDate().plusDays(30)
        )

        val originalRefusjonsgrunnlag = Refusjonsgrunnlag(etTilskuddsgrunnlag())
        val originalInntektsgrunnlag =  Inntektsgrunnlag(listOf(originalInntektslinje1,originalInntektslinje2),"JSON STRING")
        originalRefusjonsgrunnlag.inntektsgrunnlag = originalInntektsgrunnlag


        // NYTT KALL MED TOM LISTE
        val nyInntektsgrunnlag =  Inntektsgrunnlag(listOf(),"JSON STRING")
        originalRefusjonsgrunnlag.oppgiInntektsgrunnlag(nyInntektsgrunnlag,originalRefusjonsgrunnlag.inntektsgrunnlag)
        assertTrue(originalRefusjonsgrunnlag.inntektsgrunnlag?.inntekter?.isEmpty()!! )
    }

    @Test
    fun oppgiInntektsgrunnlagFjernEldreInntektslinjer() {
        val originalInntektslinje1 = Inntektslinje(
            inntektType = "LOENNSINNTEKT",
            beskrivelse = null,
            beløp = 25000.0,
            måned = YearMonth.now(),
            opptjeningsperiodeFom = Now.localDate(),
            opptjeningsperiodeTom = Now.localDate().plusDays(30)
        )
        val originalInntektslinje2 = Inntektslinje(
            inntektType = "LOENNSINNTEKT",
            beskrivelse = null,
            beløp = -25000.0,
            måned = YearMonth.now(),
            opptjeningsperiodeFom = Now.localDate(),
            opptjeningsperiodeTom = Now.localDate().plusDays(30)
        )

        val originalRefusjonsgrunnlag = Refusjonsgrunnlag(etTilskuddsgrunnlag())
        val originalInntektsgrunnlag =  Inntektsgrunnlag(listOf(originalInntektslinje1,originalInntektslinje2),"JSON STRING")
        originalRefusjonsgrunnlag.inntektsgrunnlag = originalInntektsgrunnlag
        val inntektslinje1AndreGangKall = Inntektslinje(
            inntektType = "LOENNSINNTEKT",
            beskrivelse = null,
            beløp = 1000.0,
            måned = YearMonth.now(),
            opptjeningsperiodeFom = Now.localDate(),
            opptjeningsperiodeTom = Now.localDate().plusDays(30)
        )

        // NYTT KALL MED TOM LISTE
        val nyInntektsgrunnlag =  Inntektsgrunnlag(listOf(inntektslinje1AndreGangKall),"JSON STRING")
        originalRefusjonsgrunnlag.oppgiInntektsgrunnlag(nyInntektsgrunnlag,originalRefusjonsgrunnlag.inntektsgrunnlag)
        assertEquals(1,originalRefusjonsgrunnlag.inntektsgrunnlag?.inntekter?.size )
        assertEquals(inntektslinje1AndreGangKall.id,originalRefusjonsgrunnlag.inntektsgrunnlag?.inntekter?.first { it.id == inntektslinje1AndreGangKall.id }?.id )

    }
}