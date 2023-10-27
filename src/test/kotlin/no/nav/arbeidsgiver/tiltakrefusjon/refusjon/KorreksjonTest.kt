package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.*
import no.nav.arbeidsgiver.tiltakrefusjon.utils.ulid
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.YearMonth

class KorreksjonTest {
    val innloggetBeslutter = innloggetBruker("X123456", BrukerRolle.BESLUTTER)

    @Test
    internal fun `kan utbetale når alt er fylt ut`() {
        val tilskuddsgrunnlag = etTilskuddsgrunnlag()
        val korreksjon = Korreksjon(
            korrigererRefusjonId = ulid(),
            korreksjonsnummer = 1,
            tidligereUtbetalt = 0,
            korreksjonsgrunner = setOf(Korreksjonsgrunn.HENT_INNTEKTER_PÅ_NYTT),
            tilskuddsgrunnlag = tilskuddsgrunnlag,
            deltakerFnr = tilskuddsgrunnlag.deltakerFnr,
            bedriftNr = tilskuddsgrunnlag.bedriftNr,
            inntekterKunFraTiltaket = true,
            endretBruttoLønn = null,
            unntakOmInntekterFremitid = 1,
            annenGrunn = null
        )
        korreksjon.oppgiInntektsgrunnlag(
            innloggetBeslutter,
            etInntektsgrunnlag(
                måned = YearMonth.of(
                    tilskuddsgrunnlag.tilskuddFom.year,
                    tilskuddsgrunnlag.tilskuddFom.month
                )
            )
        )
        korreksjon.oppgiBedriftKontonummer(innloggetBeslutter, "99999999999")
        korreksjon.utbetalKorreksjon(innloggetBeslutter, "1000")
        assertThat(korreksjon.status).isEqualTo(Korreksjonstype.TILLEGSUTBETALING)
    }

    @Test
    internal fun `kostnadssted må være med`() {
        val tilskuddsgrunnlag = etTilskuddsgrunnlag()
        val korreksjon = Korreksjon(
            korrigererRefusjonId = ulid(),
            korreksjonsnummer = 1,
            tidligereUtbetalt = 0,
            korreksjonsgrunner = setOf(Korreksjonsgrunn.HENT_INNTEKTER_PÅ_NYTT),
            tilskuddsgrunnlag = tilskuddsgrunnlag,
            deltakerFnr = tilskuddsgrunnlag.deltakerFnr,
            bedriftNr = tilskuddsgrunnlag.bedriftNr,
            inntekterKunFraTiltaket = true,
            endretBruttoLønn = null,
            unntakOmInntekterFremitid = 1,
            annenGrunn = null
        )
        korreksjon.oppgiInntektsgrunnlag(
            innloggetBeslutter,
            etInntektsgrunnlag(
                måned = YearMonth.of(
                    tilskuddsgrunnlag.tilskuddFom.year,
                    tilskuddsgrunnlag.tilskuddFom.month
                )
            )
        )
        korreksjon.oppgiBedriftKontonummer(innloggetBeslutter, "99999999999")
        assertFeilkode(Feilkode.KOSTNADSSTED_MANGLER) { korreksjon.utbetalKorreksjon(innloggetBeslutter, "") }
    }

    @Disabled("Sperrer ikke for dette pt")
    @Test
    internal fun `kostnadssted kan ikke høre til et annet fylke enn refusjonen være med`() {
        val tilskuddsgrunnlag = etTilskuddsgrunnlag()
        val korreksjon = Korreksjon(
            korrigererRefusjonId = ulid(),
            korreksjonsnummer = 1,
            tidligereUtbetalt = 0,
            korreksjonsgrunner = setOf(Korreksjonsgrunn.HENT_INNTEKTER_PÅ_NYTT),
            tilskuddsgrunnlag = tilskuddsgrunnlag,
            deltakerFnr = tilskuddsgrunnlag.deltakerFnr,
            bedriftNr = tilskuddsgrunnlag.bedriftNr,
            inntekterKunFraTiltaket = true,
            endretBruttoLønn = null,
            unntakOmInntekterFremitid = 1,
            annenGrunn = null
        )
        korreksjon.oppgiInntektsgrunnlag(
            innloggetBeslutter,
            etInntektsgrunnlag(
                måned = YearMonth.of(
                    tilskuddsgrunnlag.tilskuddFom.year,
                    tilskuddsgrunnlag.tilskuddFom.month
                )
            )
        )
        korreksjon.oppgiBedriftKontonummer(innloggetBeslutter, "99999999999")
        assertFeilkode(Feilkode.KORREKSJON_KOSTNADSSTED_ANNET_FYLKE) {
            korreksjon.utbetalKorreksjon(innloggetBeslutter, "2009")
        }
    }

    @Test
    internal fun `kostnadssted kan være ulikt innenfor samme fylke`() {
        val tilskuddsgrunnlag = etTilskuddsgrunnlag()
        val korreksjon = Korreksjon(
            korrigererRefusjonId = ulid(),
            korreksjonsnummer = 1,
            tidligereUtbetalt = 0,
            korreksjonsgrunner = setOf(Korreksjonsgrunn.HENT_INNTEKTER_PÅ_NYTT),
            tilskuddsgrunnlag = tilskuddsgrunnlag,
            deltakerFnr = tilskuddsgrunnlag.deltakerFnr,
            bedriftNr = tilskuddsgrunnlag.bedriftNr,
            inntekterKunFraTiltaket = true,
            endretBruttoLønn = null,
            unntakOmInntekterFremitid = 1,
            annenGrunn = null
        )
        korreksjon.oppgiInntektsgrunnlag(
            innloggetBeslutter,
            etInntektsgrunnlag(
                måned = YearMonth.of(
                    tilskuddsgrunnlag.tilskuddFom.year,
                    tilskuddsgrunnlag.tilskuddFom.month
                )
            )
        )
        korreksjon.oppgiBedriftKontonummer(innloggetBeslutter, "99999999999")
        korreksjon.utbetalKorreksjon(innloggetBeslutter, "1009")
        assertThat(korreksjon.status).isEqualTo(Korreksjonstype.TILLEGSUTBETALING)
    }

