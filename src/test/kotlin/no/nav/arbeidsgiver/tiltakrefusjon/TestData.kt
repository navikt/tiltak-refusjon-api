package no.nav.arbeidsgiver.tiltakrefusjon

import com.github.guepardoapps.kulid.ULID
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.*
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import java.time.YearMonth

fun enRefusjon(tilskuddsgrunnlag: Tilskuddsgrunnlag = etTilskuddsgrunnlag()): Refusjon {
    val deltakerFnr = "07098142678"
    val bedriftNr = "999999999"
    return Refusjon(tilskuddsgrunnlag = tilskuddsgrunnlag, bedriftNr = bedriftNr, deltakerFnr = deltakerFnr)
}

fun refusjoner(): List<Refusjon> {
    val kiellandNy = `Alexander Kielland`()
    val kiellandGammel = `Alexander Kielland`().let {
        val tilskuddFom = kiellandNy.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom.minusMonths(5)
        it.copy(
            tilskuddsgrunnlag = it.refusjonsgrunnlag.tilskuddsgrunnlag.copy(
                avtaleId = kiellandNy.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleId,
                tilskuddFom = tilskuddFom,
                tilskuddTom = kiellandNy.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddTom.minusDays(1)
            ),
            deltakerFnr = "12345678901"
        )
            .medInntektsgrunnlag(måned = YearMonth.of(tilskuddFom.year, tilskuddFom.month))
            .medBedriftKontonummer()
            .medSvarPåInntekter()
            .medSendtKravFraArbeidsgiver()
    }
    val BjørnsonUtgått = `Bjørnstjerne Bjørnson`().let {
        val tilskuddFom = Now.localDate().minusMonths(2)
        it.copy(
            deltakerFnr = "12345678901",
            tilskuddsgrunnlag = it.tilskuddsgrunnlag.copy(
                avtaleId = `Bjørnstjerne Bjørnson`().tilskuddsgrunnlag.avtaleId,
                tilskuddFom = tilskuddFom,
                tilskuddTom = Now.localDate().plusMonths(1)
            )
        )
    }
    val bjørnsonSendtKrav = `Bjørnstjerne Bjørnson`().let {
        it.medInntektsgrunnlag(
            måned = YearMonth.of(
                it.tilskuddsgrunnlag.tilskuddFom.year,
                it.tilskuddsgrunnlag.tilskuddFom.month
            )
        )
        it.medBedriftKontonummer()
        it.medSvarPåInntekter()
        it.medSendtKravFraArbeidsgiver()
    }

    return listOf(
        kiellandNy,
        kiellandGammel,
        BjørnsonUtgått,
        `Bjørnstjerne Bjørnson`(),
        bjørnsonSendtKrav,
        `Nils Nilsen`(),
        `Inger Hagerup`(),
        `Amalie Skram`(),
        `Suzanna Hansen`(),
        `Siri Hansen`(),
        `Camilla Collett`(),
        `Sigrid Undset`(),
        `Henrik Wergeland`(),
        `Jonas Lie`(),
        kiellandNy,
        kiellandGammel,
        BjørnsonUtgått,
        `Bjørnstjerne Bjørnson`(),
        bjørnsonSendtKrav,
        `Nils Nilsen`(),
        `Inger Hagerup`(),
        `Amalie Skram`(),
        `Suzanna Hansen`(),
        `Siri Hansen`(),
        `Camilla Collett`(),
        `Sigrid Undset`(),
        `Henrik Wergeland`(),
        `Jonas Lie`()

    )
}

private fun Refusjon.medSvarPåInntekter(): Refusjon {
    this.endreBruttolønn(true, null)
    return this
}

fun etTilskuddsgrunnlag() = Tilskuddsgrunnlag(
    avtaleId = ULID.random(),
    tilskuddsperiodeId = ULID.random(),
    deltakerFornavn = "",
    deltakerEtternavn = "",
    tiltakstype = Tiltakstype.SOMMERJOBB,
    deltakerFnr = "",
    veilederNavIdent = "",
    bedriftNavn = "Kiwi Majorstuen",
    bedriftNr = "",
    otpSats = 0.02,
    feriepengerSats = 0.12,
    arbeidsgiveravgiftSats = 0.141,
    lønnstilskuddsprosent = 40,
    tilskuddFom = Now.localDate().minusMonths(3).withDayOfMonth(1),
    tilskuddTom = Now.localDate().minusMonths(1).withDayOfMonth(20),
    tilskuddsbeløp = 13579,
    avtaleNr = 3456,
    løpenummer = 3,
    enhet = "1000"
)

