package no.nav.arbeidsgiver.tiltakrefusjon

import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBruker
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.*
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import no.nav.arbeidsgiver.tiltakrefusjon.utils.ulid
import no.nav.arbeidsgiver.tiltakrefusjon.varsling.VarselType
import no.nav.arbeidsgiver.tiltakrefusjon.varsling.Varsling
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters.lastDayOfMonth
import java.util.UUID

val innloggetTestbruker = innloggetBruker("testsystem", BrukerRolle.SYSTEM)

fun innloggetBruker(identifikator: String, rolle: BrukerRolle) = object : InnloggetBruker {
    override val identifikator: String
        get() = identifikator
    override val rolle: BrukerRolle
        get() = rolle
}

fun enRefusjon(tilskuddsgrunnlag: Tilskuddsgrunnlag = etTilskuddsgrunnlag()): Refusjon {
    val deltakerFnr = "07098142678"
    val bedriftNr = "999999999"
    return Refusjon(
        tilskuddsgrunnlag = tilskuddsgrunnlag,
        bedriftNr = bedriftNr,
        deltakerFnr = deltakerFnr,
    )
}

fun enVarsling(varselType: VarselType = VarselType.KLAR ) : Varsling {
    val refusjonId = ulid()
    val varselTidspunkt = Now.localDateTime()
    return Varsling(refusjonId, varselType, varselTidspunkt)
}

fun refusjonerMedFerietrekk(): List<Refusjon> {
    val deltakerFnrMedMinusOgPlussFerietrekk = "26089638754"
    val bedriftNr = "999999999"
    val refusjon1 =  Refusjon(
        tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            deltakerFnr = deltakerFnrMedMinusOgPlussFerietrekk,
            bedriftNr = bedriftNr,
            deltakerFornavn = "Bjartulf",
            deltakerEtternavn = "Ferietrekksen",
            tilskuddsbeløp = 20579,
            veilederNavIdent = "X123456"
        ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnrMedMinusOgPlussFerietrekk
    )
    val deltakerFnrMedPlussFerietrekk = "23039648083"
    val refusjon2 =  Refusjon(
        tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            deltakerFnr = deltakerFnrMedPlussFerietrekk,
            bedriftNr = bedriftNr,
            deltakerFornavn = "Plussulf",
            deltakerEtternavn = "Ferietrekksen",
            tilskuddsbeløp = 20579,
            veilederNavIdent = "X123456"
        ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnrMedPlussFerietrekk
    )

    return listOf(refusjon1, refusjon2)
}

