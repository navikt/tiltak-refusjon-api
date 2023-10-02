package no.nav.arbeidsgiver.tiltakrefusjon.featuretoggles

import io.getunleash.MoreOperations
import io.getunleash.Unleash
import io.getunleash.UnleashContext
import io.getunleash.Variant
import java.util.function.BiPredicate


class FakeFakeUnleash : Unleash {
    private var enableAll = false
    private var disableAll = false
    private val features: MutableMap<String, Boolean> = HashMap()
    private val variants: MutableMap<String, Variant> = HashMap()
    override fun isEnabled(toggleName: String): Boolean {
        return isEnabled(toggleName, false)
    }

    override fun isEnabled(toggleName: String, defaultSetting: Boolean): Boolean {
        return if (features.containsKey(toggleName)) {
            features[toggleName]!!
        } else if (enableAll) {
            true
        } else if (disableAll) {
            false
        } else {
            defaultSetting
        }
    }

    override fun isEnabled(toggleName: String, unleashContext: UnleashContext, p2: BiPredicate<String, UnleashContext>): Boolean {
        return if (features.containsKey(toggleName)) {
            features[toggleName]!!
        } else if (enableAll) {
            true
        } else if (disableAll) {
            false
        } else {
            p2.test(toggleName, unleashContext)
        }
    }

    override fun getVariant(toggleName: String, context: UnleashContext): Variant {
        return getVariant(toggleName, Variant.DISABLED_VARIANT)
    }

    override fun getVariant(toggleName: String, context: UnleashContext, defaultValue: Variant): Variant {
        return getVariant(toggleName, defaultValue)
    }

    override fun getVariant(toggleName: String): Variant {
        return getVariant(toggleName, Variant.DISABLED_VARIANT)
    }

    override fun getVariant(toggleName: String, defaultValue: Variant): Variant {
        return if (isEnabled(toggleName) && variants.containsKey(toggleName)) {
            variants[toggleName]!!
        } else {
            defaultValue
        }
    }

    override fun getFeatureToggleNames(): List<String> {
        return ArrayList(features.keys)
    }

    override fun more(): MoreOperations {
        TODO("Not yet implemented")
    }

    fun enableAll() {
        disableAll = false
        enableAll = true
        features.clear()
    }

    fun disableAll() {
        disableAll = true
        enableAll = false
        features.clear()
    }

    fun resetAll() {
        disableAll = false
        enableAll = false
        features.clear()
    }

    fun enable(vararg features: String) {
        for (name in features) {
            this.features[name] = true
        }
    }

    fun disable(vararg features: String) {
        for (name in features) {
            this.features[name] = false
        }
    }

    fun reset(vararg features: String) {
        for (name in features) {
            this.features.remove(name)
        }
    }

    fun setVariant(t1: String, a: Variant) {
        variants[t1] = a
    }
}