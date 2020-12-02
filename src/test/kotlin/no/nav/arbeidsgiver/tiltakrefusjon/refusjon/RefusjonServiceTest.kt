package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.tilskudd.TilskuddMelding
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("local")
@AutoConfigureWireMock(port = 8090)
class RefusjonServiceTest(
        @Autowired
        val refusjonService: RefusjonService,
        @Autowired
        val refusjonRepository: RefusjonRepository
) {
    @Test
    fun opprett() {
        val deltakerFnr = "00000000000"
        val tilskuddMelding = TilskuddMelding(
                avtaleId = "1",
                tilskuddsbeløp = 1000,
                tiltakstype = Tiltakstype.VARIG_LONNSTILSKUDD,
                deltakerEtternavn = "Mus",
                deltakerFornavn = "Mikke",
                arbeidsgiveravgiftSats = 0.101,
                avtaleInnholdId = "2",
                bedriftNavn = "Bedriften AS",
                bedriftNr = "999999999",
                deltakerFnr = deltakerFnr,
                feriepengerSats = 0.141,
                otpSats = 0.02,
                tilskuddFom = LocalDate.of(2020, 10, 1),
                tilskuddTom = LocalDate.of(2020, 11, 1),
                tilskuddsperiodeId = "3",
                veilederNavIdent = "X123456",
                lønnstilskuddsprosent = 60
        )
        refusjonService.opprettRefusjon(tilskuddMelding)
        val lagretRefusjon = refusjonRepository.findAllByDeltakerFnr(deltakerFnr)[0]
        assertThat(lagretRefusjon.tilskuddsgrunnlag).isNotNull
    }

    @Test
    fun `henter inntekter for refusjon`() {
        val deltakerFnr = "28128521498"
        val tilskuddMelding = TilskuddMelding(
                avtaleId = "1",
                tilskuddsbeløp = 1000,
                tiltakstype = Tiltakstype.VARIG_LONNSTILSKUDD,
                deltakerEtternavn = "Mus",
                deltakerFornavn = "Mikke",
                arbeidsgiveravgiftSats = 0.101,
                avtaleInnholdId = "2",
                bedriftNavn = "Bedriften AS",
                bedriftNr = "998877665",
                deltakerFnr = deltakerFnr,
                feriepengerSats = 0.141,
                otpSats = 0.02,
                tilskuddFom = LocalDate.of(2020, 9, 1),
                tilskuddTom = LocalDate.of(2020, 10, 1),
                tilskuddsperiodeId = "3",
                veilederNavIdent = "X123456",
                lønnstilskuddsprosent = 60
        )
        val refusjonId = refusjonService.opprettRefusjon(tilskuddMelding)
        refusjonService.hentInntekterForRefusjon(refusjonId)
        val lagretRefusjon = refusjonRepository.findAllByDeltakerFnr(deltakerFnr)[0]
        assertThat(lagretRefusjon.inntektsgrunnlag?.inntekter).isNotEmpty
    }
}