fun gamleUtbetalteRefusjonerOgEnNy(): List<Refusjon> {
    val deltakerFnrMedMasseUtbetalt = "08098138758"
    val bedriftNr = "999999999"
    val refusjon1 =  Refusjon(
        tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            tiltakstype = Tiltakstype.VARIG_LONNSTILSKUDD,
            deltakerFnr = deltakerFnrMedMasseUtbetalt,
            bedriftNr = bedriftNr,
            deltakerFornavn = "Olav",
            deltakerEtternavn = "Over5gsen",
            tilskuddsbeløp = 70000,
            veilederNavIdent = "X123456"
        ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnrMedMasseUtbetalt
    )
    refusjon1.let {
        it.medStortInntektsgrunnlag(
            måned = YearMonth.of(
                it.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom.year,
                it.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom.month
            ),

        )
        it.medBedriftKontonummer()
        it.medSvarPåInntekter()
        it.medBeregning()
        it.medSendtKravFraArbeidsgiver()
        it.utbetalingVellykket()
    }
    val refusjon2 =  Refusjon(
        tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            tiltakstype = Tiltakstype.VARIG_LONNSTILSKUDD,
            deltakerFnr = deltakerFnrMedMasseUtbetalt,
            bedriftNr = bedriftNr,
            deltakerFornavn = "Olav",
            deltakerEtternavn = "Over5gsen",
            tilskuddsbeløp = 70000,
            veilederNavIdent = "X123456"
        ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnrMedMasseUtbetalt
    )
    refusjon2.let {
        it.medStortInntektsgrunnlag(
            måned = YearMonth.of(
                it.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom.year,
                it.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom.month
            )
        )
        it.medBedriftKontonummer()
        it.medSvarPåInntekter()
        it.medBeregning()
        it.medSendtKravFraArbeidsgiver()
        it.utbetalingVellykket()
    }
    val refusjon3 =  Refusjon(
        tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            tiltakstype = Tiltakstype.VARIG_LONNSTILSKUDD,
            deltakerFnr = deltakerFnrMedMasseUtbetalt,
            bedriftNr = bedriftNr,
            deltakerFornavn = "Olav",
            deltakerEtternavn = "Over5gsen",
            tilskuddsbeløp = 70000,
            veilederNavIdent = "X123456"
        ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnrMedMasseUtbetalt
    )
    refusjon3.let {
        it.medStortInntektsgrunnlag(
            måned = YearMonth.of(
                it.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom.year,
                it.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom.month
            )
        )
        it.medBedriftKontonummer()
        it.medSvarPåInntekter()
        it.medBeregning()
        it.medSendtKravFraArbeidsgiver()
        it.utbetalingVellykket()
    }
    val refusjon4 =  Refusjon(
        tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            tiltakstype = Tiltakstype.VARIG_LONNSTILSKUDD,
            deltakerFnr = deltakerFnrMedMasseUtbetalt,
            bedriftNr = bedriftNr,
            deltakerFornavn = "Olav",
            deltakerEtternavn = "Over5gsen",
            tilskuddsbeløp = 70000,
            veilederNavIdent = "X123456"
        ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnrMedMasseUtbetalt
    )
    refusjon4.let {
        it.medStortInntektsgrunnlag(
            måned = YearMonth.of(
                it.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom.year,
                it.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom.month
            )
        )
        it.medBedriftKontonummer()
        it.medSvarPåInntekter()
        it.medBeregning()
        it.medSendtKravFraArbeidsgiver()
        it.utbetalingVellykket()
    }
    val refusjon5 =  Refusjon(
        tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            tiltakstype = Tiltakstype.VARIG_LONNSTILSKUDD,
            deltakerFnr = deltakerFnrMedMasseUtbetalt,
            bedriftNr = bedriftNr,
            deltakerFornavn = "Olav",
            deltakerEtternavn = "Over5gsen",
            tilskuddsbeløp = 70000,
            veilederNavIdent = "X123456"
        ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnrMedMasseUtbetalt
    )
    refusjon5.let {
        it.medStortInntektsgrunnlag(
            måned = YearMonth.of(
                it.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom.year,
                it.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom.month
            )
        )
        it.medBedriftKontonummer()
        it.medSvarPåInntekter()
        it.medBeregning()
        it.medSendtKravFraArbeidsgiver()
        it.utbetalingVellykket()
    }
    val refusjon6 =  Refusjon(
        tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            tiltakstype = Tiltakstype.VARIG_LONNSTILSKUDD,
            deltakerFnr = deltakerFnrMedMasseUtbetalt,
            bedriftNr = bedriftNr,
            deltakerFornavn = "Olav",
            deltakerEtternavn = "Over5gsen",
            tilskuddsbeløp = 70000,
            veilederNavIdent = "X123456"
        ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnrMedMasseUtbetalt
    )
    refusjon6.let {
        it.medStortInntektsgrunnlag(
            måned = YearMonth.of(
                it.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom.year,
                it.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom.month
            )
        )
        it.medBedriftKontonummer()
        it.medSvarPåInntekter()
        it.medBeregning()
        it.medSendtKravFraArbeidsgiver()
        it.utbetalingVellykket()
    }
    val refusjon7 =  Refusjon(
        tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            tiltakstype = Tiltakstype.VARIG_LONNSTILSKUDD,
            deltakerFnr = deltakerFnrMedMasseUtbetalt,
            bedriftNr = bedriftNr,
            deltakerFornavn = "Olav",
            deltakerEtternavn = "Over5gsen",
            tilskuddsbeløp = 70000,
            veilederNavIdent = "X123456"
        ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnrMedMasseUtbetalt
    )
    refusjon7.let {
        it.medStortInntektsgrunnlag(
            måned = YearMonth.of(
                it.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom.year,
                it.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom.month
            )
        )
        it.medBedriftKontonummer()
        it.medSvarPåInntekter()
        it.medBeregning()
        it.medSendtKravFraArbeidsgiver()
        it.utbetalingVellykket()
    }
    val refusjon8 =  Refusjon(
        tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            tiltakstype = Tiltakstype.VARIG_LONNSTILSKUDD,
            deltakerFnr = deltakerFnrMedMasseUtbetalt,
            bedriftNr = bedriftNr,
            deltakerFornavn = "Olav",
            deltakerEtternavn = "Over5gsen",
            tilskuddsbeløp = 70000,
            veilederNavIdent = "X123456"
        ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnrMedMasseUtbetalt
    )
    refusjon8.let {
        it.medStortInntektsgrunnlag(
            måned = YearMonth.of(
                it.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom.year,
                it.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom.month
            )
        )
        it.medBedriftKontonummer()
        it.medSvarPåInntekter()
        it.medBeregning()
        it.medSendtKravFraArbeidsgiver()
        it.utbetalingVellykket()
    }
    val refusjon9 =  Refusjon(
        tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            tiltakstype = Tiltakstype.VARIG_LONNSTILSKUDD,
            deltakerFnr = deltakerFnrMedMasseUtbetalt,
            bedriftNr = bedriftNr,
            deltakerFornavn = "Olav",
            deltakerEtternavn = "Over5gsen",
            tilskuddsbeløp = 55000,
            veilederNavIdent = "X123456"
        ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnrMedMasseUtbetalt
    )
    val refusjon10 =  Refusjon(
        tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            tiltakstype = Tiltakstype.VARIG_LONNSTILSKUDD,
            deltakerFnr = deltakerFnrMedMasseUtbetalt,
            bedriftNr = bedriftNr,
            deltakerFornavn = "Olav",
            deltakerEtternavn = "Over5gsen",
            tilskuddsbeløp = 55000,
            veilederNavIdent = "X123456"
        ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnrMedMasseUtbetalt
    )
    return listOf(refusjon1, refusjon2, refusjon3, refusjon4, refusjon5, refusjon6, refusjon7, refusjon8, refusjon9, refusjon10)
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
            .medBeregning()
            .medSendtKravFraArbeidsgiver()
    }
    val BjørnsonUtgått = `Bjørnstjerne Bjørnson`().let {
        val tilskuddFom = Now.localDate().minusMonths(2)
        it.copy(
            deltakerFnr = "12345678901",
            tilskuddsgrunnlag = it.refusjonsgrunnlag.tilskuddsgrunnlag.copy(
                avtaleId = `Bjørnstjerne Bjørnson`().refusjonsgrunnlag.tilskuddsgrunnlag.avtaleId,
                tilskuddFom = tilskuddFom,
                tilskuddTom = Now.localDate().plusMonths(1)
            )
        )
    }
    val bjørnsonSendtKrav = `Bjørnstjerne Bjørnson`().let {
        it.medInntektsgrunnlag(
            måned = YearMonth.of(
                it.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom.year,
                it.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom.month
            )
        )
        it.medBedriftKontonummer()
        it.medSvarPåInntekter()
        it.medBeregning()
        it.medSendtKravFraArbeidsgiver()
    }

    val SnorreKorreksjonLønnEtterDødsfallMedIngenInntekt  = `Snorre Sturlason`().let {
        val tilskuddFom = Now.localDate().withDayOfMonth(1);
        it.copy(
            deltakerFnr = "09078349333",
            tilskuddsgrunnlag = it.refusjonsgrunnlag.tilskuddsgrunnlag.copy(
                avtaleId = `Snorre Sturlason`().refusjonsgrunnlag.tilskuddsgrunnlag.avtaleId,
                tilskuddFom = tilskuddFom,
                tilskuddTom = Now.localDate().minusDays(1)
            )
        )
            .medInntektsgrunnlag(måned = YearMonth.of(
                it.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom.year,
                it.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom.month
            ))
    }

    fun `Jon Janson Ferietrekk minus beløp 1`(): Refusjon {
        val deltakerFnr = "08124521514"
        val bedriftNr = "910712306"
        return Refusjon(
            tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
                avtaleNr = 7,
                løpenummer = 1,
                lønnstilskuddsprosent = 60,
                otpSats = 0.03,
                feriepengerSats = 0.125,
                arbeidsgiveravgiftSats = 0.141,
                deltakerFnr = deltakerFnr,
                bedriftNr = bedriftNr,
                deltakerFornavn = "Jon",
                deltakerEtternavn = "Janson Ferietrekk minus beløp 1",
                tilskuddsbeløp = 30000,
                veilederNavIdent = "Z123456"
            ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnr
        )
    }

    fun `Jon Janson Ferietrekk minus beløp 2`(): Refusjon {
        val deltakerFnr = "08124521514"
        val bedriftNr = "910712306"
        return Refusjon(
            tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
                løpenummer = 2,
                avtaleNr = 7,
                lønnstilskuddsprosent = 60,
                otpSats = 0.03,
                feriepengerSats = 0.125,
                arbeidsgiveravgiftSats = 0.141,
                deltakerFnr = deltakerFnr,
                tilskuddsbeløp = 20000,
                bedriftNr = bedriftNr,
                deltakerFornavn = "Jon",
                deltakerEtternavn = "Janson Ferietrekk minus beløp 2",
                veilederNavIdent = "Z123456"
            ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnr
        )
    }

    fun `Jon Janson Ferietrekk minus beløp 3`(): Refusjon {
        val deltakerFnr = "08124521514"
        val bedriftNr = "910712306"
        return Refusjon(
            tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
                løpenummer = 3,
                lønnstilskuddsprosent = 60,
                otpSats = 0.03,
                feriepengerSats = 0.125,
                arbeidsgiveravgiftSats = 0.141,
                avtaleNr = 7,
                deltakerFnr = deltakerFnr,
                tilskuddsbeløp = 30000,
                bedriftNr = bedriftNr,
                deltakerFornavn = "Jon",
                deltakerEtternavn = "Janson Ferietrekk minus beløp 3",
                veilederNavIdent = "Z123456"
            ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnr
        )
    }

    fun `Jon Janson Ferietrekk minus beløp 4`(): Refusjon {
        val deltakerFnr = "08124521514"
        val bedriftNr = "910712306"
        return Refusjon(
            tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
                løpenummer = 4,
                lønnstilskuddsprosent = 60,
                otpSats = 0.03,
                feriepengerSats = 0.125,
                arbeidsgiveravgiftSats = 0.141,
                avtaleNr = 7,
                deltakerFnr = deltakerFnr,
                tilskuddsbeløp = 30000,
                bedriftNr = bedriftNr,
                deltakerFornavn = "Jon",
                deltakerEtternavn = "Janson Ferietrekk minus beløp 4",
                veilederNavIdent = "Z123456",
                avtaleFom = Now.localDate().minusMonths(1).withDayOfMonth(1),
                avtaleTom = Now.localDate().minusMonths(1).with(lastDayOfMonth()),
            ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnr
        )
    }

    return listOf(
        `Jon Janson Ferietrekk minus beløp 1`(),
        `Jon Janson Ferietrekk minus beløp 2`(),
        `Jon Janson Ferietrekk minus beløp 3`(),
        `Jon Janson Ferietrekk minus beløp 4`(),
        kiellandNy,
        kiellandGammel,
        BjørnsonUtgått,
        `Bjørnstjerne Bjørnson`().copy(
            tilskuddsgrunnlag = `Bjørnstjerne Bjørnson`().refusjonsgrunnlag.tilskuddsgrunnlag.copy(
                tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD
            )
        ),
        `Bjørnstjerne Bjørnson unntak`(),
        `Nils Nilsen`(),
        `Inger Hagerup`(),
        `Amalie Skram`(),
        `Suzanna Hansen`(),
        `Siri Hansen`(),
        `Vidar Fortidlig`(),
        `Vidar SendKrav`(),
        `Vidar Utbetalt`(),
        `Camilla Collett`(),
        `Snorre Sturlason`(),
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
        `Jonas Lie`(),
        `Geir Geirsen`(),
        `dodsfallUnderTiltakRefusjon`(),
        `Formye Ferietrekksen`(),
        SnorreKorreksjonLønnEtterDødsfallMedIngenInntekt
    )
}

