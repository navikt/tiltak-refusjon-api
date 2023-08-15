package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.github.guepardoapps.kulid.ULID
import no.nav.arbeidsgiver.tiltakrefusjon.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.YearMonth

class KorreksjonTest {
    @Test
    internal fun `kan utbetale når alt er fylt ut`() {
        val tilskuddsgrunnlag = etTilskuddsgrunnlag()
        val korreksjon = Korreksjon(
            korrigererRefusjonId = ULID.random(),
            korreksjonsnummer = 1,
            tidligereUtbetalt = 0,
            korreksjonsgrunner = setOf(Korreksjonsgrunn.HENT_INNTEKTER_PÅ_NYTT),
            tilskuddsgrunnlag = tilskuddsgrunnlag,
            deltakerFnr = tilskuddsgrunnlag.deltakerFnr,
            bedriftNr = tilskuddsgrunnlag.bedriftNr,
            inntekterKunFraTiltaket = true,
            endretBruttoLønn = null,
            unntakOmInntekterFremitid = 1
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
        korreksjon.utbetalKorreksjon("", "X123456", "1000")
        assertThat(korreksjon.status).isEqualTo(Korreksjonstype.TILLEGSUTBETALING)
    }

    @Test
    internal fun `kostnadssted må være med`() {
        val tilskuddsgrunnlag = etTilskuddsgrunnlag()
        val korreksjon = Korreksjon(
            korrigererRefusjonId = ULID.random(),
            korreksjonsnummer = 1,
            tidligereUtbetalt = 0,
            korreksjonsgrunner = setOf(Korreksjonsgrunn.HENT_INNTEKTER_PÅ_NYTT),
            tilskuddsgrunnlag = tilskuddsgrunnlag,
            deltakerFnr = tilskuddsgrunnlag.deltakerFnr,
            bedriftNr = tilskuddsgrunnlag.bedriftNr,
            inntekterKunFraTiltaket = true,
            endretBruttoLønn = null,
            unntakOmInntekterFremitid = 1
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

    @Disabled("Sperrer ikke for dette pt")
    @Test
    internal fun `kostnadssted kan ikke høre til et annet fylke enn refusjonen være med`() {
        val tilskuddsgrunnlag = etTilskuddsgrunnlag()
        val korreksjon = Korreksjon(
            korrigererRefusjonId = ULID.random(),
            korreksjonsnummer = 1,
            tidligereUtbetalt = 0,
            korreksjonsgrunner = setOf(Korreksjonsgrunn.HENT_INNTEKTER_PÅ_NYTT),
            tilskuddsgrunnlag = tilskuddsgrunnlag,
            deltakerFnr = tilskuddsgrunnlag.deltakerFnr,
            bedriftNr = tilskuddsgrunnlag.bedriftNr,
            inntekterKunFraTiltaket = true,
            endretBruttoLønn = null,
            unntakOmInntekterFremitid = 1
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
        assertFeilkode(Feilkode.KORREKSJON_KOSTNADSSTED_ANNET_FYLKE) { korreksjon.utbetalKorreksjon("", "X123456", "2009") }
    }

    @Test
    internal fun `kostnadssted kan være ulikt innenfor samme fylke`() {
        val tilskuddsgrunnlag = etTilskuddsgrunnlag()
        val korreksjon = Korreksjon(
            korrigererRefusjonId = ULID.random(),
            korreksjonsnummer = 1,
            tidligereUtbetalt = 0,
            korreksjonsgrunner = setOf(Korreksjonsgrunn.HENT_INNTEKTER_PÅ_NYTT),
            tilskuddsgrunnlag = tilskuddsgrunnlag,
            deltakerFnr = tilskuddsgrunnlag.deltakerFnr,
            bedriftNr = tilskuddsgrunnlag.bedriftNr,
            inntekterKunFraTiltaket = true,
            endretBruttoLønn = null,
            unntakOmInntekterFremitid = 1
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
        korreksjon.utbetalKorreksjon("", "X123456", "1009")
        assertThat(korreksjon.status).isEqualTo(Korreksjonstype.TILLEGSUTBETALING)
    }

    @Test
    internal fun `kun lov med tilbakekreving ved negativ sum`() {
        val tilskuddsgrunnlag = etTilskuddsgrunnlag()
        val korreksjon = Korreksjon(
            korrigererRefusjonId = ULID.random(),
            korreksjonsnummer = 1,
            tidligereUtbetalt = 1000000,
            korreksjonsgrunner = setOf(Korreksjonsgrunn.HENT_INNTEKTER_PÅ_NYTT),
            tilskuddsgrunnlag = tilskuddsgrunnlag,
            deltakerFnr = tilskuddsgrunnlag.deltakerFnr,
            bedriftNr = tilskuddsgrunnlag.bedriftNr,
            inntekterKunFraTiltaket = true,
            endretBruttoLønn = null,
            unntakOmInntekterFremitid = 1
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
            korrigererRefusjonId = ULID.random(),
            korreksjonsnummer = 1,
            tidligereUtbetalt = 4055,
            korreksjonsgrunner = setOf(Korreksjonsgrunn.HENT_INNTEKTER_PÅ_NYTT),
            tilskuddsgrunnlag = tilskuddsgrunnlag,
            deltakerFnr = tilskuddsgrunnlag.deltakerFnr,
            bedriftNr = tilskuddsgrunnlag.bedriftNr,
            inntekterKunFraTiltaket = true,
            endretBruttoLønn = null,
            unntakOmInntekterFremitid = 1
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

    @Test
    internal fun `kun inntekter som er huket av for at de er opptjent i perioden blir regnet med`() {
        val tilskuddsgrunnlag = etTilskuddsgrunnlag()
        val korreksjon = Korreksjon(
            korrigererRefusjonId = ULID.random(),
            korreksjonsnummer = 1,
            tidligereUtbetalt = 4055,
            korreksjonsgrunner = setOf(Korreksjonsgrunn.HENT_INNTEKTER_PÅ_NYTT),
            tilskuddsgrunnlag = tilskuddsgrunnlag,
            deltakerFnr = tilskuddsgrunnlag.deltakerFnr,
            bedriftNr = tilskuddsgrunnlag.bedriftNr,
            inntekterKunFraTiltaket = true,
            endretBruttoLønn = null,
            unntakOmInntekterFremitid = 1
        )
        val inntektslinjeOpptjentIPeriode = enInntektslinje(opptjentIPeriode = true)
        val inntektslinjeIkkeOptjentIPeriode = enInntektslinje(opptjentIPeriode = false)
        val inntekter = listOf(
            inntektslinjeOpptjentIPeriode,
            inntektslinjeIkkeOptjentIPeriode
        )
        val inntektsgrunnlag = Inntektsgrunnlag(inntekter, "")
        korreksjon.oppgiBedriftKontonummer("123456789")
        korreksjon.oppgiInntektsgrunnlag(inntektsgrunnlag)

        assertThat(korreksjon.refusjonsgrunnlag.beregning?.lønn).isEqualTo(inntektslinjeOpptjentIPeriode.beløp.toInt())
    }
}