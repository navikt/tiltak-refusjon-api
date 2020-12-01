package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.nydatamodell.RefusjonsakRepository
import no.nav.arbeidsgiver.tiltakrefusjon.tilskudd.TilskuddMelding
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("local")
class RefusjonsberegningServiceTest(
        @Autowired
        val refusjonsberegningService: RefusjonsberegningService,
        @Autowired
        val refusjonsakRepository: RefusjonsakRepository
) {
    @Test
    internal fun opprett() {
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
                veilederNavIdent = "X123456"
        )
        refusjonsberegningService.opprettRefusjon(tilskuddMelding)
        assertThat(refusjonsakRepository.findAllByDeltakerFnr(deltakerFnr)).hasSize(1)
    }
}