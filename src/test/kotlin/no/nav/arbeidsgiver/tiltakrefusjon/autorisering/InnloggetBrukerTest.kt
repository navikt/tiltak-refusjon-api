package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.arbeidsgiver.tiltakrefusjon.altinn.enOrganisasjon
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class InnloggetBrukerTest {

    @Test
    fun `skal ha tilgang`() {

        // GITT

        val innloggetBruker = InnloggetBruker("12345678901", setOf(enOrganisasjon()))


        // NÅR
        innloggetBruker.harTilgang("1007")
    }

    @Test
    fun `skal ikke ha tilgang`() {

        // GITT

        val innloggetBruker = InnloggetBruker("12345678901", setOf(enOrganisasjon()))


        // NÅR
        assertThrows<TilgangskontrollException>{
            (innloggetBruker.harTilgang("111111"))
        }
    }
}