fun `Jonas Lie`(): Refusjon {
    val deltakerFnr = "07098142678"
    val bedriftNr = "910712306"
    return Refusjon(
        tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            deltakerFnr = deltakerFnr,
            bedriftNr = bedriftNr,
            deltakerFornavn = "Jonas",
            deltakerEtternavn = "Lie",
            tilskuddsbeløp = 1357,
            veilederNavIdent = "Z123456"
        ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnr
    )
}

fun `Henrik Wergeland`(): Refusjon {
    val deltakerFnr = "07098142678"
    val bedriftNr = "990000000"
    return Refusjon(
        tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            deltakerFnr = deltakerFnr,
            bedriftNr = bedriftNr,
            deltakerFornavn = "Henrik",
            deltakerEtternavn = "Wergeland",
            tilskuddsbeløp = 1357,
            veilederNavIdent = "Z123456"
        ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnr
    )
}

fun `Sigrid Undset`(): Refusjon {
    val deltakerFnr = "07098142678"
    val bedriftNr = "990000000"
    return Refusjon(
        tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            deltakerFnr = deltakerFnr,
            bedriftNr = bedriftNr,
            deltakerFornavn = "Sigrid",
            deltakerEtternavn = "Undset",
            tilskuddsbeløp = 1357,
            veilederNavIdent = "Z123456"
        ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnr
    )
}

fun `Camilla Collett`(): Refusjon {
    val deltakerFnr = "07098142678"
    val bedriftNr = "990000000"
    return Refusjon(
        tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            deltakerFnr = deltakerFnr,
            bedriftNr = bedriftNr,
            deltakerFornavn = "Camilla",
            deltakerEtternavn = "Collett",
            tilskuddsbeløp = 1357,
            veilederNavIdent = "Z123456"
        ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnr
    )
}

fun `Alexander Kielland`(): Refusjon {
    val deltakerFnr = "07098142678"
    val bedriftNr = "999999999"
    return Refusjon(
        tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            deltakerFnr = deltakerFnr,
            bedriftNr = bedriftNr,
            deltakerFornavn = "Alexander",
            deltakerEtternavn = "Kielland",
            tilskuddsbeløp = 1357,
            veilederNavIdent = "Z123456"
        ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnr
    )
}

fun `Bjørnstjerne Bjørnson`(): Refusjon {
    val deltakerFnr = "28128521498"
    val bedriftNr = "999999999"
    return Refusjon(
        tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            deltakerFnr = deltakerFnr,
            bedriftNr = bedriftNr,
            deltakerFornavn = "Bjørnstjerne",
            deltakerEtternavn = "Bjørnson",
            tilskuddsbeløp = 20579,
            veilederNavIdent = "X123456"
        ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnr
    )
}

fun `Nils Nilsen`(): Refusjon {
    val deltakerFnr = "07049223188"
    val bedriftNr = "998877665"
    return Refusjon(
        tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            deltakerFnr = deltakerFnr,
            bedriftNr = bedriftNr,
            deltakerFornavn = "Nils",
            deltakerEtternavn = "Nilsen",
            tilskuddsbeløp = 10579,
            veilederNavIdent = "X123456"
        ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnr
    )
}

fun `Inger Hagerup`(): Refusjon {
    val deltakerFnr = "07049223190"
    val bedriftNr = "998877665"
    return Refusjon(
        tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            deltakerFornavn = "Inger",
            deltakerEtternavn = "Hagerup",
            deltakerFnr = deltakerFnr,
            bedriftNr = bedriftNr,
            tilskuddsbeløp = 10579,
            veilederNavIdent = "X123456"
        ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnr
    )
}

