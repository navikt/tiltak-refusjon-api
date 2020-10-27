package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("local")
class RefusjonControllerTest(@Autowired val refusjonController: RefusjonController){

    @Test fun `skal kunne opprette klassen`(){
        assertThat(refusjonController).isNotNull
    }

    @Test fun `skal kunne hente alle refusjoner`(){
        assertThat(refusjonController.hentAlle()).hasSize(14)
    }

}