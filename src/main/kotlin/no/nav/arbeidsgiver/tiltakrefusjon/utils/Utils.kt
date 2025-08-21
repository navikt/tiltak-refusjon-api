package no.nav.arbeidsgiver.tiltakrefusjon.utils

import de.huxhorn.sulky.ulid.ULID
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.AltinnTilgang
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.isLeaf

fun erIkkeTomme(vararg objekter: Any?): Boolean {
    for (objekt in objekter) {
        if (objekt is String && objekt.isEmpty()) {
            return false
        }
        if (objekt == null) {
            return false
        }
    }
    return true
}

fun erNoenTomme(vararg objekter: Any?): Boolean {
    return !erIkkeTomme(*objekter)
}

private val ulidGenerator = ULID()
fun ulid(): String = ulidGenerator.nextULID()


/** Altinn Utils */
fun <T : AltinnTilgang> split(
    predicate: (T) -> Boolean,
    liste: List<T>
): Pair<List<T>, List<T>> {
    val children = liste.filter(predicate)
    val otherParents = liste.filterNot(predicate)
    return Pair(children, otherParents)
}
/**
 * Altinn util: Function that returns leaf nodes and first-level parents as a flat list
 */
fun flatUtHierarki(organisasjonstre: List<AltinnTilgang>): List<AltinnTilgang> {
    fun mapR(parent: AltinnTilgang): List<AltinnTilgang> {
        val (children, otherParents) = split(AltinnTilgang::isLeaf, parent.underenheter)
        val current = if (children.isNotEmpty()) {
            listOf(parent.copy(underenheter = children))
        } else {
            emptyList()
        }
        return current + otherParents.flatMap { mapR(it) }
    }
    return organisasjonstre
        .flatMap { mapR(it) }
        .sortedBy { it.navn }
}
