package no.nav.arbeidsgiver.tiltakrefusjon

import com.github.guepardoapps.kulid.ULID
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.*
import java.time.LocalDate
import java.time.YearMonth

fun enRefusjon(tilskuddsgrunnlag: Tilskuddsgrunnlag = etTilskuddsgrunnlag()): Refusjon {
    val deltakerFnr = "07098142678"
    val bedriftNr = "999999999"
    return Refusjon(tilskuddsgrunnlag = tilskuddsgrunnlag, bedriftNr = bedriftNr, deltakerFnr = deltakerFnr)
}

fun refusjoner(): List<Refusjon> {
    val kiellandNy = `Alexander Kielland`()
    val kiellandGammel = `Alexander Kielland`().let {
        val tilskuddFom = kiellandNy.tilskuddsgrunnlag.tilskuddFom.minusMonths(5)
        it.copy(
            tilskuddsgrunnlag = it.tilskuddsgrunnlag.copy(
                avtaleId = kiellandNy.tilskuddsgrunnlag.avtaleId,
                tilskuddFom = tilskuddFom,
                tilskuddTom = kiellandNy.tilskuddsgrunnlag.tilskuddTom.minusDays(1)
            )
        )
            .medInntektsgrunnlag(måned = YearMonth.of(tilskuddFom.year, tilskuddFom.month))
            .medBedriftKontonummer()
            .medSendtKravFraArbeidsgiver()
    }
    val BjørnsonUtgått = `Bjørnstjerne Bjørnson`().let {
        val tilskuddFom = LocalDate.now().minusMonths(2)
        it.copy(
            deltakerFnr = "12345678901",
            tilskuddsgrunnlag = it.tilskuddsgrunnlag.copy(
                avtaleId = `Bjørnstjerne Bjørnson`().tilskuddsgrunnlag.avtaleId,
                tilskuddFom = tilskuddFom,
                tilskuddTom = LocalDate.now().plusMonths(1)
            )
        )
    }
    return listOf(
        kiellandNy,
        kiellandGammel,
        BjørnsonUtgått,
        `Bjørnstjerne Bjørnson`(),
        `Nils Nilsen`(),
        `Inger Hagerup`(),
        `Amalie Skram`()
    )
}

fun etTilskuddsgrunnlag() = Tilskuddsgrunnlag(
    avtaleId = ULID.random(),
    tilskuddsperiodeId = ULID.random(),
    deltakerFornavn = "",
    deltakerEtternavn = "",
    tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
    deltakerFnr = "",
    veilederNavIdent = "",
    bedriftNavn = "Kiwi Majorstuen",
    bedriftNr = "",
    otpSats = 0.02,
    feriepengerSats = 0.12,
    arbeidsgiveravgiftSats = 0.141,
    lønnstilskuddsprosent = 40,
    tilskuddFom = LocalDate.now().minusMonths(3).withDayOfMonth(1),
    tilskuddTom = LocalDate.now().minusMonths(1).withDayOfMonth(20),
    tilskuddsbeløp = 13579,
    avtaleNr = 3456,
    løpenummer = 3,
    enhet = "1000"
)

fun `Alexander Kielland`(): Refusjon {
    val deltakerFnr = "07098142678"
    val bedriftNr = "999999999"
    return Refusjon(
        tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            deltakerFnr = deltakerFnr,
            bedriftNr = bedriftNr,
            deltakerFornavn = "Alexander",
            deltakerEtternavn = "Kielland",
            tilskuddsbeløp = 1357
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
            tilskuddsbeløp = 10579
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
            tilskuddsbeløp = 10579
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
            tilskuddsbeløp = 10579
        ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnr
    )
}

fun Refusjon.medInntektsgrunnlag(
    måned: YearMonth = YearMonth.now(),
    inntektsgrunnlag: Inntektsgrunnlag = etInntektsgrunnlag(måned = måned),
): Refusjon {
    this.oppgiInntektsgrunnlag(inntektsgrunnlag, "")
    return this
}

fun Refusjon.medSendtKravFraArbeidsgiver(): Refusjon {
    this.godkjennForArbeidsgiver()
    return this
}

fun Refusjon.medBedriftKontonummer(): Refusjon {
    this.oppgiBedriftKontonummer("12345670910")
    return this
}

fun etInntektsgrunnlag(måned: YearMonth = YearMonth.of(2020, 10)) = Inntektsgrunnlag(
    inntekter = listOf(
        Inntektslinje(
            inntektType = "LOENNSINNTEKT",
            beskrivelse = "",
            måned = måned,
            beløp = 7777.0,
            opptjeningsperiodeTom = null,
            opptjeningsperiodeFom = null
        )
    ),
    respons = ""
)