private fun Refusjon.medSvarPåInntekter(): Refusjon {
    this.endreBruttolønn(true, null)
    return this
}

fun etTilskuddsgrunnlag(tiltakstype: Tiltakstype = Tiltakstype.SOMMERJOBB) = Tilskuddsgrunnlag(
    avtaleId = UUID.randomUUID().toString(),
    tilskuddsperiodeId = UUID.randomUUID().toString(),
    deltakerFornavn = "",
    deltakerEtternavn = "",
    arbeidsgiverFornavn = "Arne",
    arbeidsgiverEtternavn = "Arbeidsgiver",
    arbeidsgiverTlf = "41111111",
    tiltakstype = tiltakstype,
    deltakerFnr = "",
    veilederNavIdent = "",
    bedriftNavn = "Kiwi Majorstuen",
    bedriftNr = "999999999",
    otpSats = 0.02,
    feriepengerSats = 0.12,
    arbeidsgiveravgiftSats = 0.141,
    lønnstilskuddsprosent = 40,
    tilskuddFom = Now.localDate().minusMonths(1).withDayOfMonth(1),
    tilskuddTom = Now.localDate().minusMonths(1).with(lastDayOfMonth()),
    tilskuddsbeløp = 13579,
    avtaleNr = 3456,
    løpenummer = 3,
    resendingsnummer = 1,
    enhet = "1000",
    godkjentAvBeslutterTidspunkt = Now.localDateTime().minusMonths(3).withDayOfMonth(1),
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

fun `Snorre Sturlason`(): Refusjon {
    val deltakerFnr = "09078349333"
    val bedriftNr = "999999999"
    return Refusjon(
        tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            deltakerFnr = deltakerFnr,
            bedriftNr = bedriftNr,
            deltakerFornavn = "Snorre",
            deltakerEtternavn = "Sturlason",
            tilskuddsbeløp = 13337,
            veilederNavIdent = "Z123456"
        ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnr
    )
}

fun `Formye Ferietrekksen`(): Refusjon {
    val deltakerFnr = "28061827902"
    val bedriftNr = "999999999"
    return Refusjon(
        tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            deltakerFnr = deltakerFnr,
            bedriftNr = bedriftNr,
            deltakerFornavn = "Formye",
            deltakerEtternavn = "Ferietrekksen",
            tilskuddsbeløp = 13337,
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

fun `Geir Geirsen`(): Refusjon {
    val deltakerFnr = "18079238011"
    val bedriftNr = "999999999"
    return Refusjon(
        tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            deltakerFnr = deltakerFnr,
            bedriftNr = bedriftNr,
            deltakerFornavn = "Geir",
            deltakerEtternavn = "Geirsen",
            tilskuddsbeløp = 13337,
            veilederNavIdent = "Z123456",
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD
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

fun `Bjørnstjerne Bjørnson unntak`(): Refusjon {
    val deltakerFnr = "28128521498"
    val bedriftNr = "999999999"
    val refusjon = Refusjon(
        tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            deltakerFnr = deltakerFnr,
            bedriftNr = bedriftNr,
            deltakerFornavn = "Bjørnstjerne",
            deltakerEtternavn = "Bjørnson",
            tilskuddsbeløp = 20579,
            veilederNavIdent = "X123456"
        ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnr
    )
    refusjon.unntakOmInntekterFremitid = 4
    return refusjon
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
            veilederNavIdent = "X123456",
            tilskuddFom = Now.localDate().minusMonths(3).withDayOfMonth(2),
            tilskuddTom = Now.localDate().minusMonths(1).withDayOfMonth(21),
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
                it.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom.year,
                it.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom.month
            )
        )
        it.medBedriftKontonummer()
        it.medSvarPåInntekter()
        it.medBeregning()
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
                it.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom.year,
                it.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom.month
            )
        )
        it.medBedriftKontonummer()
        it.medSvarPåInntekter()
        it.medBeregning()
        it.medSendtKravFraArbeidsgiver()
        it.utbetalingMislykket()
    }

    return refusjon
}

