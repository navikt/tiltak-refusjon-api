package no.nav.arbeidsgiver.tiltakrefusjon.utils

import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.domain.Sort.Direction.DESC

enum class SortingOrder {
    TILTAKSTYPE_ASC,
    TILTAKSTYPE_DESC,
    BEDRIFT_ASC,
    BEDRIFT_DESC,
    DELTAKER_ASC,
    DELTAKER_DESC,
    PERIODE_ASC,
    PERIODE_DESC,
    STATUS_ASC,
    STATUS_DESC,
    FRISTFORGODKJENNING_ASC,
    FRISTFORGODKJENNING_DESC,
    LØPENUMMER_ASC,
    LØPENUMMER_DESC,
    REFUSJONSNUMMER_ASC,
    REFUSJONSNUMMER_DESC
}

fun sortPageable(sortingOrder: SortingOrder): Sort {
    val sort = when (sortingOrder) {
        SortingOrder.TILTAKSTYPE_ASC -> Sort.by(ASC, "refusjonsgrunnlag.tilskuddsgrunnlag.tiltakstype")
        SortingOrder.TILTAKSTYPE_DESC -> Sort.by(DESC, "refusjonsgrunnlag.tilskuddsgrunnlag.tiltakstype")
        SortingOrder.BEDRIFT_ASC -> Sort.by(ASC, "refusjonsgrunnlag.tilskuddsgrunnlag.bedriftNavn")
        SortingOrder.BEDRIFT_DESC -> Sort.by(DESC, "refusjonsgrunnlag.tilskuddsgrunnlag.bedriftNavn")
        SortingOrder.DELTAKER_ASC -> Sort.by(ASC, "refusjonsgrunnlag.tilskuddsgrunnlag.deltakerFornavn")
        SortingOrder.DELTAKER_DESC -> Sort.by(DESC, "refusjonsgrunnlag.tilskuddsgrunnlag.deltakerFornavn")
        SortingOrder.PERIODE_ASC -> Sort.by(ASC, "refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddTom")
        SortingOrder.PERIODE_DESC -> Sort.by(DESC, "refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddTom")
        SortingOrder.FRISTFORGODKJENNING_ASC -> Sort.by(ASC, "fristForGodkjenning")
        SortingOrder.FRISTFORGODKJENNING_DESC -> Sort.by(DESC, "fristForGodkjenning")
        SortingOrder.REFUSJONSNUMMER_ASC -> Sort.by(ASC, "refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr", "refusjonsgrunnlag.tilskuddsgrunnlag.løpenummer")
        SortingOrder.REFUSJONSNUMMER_DESC -> Sort.by(DESC, "refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr", "refusjonsgrunnlag.tilskuddsgrunnlag.løpenummer")
        SortingOrder.STATUS_DESC -> Sort.by(DESC, "status")
        else -> Sort.by(ASC, "status")
    }
    return sort.and(Sort.by(ASC, "id"))
}
