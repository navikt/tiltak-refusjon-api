package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.github.guepardoapps.kulid.ULID
import java.time.Instant
import java.util.*
import javax.persistence.*

@Entity
class Korreksjon() {

    @Id
    val id: String = ULID.random()

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    val korreksjonsgrunner: MutableSet<Korreksjonsgrunn> = EnumSet.noneOf(Korreksjonsgrunn::class.java)

    var korrigererRefusjonId: String? = null
    var godkjentTidspunkt: Instant? = null
    var godkjentAvSaksbehandlerNavIdent: String? = null
    var beslutterNavIdent: String? = null
    var kostnadssted: String? = null

}