fun `Vidar Fortidlig`(): Refusjon {
    val deltakerFnr = "23119409195"
    val bedriftNr = "999999999"
    val refusjon = Refusjon(
        tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            tiltakstype = Tiltakstype.VTAO,
            deltakerFornavn = "Vidar",
            deltakerEtternavn = "Olsen",
            deltakerFnr = deltakerFnr,
            bedriftNr = bedriftNr,
            tilskuddsbeløp = 6808,
            veilederNavIdent = "X123456",
            avtaleFom = Now.localDate().minusMonths(3).withDayOfMonth(1),
            avtaleTom = Now.localDate().plusYears(2).withDayOfMonth(1),
        ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnr,
    )

    refusjon.let {
        it.medBedriftKontonummer()
        it.status = RefusjonStatus.FOR_TIDLIG
    }

    return refusjon
}

fun `Vidar SendKrav`(): Refusjon {
    val deltakerFnr = "23119409195"
    val bedriftNr = "999999999"
    val refusjon = Refusjon(
        tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            tiltakstype = Tiltakstype.VTAO,
            deltakerFornavn = "Vidar",
            deltakerEtternavn = "Olsen",
            deltakerFnr = deltakerFnr,
            bedriftNr = bedriftNr,
            tilskuddsbeløp = 6808,
            veilederNavIdent = "X123456",
            avtaleFom = Now.localDate().minusMonths(3).withDayOfMonth(1),
            avtaleTom = Now.localDate().plusYears(2).withDayOfMonth(1),
        ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnr
    )

    refusjon.let {
        it.medBedriftKontonummer()
        it.status = RefusjonStatus.SENDT_KRAV
        it.godkjentAvArbeidsgiver = Now.localDate().minusMonths(1).with(lastDayOfMonth()).plusDays(1).atStartOfDay(ZoneId.of("Europe/Oslo")).toInstant()
    }

    return refusjon
}

