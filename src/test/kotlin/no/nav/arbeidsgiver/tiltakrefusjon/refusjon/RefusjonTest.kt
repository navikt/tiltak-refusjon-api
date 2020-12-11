package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class RefusjonTest {
    // Godkjennelse arbeidsgiver
    @Test
    fun `kan ikke godkjenne for ag uten beregning`() {
        val refusjon = enRefusjon()
        assertThatThrownBy { refusjon.godkjennForArbeidsgiver() }.hasFeilkode(Feilkode.UGYLDIG_STATUS)
    }

    @Test
    fun `kan godkjenne for ag med beregning`() {
        val refusjon = enRefusjon().medInntektsgrunnlag()
        refusjon.godkjennForArbeidsgiver()
        assertThat(refusjon.godkjentAvArbeidsgiver).isNotNull()
        assertThat(refusjon.status).isEqualTo(RefusjonStatus.KRAV_FREMMET)
    }

    @Test
    fun `kan ikke godkjenne for ag to ganger`() {
        val refusjon = enRefusjon().medInntektsgrunnlag().medGodkjennelseFraArbeidsgiver()
        assertThatThrownBy { refusjon.godkjennForArbeidsgiver() }.hasFeilkode(Feilkode.UGYLDIG_STATUS)
    }

    // Godkjennelse saksbehandler
    @Test
    fun `kan godkjenne for saksbehandler med ag godkjennelse`() {
        val refusjon = enRefusjon().medInntektsgrunnlag().medGodkjennelseFraArbeidsgiver()
        refusjon.godkjennForSaksbehandler()
        assertThat(refusjon.godkjentAvSaksbehandler).isNotNull()
        assertThat(refusjon.status).isEqualTo(RefusjonStatus.BEHANDLET)
    }

    @Test
    fun `kan ikke godkjenne for saksbehandler to ganger`() {
        val refusjon = enRefusjon()
                .medInntektsgrunnlag()
                .medGodkjennelseFraArbeidsgiver()
                .medGodkjennelseFraSaksbehandler()
        assertThatThrownBy { refusjon.godkjennForSaksbehandler() }.hasFeilkode(Feilkode.UGYLDIG_STATUS)
    }

    //Inntektsgrunnlag
    @Test
    fun `oppgir inntektsgrunnlag for tidlig`() {
        val refusjon = enRefusjon(etTilskuddsgrunnlag.copy(tilskuddTom = LocalDate.now().plusDays(1)))
        assertThatThrownBy { refusjon.oppgiInntektsgrunnlag(etInntektsgrunnlag()) }.hasFeilkode(Feilkode.INNTEKT_HENTET_FOR_TIDLIG)
    }
}

