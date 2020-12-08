package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.enRefusjon
import no.nav.arbeidsgiver.tiltakrefusjon.medGodkjennelseFraArbeidsgiver
import no.nav.arbeidsgiver.tiltakrefusjon.medGodkjennelseFraSaksbehandler
import no.nav.arbeidsgiver.tiltakrefusjon.medInntektsgrunnlag
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

internal class RefusjonTest {
    // Godkjennelse arbeidsgiver
    @Test
    fun `kan ikke godkjenne for ag uten beregning`() {
        val refusjon = enRefusjon()
        assertThatThrownBy { refusjon.godkjennForArbeidsgiver() }.isInstanceOf(FeilkodeException::class.java)
    }

    @Test
    fun `kan godkjenne for ag med beregning`() {
        val refusjon = enRefusjon().medInntektsgrunnlag()
        refusjon.godkjennForArbeidsgiver()
        assertThat(refusjon.godkjentAvArbeidsgiver).isNotNull()
    }

    @Test
    fun `kan ikke godkjenne for ag to ganger`() {
        val refusjon = enRefusjon().medInntektsgrunnlag().medGodkjennelseFraArbeidsgiver()
        assertThatThrownBy { refusjon.godkjennForArbeidsgiver() }.isInstanceOf(FeilkodeException::class.java)
    }

    // Godkjennelse saksbehandler
    @Test
    fun `kan godkjenne for saksbehandler med ag godkjennelse`() {
        val refusjon = enRefusjon().medInntektsgrunnlag().medGodkjennelseFraArbeidsgiver()
        refusjon.godkjennForSaksbehandler()
        assertThat(refusjon.godkjentAvSaksbehandler).isNotNull()
    }

    @Test
    fun `kan ikke godkjenne for saksbehandler to ganger`() {
        val refusjon = enRefusjon()
                .medInntektsgrunnlag()
                .medGodkjennelseFraArbeidsgiver()
                .medGodkjennelseFraSaksbehandler()
        assertThatThrownBy { refusjon.godkjennForSaksbehandler() }.isInstanceOf(FeilkodeException::class.java)
    }
}
