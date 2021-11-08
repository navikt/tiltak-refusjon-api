package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.`Bjørnstjerne Bjørnson`
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeAnnullertMelding
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeForkortetMelding
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeGodkjentMelding
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(properties = ["NAIS_APP_IMAGE=test"])
@ActiveProfiles("local")
@AutoConfigureWireMock(port = 8090)
class RefusjonServiceTest(
    @Autowired
    val refusjonService: RefusjonService,
    @Autowired
    val refusjonRepository: RefusjonRepository
) {
    @Test
    fun `oppretter, forkorter, og forlenger`() {
        val deltakerFnr = "00000000000"
        val tilskuddMelding = TilskuddsperiodeGodkjentMelding(
            avtaleId = "1",
            tilskuddsbeløp = 1000,
            tiltakstype = Tiltakstype.SOMMERJOBB,
            deltakerEtternavn = "Mus",
            deltakerFornavn = "Mikke",
            arbeidsgiveravgiftSats = 0.101,
             avtaleInnholdId = "2",
            bedriftNavn = "Bedriften AS",
            bedriftNr = "999999999",
            deltakerFnr = deltakerFnr,
            feriepengerSats = 0.141,
            otpSats = 0.02,
            tilskuddFom = Now.localDate().minusWeeks(4).plusDays(1),
            tilskuddTom = Now.localDate(),
            tilskuddsperiodeId = "3",
            veilederNavIdent = "X123456",
            lønnstilskuddsprosent = 60,
            avtaleNr = 3456,
            løpenummer = 3,
            enhet = "1000"
        )
        refusjonService.opprettRefusjon(tilskuddMelding)
        var lagretRefusjon = refusjonRepository.findAllByDeltakerFnr(deltakerFnr)[0]
        assertThat(lagretRefusjon.tilskuddsgrunnlag).isNotNull
        assertThat(lagretRefusjon.status).isEqualTo(RefusjonStatus.FOR_TIDLIG)

        // Forkorting
        val forkortetTilskuddTom = lagretRefusjon.tilskuddsgrunnlag.tilskuddTom.minusDays(1)
        val nyttBeløp = 19
        refusjonService.forkortRefusjon(TilskuddsperiodeForkortetMelding(lagretRefusjon.tilskuddsgrunnlag.tilskuddsperiodeId, nyttBeløp, forkortetTilskuddTom))
        lagretRefusjon = refusjonRepository.findByIdOrNull(lagretRefusjon.id) ?: throw RuntimeException()
        assertThat(lagretRefusjon.tilskuddsgrunnlag.tilskuddTom).isEqualTo(forkortetTilskuddTom)
        assertThat(lagretRefusjon.tilskuddsgrunnlag.tilskuddsbeløp).isEqualTo(nyttBeløp)
        assertThat(lagretRefusjon.status).isEqualTo(RefusjonStatus.KLAR_FOR_INNSENDING)

        // Annullering
        refusjonService.annullerRefusjon(TilskuddsperiodeAnnullertMelding(lagretRefusjon.tilskuddsgrunnlag.tilskuddsperiodeId))
        lagretRefusjon = refusjonRepository.findByIdOrNull(lagretRefusjon.id) ?: throw RuntimeException()
        assertThat(lagretRefusjon.status).isEqualTo(RefusjonStatus.ANNULLERT)
    }

    @Test
    @Ignore("Endres til å lagre regelsport i egen tabell, når det er gjort slett denne testen")
    fun `setter appImageId ved beregning`() {
        val refusjon = `Bjørnstjerne Bjørnson`()
        refusjonService.gjørInntektsoppslag(refusjon)
        // refusjonService.endreBruttolønn(refusjon, true, null)
        assertThat(refusjon.beregning?.appImageId).isEqualTo("test")
    }

    @Test
    fun `kaller opprett på samme melding flere ganger, skal bare lagre den ene`() {
        val deltakerFnr = "00000000000"
        val tilskuddMelding = TilskuddsperiodeGodkjentMelding(
            avtaleId = "1",
            tilskuddsbeløp = 1000,
            tiltakstype = Tiltakstype.SOMMERJOBB,
            deltakerEtternavn = "Mus",
            deltakerFornavn = "Mikke",
            arbeidsgiveravgiftSats = 0.101,
            avtaleInnholdId = "2",
            bedriftNavn = "Bedriften AS",
            bedriftNr = "999999999",
            deltakerFnr = deltakerFnr,
            feriepengerSats = 0.141,
            otpSats = 0.02,
            tilskuddFom = Now.localDate().minusWeeks(4).plusDays(1),
            tilskuddTom = Now.localDate(),
            tilskuddsperiodeId = "3",
            veilederNavIdent = "X123456",
            lønnstilskuddsprosent = 60,
            avtaleNr = 3456,
            løpenummer = 3,
            enhet = "1000"
        )
        refusjonService.opprettRefusjon(tilskuddMelding)
        refusjonService.opprettRefusjon(tilskuddMelding)

        assertThat(refusjonRepository.findAll().filter { it.tilskuddsgrunnlag.tilskuddsperiodeId == tilskuddMelding.tilskuddsperiodeId }).hasSize(1)

    }
}