package no.nav.arbeidsgiver.tiltakrefusjon

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Tiltakstype
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.nydatamodell.Refusjonsak
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.nydatamodell.Tilskuddsgrunnlag
import java.time.LocalDate

val TESTDATAPATH = "/src/test/resources/testdata/refusjon.json"

fun enRefusjonsak(): Refusjonsak {
    return `Alexander Kielland`()

}

fun refusjoner(): List<Refusjonsak> {
//    val mapper = jacksonObjectMapper()
//    mapper.registerKotlinModule()
//    mapper.registerModule(JavaTimeModule())
//    val refusjoner = mapper.readValue<List<Refusjonsak>>(File(System.getProperty("user.dir") + TESTDATAPATH).readText(Charsets.UTF_8))
    return listOf(`Alexander Kielland`(), `Bjørnstjerne Bjørnson`(), `Nils Nilsen`(), `Inger Hagerup`(), `Amalie Skram`())
}

fun `Alexander Kielland`(): Refusjonsak {
    val deltakerFnr = "07098142678"
    val bedriftNr = "999999999"
    return Refusjonsak(tilskuddsgrunnlag = Tilskuddsgrunnlag(
            avtaleId = "",
            tilskuddsperiodeId = "",
            deltakerFornavn = "Alexander",
            deltakerEtternavn = "Kielland",
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            deltakerFnr = deltakerFnr,
            veilederNavIdent = "",
            bedriftNavn = "Kiwi Majorstuen",
            bedriftNr = bedriftNr,
            otpSats = 0.02,
            feriepengerSats = 0.12,
            arbeidsgiveravgiftSats = 0.141,
            lønnstilskuddsprosent = 40,
            tilskuddFom = LocalDate.parse("2020-08-01"),
            tilskuddTom = LocalDate.parse("2020-10-31"),
            tilskuddsbeløp = 13579
    ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnr)
}

fun `Bjørnstjerne Bjørnson`(): Refusjonsak {
    val deltakerFnr = "28128521498"
    val bedriftNr = "998877665"
    return Refusjonsak(tilskuddsgrunnlag = Tilskuddsgrunnlag(
            avtaleId = "",
            tilskuddsperiodeId = "",
            deltakerFornavn = "Bjørnstjerne",
            deltakerEtternavn = "Bjørnson",
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            deltakerFnr = deltakerFnr,
            veilederNavIdent = "",
            bedriftNavn = "Kiwi Majorstuen",
            bedriftNr = bedriftNr,
            otpSats = 0.02,
            feriepengerSats = 0.12,
            arbeidsgiveravgiftSats = 0.141,
            lønnstilskuddsprosent = 40,
            tilskuddFom = LocalDate.parse("2020-09-01"),
            tilskuddTom = LocalDate.parse("2020-10-01"),
            tilskuddsbeløp = 10579
    ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnr)
}

fun `Nils Nilsen`(): Refusjonsak {
    val deltakerFnr = "07049223188"
    val bedriftNr = "998877665"
    return Refusjonsak(tilskuddsgrunnlag = Tilskuddsgrunnlag(
            avtaleId = "",
            tilskuddsperiodeId = "",
            deltakerFornavn = "Nils",
            deltakerEtternavn = "Nilsen",
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            deltakerFnr = deltakerFnr,
            veilederNavIdent = "",
            bedriftNavn = "Kiwi Majorstuen",
            bedriftNr = bedriftNr,
            otpSats = 0.02,
            feriepengerSats = 0.12,
            arbeidsgiveravgiftSats = 0.141,
            lønnstilskuddsprosent = 40,
            tilskuddFom = LocalDate.parse("2020-08-01"),
            tilskuddTom = LocalDate.parse("2020-11-01"),
            tilskuddsbeløp = 10579
    ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnr)
}

fun `Inger Hagerup`(): Refusjonsak {
    val deltakerFnr = "07049223190"
    val bedriftNr = "998877665"
    return Refusjonsak(tilskuddsgrunnlag = Tilskuddsgrunnlag(
            avtaleId = "",
            tilskuddsperiodeId = "",
            deltakerFornavn = "Inger",
            deltakerEtternavn = "Hagerup",
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            deltakerFnr = deltakerFnr,
            veilederNavIdent = "",
            bedriftNavn = "Kiwi Majorstuen",
            bedriftNr = bedriftNr,
            otpSats = 0.02,
            feriepengerSats = 0.12,
            arbeidsgiveravgiftSats = 0.141,
            lønnstilskuddsprosent = 40,
            tilskuddFom = LocalDate.parse("2020-08-01"),
            tilskuddTom = LocalDate.parse("2020-11-01"),
            tilskuddsbeløp = 10579
    ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnr)
}

fun `Amalie Skram`(): Refusjonsak {
    val deltakerFnr = "07049223190"
    val bedriftNr = "998877665"
    return Refusjonsak(tilskuddsgrunnlag = Tilskuddsgrunnlag(
            avtaleId = "",
            tilskuddsperiodeId = "",
            deltakerFornavn = "Amalie",
            deltakerEtternavn = "Skram",
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            deltakerFnr = deltakerFnr,
            veilederNavIdent = "",
            bedriftNavn = "Kiwi Majorstuen",
            bedriftNr = bedriftNr,
            otpSats = 0.02,
            feriepengerSats = 0.12,
            arbeidsgiveravgiftSats = 0.141,
            lønnstilskuddsprosent = 40,
            tilskuddFom = LocalDate.parse("2020-08-01"),
            tilskuddTom = LocalDate.parse("2020-11-01"),
            tilskuddsbeløp = 10579
    ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnr)
}