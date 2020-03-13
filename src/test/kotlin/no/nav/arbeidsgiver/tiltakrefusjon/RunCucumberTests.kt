package no.nav.arbeidsgiver.tiltakrefusjon

import io.cucumber.junit.Cucumber
import io.cucumber.junit.CucumberOptions
import org.junit.runner.RunWith


@RunWith(Cucumber::class)
@CucumberOptions(
        features = ["src/test/resources/features"],
        tags = ["not @ignored"],
        plugin = ["pretty", "html:target/cucumber"]
)
class RunCucumberTests