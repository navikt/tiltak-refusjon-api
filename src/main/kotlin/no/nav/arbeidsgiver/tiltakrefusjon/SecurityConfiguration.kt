package no.nav.arbeidsgiver.tiltakrefusjon

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.context.annotation.Configuration

@EnableJwtTokenValidation(ignore = [
    "springfox.documentation.swagger.web.ApiResourceController",
    "org.springframework"])
@Configuration
class SecurityConfiguration