fun `Vidar Utbetalt`(): Refusjon {
    val deltakerFnr = "23119409195"
    val bedriftNr = "999999999"
    val refusjon = Refusjon(
        tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            tiltakstype = Tiltakstype.VTAO,
            deltakerFornavn = "Vidar",
            deltakerEtternavn = "Olsen",
            deltakerFnr = deltakerFnr,
            bedriftNr = bedriftNr,
            tilskuddsbeløp = 6808,
            veilederNavIdent = "X123456",
            avtaleFom = Now.localDate().minusMonths(3).withDayOfMonth(1),
            avtaleTom = Now.localDate().plusYears(2).withDayOfMonth(1),
        ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnr
    )

    refusjon.let {
        it.medBedriftKontonummer()
        it.status = RefusjonStatus.UTBETALT
        it.godkjentAvArbeidsgiver = Now.localDate().minusMonths(1).with(lastDayOfMonth()).plusDays(1).atStartOfDay(ZoneId.of("Europe/Oslo")).toInstant()
        it.utbetaltTidspunkt = Now.localDate().minusMonths(1).with(lastDayOfMonth()).plusDays(3).atStartOfDay(ZoneId.of("Europe/Oslo")).toInstant()
    }

    return refusjon
}

fun dodsfallUnderTiltakRefusjon():Refusjon{
    val deltakerFnrMedMasseUtbetalt = "30038738743"
    val bedriftNr = "999999999"
    val refusjon =  Refusjon(
        tilskuddsgrunnlag = etTilskuddsgrunnlag().copy(
            tiltakstype = Tiltakstype.VARIG_LONNSTILSKUDD,
            deltakerFnr = deltakerFnrMedMasseUtbetalt,
            bedriftNr = bedriftNr,
            deltakerFornavn = "Grim",
            deltakerEtternavn = "Grimesen",
            tilskuddsbeløp = 70000,
            veilederNavIdent = "X123456"
        ), bedriftNr = bedriftNr, deltakerFnr = deltakerFnrMedMasseUtbetalt
    )
    return refusjon
}

