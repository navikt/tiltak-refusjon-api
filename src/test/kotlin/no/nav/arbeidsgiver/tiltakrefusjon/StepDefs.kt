package no.nav.arbeidsgiver.tiltakrefusjon

import io.cucumber.java8.En
import io.cucumber.java8.PendingException
import org.assertj.core.api.Assertions.assertThat

class StepDefs: En {
    init {
        Given("I have {int} cukes in my belly") { int1: Int? -> {} }
        When("I wait {int} hour") { int1: Int? -> {} }
        Then("my belly should growl") {
            assertThat(true).isTrue()
        }
    }
}