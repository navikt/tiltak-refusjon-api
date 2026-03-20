package no.nav.arbeidsgiver.tiltakrefusjon

import jakarta.annotation.PostConstruct
import no.bekk.bekkopen.person.FodselsnummerValidator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment


@Configuration
class RefusjonConfiguration {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    private val environment: Environment? = null

    @PostConstruct
    fun init() {
        val erProd = environment?.activeProfiles?.any { env -> env.equals("prod-gcp") } ?: false
        if (erProd) {
            log.info("Syntetiske fødselsnumre er skrudd av")
            FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = false
        } else {
            log.info("Syntetiske fødselsnumre er skrudd på")
            FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true
        }
    }
}