fun Refusjon.medInntektsgrunnlag(
    måned: YearMonth = Now.yearMonth(),
    inntektsgrunnlag: Inntektsgrunnlag = etInntektsgrunnlag(måned = måned),
): Refusjon {
    this.oppgiInntektsgrunnlag(inntektsgrunnlag)
    return this
}

fun Refusjon.medBeregning(
): Refusjon {
    this.refusjonsgrunnlag.beregning = beregnRefusjonsbeløp(
        inntekter = this.refusjonsgrunnlag.inntektsgrunnlag!!.inntekter.toList(),
        tilskuddsgrunnlag = this.refusjonsgrunnlag.tilskuddsgrunnlag,
        tidligereUtbetalt = this.refusjonsgrunnlag.tidligereUtbetalt,
        korrigertBruttoLønn = this.refusjonsgrunnlag.endretBruttoLønn,
        fratrekkRefunderbarSum = this.refusjonsgrunnlag.refunderbarBeløp,
        forrigeRefusjonMinusBeløp = this.refusjonsgrunnlag.forrigeRefusjonMinusBeløp,
        tilskuddFom = this.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom,
        sumUtbetaltVarig = this.refusjonsgrunnlag.sumUtbetaltVarig,
        harFerietrekkForSammeMåned = this.refusjonsgrunnlag.harFerietrekkForSammeMåned)
    return this
}

