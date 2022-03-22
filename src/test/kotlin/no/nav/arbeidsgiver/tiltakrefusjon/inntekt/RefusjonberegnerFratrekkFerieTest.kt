package no.nav.arbeidsgiver.tiltakrefusjon.inntekt

import com.github.guepardoapps.kulid.ULID
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.*
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeGodkjentMelding
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import org.junit.jupiter.api.Test
import no.nav.arbeidsgiver.tiltakrefusjon.etInntektsgrunnlag
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate


@DirtiesContext
@SpringBootTest(properties = ["NAIS_APP_IMAGE=test", "tiltak-refusjon.inntektskomponenten.fake=false"])
@ActiveProfiles("local", "wiremock")
@AutoConfigureWireMock(port = 8090)
class RefusjonberegnerFratrekkFerieTest(
    @Autowired
    val refusjonService: RefusjonService,
) {
    val WIREMOCK_IDENT: String = "08098613316"
    val WIREMOCK_VIRKSOMHET_IDENTIFIKATOR: String = "972674818"

    fun lagEnTilskuddsperiodeGodkjentMelding(
        tilskuddFom: LocalDate,
        tilskuddTom: LocalDate,
        tiltakstype: Tiltakstype,
        tilskuddsbeløp: Int,
        deltakerFnr: String,
        bedriftNr: String
    ): TilskuddsperiodeGodkjentMelding {
        val tilskuddsperiodeGodkjentMelding = TilskuddsperiodeGodkjentMelding(
            avtaleId = ULID.random(),
            tilskuddsperiodeId = ULID.random(),
            avtaleInnholdId = ULID.random(),
            deltakerFornavn = "Henrik",
            deltakerEtternavn = "Wergeland",
            tiltakstype = tiltakstype,
            deltakerFnr = deltakerFnr,
            veilederNavIdent = "X123456",
            bedriftNavn = "Pengeløs sparebank AS",
            bedriftNr = bedriftNr,
            otpSats = 0.02,
            feriepengerSats = 0.12,
            arbeidsgiveravgiftSats = 0.141,
            lønnstilskuddsprosent = 40,
            tilskuddFom = tilskuddFom,
            tilskuddTom = tilskuddTom,
            tilskuddsbeløp = tilskuddsbeløp,
            avtaleNr = 1337,
            løpenummer = 9,
            enhet = "1104"
        )
        return tilskuddsperiodeGodkjentMelding
    }

    fun opprettRefusjonOgGjørInntektoppslag(tilskuddsperiodeGodkjentMelding: TilskuddsperiodeGodkjentMelding): Refusjon {
        val refusjon = refusjonService.opprettRefusjon(tilskuddsperiodeGodkjentMelding) ?: throw Exception()
        refusjon.status = RefusjonStatus.KLAR_FOR_INNSENDING
        refusjon.unntakOmInntekterToMånederFrem = false
        refusjon.fristForGodkjenning = Now.localDate().plusDays(1)
        refusjonService.gjørBedriftKontonummeroppslag(refusjon)
        refusjonService.gjørInntektsoppslag(refusjon)
        // Sett innhentede inntekter til opptjent i periode
        refusjon.inntektsgrunnlag?.inntekter?.filter { it.erMedIInntektsgrunnlag() }?.forEach { it.skalRefunderes = true }
        // Bekreft at alle inntektene kun er fra tiltaket
        refusjon.endreBruttolønn(true, null)
        return refusjon;
    }

    fun `vis utregning med feriefratrekk`(refusjon: Refusjon, TREKKFORFERIEGRUNNLAG: Int): Int {
        val beregning: Beregning = refusjon.refusjonsgrunnlag.beregning ?: throw Exception()
        val (lønn, _, feriepenger,tjenestepensjon, arbeidsgiveravgift) = beregning

        return ((lønn - TREKKFORFERIEGRUNNLAG + feriepenger + tjenestepensjon + arbeidsgiveravgift) * 0.40).toInt()
    }

    @Test
    fun `hent inntektsoppslag som har feriefratrekk og beregn`() {
        val TREKKFORFERIEGRUNNLAG: Int = 7500 // trekk grunnlag fra inntektoppslag

        val tilskuddsperiodeGodkjentMelding: TilskuddsperiodeGodkjentMelding = lagEnTilskuddsperiodeGodkjentMelding(
            LocalDate.of(2021, 7, 1),
            LocalDate.of(2021, 7, 31),
            Tiltakstype.SOMMERJOBB,
            60000,
            WIREMOCK_IDENT,
            WIREMOCK_VIRKSOMHET_IDENTIFIKATOR,
        )
        val refusjon = opprettRefusjonOgGjørInntektoppslag(tilskuddsperiodeGodkjentMelding)

        assert(refusjon.refusjonsgrunnlag.beregning!!.refusjonsbeløp == `vis utregning med feriefratrekk`(refusjon, TREKKFORFERIEGRUNNLAG))
        assert(refusjon.refusjonsgrunnlag.beregning!!.fratrekkLønnFerie == TREKKFORFERIEGRUNNLAG)
    }

    @Test
    fun `hent inntektsoppslag som ikke har feriefratrekk og beregn`() {
        val TREKKFORFERIEGRUNNLAG: Int = 0 // trekk grunnlag fra inntektoppslag

        val tilskuddsperiodeGodkjentMelding: TilskuddsperiodeGodkjentMelding = lagEnTilskuddsperiodeGodkjentMelding(
            LocalDate.of(2021, 6, 1),
            LocalDate.of(2021, 6, 30),
            Tiltakstype.SOMMERJOBB,
            60000,
            WIREMOCK_IDENT,
            WIREMOCK_VIRKSOMHET_IDENTIFIKATOR,
        )
        val refusjon = opprettRefusjonOgGjørInntektoppslag(tilskuddsperiodeGodkjentMelding)

        assert(refusjon.refusjonsgrunnlag.beregning!!.refusjonsbeløp == `vis utregning med feriefratrekk`(refusjon, TREKKFORFERIEGRUNNLAG))
        assert(refusjon.refusjonsgrunnlag.beregning!!.fratrekkLønnFerie == TREKKFORFERIEGRUNNLAG)
    }

    @Test
    fun `sjekk at leggSammenTrekkGrunnlag returnerer primiviteInt-eller-double`() {
        val etInntektsgrunnlag = etInntektsgrunnlag()
        val leggSammenTrekkGrunnlag: Double = leggSammenTrekkGrunnlag(etInntektsgrunnlag.inntekter.toList())

        assertThat(leggSammenTrekkGrunnlag).isNotNull

    }
}