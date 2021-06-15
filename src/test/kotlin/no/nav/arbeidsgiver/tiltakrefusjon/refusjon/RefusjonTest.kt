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
        assertFeilkode(Feilkode.UGYLDIG_STATUS) { refusjon.oppgiInntektsgrunnlag(etInntektsgrunnlag()) }
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
        assertFeilkode(Feilkode.UGYLDIG_STATUS) { refusjon.godkjennForArbeidsgiver() }
    }

    @Test
    fun `oppdater status til UTGÅTT`() {
        val refusjon = enRefusjon(
            etTilskuddsgrunnlag().copy(
                tilskuddFom = LocalDate.now().minusMonths(6),
                tilskuddTom = LocalDate.now().minusMonths(5)
            )
        )
        refusjon.oppdaterStatus()
        assertThat(refusjon.status).isEqualTo(RefusjonStatus.UTGÅTT)
    }
    @Test
    fun `oppdater status til FOR_TIDLIG`() {
        val refusjon = enRefusjon(
            etTilskuddsgrunnlag().copy(
                tilskuddFom = LocalDate.now().minusDays(1),
                tilskuddTom = LocalDate.now()
            )
        )
        refusjon.oppdaterStatus()
        assertThat(refusjon.status).isEqualTo(RefusjonStatus.FOR_TIDLIG)
    }
    @Test
    fun `oppdater status til KLAR_FOR_INNSENDING`() {
        val refusjon = enRefusjon(
            etTilskuddsgrunnlag().copy(
                tilskuddFom = LocalDate.now().minusDays(2),
                tilskuddTom = LocalDate.now().minusDays(1)
            )
        )
        refusjon.oppdaterStatus()
        assertThat(refusjon.status).isEqualTo(RefusjonStatus.KLAR_FOR_INNSENDING)
    }
    @Test
    fun `oppdaterer ikke status hvis ANNULLERT`() {

        val refusjon = enRefusjon(
            etTilskuddsgrunnlag().copy(
                tilskuddFom = Now.localDate().minusDays(1),
                tilskuddTom = Now.localDate()
            )
        )
        refusjon.annuller()
        Now.fixedDate(LocalDate.now().plusMonths(3))
        refusjon.oppdaterStatus()

        assertThat(refusjon.status).isEqualTo(RefusjonStatus.ANNULLERT)
        Now.resetClock()

    }


}

