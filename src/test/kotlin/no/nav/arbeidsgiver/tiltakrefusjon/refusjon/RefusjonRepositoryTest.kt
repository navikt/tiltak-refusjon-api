package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.enRefusjon
import no.nav.arbeidsgiver.tiltakrefusjon.etTilskuddsgrunnlag
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("local")
class RefusjonRepositoryTest(
    @Autowired
    val refusjonRepository: RefusjonRepository,
) {
    @Test
    fun `finn refusjon fra tilskuddsperiodeId`() {
        val refusjon = enRefusjon()
        val id = refusjon.tilskuddsgrunnlag.tilskuddsperiodeId;
        refusjonRepository.save(refusjon)

        val lagretRefusjon =
            refusjonRepository.findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_TilskuddsperiodeId(id).elementAtOrNull(0) ?: fail("Fant ikke refusjon");
        assertThat(lagretRefusjon).isEqualTo(refusjon)
    }

    @Test
    fun `finn forrige refusjon`() {
        val refusjon = enRefusjon()
        refusjonRepository.save(refusjon)
        val refusjon2 = enRefusjon(etTilskuddsgrunnlag().copy(løpenummer = 2))
        refusjonRepository.save(refusjon2)

        val lagretRefusjon = refusjonRepository.findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_AvtaleNr_AndRefusjonsgrunnlag_Tilskuddsgrunnlag_Løpenummer(refusjon2.tilskuddsgrunnlag.avtaleNr, refusjon2.tilskuddsgrunnlag.løpenummer)

        assertThat(lagretRefusjon).size().isEqualTo(1)
        assertThat(lagretRefusjon.first()).isEqualTo(refusjon2)
    }

}