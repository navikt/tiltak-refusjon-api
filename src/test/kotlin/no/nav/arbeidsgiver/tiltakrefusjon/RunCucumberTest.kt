package no.nav.arbeidsgiver.tiltakrefusjon

import io.cucumber.junit.Cucumber
import io.cucumber.junit.CucumberOptions
import org.junit.runner.RunWith
import org.springframework.test.context.TestPropertySource

@RunWith(Cucumber::class)
@CucumberOptions(
    features = ["src/test/resources/features"]
)
@TestPropertySource(properties = ["cucumber.reporting.config.file=src/test/resources/cucumber-reporting.properties"])
class RunCucumberTest