package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.*
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth

internal class RefusjonTest {
    // Godkjennelse arbeidsgiver
    @Test
    fun `kan ikke godkjenne for ag uten beregning`() {
        val refusjon = enRefusjon()
        assertFeilkode(Feilkode.INGEN_INNTEKTER) { refusjon.godkjennForArbeidsgiver() }
    }

    @Test
    fun `kan godkjenne for ag med beregning`() {
        val refusjon = enRefusjon().medInntektsgrunnlag().medBedriftKontonummer()
        refusjon.godkjennForArbeidsgiver()
        assertThat(refusjon.godkjentAvArbeidsgiver).isNotNull
        assertThat(refusjon.status).isEqualTo(RefusjonStatus.SENDT_KRAV)
    }

    @Test
    fun `kan ikke godkjenne for ag to ganger`() {
        val refusjon = enRefusjon().medInntektsgrunnlag().medBedriftKontonummer().medSendtKravFraArbeidsgiver()
        assertFeilkode(Feilkode.UGYLDIG_STATUS) { refusjon.godkjennForArbeidsgiver() }
    }

    @Test
    fun `oppgir inntektsgrunnlag for tidlig`() {
        val refusjon = enRefusjon(etTilskuddsgrunnlag().copy(tilskuddTom = LocalDate.now().plusDays(1)))
        assertFeilkode(Feilkode.UGYLDIG_STATUS) {
            refusjon.oppgiInntektsgrunnlag(etInntektsgrunnlag(),
                "",
                0)
        }
    }

    @Test
    fun `godkjenner rett før frist`() {
        val refusjon = enRefusjon(
            etTilskuddsgrunnlag().copy(
                tilskuddFom = LocalDate.now().minusMonths(2),
                tilskuddTom = LocalDate.now().minusMonths(2)
            )
        ).medInntektsgrunnlag().medBedriftKontonummer()
        refusjon.godkjennForArbeidsgiver()
        assertThat(refusjon.godkjentAvArbeidsgiver).isNotNull
        assertThat(refusjon.status).isEqualTo(RefusjonStatus.SENDT_KRAV)
    }

    @Test
    fun `godkjenner etter frist`() {
        Now.fixedDate(LocalDate.now().minusDays(1))

        val refusjon = enRefusjon(
            etTilskuddsgrunnlag().copy(
                tilskuddFom = LocalDate.now().minusMonths(2).minusDays(1),
                tilskuddTom = LocalDate.now().minusMonths(2).minusDays(1)
            )
        ).medInntektsgrunnlag()
        Now.resetClock()
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

    @Test
    fun `Sjekker om bedriftKontonummerer er null`() {
        val refusjon = enRefusjon(
            etTilskuddsgrunnlag().copy(
                tilskuddFom = LocalDate.now().minusDays(1),
                tilskuddTom = LocalDate.now()
            )
        )
        refusjon.oppgiBedriftKontonummer("10000008145")
        assertThat(refusjon.bedriftKontonummer).isEqualTo("10000008145")
    }

    @Test
    fun `ikke kunne sende inn refusjon uten kontonummer`() {
        val refusjon = enRefusjon(
            etTilskuddsgrunnlag().copy(
                tilskuddFom = LocalDate.now().minusDays(2),
                tilskuddTom = LocalDate.now().minusDays(1)
            )
        ).medInntektsgrunnlag()
        assertFeilkode(Feilkode.INGEN_BEDRIFTKONTONUMMER) { refusjon.godkjennForArbeidsgiver() }
        refusjon.oppgiBedriftKontonummer("10000008145")
        refusjon.godkjennForArbeidsgiver()
        assertThat(refusjon.status).isEqualTo(RefusjonStatus.SENDT_KRAV)
    }

    @Test
    internal fun `har inntekt for alle måneder`() {
        val refusjon = enRefusjon(
            etTilskuddsgrunnlag().copy(
                tilskuddFom = LocalDate.now().minusDays(2),
                tilskuddTom = LocalDate.now().minusDays(1)
            )
        ).medInntektsgrunnlag(YearMonth.now(),
            Inntektsgrunnlag(inntekter = listOf(Inntektslinje("LOENNSINNTEKT",
                "fastloenn",
                99.0,
                YearMonth.now(),
                null,
                null)), respons = ""))
        assertThat(refusjon.harInntektIAlleMåneder()).isTrue()
    }

    @Test
    internal fun `har ikke inntekt for alle måneder`() {
        val refusjon = enRefusjon(
            etTilskuddsgrunnlag().copy(
                tilskuddFom = LocalDate.now().minusMonths(1).minusDays(2),
                tilskuddTom = LocalDate.now().minusDays(1)
            )
        ).medInntektsgrunnlag(YearMonth.now(), Inntektsgrunnlag(inntekter = listOf(
            Inntektslinje("LOENNSINNTEKT", "fastloenn", 99.0, YearMonth.now(), null, null),
            Inntektslinje("LOENNSINNTEKT", "feriepenger", 99.0, YearMonth.now().minusMonths(1), null, null)
        ), respons = ""))
        assertThat(refusjon.harInntektIAlleMåneder()).isFalse()
    }

    @Test
    internal fun `har ingen inntekt for alle måneder`() {
        val refusjon = enRefusjon(
            etTilskuddsgrunnlag().copy(
                tilskuddFom = LocalDate.now().minusMonths(1).minusDays(2),
                tilskuddTom = LocalDate.now().minusDays(1)
            )
        ).medInntektsgrunnlag(YearMonth.now(), Inntektsgrunnlag(inntekter = listOf(), respons = ""))
        assertThat(refusjon.harInntektIAlleMåneder()).isFalse()
    }

    @Test
    internal fun `har ikke gjort inntektsoppslag`() {
        val refusjon = enRefusjon(
            etTilskuddsgrunnlag().copy(
                tilskuddFom = LocalDate.now().minusMonths(1).minusDays(2),
                tilskuddTom = LocalDate.now().minusDays(1)
            )
        )
        assertThat(refusjon.harInntektIAlleMåneder()).isFalse()
    }

    @Test
    internal fun `korreksjon`() {
        val refusjon = enRefusjon().medInntektsgrunnlag().medBedriftKontonummer().medSendtKravFraArbeidsgiver()
        val korreksjon = refusjon.lagKorreksjon(setOf(Korreksjonsgrunn.UTBETALT_HELE_TILSKUDDSBELØP))
        assertThat(refusjon.tilskuddsgrunnlag).isEqualTo(korreksjon.tilskuddsgrunnlag)
        assertThat(refusjon.korrigeresAvId).isEqualTo(korreksjon.id)
        assertThat(korreksjon.korreksjonAvId).isEqualTo(refusjon.id)
        assertThat(korreksjon.status).isEqualTo(RefusjonStatus.MANUELL_KORREKSJON)

        // Kan kun ha en korreksjon av refusjonen
        assertFeilkode(Feilkode.HAR_KORREKSJON) { refusjon.lagKorreksjon(emptySet()) }
    }

    @Test
    internal fun `korreksjon av uriktig status`() {
        val refusjon = enRefusjon().medInntektsgrunnlag().medBedriftKontonummer()
        assertFeilkode(Feilkode.UGYLDIG_STATUS) { refusjon.lagKorreksjon(setOf(Korreksjonsgrunn.UTBETALT_HELE_TILSKUDDSBELØP)) }
    }
}

