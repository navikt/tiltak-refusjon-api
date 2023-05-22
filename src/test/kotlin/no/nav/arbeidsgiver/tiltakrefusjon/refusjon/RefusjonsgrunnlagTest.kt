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
    fun oppgiInntektsgrunnlag() {
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
}