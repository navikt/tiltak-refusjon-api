package no.nav.arbeidsgiver.tiltakrefusjon

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType.SWAGGER_2
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
class SwaggerConfig {
    @Bean
    fun api(): Docket {
        return Docket(SWAGGER_2)
                .select()
//                .apis(RequestHandlerSelectors.basePackage("no.nav.arbeidsgiver.tiltakrefusjon"))
                .paths(PathSelectors.any())
                .build()
    }
}