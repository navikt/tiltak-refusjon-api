package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.ninjasquad.springmockk.SpykBean
import io.mockk.verify
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.InntektskomponentService
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeAnnullertMelding
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.MidlerFrigjortÅrsak
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeForkortetMelding
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeGodkjentMelding
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest(properties = ["NAIS_APP_IMAGE=test"])
@ActiveProfiles("local")
@AutoConfigureWireMock(port = 8090)
class RefusjonServiceTest(
    @Autowired
    val refusjonService: RefusjonService,
    @Autowired
    val refusjonRepository: RefusjonRepository
) {
    @SpykBean
    lateinit var inntektskomponentService: InntektskomponentService

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
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
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
        refusjonService.annullerRefusjon(TilskuddsperiodeAnnullertMelding(lagretRefusjon.tilskuddsgrunnlag.tilskuddsperiodeId, MidlerFrigjortÅrsak.AVTALE_ANNULLERT))
        lagretRefusjon = refusjonRepository.findByIdOrNull(lagretRefusjon.id) ?: throw RuntimeException()
        assertThat(lagretRefusjon.status).isEqualTo(RefusjonStatus.ANNULLERT)
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
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )
        refusjonService.opprettRefusjon(tilskuddMelding)
        refusjonService.opprettRefusjon(tilskuddMelding)

        assertThat(refusjonRepository.findAll().filter { it.tilskuddsgrunnlag.tilskuddsperiodeId == tilskuddMelding.tilskuddsperiodeId }).hasSize(1)

    }

    @Test
    internal fun `inntektsoppslag skal ta hensyn til unntaksregel`() {
        val deltakerFnr = "00000000000"
        val tilskuddMelding = TilskuddsperiodeGodkjentMelding(
            avtaleId = "2",
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
            tilskuddTom = Now.localDate().minusDays(1),
            tilskuddsperiodeId = "4",
            veilederNavIdent = "X123456",
            lønnstilskuddsprosent = 60,
            avtaleNr = 3456,
            løpenummer = 3,
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )
        var refusjon = refusjonService.opprettRefusjon(tilskuddMelding) ?: fail("Skulle kunne opprette refusjon")

        refusjonService.gjørInntektsoppslag(refusjon)
        verify {
            inntektskomponentService.hentInntekter(tilskuddMelding.deltakerFnr, tilskuddMelding.bedriftNr, tilskuddMelding.tilskuddFom, tilskuddMelding.tilskuddTom.plusMonths(1))
        }

        Now.fixedDate(LocalDate.now().plusDays(1))
        refusjon.merkForUnntakOmInntekterToMånederFrem(true, "")
        refusjonService.gjørInntektsoppslag(refusjon)
        verify {
            inntektskomponentService.hentInntekter(tilskuddMelding.deltakerFnr, tilskuddMelding.bedriftNr, tilskuddMelding.tilskuddFom, tilskuddMelding.tilskuddTom.plusMonths(2))
        }
        Now.resetClock()
    }

    @Test
    fun `Inntektsoppslag for andre typer enn sommerjobb skal sjekke 0 eller 1 månede ekstra`() {
        val deltakerFnr = "00000000000"
        val tilskuddMelding = TilskuddsperiodeGodkjentMelding(
            avtaleId = "2",
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
            tilskuddFom = Now.localDate().minusWeeks(4).plusDays(1),
            tilskuddTom = Now.localDate().minusDays(1),
            tilskuddsperiodeId = "4",
            veilederNavIdent = "X123456",
            lønnstilskuddsprosent = 60,
            avtaleNr = 3456,
            løpenummer = 3,
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )
        var refusjon = refusjonService.opprettRefusjon(tilskuddMelding) ?: fail("Skulle kunne opprette refusjon")
        refusjonService.gjørInntektsoppslag(refusjon)
        verify {
            inntektskomponentService.hentInntekter(tilskuddMelding.deltakerFnr, tilskuddMelding.bedriftNr, tilskuddMelding.tilskuddFom, tilskuddMelding.tilskuddTom.plusMonths(0))
        }
        Now.fixedDate(LocalDate.now().plusDays(1))
        refusjon.merkForUnntakOmInntekterToMånederFrem(true, "")
        refusjonService.gjørInntektsoppslag(refusjon)
        verify {
            inntektskomponentService.hentInntekter(tilskuddMelding.deltakerFnr, tilskuddMelding.bedriftNr, tilskuddMelding.tilskuddFom, tilskuddMelding.tilskuddTom.plusMonths(2))
        }
        Now.resetClock()
    }

    @Test
    fun `Manuell annullering av tilskuddsperiode fordi det ikke vil bli søkt om refusjon annullerer ikke refusjon`() {
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
            tilskuddsperiodeId = "5",
            veilederNavIdent = "X123456",
            lønnstilskuddsprosent = 60,
            avtaleNr = 3456,
            løpenummer = 3,
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )
        refusjonService.opprettRefusjon(tilskuddMelding)
        assertThat(refusjonRepository.findAll().filter { it.tilskuddsgrunnlag.tilskuddsperiodeId == tilskuddMelding.tilskuddsperiodeId }).hasSize(1)
        var lagretRefusjon = refusjonRepository.findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_TilskuddsperiodeId(tilskuddMelding.tilskuddsperiodeId).firstOrNull()

        refusjonService.annullerRefusjon(TilskuddsperiodeAnnullertMelding(tilskuddMelding.tilskuddsperiodeId, MidlerFrigjortÅrsak.REFUSJON_IKKE_SØKT))
        lagretRefusjon = refusjonRepository.findByIdOrNull(lagretRefusjon?.id) ?: throw RuntimeException()
        assertThat(lagretRefusjon.status).isNotEqualTo(RefusjonStatus.ANNULLERT)
    }
}