fun Refusjon.medStortInntektsgrunnlag(
    måned: YearMonth = Now.yearMonth(),
    inntektsgrunnlag: Inntektsgrunnlag = etStortInntektsgrunnlag(måned = måned),
): Refusjon {
    this.oppgiInntektsgrunnlag(inntektsgrunnlag)
    return this
}

fun Refusjon.medSendtKravFraArbeidsgiver(): Refusjon {
    this.godkjennForArbeidsgiver(innloggetTestbruker)
    return this
}

fun Refusjon.medBedriftKontonummer(): Refusjon {
    this.oppgiBedriftKontonummer("12345670910")
    return this
}

fun Refusjon.medInntekterKunFraTiltaket(): Refusjon {
    this.refusjonsgrunnlag.inntekterKunFraTiltaket = true
    val gyldigKID = "2345676"
    this.refusjonsgrunnlag.bedriftKid = gyldigKID
    return this
}

fun Refusjon.copy(
    tilskuddsgrunnlag: Tilskuddsgrunnlag = this.refusjonsgrunnlag.tilskuddsgrunnlag,
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
            erOpptjentIPeriode = opptjentIPeriode
        )
    ),
    respons = ""
)

fun etStortInntektsgrunnlag(måned: YearMonth = YearMonth.of(2020, 10), opptjentIPeriode: Boolean = true) = Inntektsgrunnlag(
    inntekter = listOf(
        Inntektslinje(
            inntektType = "LOENNSINNTEKT",
            beskrivelse = "timeloenn",
            måned = måned,
            beløp = 200000.0,
            opptjeningsperiodeTom = null,
            opptjeningsperiodeFom = null,
            erOpptjentIPeriode = opptjentIPeriode
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
        erOpptjentIPeriode = opptjentIPeriode
    )
