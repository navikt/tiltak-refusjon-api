package no.nav.arbeidsgiver.tiltakrefusjon.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

val norskLocale: Locale = Locale.of("nb", "NO")

val norskeSymboler = DecimalFormatSymbols(norskLocale).apply {
    this.setCurrencySymbol("kr")
    this.setGroupingSeparator(' ')
    this.setDecimalSeparator(',')
}

val norskKroneformatering = DecimalFormat("#,##0.## ¤", norskeSymboler)
val prosentformatering = DecimalFormat("##0.## %", norskeSymboler)
val desimalformatering = DecimalFormat("##0.##", norskeSymboler)

fun formaterTilNorskeKroner(amount: Number) = norskKroneformatering.format(amount)
fun formaterProsent(amount: Number) = prosentformatering.format(amount)
fun formaterDesimal(amount: Number) = desimalformatering.format(amount)
