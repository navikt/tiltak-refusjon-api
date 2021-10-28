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
        val refusjon = enRefusjon(etTilskuddsgrunnlag().copy(tilskuddTom = Now.localDate().plusDays(1)))
        assertFeilkode(Feilkode.UGYLDIG_STATUS) {
            refusjon.oppgiInntektsgrunnlag(etInntektsgrunnlag())
        }
    }

    @Test
    fun `godkjenner rett før frist`() {
        Now.fixedDate(LocalDate.of(2021, 8, 30))

        val refusjon = enRefusjon(
            etTilskuddsgrunnlag().copy(
                tilskuddFom = LocalDate.of(2021, 6, 30),
                tilskuddTom = LocalDate.of(2021, 6, 30)
            )
        ).medInntektsgrunnlag().medBedriftKontonummer()
        refusjon.godkjennForArbeidsgiver()
        Now.resetClock()
        assertThat(refusjon.godkjentAvArbeidsgiver).isNotNull
        assertThat(refusjon.status).isEqualTo(RefusjonStatus.SENDT_KRAV)
    }

    @Test
    fun `godkjenner etter frist`() {
        Now.fixedDate(LocalDate.of(2021, 8, 30))

        val refusjon = enRefusjon(
            etTilskuddsgrunnlag().copy(
                tilskuddFom = LocalDate.of(2021, 6, 30),
                tilskuddTom = LocalDate.of(2021, 6, 30)
            )
        ).medInntektsgrunnlag()
        Now.resetClock()
        assertFeilkode(Feilkode.UGYLDIG_STATUS) { refusjon.godkjennForArbeidsgiver() }
    }

    @Test
    fun `oppdater status til UTGÅTT`() {
        val refusjon = enRefusjon(
            etTilskuddsgrunnlag().copy(
                tilskuddFom = Now.localDate().minusMonths(6),
                tilskuddTom = Now.localDate().minusMonths(5)
            )
        )
        refusjon.oppdaterStatus()
        assertThat(refusjon.status).isEqualTo(RefusjonStatus.UTGÅTT)
    }

    @Test
    fun `oppdater status til FOR_TIDLIG`() {
        val refusjon = enRefusjon(
            etTilskuddsgrunnlag().copy(
                tilskuddFom = Now.localDate().minusDays(1),
                tilskuddTom = Now.localDate()
            )
        )
        refusjon.oppdaterStatus()
        assertThat(refusjon.status).isEqualTo(RefusjonStatus.FOR_TIDLIG)
    }

    @Test
    fun `oppdater status til KLAR_FOR_INNSENDING`() {
        val refusjon = enRefusjon(
            etTilskuddsgrunnlag().copy(
                tilskuddFom = Now.localDate().minusDays(2),
                tilskuddTom = Now.localDate().minusDays(1)
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
        Now.fixedDate(Now.localDate().plusMonths(3))
        refusjon.oppdaterStatus()

        assertThat(refusjon.status).isEqualTo(RefusjonStatus.ANNULLERT)
        Now.resetClock()

    }

    @Test
    fun `Sjekker om bedriftKontonummerer er null`() {
        val refusjon = enRefusjon(
            etTilskuddsgrunnlag().copy(
                tilskuddFom = Now.localDate().minusDays(1),
                tilskuddTom = Now.localDate()
            )
        )
        refusjon.oppgiBedriftKontonummer("10000008145")
        assertThat(refusjon.bedriftKontonummer).isEqualTo("10000008145")
    }

    @Test
    fun `ikke kunne sende inn refusjon uten kontonummer`() {
        val refusjon = enRefusjon(
            etTilskuddsgrunnlag().copy(
                tilskuddFom = Now.localDate().minusDays(2),
                tilskuddTom = Now.localDate().minusDays(1)
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
                tilskuddFom = Now.localDate().minusDays(2),
                tilskuddTom = Now.localDate().minusDays(1)
            )
        ).medInntektsgrunnlag(YearMonth.now(),
            Inntektsgrunnlag(inntekter = listOf(
                Inntektslinje("LOENNSINNTEKT",
                    "fastloenn",
                    99.0,
                    YearMonth.now(),
                    null,
                    null),
                Inntektslinje("LOENNSINNTEKT",
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
                tilskuddFom = Now.localDate().minusMonths(1).minusDays(2),
                tilskuddTom = Now.localDate().minusDays(1)
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
                tilskuddFom = Now.localDate().minusMonths(1).minusDays(2),
                tilskuddTom = Now.localDate().minusDays(1)
            )
        ).medInntektsgrunnlag(YearMonth.now(), Inntektsgrunnlag(inntekter = listOf(), respons = ""))
        assertThat(refusjon.harInntektIAlleMåneder()).isFalse()
    }

    @Test
    internal fun `har ikke gjort inntektsoppslag`() {
        val refusjon = enRefusjon(
            etTilskuddsgrunnlag().copy(
                tilskuddFom = Now.localDate().minusMonths(1).minusDays(2),
                tilskuddTom = Now.localDate().minusDays(1)
            )
        )
        assertThat(refusjon.harInntektIAlleMåneder()).isFalse()
    }

    @Test
    internal fun `korreksjon`() {
        val refusjon = enRefusjon().medInntektsgrunnlag().medBedriftKontonummer().medSendtKravFraArbeidsgiver()
        val korreksjon = refusjon.opprettKorreksjonsutkast(setOf(Korreksjonsgrunn.UTBETALT_HELE_TILSKUDDSBELØP))
        assertThat(refusjon.tilskuddsgrunnlag).isEqualTo(korreksjon.tilskuddsgrunnlag)
        assertThat(refusjon.korrigeresAvId).isEqualTo(korreksjon.id)
        assertThat(korreksjon.korreksjonAvId).isEqualTo(refusjon.id)
        assertThat(korreksjon.status).isEqualTo(RefusjonStatus.KORREKSJON_UTKAST)

        // Kan kun ha en korreksjon av refusjonen
        assertFeilkode(Feilkode.HAR_KORREKSJON) { refusjon.opprettKorreksjonsutkast(emptySet()) }
    }

    @Test
    internal fun `korreksjon av uriktig status`() {
        val refusjon = enRefusjon().medInntektsgrunnlag().medBedriftKontonummer()
        assertFeilkode(Feilkode.UGYLDIG_STATUS) { refusjon.opprettKorreksjonsutkast(setOf(Korreksjonsgrunn.UTBETALT_HELE_TILSKUDDSBELØP)) }
    }

    @Test
    internal fun `forleng frist`() {
        val refusjon = enRefusjon().medInntektsgrunnlag().medBedriftKontonummer()
        val opprinneligFrist = refusjon.fristForGodkjenning
        val sisteDagDetErMuligÅForlengeTil = refusjon.tilskuddsgrunnlag.tilskuddTom.plusMonths(6)

        // Positiv test
        refusjon.forlengFrist(sisteDagDetErMuligÅForlengeTil, "", "")
        assertThat(refusjon.fristForGodkjenning).isEqualTo(sisteDagDetErMuligÅForlengeTil)
        assertThat(refusjon.forrigeFristForGodkjenning).isEqualTo(opprinneligFrist)

        // Negativ test 1
        assertFeilkode(Feilkode.FOR_LANG_FORLENGELSE_AV_FRIST) {
            refusjon.forlengFrist(sisteDagDetErMuligÅForlengeTil.plusDays(1), "", "")
        }

        // Negativ test 2
        assertFeilkode(Feilkode.UGYLDIG_FORLENGELSE_AV_FRIST) {
            refusjon.forlengFrist(refusjon.fristForGodkjenning.minusDays(1), "", "")
        }
    }

    @Test
    internal fun `forleng frist på utgått refusjon skal endre status tilbake til klar for innsending`() {
        val refusjon = enRefusjon(etTilskuddsgrunnlag().copy(
            tilskuddFom = Now.localDate().minusMonths(2).minusDays(1),
            tilskuddTom = Now.localDate().minusMonths(2).minusDays(1)
        ))

        val idag = Now.localDate()
        refusjon.forlengFrist(idag, "", "")
        assertThat(refusjon.fristForGodkjenning).isEqualTo(idag)
        assertThat(refusjon.status).isEqualTo(RefusjonStatus.KLAR_FOR_INNSENDING)
    }

    @Test
    internal fun `utbetal korreksjon, etterbetaling`() {
        val refusjon = enRefusjon().medInntektsgrunnlag().medBedriftKontonummer().medSendtKravFraArbeidsgiver()
        val korreksjon = refusjon.opprettKorreksjonsutkast(setOf())
        korreksjon.gjørBeregning("", 0)
        korreksjon.utbetalKorreksjon("X123456", "Y123456")
        assertThat(korreksjon.status).isEqualTo(RefusjonStatus.KORREKSJON_SENDT_TIL_UTBETALING)
    }

    @Test
    internal fun `utbetal korreksjon, etterbetaling, feilsituasjoner`() {
        val refusjon = enRefusjon().medInntektsgrunnlag().medBedriftKontonummer().medSendtKravFraArbeidsgiver()
        val korreksjon = refusjon.opprettKorreksjonsutkast(setOf())
        korreksjon.gjørBeregning("", 0)
        assertFeilkode(Feilkode.SAMME_SAKSBEHANDLER_OG_BESLUTTER) { korreksjon.utbetalKorreksjon("X123456", "X123456") }
        assertFeilkode(Feilkode.INGEN_BESLUTTER) { korreksjon.utbetalKorreksjon("X123456", "") }
        korreksjon.bedriftKontonummer = null
        assertFeilkode(Feilkode.INGEN_BEDRIFTKONTONUMMER) { korreksjon.utbetalKorreksjon("X123456", "Y123456") }

        // er ikke en etterbetaling, skal ikke kunne utbetale korreksjonen
        korreksjon.gjørBeregning("", 1000000)
        assertFeilkode(Feilkode.KORREKSJONSBELOP_NEGATIVT) { korreksjon.utbetalKorreksjon("X123456", "Y123456") }
    }

    @Test
    internal fun `fullfør korreksjon, tilbakekreving`() {
        val refusjon = enRefusjon().medInntektsgrunnlag().medBedriftKontonummer().medSendtKravFraArbeidsgiver()
        val korreksjon = refusjon.opprettKorreksjonsutkast(setOf())
        korreksjon.gjørBeregning("", 1000000)
        korreksjon.fullførKorreksjonVedTilbakekreving("X123456")
        assertThat(korreksjon.status).isEqualTo(RefusjonStatus.KORREKSJON_SKAL_TILBAKEKREVES)
    }

    @Test
    internal fun `fullfør korreksjon, ikke tilbakekreving allikevel`() {
        val refusjon = enRefusjon().medInntektsgrunnlag().medBedriftKontonummer().medSendtKravFraArbeidsgiver()
        val korreksjon = refusjon.opprettKorreksjonsutkast(setOf())
        korreksjon.gjørBeregning("", refusjon.beregning!!.refusjonsbeløp)
        assertFeilkode(Feilkode.KORREKSJONSBELOP_POSITIVT) { korreksjon.fullførKorreksjonVedTilbakekreving("X123456") }
    }

    @Test
    internal fun `fullfør korreksjon, går i 0`() {
        val refusjon = enRefusjon().medInntektsgrunnlag().medBedriftKontonummer().medSendtKravFraArbeidsgiver()
        val korreksjon = refusjon.opprettKorreksjonsutkast(setOf())
        korreksjon.gjørBeregning("", refusjon.beregning!!.refusjonsbeløp)
        korreksjon.fullførKorreksjonVedOppgjort("X123456")
        assertThat(korreksjon.status).isEqualTo(RefusjonStatus.KORREKSJON_OPPGJORT)
    }

    @Test
    internal fun `fullfør korreksjon, går ikke i 0 allikevel`() {
        val refusjon = enRefusjon().medInntektsgrunnlag().medBedriftKontonummer().medSendtKravFraArbeidsgiver()
        val korreksjon = refusjon.opprettKorreksjonsutkast(setOf())
        korreksjon.gjørBeregning("", refusjon.beregning!!.refusjonsbeløp + 1)
        assertFeilkode(Feilkode.KORREKSJONSBELOP_IKKE_NULL) { korreksjon.fullførKorreksjonVedOppgjort("X123456") }
    }
}

