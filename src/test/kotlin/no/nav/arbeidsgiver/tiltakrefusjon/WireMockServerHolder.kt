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

    fun port(): Int = server().port()

    private fun server(): WireMockServer = serverRef.get()?.takeIf { it.isRunning } ?: synchronized(this) {
        serverRef.get()?.takeIf { it.isRunning } ?: startServer().also { serverRef.set(it) }
    }

    private fun startServer(): WireMockServer {
        repeat(5) { attempt ->
            val port = selectFreePort()
            val candidate = WireMockServer(
                WireMockConfiguration.options()
                    .port(port)
                    .bindAddress("127.0.0.1")
                    .usingFilesUnderClasspath(".")
                    .notifier(ConsoleNotifier(false))
            )
            try {
                candidate.start()
                return candidate
            } catch (exception: RuntimeException) {
                candidate.stop()
                if (attempt == 4) {
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
