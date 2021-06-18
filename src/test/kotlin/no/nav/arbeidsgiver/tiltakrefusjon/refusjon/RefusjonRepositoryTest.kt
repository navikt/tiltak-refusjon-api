package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.ibm.icu.impl.Assert.fail

import no.nav.arbeidsgiver.tiltakrefusjon.enRefusjon
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("local")
class RefusjonRepositoryTest(
        @Autowired
        val refusjonRepository: RefusjonRepository
){
    @Test
    fun FinnLagretRefusjonTest() {
        val refusjon = enRefusjon()
        val id = refusjon.tilskuddsgrunnlag.tilskuddsperiodeId;
        refusjonRepository.save(refusjon)

        val lagretRefusjon = refusjonRepository.findByTilskuddsgrunnlag_TilskuddsperiodeId(id)?:throw RuntimeException();
        assertThat(lagretRefusjon).isEqualTo(refusjon)
    }

}