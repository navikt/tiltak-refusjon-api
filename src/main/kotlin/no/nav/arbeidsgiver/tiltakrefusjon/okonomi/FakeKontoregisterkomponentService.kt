package no.nav.arbeidsgiver.tiltakrefusjon.okonomi

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty("tiltak-refusjon.kontoregister.fake")
class FakeKontoregisterkomponentService : KontoregisterkomponentService {
    override fun hentBankkontonummer(
        bedriftNr: String
    ): String {
       return "10000008145";
    }
}