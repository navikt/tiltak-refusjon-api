package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class RefusjonTest {
    // Godkjennelse arbeidsgiver
    @Test
    fun `kan ikke godkjenne for ag uten beregning`() {
        val refusjon = enRefusjon()
        assertFeilkode(Feilkode.UGYLDIG_STATUS) { refusjon.godkjennForArbeidsgiver() }
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
        assertFeilkode(Feilkode.UGYLDIG_STATUS) { refusjon.godkjennForArbeidsgiver() }
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
        assertFeilkode(Feilkode.UGYLDIG_STATUS) { refusjon.godkjennForSaksbehandler() }
    }

    @Test
    fun `oppgir inntektsgrunnlag for tidlig`() {
        val refusjon = enRefusjon(etTilskuddsgrunnlag().copy(tilskuddTom = LocalDate.now().plusDays(1)))
        assertFeilkode(Feilkode.INNTEKT_HENTET_FOR_TIDLIG) { refusjon.oppgiInntektsgrunnlag(etInntektsgrunnlag()) }
    }

    @Test
    fun `godkjenner rett før frist`() {
        val refusjon = enRefusjon(
            etTilskuddsgrunnlag().copy(
                tilskuddFom = LocalDate.now().minusMonths(2),
                tilskuddTom = LocalDate.now().minusMonths(2)
            )
        ).medInntektsgrunnlag()
        refusjon.godkjennForArbeidsgiver()
        assertThat(refusjon.godkjentAvArbeidsgiver).isNotNull()
        assertThat(refusjon.status).isEqualTo(RefusjonStatus.KRAV_FREMMET)
    }

    @Test
    fun `godkjenner etter frist`() {
        val refusjon = enRefusjon(
            etTilskuddsgrunnlag().copy(
                tilskuddFom = LocalDate.now().minusMonths(2).minusDays(1),
                tilskuddTom = LocalDate.now().minusMonths(2).minusDays(1)
            )
        ).medInntektsgrunnlag()
        assertFeilkode(Feilkode.ETTER_FRIST) { refusjon.godkjennForArbeidsgiver() }
    }
}

