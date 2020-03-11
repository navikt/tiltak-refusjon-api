package no.nav.arbeidsgiver.tiltakrefusjon

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {
    @GetMapping("hello")
    fun hello() : Hello {
        return Hello("yes", 9)
    }

    @GetMapping("world")
    fun world() : World {
        val world = World("fdfs")
        println(world.foo);
        println(world);
        return world
    }
}