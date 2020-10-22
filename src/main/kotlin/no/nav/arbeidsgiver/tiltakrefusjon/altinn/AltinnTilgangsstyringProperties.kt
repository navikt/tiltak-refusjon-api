package no.nav.arbeidsgiver.tiltakrefusjon.altinn

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.net.URI

@Component
@ConfigurationProperties(prefix = "tiltak-refusjon.altinn-tilgangstyring")
data class AltinnTilgangsstyringProperties (
         var  uri:URI = URI(""),
var proxyUri:URI = URI(""),
var altinnApiKey:String = "",
var apiGwApiKey:String = "",
var beOmRettighetBaseUrl:String = "",
var ltsMidlertidigServiceCode:Int = 0,
var ltsMidlertidigServiceEdition:Int = 0,
var ltsVarigServiceCode:Int = 0,
var ltsVarigServiceEdition:Int = 0,
var arbtreningServiceCode:Int = 0,
var arbtreningServiceEdition:Int =0
)