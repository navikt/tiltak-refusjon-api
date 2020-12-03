package no.nav.arbeidsgiver.tiltakrefusjon

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Tiltakstype
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Tilskuddsgrunnlag
import java.time.LocalDate

fun enRefusjon(): Refusjon {
    return `Alexander Kielland`()
}

fun refusjoner(): List<Refusjon> {
    return listOf(`Alexander Kielland`(), `Bjørnstjerne Bjørnson`(), `Nils Nilsen`(), `Inger Hagerup`(), `Amalie Skram`())
}

val etTilskuddsgrunnlag = Tilskuddsgrunnlag(
        avtaleId = "",
        tilskuddsperiodeId = "",
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
        tilskuddFom = LocalDate.parse("2020-08-01"),
        tilskuddTom = LocalDate.parse("2020-10-31"),
        tilskuddsbeløp = 13579
)

fun `Alexander Kielland`(): Refusjon {
    val deltakerFnr = "07098142678"
    val bedriftNr = "999999999"
    return Refusjon(tilskuddsgrunnlag = etTilskuddsgrunnlag.copy(
            deltakerFnr = deltakerFnr,
            bedriftNr = bedriftNr,
            deltakerFornavn = "Alexander",
            deltakerEtternavn = "Kielland",
            tilskuddsbeløp = 13579
    ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnr)
}

fun `Bjørnstjerne Bjørnson`(): Refusjon {
    val deltakerFnr = "28128521498"
    val bedriftNr = "998877665"
    return Refusjon(tilskuddsgrunnlag = etTilskuddsgrunnlag.copy(
            deltakerFnr = deltakerFnr,
            bedriftNr = bedriftNr,
            deltakerFornavn = "Bjørnstjerne",
            deltakerEtternavn = "Bjørnson",
            tilskuddsbeløp = 10579,
    ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnr)
}

fun `Nils Nilsen`(): Refusjon {
    val deltakerFnr = "07049223188"
    val bedriftNr = "998877665"
    return Refusjon(tilskuddsgrunnlag = etTilskuddsgrunnlag.copy(
            deltakerFnr = deltakerFnr,
            bedriftNr = bedriftNr,
            deltakerFornavn = "Nils",
            deltakerEtternavn = "Nilsen",
            tilskuddsbeløp = 10579
    ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnr)
}

fun `Inger Hagerup`(): Refusjon {
    val deltakerFnr = "07049223190"
    val bedriftNr = "998877665"
    return Refusjon(tilskuddsgrunnlag = etTilskuddsgrunnlag.copy(
            deltakerFornavn = "Inger",
            deltakerEtternavn = "Hagerup",
            deltakerFnr = deltakerFnr,
            bedriftNr = bedriftNr,
            tilskuddsbeløp = 10579
    ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnr)
}

fun `Amalie Skram`(): Refusjon {
    val deltakerFnr = "23119409195"
    val bedriftNr = "955555555"
    return Refusjon(tilskuddsgrunnlag = etTilskuddsgrunnlag.copy(
            deltakerFornavn = "Amalie",
            deltakerEtternavn = "Skram",
            deltakerFnr = deltakerFnr,
            bedriftNr = bedriftNr,
            tilskuddsbeløp = 10579
    ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnr)
}