package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import org.mockito.junit.jupiter.MockitoExtension
@ExtendWith(MockitoExtension::class)
class RefusjonControllerTest{

    val refusjonRepository:RefusjonRepository = mockk<RefusjonRepository>()
    val refusjonController:RefusjonController = RefusjonController(refusjonRepository)


    @Test fun `skal kunne opprette klassen`(){
        assertThat(refusjonController).isNotNull
    }


}