package no.nav.arbeidsgiver.tiltakrefusjon

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.HandlerMethod


@Configuration

class SwaggerConfig {
 private fun getInfo(openAPI: OpenAPI): OpenAPI? {
     return openAPI
         .info(
             Info().title("Tiltak Refusjon API")
                 .license(
                     License()
                         .name("MIT License")
                         .url("https://github.com/navikt/tiltak-refusjon-api/blob/master/LICENSE.md")
                 )
         ).externalDocs(
             ExternalDocumentation()
                 .description("Refusjon for arbeidstiltak.")
                 .url("https://github.com/navikt/tiltak-refusjon-api")
         )
 }

    @Bean
    fun TiltakOpenAPI(): OpenAPI? {
        val securitySchemeName = "bearerAuth"
        val openAPI = OpenAPI()
            .addSecurityItem(SecurityRequirement().addList(securitySchemeName))
            .components(
                Components()
                    .addSecuritySchemes(
                        securitySchemeName, SecurityScheme()
                            .name(securitySchemeName)
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                    )
            )
        return getInfo(openAPI)
    }

    @Bean
    fun publicApi(): GroupedOpenApi? {
        return GroupedOpenApi.builder()
            .group("tiltak-refusjon-api")
            .pathsToMatch("/**")
            .addOperationCustomizer { operation: Operation, handlerMethod: HandlerMethod? ->
                operation.addSecurityItem(SecurityRequirement().addList("bearerAuth"))
                operation
            }
            .addOpenApiCustomiser { openAPI: OpenAPI -> this.getInfo(openAPI) }.build()
    }
}