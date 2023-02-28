package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.*
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import no.nav.arbeidsgiver.tiltakrefusjon.utils.antallMånederEtter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.RuntimeException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

internal class RefusjonTest {


    @Test
    fun `kan sette status til UTBETALING_FEILET når refusjon har status SENDT_KRAV eller UTBETALT`() {
        val refusjon = enRefusjon()

        refusjon.status = RefusjonStatus.SENDT_KRAV
        refusjon.utbetalingMislykket()
        assertThat(refusjon.status).isEqualTo(RefusjonStatus.UTBETALING_FEILET)

        refusjon.status = RefusjonStatus.UTBETALT
        refusjon.utbetalingMislykket()
        assertThat(refusjon.status).isEqualTo(RefusjonStatus.UTBETALING_FEILET)
    }

    @Test
    fun `kan sette status til utbetalt når refusjon har status SENDT_KRAV eller UTBETALING FEILET`() {
        // GITT
        val refusjon = enRefusjon()
        refusjon.status = RefusjonStatus.SENDT_KRAV

        // NÅR
        refusjon.utbetalingVellykket()
        assertThat(refusjon.status).isEqualTo(RefusjonStatus.UTBETALT)
        assertThat(refusjon.utbetaltTidspunkt).isBefore(Now.instant())

        refusjon.status = RefusjonStatus.UTBETALING_FEILET
        refusjon.utbetalingVellykket()
        assertThat(refusjon.utbetaltTidspunkt).isBefore(Now.instant())
        assertThat(refusjon.status).isEqualTo(RefusjonStatus.UTBETALT)
    }

    @Test
    fun `kan ikke sette status til utbetalt når refusjon har status KLAR_FOR_INNSENDING`() {
        // GITT
        val refusjon = enRefusjon()

        // NÅR
        refusjon.utbetalingVellykket()

        assertThat(refusjon.status).isNotEqualTo(RefusjonStatus.UTBETALT)
    }


    // Godkjennelse arbeidsgiver
    @Test
    fun `kan ikke godkjenne for ag uten å ha tatt stilling til alle inntekstlinjer`() {
        val enInntektslinjeIkkeTattStillingTilOpptjening = Inntektslinje(
            inntektType = "LOENNSINNTEKT",
            beskrivelse = "timeloenn",
            måned = YearMonth.of(2020, 10),
            beløp = 7777.0,
            opptjeningsperiodeTom = null,
            opptjeningsperiodeFom = null,
            erOpptjentIPeriode = null
        )
        val enInntektslinjeOpptjentIPeriode = enInntektslinje()
        val inntektsgrunnlag = Inntektsgrunnlag(listOf(enInntektslinjeIkkeTattStillingTilOpptjening, enInntektslinjeOpptjentIPeriode), "")

        val refusjon = enRefusjon().medBedriftKontonummer().medInntektsgrunnlag(inntektsgrunnlag = inntektsgrunnlag)
        refusjon.endreBruttolønn(true, null)

        // 1 inntektslinje er ikke tatt stilling til
        assertFeilkode(Feilkode.IKKE_TATT_STILLING_TIL_ALLE_INNTEKTSLINJER) { refusjon.godkjennForArbeidsgiver("") }

        // Tar stilling til alle inntektslinjer
        refusjon.setInntektslinjeTilOpptjentIPeriode(enInntektslinjeIkkeTattStillingTilOpptjening.id, true)
        refusjon.godkjennForArbeidsgiver("")
        assertThat(refusjon.godkjentAvArbeidsgiver).isNotNull
    }

    @Test
    fun `kan ikke godkjenne for ag uten beregning`() {
        val refusjon = enRefusjon()
        assertFeilkode(Feilkode.INGEN_INNTEKTER) { refusjon.godkjennForArbeidsgiver("") }
    }

    @Test
    fun `kan godkjenne for ag med beregning`() {
        val refusjon = enRefusjon().medBedriftKontonummer().medInntekterKunFraTiltaket().medInntektsgrunnlag()
        refusjon.godkjennForArbeidsgiver("")
        assertThat(refusjon.godkjentAvArbeidsgiver).isNotNull
        assertThat(refusjon.status).isEqualTo(RefusjonStatus.SENDT_KRAV)
    }

