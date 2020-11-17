package no.nav.arbeidsgiver.tiltakrefusjon

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.ParameterBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.schema.ModelRef
import springfox.documentation.spi.DocumentationType.SWAGGER_2
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2


@Configuration
@EnableSwagger2
class SwaggerConfig {
    @Bean
    fun api(): Docket {
        val parameters = ParameterBuilder().name("Authorization")
                .modelRef(ModelRef("string"))
                .parameterType("header")
                .description("JWT token")
                .required(false)
                .build()
        return Docket(SWAGGER_2)
                .select()
                .paths(PathSelectors.any())
                .build()
                .globalOperationParameters(listOf(parameters))
    }
}