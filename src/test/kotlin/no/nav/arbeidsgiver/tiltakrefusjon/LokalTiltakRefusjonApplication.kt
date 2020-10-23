package no.nav.arbeidsgiver.tiltakrefusjon

import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.springframework.web.client.RestTemplate


@SpringBootApplication
@Profile("local")
@Import(TokenGeneratorConfiguration::class)
class LokalTiltakRefusjonApplication
@Bean
fun restTemplate():RestTemplate{
    return RestTemplate()
}
fun main(args: Array<String>) {
    runApplication<LokalTiltakRefusjonApplication>(*args) {
        setAdditionalProfiles("local")
    }
}