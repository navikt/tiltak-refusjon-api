package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.enRefusjon
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

}