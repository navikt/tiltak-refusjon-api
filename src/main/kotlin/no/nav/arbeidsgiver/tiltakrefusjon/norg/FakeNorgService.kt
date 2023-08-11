package no.nav.arbeidsgiver.tiltakrefusjon.norg

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("local", "dokcker-compose")
class FakeNorgService: NorgService {
    override fun hentEnhetNavn(enhet: String): String? {
        val toFørsteSiffer = enhet.substring(0,2)
        when(toFørsteSiffer) {
            "01" -> return "NAV Fredrikstad"
            "02" -> return "NAV Lillestrøm"
            "03" -> return "NAV Grünerløkka"
            "04" -> return "NAV Elverum"
            "05" -> return "NAV Gjøvik"
            "06" -> return "NAV Drammen"
            "07" -> return "NAV Tønsberg"
            "08" -> return "NAV Porgrunn"
            "09" -> return "NAV Grimstad"
            "10" -> return "NAV Kristiansand"
            "11" -> return "NAV Sandnes"
            "12" -> return "NAV Voss"
            "14" -> return "NAV Sogndal"
            "15" -> return "NAV Molde"
            "16" -> return "NAV Ørland"
            "17" -> return "NAV Verdal"
            "18" -> return "NAV Bodø"
            "19" -> return "NAV Tromsø"
            "20" -> return "NAV Vadsø"
        }
        return "NAV Enhetsen";
    }
}