    @Test
    internal fun `kun lov med tilbakekreving ved negativ sum`() {
        val tilskuddsgrunnlag = etTilskuddsgrunnlag()
        val korreksjon = Korreksjon(
            korrigererRefusjonId = ulid(),
            korreksjonsnummer = 1,
            tidligereUtbetalt = 1000000,
            korreksjonsgrunner = setOf(Korreksjonsgrunn.HENT_INNTEKTER_PÅ_NYTT),
            tilskuddsgrunnlag = tilskuddsgrunnlag,
            deltakerFnr = tilskuddsgrunnlag.deltakerFnr,
            bedriftNr = tilskuddsgrunnlag.bedriftNr,
            inntekterKunFraTiltaket = true,
            endretBruttoLønn = null,
            unntakOmInntekterFremitid = 1,
            annenGrunn = null
        )
        korreksjon.oppgiInntektsgrunnlag(
            innloggetBeslutter,
            etInntektsgrunnlag(
                måned = YearMonth.of(
                    tilskuddsgrunnlag.tilskuddFom.year,
                    tilskuddsgrunnlag.tilskuddFom.month
                )
            )
        )
        korreksjon.oppgiBedriftKontonummer(innloggetBeslutter, "99999999999")
        assertFeilkode(Feilkode.KORREKSJONSBELOP_NEGATIVT) { korreksjon.utbetalKorreksjon(innloggetBeslutter, "9999") }
        assertFeilkode(Feilkode.KORREKSJONSBELOP_IKKE_NULL) { korreksjon.fullførKorreksjonVedOppgjort(innloggetBeslutter) }
        korreksjon.fullførKorreksjonVedTilbakekreving(innloggetBeslutter)
        assertThat(korreksjon.status).isEqualTo(Korreksjonstype.TILBAKEKREVING)
    }

    @Test
    internal fun `kun lov med oppgjort`() {
        val tilskuddsgrunnlag = etTilskuddsgrunnlag()
        val korreksjon = Korreksjon(
            korrigererRefusjonId = ulid(),
            korreksjonsnummer = 1,
            tidligereUtbetalt = 4055,
            korreksjonsgrunner = setOf(Korreksjonsgrunn.HENT_INNTEKTER_PÅ_NYTT),
            tilskuddsgrunnlag = tilskuddsgrunnlag,
            deltakerFnr = tilskuddsgrunnlag.deltakerFnr,
            bedriftNr = tilskuddsgrunnlag.bedriftNr,
            inntekterKunFraTiltaket = true,
            endretBruttoLønn = null,
            unntakOmInntekterFremitid = 1,
            annenGrunn = null
        )
        korreksjon.oppgiInntektsgrunnlag(
            innloggetBeslutter,
            etInntektsgrunnlag(
                måned = YearMonth.of(
                    tilskuddsgrunnlag.tilskuddFom.year,
                    tilskuddsgrunnlag.tilskuddFom.month
                )
            )
        )
        korreksjon.oppgiBedriftKontonummer(innloggetBeslutter, "99999999999")
        assertFeilkode(Feilkode.KORREKSJONSBELOP_NEGATIVT) { korreksjon.utbetalKorreksjon(innloggetBeslutter, "9999") }
        assertFeilkode(Feilkode.KORREKSJONSBELOP_POSITIVT) {
            korreksjon.fullførKorreksjonVedTilbakekreving(
                innloggetBeslutter
            )
        }
        korreksjon.fullførKorreksjonVedOppgjort(innloggetBeslutter)
        assertThat(korreksjon.status).isEqualTo(Korreksjonstype.OPPGJORT)
    }

    @Test
    internal fun `kun inntekter som er huket av for at de er opptjent i perioden blir regnet med`() {
        val tilskuddsgrunnlag = etTilskuddsgrunnlag()
        val korreksjon = Korreksjon(
            korrigererRefusjonId = ulid(),
            korreksjonsnummer = 1,
            tidligereUtbetalt = 4055,
            korreksjonsgrunner = setOf(Korreksjonsgrunn.HENT_INNTEKTER_PÅ_NYTT),
            tilskuddsgrunnlag = tilskuddsgrunnlag,
            deltakerFnr = tilskuddsgrunnlag.deltakerFnr,
            bedriftNr = tilskuddsgrunnlag.bedriftNr,
            inntekterKunFraTiltaket = true,
            endretBruttoLønn = null,
            unntakOmInntekterFremitid = 1,
            annenGrunn = null
        )
        val inntektslinjeOpptjentIPeriode = enInntektslinje(opptjentIPeriode = true)
        val inntektslinjeIkkeOptjentIPeriode = enInntektslinje(opptjentIPeriode = false)
        val inntekter = listOf(
            inntektslinjeOpptjentIPeriode,
            inntektslinjeIkkeOptjentIPeriode
        )
        val inntektsgrunnlag = Inntektsgrunnlag(inntekter, "")
        korreksjon.oppgiBedriftKontonummer(innloggetBeslutter, "123456789")
        korreksjon.oppgiInntektsgrunnlag(innloggetBeslutter, inntektsgrunnlag)

        assertThat(korreksjon.refusjonsgrunnlag.beregning?.lønn).isEqualTo(inntektslinjeOpptjentIPeriode.beløp.toInt())
    }
}