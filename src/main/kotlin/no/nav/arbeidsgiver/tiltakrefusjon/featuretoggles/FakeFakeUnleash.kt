package no.nav.arbeidsgiver.tiltakrefusjon.featuretoggles

import io.getunleash.FakeUnleash
import io.getunleash.UnleashContext
import io.getunleash.Variant
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes


class FakeFakeUnleash : FakeUnleash() {

    override fun isEnabled(toggleName: String): Boolean {
        return isEnabled(toggleName, false)
    }

    override fun isEnabled(toggleName: String, context: UnleashContext): Boolean {
        updateTogglesFromRequest()
        return super.isEnabled(toggleName, false)
    }

    override fun isEnabled(toggleName: String, defaultSetting: Boolean): Boolean {
        updateTogglesFromRequest()
        return super.isEnabled(toggleName, defaultSetting)
    }

    override fun isEnabled(
        toggleName: String,
        fallbackAction: java.util.function.BiPredicate<String, UnleashContext>
    ): Boolean {
        updateTogglesFromRequest()
        return super.isEnabled(toggleName, fallbackAction)
    }

    override fun getVariant(toggleName: String, defaultValue: Variant): Variant {
        updateTogglesFromRequest()
        return super.getVariant(toggleName, defaultValue)
    }

    private fun updateTogglesFromRequest() {

        val request = resolveRequest()
        if (request == null) {
            enableAll()
            return
        }

        val headers: List<String> = request.getHeader("features")
            ?.split(',')
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

        val first = headers.firstOrNull()
        when {
            first == "enabled" -> enableAllExcept(*getToggleExclusions(headers))
            first == "disabled" -> disableAllExcept(*getToggleExclusions(headers))
            else -> disableAll()
        }
    }

    private fun getToggleExclusions(headers: List<String>): Array<String> {
        return headers
            .asSequence()
            .filter { it.startsWith("!") }
            .map { it.substring(1) }
            .toList()
            .toTypedArray()
    }

    private fun resolveRequest(): HttpServletRequest? =
        try {
            (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
        } catch (_: IllegalStateException) {
            null
        }

}
