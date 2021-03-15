package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.*
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class RefusjonTest {
    // Godkjennelse arbeidsgiver
    @Test
    fun `kan ikke godkjenne for ag uten beregning`() {
        val refusjon = enRefusjon()
        assertFeilkode(Feilkode.INGEN_INNTEKTER) { refusjon.godkjennForArbeidsgiver() }
    }

    @Test
    fun `kan godkjenne for ag med beregning`() {
        val refusjon = enRefusjon().medInntektsgrunnlag()
        refusjon.godkjennForArbeidsgiver()
        assertThat(refusjon.godkjentAvArbeidsgiver).isNotNull()
        assertThat(refusjon.status).isEqualTo(RefusjonStatus.SENDT_KRAV)
    }

    @Test
    fun `kan ikke godkjenne for ag to ganger`() {
        val refusjon = enRefusjon().medInntektsgrunnlag().medSendtKravFraArbeidsgiver()
        assertFeilkode(Feilkode.UGYLDIG_STATUS) { refusjon.godkjennForArbeidsgiver() }
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
        assertThat(refusjon.status).isEqualTo(RefusjonStatus.SENDT_KRAV)
    }

    @Test
    fun `godkjenner etter frist`() {
        Now.fixedDate(LocalDate.now().minusDays(1));

        val refusjon = enRefusjon(
            etTilskuddsgrunnlag().copy(
                tilskuddFom = LocalDate.now().minusMonths(2).minusDays(1),
                tilskuddTom = LocalDate.now().minusMonths(2).minusDays(1)
            )
        ).medInntektsgrunnlag()
        Now.resetClock();
        assertFeilkode(Feilkode.ETTER_FRIST) { refusjon.godkjennForArbeidsgiver() }
    }
}

