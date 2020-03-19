import no.nav.arbeidsgiver.tiltakrefusjon.domain.Refusjon
import no.nav.arbeidsgiver.tiltakrefusjon.domain.Varighet
import java.time.LocalDate

fun hentRefusjon(id: String): Refusjon {
    return Refusjon(
            "1",
            "Arbeidstrening",
            "Mikke Mus",
            "Jonas Trane",
            "bedriftNavn: Kiwi Majorstuen",
            "Martine Loren",
            2,
            1500,
            2,
            2000,
            100,
            30000,
            26500,
            0.02,
            530,
            0.12,
            3180,
            0.141,
            3737,
            33947,
            40,
            13579,
            Varighet(LocalDate.now(), LocalDate.now().plusMonths(3))
    )
}