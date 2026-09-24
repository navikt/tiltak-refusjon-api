package no.nav.arbeidsgiver.tiltakrefusjon

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import java.net.ServerSocket
import java.util.concurrent.atomic.AtomicReference

object WireMockServerHolder {
    private val serverRef = AtomicReference<WireMockServer?>()

    init {
        Runtime.getRuntime().addShutdownHook(Thread {
            serverRef.getAndSet(null)?.stop()
        })
    }

    /** Starter serveren ved første kall. `preferredPort` 0 gir en tilfeldig ledig port. */
    fun port(preferredPort: Int = 0): Int = server(preferredPort).port()

    private fun server(preferredPort: Int): WireMockServer = serverRef.get()?.takeIf { it.isRunning } ?: synchronized(this) {
        serverRef.get()?.takeIf { it.isRunning } ?: startServer(preferredPort).also { serverRef.set(it) }
    }

    private fun startServer(preferredPort: Int): WireMockServer {
        val attempts = if (preferredPort > 0) 1 else 5
        repeat(attempts) { attempt ->
            val port = if (preferredPort > 0) preferredPort else selectFreePort()
            val candidate = WireMockServer(
                WireMockConfiguration.options()
                    .port(port)
                    .bindAddress("127.0.0.1")
                    // JDK HttpClient (RestTemplate i Boot 4) ber om h2c-oppgradering, og Jetty mister da POST-bodyen
                    .http2PlainDisabled(true)
                    .usingFilesUnderClasspath(".")
                    .notifier(ConsoleNotifier(false))
            )
            try {
                candidate.start()
                return candidate
            } catch (exception: RuntimeException) {
                candidate.stop()
                if (attempt == attempts - 1) {
                    throw exception
                }
            }
        }

        error("Unreachable")
    }

    private fun selectFreePort(): Int =
        ServerSocket(0).use { socket ->
            socket.reuseAddress = true
            socket.localPort
        }
}