fun `Amalie Skram`(): Refusjon {
    val deltakerFnr = "23119409195"
    val bedriftNr = "955555555"
    return Refusjon(
        tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            deltakerFornavn = "Amalie",
            deltakerEtternavn = "Skram",
            deltakerFnr = deltakerFnr,
            bedriftNr = bedriftNr,
            tilskuddsbeløp = 10579,
            veilederNavIdent = "X123456"
        ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnr
    )
}

fun `Suzanna Hansen`(): Refusjon {
    val deltakerFnr = "23119409195"
    val bedriftNr = "999999999"
    val refusjon = Refusjon(
        tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            deltakerFornavn = "Suzanna",
            deltakerEtternavn = "Hansen",
            deltakerFnr = deltakerFnr,
            bedriftNr = bedriftNr,
            tilskuddsbeløp = 10579,
            veilederNavIdent = "X123456"
        ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnr
    )
    refusjon.let {
        it.medInntektsgrunnlag(
            måned = YearMonth.of(
                it.tilskuddsgrunnlag.tilskuddFom.year,
                it.tilskuddsgrunnlag.tilskuddFom.month
            )
        )
        it.medBedriftKontonummer()
        it.medSvarPåInntekter()
        it.medSendtKravFraArbeidsgiver()
        it.utbetalingVellykket()
    }
    return refusjon
}

fun `Siri Hansen`(): Refusjon {
    val deltakerFnr = "23119409195"
    val bedriftNr = "999999999"
    val refusjon = Refusjon(
        tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            deltakerFornavn = "Siri",
            deltakerEtternavn = "Hansen",
            deltakerFnr = deltakerFnr,
            bedriftNr = bedriftNr,
            tilskuddsbeløp = 10579,
            veilederNavIdent = "X123456"
        ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnr
    )

    refusjon.let {
        it.medInntektsgrunnlag(
            måned = YearMonth.of(
                it.tilskuddsgrunnlag.tilskuddFom.year,
                it.tilskuddsgrunnlag.tilskuddFom.month
            )
        )
        it.medBedriftKontonummer()
        it.medSvarPåInntekter()
        it.medSendtKravFraArbeidsgiver()
        it.utbetalingMislykket()
    }

    return refusjon
}

fun Refusjon.medInntektsgrunnlag(
    måned: YearMonth = Now.yearMonth(),
    inntektsgrunnlag: Inntektsgrunnlag = etInntektsgrunnlag(måned = måned),
): Refusjon {
    this.oppgiInntektsgrunnlag(inntektsgrunnlag)
    return this
}

fun Refusjon.medSendtKravFraArbeidsgiver(): Refusjon {
    this.godkjennForArbeidsgiver("")
    return this
}

fun Refusjon.medBedriftKontonummer(): Refusjon {
    this.oppgiBedriftKontonummer("12345670910")
    return this
}

fun Refusjon.medInntekterKunFraTiltaket(): Refusjon {
    this.refusjonsgrunnlag.inntekterKunFraTiltaket = true
    return this
}

fun Refusjon.copy(
    tilskuddsgrunnlag: Tilskuddsgrunnlag = this.tilskuddsgrunnlag,
    deltakerFnr: String = this.deltakerFnr
): Refusjon {
    return Refusjon(tilskuddsgrunnlag, bedriftNr, deltakerFnr)
}

fun etInntektsgrunnlag(måned: YearMonth = YearMonth.of(2020, 10), opptjentIPeriode: Boolean = true) = Inntektsgrunnlag(
    inntekter = listOf(
        Inntektslinje(
            inntektType = "LOENNSINNTEKT",
            beskrivelse = "timeloenn",
            måned = måned,
            beløp = 7777.0,
            opptjeningsperiodeTom = null,
            opptjeningsperiodeFom = null,
            skalRefunderes = opptjentIPeriode
        )
    ),
    respons = ""
)

fun enInntektslinje(måned: YearMonth = YearMonth.of(2020, 10), opptjentIPeriode: Boolean = true): Inntektslinje =
    Inntektslinje(
        inntektType = "LOENNSINNTEKT",
        beskrivelse = "timeloenn",
        måned = måned,
        beløp = 7777.0,
        opptjeningsperiodeTom = null,
        opptjeningsperiodeFom = null,
        skalRefunderes = opptjentIPeriode
    )
