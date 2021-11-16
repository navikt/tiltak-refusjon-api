package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.github.guepardoapps.kulid.ULID
import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.assertFeilkode
import no.nav.arbeidsgiver.tiltakrefusjon.etInntektsgrunnlag
import no.nav.arbeidsgiver.tiltakrefusjon.etTilskuddsgrunnlag
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.YearMonth

class KorreksjonTest {
    @Test
    internal fun `kan utbetale når alt er fylt ut`() {
        val tilskuddsgrunnlag = etTilskuddsgrunnlag()
        val korreksjon = Korreksjon(
            ULID.random(),
            1,
            0,
            setOf(Korreksjonsgrunn.HENT_INNTEKTER_PÅ_NYTT),
            tilskuddsgrunnlag,
            tilskuddsgrunnlag.deltakerFnr,
            tilskuddsgrunnlag.bedriftNr,
            true,
            null
        )
        korreksjon.oppgiInntektsgrunnlag(
            etInntektsgrunnlag(
                måned = YearMonth.of(
                    tilskuddsgrunnlag.tilskuddFom.year,
                    tilskuddsgrunnlag.tilskuddFom.month
                )
            )
        )
        korreksjon.oppgiBedriftKontonummer("99999999999")
        korreksjon.utbetalKorreksjon("", "X123456", "9999")
        assertThat(korreksjon.status).isEqualTo(Korreksjonstype.TILLEGSUTBETALING)
    }

    @Test
    internal fun `kostnadssted må være med`() {
        val tilskuddsgrunnlag = etTilskuddsgrunnlag()
        val korreksjon = Korreksjon(
            ULID.random(),
            1,
            0,
            setOf(Korreksjonsgrunn.HENT_INNTEKTER_PÅ_NYTT),
            tilskuddsgrunnlag,
            tilskuddsgrunnlag.deltakerFnr,
            tilskuddsgrunnlag.bedriftNr,
            true,
            null
        )
        korreksjon.oppgiInntektsgrunnlag(
            etInntektsgrunnlag(
                måned = YearMonth.of(
                    tilskuddsgrunnlag.tilskuddFom.year,
                    tilskuddsgrunnlag.tilskuddFom.month
                )
            )
        )
        korreksjon.oppgiBedriftKontonummer("99999999999")
        assertFeilkode(Feilkode.KOSTNADSSTED_MANGLER) { korreksjon.utbetalKorreksjon("", "X123456", "") }
    }

    @Test
    internal fun `kun lov med tilbakekreving ved negativ sum`() {
        val tilskuddsgrunnlag = etTilskuddsgrunnlag()
        val korreksjon = Korreksjon(
            ULID.random(),
            1,
            1000000,
            setOf(Korreksjonsgrunn.HENT_INNTEKTER_PÅ_NYTT),
            tilskuddsgrunnlag,
            tilskuddsgrunnlag.deltakerFnr,
            tilskuddsgrunnlag.bedriftNr,
            true,
            null
        )
        korreksjon.oppgiInntektsgrunnlag(
            etInntektsgrunnlag(
                måned = YearMonth.of(
                    tilskuddsgrunnlag.tilskuddFom.year,
                    tilskuddsgrunnlag.tilskuddFom.month
                )
            )
        )
        korreksjon.oppgiBedriftKontonummer("99999999999")
        assertFeilkode(Feilkode.KORREKSJONSBELOP_NEGATIVT) { korreksjon.utbetalKorreksjon("", "X123456", "9999") }
        assertFeilkode(Feilkode.KORREKSJONSBELOP_IKKE_NULL) { korreksjon.fullførKorreksjonVedOppgjort("") }
        korreksjon.fullførKorreksjonVedTilbakekreving("")
        assertThat(korreksjon.status).isEqualTo(Korreksjonstype.TILBAKEKREVING)
    }

    @Test
    internal fun `kun lov med oppgjort`() {
        val tilskuddsgrunnlag = etTilskuddsgrunnlag()
        val korreksjon = Korreksjon(
            ULID.random(),
            1,
            4055,
            setOf(Korreksjonsgrunn.HENT_INNTEKTER_PÅ_NYTT),
            tilskuddsgrunnlag,
            tilskuddsgrunnlag.deltakerFnr,
            tilskuddsgrunnlag.bedriftNr,
            true,
            null
        )
        korreksjon.oppgiInntektsgrunnlag(
            etInntektsgrunnlag(
                måned = YearMonth.of(
                    tilskuddsgrunnlag.tilskuddFom.year,
                    tilskuddsgrunnlag.tilskuddFom.month
                )
            )
        )
        korreksjon.oppgiBedriftKontonummer("99999999999")
        assertFeilkode(Feilkode.KORREKSJONSBELOP_NEGATIVT) { korreksjon.utbetalKorreksjon("", "X123456", "9999") }
        assertFeilkode(Feilkode.KORREKSJONSBELOP_POSITIVT) { korreksjon.fullførKorreksjonVedTilbakekreving("") }
        korreksjon.fullførKorreksjonVedOppgjort("")
        assertThat(korreksjon.status).isEqualTo(Korreksjonstype.OPPGJORT)
    }
}