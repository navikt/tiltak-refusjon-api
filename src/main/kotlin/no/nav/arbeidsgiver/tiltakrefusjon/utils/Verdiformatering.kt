package no.nav.arbeidsgiver.tiltakrefusjon.utils

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private val norskLocale: Locale = Locale.of("nb", "NO")

private val norskeSymboler = DecimalFormatSymbols(norskLocale).apply {
    this.setCurrencySymbol("kr")
    this.setGroupingSeparator(' ')
    this.setDecimalSeparator(',')
}

private val norskKroneformatering = ThreadLocal.withInitial {
    DecimalFormat("#,##0.## ¤;- #,##0.## ¤", norskeSymboler).apply {
        roundingMode = RoundingMode.HALF_UP
    }
}
private val prosentformatering = ThreadLocal.withInitial {
    DecimalFormat("##0.## %", norskeSymboler).apply {
        roundingMode = RoundingMode.HALF_UP
    }
}
private val desimalformatering = ThreadLocal.withInitial {
    DecimalFormat("##0.##", norskeSymboler).apply {
        roundingMode = RoundingMode.HALF_UP
    }
}

fun formaterTilNorskeKroner(amount: Number) = norskKroneformatering.get().format(amount)
fun formaterProsent(amount: Number) = prosentformatering.get().format(amount)
fun formaterDesimal(amount: Number) = desimalformatering.get().format(amount)