    @Test
    fun `kan ikke godkjenne for ag to ganger`() {
        val refusjon = enRefusjon().medInntekterKunFraTiltaket().medBedriftKontonummer().medInntektsgrunnlag().medSendtKravFraArbeidsgiver()
        assertFeilkode(Feilkode.UGYLDIG_STATUS) { refusjon.godkjennForArbeidsgiver("") }
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
        ).medInntekterKunFraTiltaket().medBedriftKontonummer().medInntektsgrunnlag()
        refusjon.godkjennForArbeidsgiver("")
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
        assertFeilkode(Feilkode.UGYLDIG_STATUS) { refusjon.godkjennForArbeidsgiver("") }
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
    fun `sett stauts til UTGÅTT`() {
        // Nå klar for innsending
        val refusjon = enRefusjon(
            etTilskuddsgrunnlag().copy(
                tilskuddFom = Now.localDate().minusMonths(1),
                tilskuddTom = Now.localDate().minusDays(1)
            )
        )
        Now.fixedDate(LocalDate.now().plusMonths(3))
        //Frist skal nå bli utgått ved neste sjekk
        refusjon.gjørRefusjonUtgått()
        assertThat(refusjon.status).isEqualTo(RefusjonStatus.UTGÅTT)
        Now.resetClock()
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
    fun `oppdater status til GODKJENT_MINUSBELØP`() {
        val refusjon = enRefusjon(
            etTilskuddsgrunnlag().copy(
                tilskuddFom = Now.localDate().minusDays(2),
                tilskuddTom = Now.localDate().minusDays(1),
                tilskuddsbeløp = -200
            )
        ).medInntekterKunFraTiltaket().medInntektsgrunnlag()
        refusjon.oppgiBedriftKontonummer("10000008145")
        refusjon.godkjennForArbeidsgiver("12345678901")
        assertThat(refusjon.status).isEqualTo(RefusjonStatus.GODKJENT_MINUSBELØP)
    }

    @Test
    fun `oppdater status til GODKJENT_NULLBELØP`() {
        val refusjon = enRefusjon(
            etTilskuddsgrunnlag().copy(
                tilskuddFom = Now.localDate().minusDays(2),
                tilskuddTom = Now.localDate().minusDays(1),
                tilskuddsbeløp = 0
            )
        ).medInntekterKunFraTiltaket().medInntektsgrunnlag()
        refusjon.oppgiBedriftKontonummer("10000008145")
        refusjon.godkjennForArbeidsgiver("12345678901")
        assertThat(refusjon.status).isEqualTo(RefusjonStatus.GODKJENT_NULLBELØP)
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
        assertThat(refusjon.refusjonsgrunnlag.bedriftKontonummer).isEqualTo("10000008145")
    }

    @Test
    fun `ikke kunne sende inn refusjon uten kontonummer`() {
        val refusjon = enRefusjon(
            etTilskuddsgrunnlag().copy(
                tilskuddFom = Now.localDate().minusDays(2),
                tilskuddTom = Now.localDate().minusDays(1)
            )
        ).medInntekterKunFraTiltaket().medInntektsgrunnlag()
        assertFeilkode(Feilkode.INGEN_BEDRIFTKONTONUMMER) { refusjon.godkjennForArbeidsgiver("") }
        refusjon.oppgiBedriftKontonummer("10000008145")
        refusjon.godkjennForArbeidsgiver("")
        assertThat(refusjon.status).isEqualTo(RefusjonStatus.SENDT_KRAV)
    }

    @Test
    internal fun `har inntekt for alle måneder i tilskuddsperioden`() {
        val refusjon = enRefusjon(
            etTilskuddsgrunnlag().copy(
                tilskuddFom = Now.localDate().minusDays(2),
                tilskuddTom = Now.localDate().minusDays(1)
            )
        ).medInntektsgrunnlag(
            YearMonth.now(),
            Inntektsgrunnlag(
                inntekter = listOf(
                    Inntektslinje(
                        "LOENNSINNTEKT",
                        "fastloenn",
                        99.0,
                        YearMonth.now(),
                        null,
                        null
                    ),
                    Inntektslinje(
                        "LOENNSINNTEKT",
                        "fastloenn",
                        99.0,
                        YearMonth.now(),
                        null,
                        null
                    ),
                    Inntektslinje(
                        "LOENNSINNTEKT",
                        "fastloenn",
                        99.0,
                        YearMonth.now().minusMonths(1),
                        null,
                        null
                    ),
                    Inntektslinje(
                        "LOENNSINNTEKT",
                        "fastloenn",
                        99.0,
                        YearMonth.now().plusMonths(1),
                        null,
                        null
                    )
                ), respons = ""
            )
        )
        assertThat(refusjon.harInntektIAlleMåneder()).isTrue
    }

    @Test
    internal fun `har ikke inntekt for alle måneder`() {
        val refusjon = enRefusjon(
            etTilskuddsgrunnlag().copy(
                tilskuddFom = Now.localDate().minusMonths(1).minusDays(2),
                tilskuddTom = Now.localDate().minusDays(1)
            )
        ).medInntektsgrunnlag(
            YearMonth.now(), Inntektsgrunnlag(
                inntekter = listOf(
                    Inntektslinje("LOENNSINNTEKT", "fastloenn", 99.0, YearMonth.now(), null, null),
                    Inntektslinje("LOENNSINNTEKT", "feriepenger", 99.0, YearMonth.now().minusMonths(1), null, null)
                ), respons = ""
            )
        )
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
    internal fun `kun avhukede inntetslinjer blir medregnet`() {
        val inntektslinjeOpptjentIPeriode = enInntektslinje(opptjentIPeriode = true)
        val inntektslinjeIkkeOptjentIPeriode = enInntektslinje(opptjentIPeriode = false)
        val inntekter = listOf(inntektslinjeOpptjentIPeriode, inntektslinjeIkkeOptjentIPeriode)
        val inntektsgrunnlag = Inntektsgrunnlag(inntekter, "")

        val refusjon = enRefusjon().medBedriftKontonummer().medInntekterKunFraTiltaket()
        refusjon.oppgiInntektsgrunnlag(inntektsgrunnlag)
        assertThat(refusjon.refusjonsgrunnlag.beregning?.lønn).isEqualTo(inntektslinjeOpptjentIPeriode.beløp.toInt())
    }

    // @Test
    // internal fun `korreksjon`() {
    //     val refusjon = enRefusjon().medInntektsgrunnlag().medBedriftKontonummer().medSendtKravFraArbeidsgiver()
    //     val korreksjon = refusjon.opprettKorreksjonsutkast(setOf(Korreksjonsgrunn.UTBETALT_HELE_TILSKUDDSBELØP))
    //     assertThat(refusjon.tilskuddsgrunnlag).isEqualTo(korreksjon.tilskuddsgrunnlag)
    //     assertThat(refusjon.korrigeresAvId).isEqualTo(korreksjon.id)
    //     assertThat(korreksjon.korreksjonAvId).isEqualTo(refusjon.id)
    //     assertThat(korreksjon.status).isEqualTo(RefusjonStatus.KORREKSJON_UTKAST)
    //
    //     // Kan kun ha en korreksjon av refusjonen
    //     assertFeilkode(Feilkode.HAR_KORREKSJON) { refusjon.opprettKorreksjonsutkast(emptySet()) }
    // }

    @Test
    internal fun `korreksjon av uriktig status`() {
        val refusjon = enRefusjon().medInntektsgrunnlag().medBedriftKontonummer()
        assertFeilkode(Feilkode.UGYLDIG_STATUS) { refusjon.opprettKorreksjonsutkast(setOf(Korreksjonsgrunn.UTBETALT_HELE_TILSKUDDSBELØP)) }
    }

    @Test
    internal fun `forleng frist`() {
        val refusjon = enRefusjon().medInntektsgrunnlag().medBedriftKontonummer()
        val opprinneligFrist = refusjon.fristForGodkjenning
        val sisteDagDetErMuligÅForlengeTil = antallMånederEtter(refusjon.tilskuddsgrunnlag.tilskuddTom, 3)

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
    internal fun `forlengelse av frist skal kunne gjøres 3 måned etter godkjentAvBeslutter hvis den ble godkjent etter tilskuddTom`() {
        val refusjon = enRefusjon(
            etTilskuddsgrunnlag().copy(
                tilskuddFom = Now.localDate().minusMonths(2).minusDays(1),
                tilskuddTom = Now.localDate().minusMonths(1).minusDays(1),
                godkjentAvBeslutterTidspunkt = Now.localDateTime()
            )
        )
        val godkjentAvBeslutterTidspunkt = refusjon.tilskuddsgrunnlag.godkjentAvBeslutterTidspunkt.toLocalDate()
        val sisteDagDetErMuligÅForlengeTil = antallMånederEtter(godkjentAvBeslutterTidspunkt, 3)

        assertFeilkode(Feilkode.FOR_LANG_FORLENGELSE_AV_FRIST) {
            refusjon.forlengFrist(sisteDagDetErMuligÅForlengeTil.plusDays(1), "", "")
        }

        // Positiv test
        refusjon.forlengFrist(sisteDagDetErMuligÅForlengeTil, "", "")
        assertThat(refusjon.fristForGodkjenning).isEqualTo(sisteDagDetErMuligÅForlengeTil)
        assertThat(refusjon.forrigeFristForGodkjenning).isEqualTo(antallMånederEtter(refusjon.tilskuddsgrunnlag.godkjentAvBeslutterTidspunkt.toLocalDate(), 2))
    }

    @Test
    internal fun `godkjent tilskuddsperiode som er ferdig før den godkjennes får 2mnd frist`() {
        val iDag = LocalDate.now()
        val tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            tilskuddFom = iDag.minusMonths(2),
            tilskuddTom = iDag.minusMonths(1),
            godkjentAvBeslutterTidspunkt = LocalDateTime.now())
        val refusjon = enRefusjon(tilskuddsgrunnlag)

        val godkjentAvBeslutterTidspunkt = tilskuddsgrunnlag.godkjentAvBeslutterTidspunkt.toLocalDate()
        assertThat(refusjon.fristForGodkjenning).isEqualTo(antallMånederEtter(godkjentAvBeslutterTidspunkt, 2))
    }

    @Test
    internal fun `godkjent tilskuddsperiode som ikke enda er ferdig får frist 2mnd etter tilskuddsperiode slutt`() {
        val iDag = LocalDate.now()
        val tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            tilskuddFom = iDag,
            tilskuddTom = iDag.plusMonths(1),
            godkjentAvBeslutterTidspunkt = LocalDateTime.now())
        val refusjon = enRefusjon(tilskuddsgrunnlag)
        assertThat(refusjon.fristForGodkjenning).isEqualTo(antallMånederEtter(tilskuddsgrunnlag.tilskuddTom, 2))
    }

    @Disabled
    @Test
    internal fun `forleng frist på utgått refusjon skal endre status tilbake til klar for innsending`() {
        val refusjon = enRefusjon(
            etTilskuddsgrunnlag().copy(
                tilskuddFom = Now.localDate().minusMonths(2).minusDays(1),
                tilskuddTom = Now.localDate().minusMonths(2).minusDays(1)
            )
        )

        val idag = Now.localDate()
        refusjon.forlengFrist(idag, "", "")
        assertThat(refusjon.fristForGodkjenning).isEqualTo(idag)
        assertThat(refusjon.status).isEqualTo(RefusjonStatus.KLAR_FOR_INNSENDING)
    }

    // @Test
    // internal fun `utbetal korreksjon, etterbetaling`() {
    //     val refusjon = enRefusjon().medInntektsgrunnlag().medBedriftKontonummer().medSendtKravFraArbeidsgiver()
    //     val korreksjon = refusjon.opprettKorreksjonsutkast(setOf())
    //     korreksjon.gjørBeregning("", 0)
    //     korreksjon.utbetalKorreksjon("X123456", "Y123456", kostnadssted)
    //     assertThat(korreksjon.status).isEqualTo(RefusjonStatus.KORREKSJON_SENDT_TIL_UTBETALING)
    // }
    //
    // @Test
    // internal fun `utbetal korreksjon, etterbetaling, feilsituasjoner`() {
    //     val refusjon = enRefusjon().medInntektsgrunnlag().medBedriftKontonummer().medSendtKravFraArbeidsgiver()
    //     val korreksjon = refusjon.opprettKorreksjonsutkast(setOf())
    //     korreksjon.gjørBeregning("", 0)
    //     assertFeilkode(Feilkode.SAMME_SAKSBEHANDLER_OG_BESLUTTER) { korreksjon.utbetalKorreksjon(
    //         "X123456",
    //         "X123456",
    //         kostnadssted
    //     ) }
    //     assertFeilkode(Feilkode.INGEN_BESLUTTER) { korreksjon.utbetalKorreksjon("X123456", "", kostnadssted) }
    //     korreksjon.bedriftKontonummer = null
    //     assertFeilkode(Feilkode.INGEN_BEDRIFTKONTONUMMER) { korreksjon.utbetalKorreksjon(
    //         "X123456",
    //         "Y123456",
    //         kostnadssted
    //     ) }
    //
    //     // er ikke en etterbetaling, skal ikke kunne utbetale korreksjonen
    //     korreksjon.gjørBeregning("", 1000000)
    //     assertFeilkode(Feilkode.KORREKSJONSBELOP_NEGATIVT) { korreksjon.utbetalKorreksjon(
    //         "X123456",
    //         "Y123456",
    //         kostnadssted
    //     ) }
    // }
    //
    // @Test
    // internal fun `fullfør korreksjon, tilbakekreving`() {
    //     val refusjon = enRefusjon().medInntektsgrunnlag().medBedriftKontonummer().medSendtKravFraArbeidsgiver()
    //     val korreksjon = refusjon.opprettKorreksjonsutkast(setOf())
    //     korreksjon.gjørBeregning("", 1000000)
    //     korreksjon.fullførKorreksjonVedTilbakekreving("X123456")
    //     assertThat(korreksjon.status).isEqualTo(RefusjonStatus.KORREKSJON_SKAL_TILBAKEKREVES)
    // }
    //
    // @Test
    // internal fun `fullfør korreksjon, ikke tilbakekreving allikevel`() {
    //     val refusjon = enRefusjon().medInntektsgrunnlag().medBedriftKontonummer().medSendtKravFraArbeidsgiver()
    //     val korreksjon = refusjon.opprettKorreksjonsutkast(setOf())
    //     korreksjon.gjørBeregning("", refusjon.beregning!!.refusjonsbeløp)
    //     assertFeilkode(Feilkode.KORREKSJONSBELOP_POSITIVT) { korreksjon.fullførKorreksjonVedTilbakekreving("X123456") }
    // }
    //
    // @Test
    // internal fun `fullfør korreksjon, går i 0`() {
    //     val refusjon = enRefusjon().medInntektsgrunnlag().medBedriftKontonummer().medSendtKravFraArbeidsgiver()
    //     val korreksjon = refusjon.opprettKorreksjonsutkast(setOf())
    //     korreksjon.gjørBeregning("", refusjon.beregning!!.refusjonsbeløp)
    //     korreksjon.fullførKorreksjonVedOppgjort("X123456")
    //     assertThat(korreksjon.status).isEqualTo(RefusjonStatus.KORREKSJON_OPPGJORT)
    // }
    //
    // @Test
    // internal fun `fullfør korreksjon, går ikke i 0 allikevel`() {
    //     val refusjon = enRefusjon().medInntektsgrunnlag().medBedriftKontonummer().medSendtKravFraArbeidsgiver()
    //     val korreksjon = refusjon.opprettKorreksjonsutkast(setOf())
    //     korreksjon.gjørBeregning("", refusjon.beregning!!.refusjonsbeløp + 1)
    //     assertFeilkode(Feilkode.KORREKSJONSBELOP_IKKE_NULL) { korreksjon.fullførKorreksjonVedOppgjort("X123456") }
    // }
}

