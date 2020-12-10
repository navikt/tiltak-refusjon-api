package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class RefusjonTest {
    // Godkjennelse arbeidsgiver
    @Test
    fun `kan ikke godkjenne for ag uten beregning`() {
        val refusjon = enRefusjon()
        assertThrows<FeilkodeException> { refusjon.godkjennForArbeidsgiver() }
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
        assertThrows<FeilkodeException> { refusjon.godkjennForArbeidsgiver() }
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
        assertThrows<FeilkodeException> { refusjon.godkjennForSaksbehandler() }
    }
}
