package no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Tiltakstype
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Melding om godkjent tilskuddsperiode fra tiltaksgjennomforing-api. JSON-en er flat,
 * og `tiltakstype` avgjør hvilken subtype og hvilke felt meldingen har.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "tiltakstype", visible = true)
@JsonSubTypes(
    JsonSubTypes.Type(
        value = TilskuddsperiodeGodkjentMelding.Lonnstilskudd::class,
        names = ["MIDLERTIDIG_LONNSTILSKUDD", "VARIG_LONNSTILSKUDD", "FIREARIG_LONNSTILSKUDD", "SOMMERJOBB"]
    ),
    JsonSubTypes.Type(value = TilskuddsperiodeGodkjentMelding.Mentor::class, name = "MENTOR"),
    JsonSubTypes.Type(value = TilskuddsperiodeGodkjentMelding.Vtao::class, name = "VTAO"),
    JsonSubTypes.Type(value = TilskuddsperiodeGodkjentMelding.Inkluderingstilskudd::class, name = "INKLUDERINGSTILSKUDD"),
)
sealed interface TilskuddsperiodeGodkjentMelding {
    val avtaleId: String
    val tilskuddsperiodeId: String
    val avtaleInnholdId: String
    val avtaleFom: LocalDate?
    val avtaleTom: LocalDate?
    val tiltakstype: Tiltakstype
    val deltakerFornavn: String
    val deltakerEtternavn: String
    val deltakerFnr: String
    val arbeidsgiverFornavn: String
    val arbeidsgiverEtternavn: String
    val arbeidsgiverTlf: String
    val veilederNavIdent: String
    val bedriftNavn: String
    val bedriftNr: String
    val tilskuddsbeløp: Int
    val tilskuddFom: LocalDate
    val tilskuddTom: LocalDate
    val avtaleNr: Int
    val løpenummer: Int
    val resendingsnummer: Int?
    val enhet: String?
    val godkjentTidspunkt: LocalDateTime
    val arbeidsgiverKontonummer: String?
    val arbeidsgiverKid: String?

    /** Midlertidig, varig og fireårig lønnstilskudd og sommerjobb. */
    data class Lonnstilskudd(
        override val avtaleId: String,
        override val tilskuddsperiodeId: String,
        override val avtaleInnholdId: String,
        override val avtaleFom: LocalDate? = null,
        override val avtaleTom: LocalDate? = null,
        override val tiltakstype: Tiltakstype,
        override val deltakerFornavn: String,
        override val deltakerEtternavn: String,
        override val deltakerFnr: String,
        override val arbeidsgiverFornavn: String,
        override val arbeidsgiverEtternavn: String,
        override val arbeidsgiverTlf: String,
        override val veilederNavIdent: String,
        override val bedriftNavn: String,
        override val bedriftNr: String,
        override val tilskuddsbeløp: Int,
        override val tilskuddFom: LocalDate,
        override val tilskuddTom: LocalDate,
        override val avtaleNr: Int,
        override val løpenummer: Int,
        override val resendingsnummer: Int? = null,
        override val enhet: String?,
        override val godkjentTidspunkt: LocalDateTime,
        override val arbeidsgiverKontonummer: String?,
        override val arbeidsgiverKid: String?,
        val feriepengerSats: Double?,
        val otpSats: Double?,
        val arbeidsgiveravgiftSats: Double?,
        val lønnstilskuddsprosent: Int?,
    ) : TilskuddsperiodeGodkjentMelding {
        init {
            require(tiltakstype in LONNSTILSKUDDSTYPER) { "Ugyldig tiltakstype $tiltakstype for lønnstilskuddsmelding" }
        }
    }

    data class Mentor(
        override val avtaleId: String,
        override val tilskuddsperiodeId: String,
        override val avtaleInnholdId: String,
        override val avtaleFom: LocalDate? = null,
        override val avtaleTom: LocalDate? = null,
        override val tiltakstype: Tiltakstype = Tiltakstype.MENTOR,
        override val deltakerFornavn: String,
        override val deltakerEtternavn: String,
        override val deltakerFnr: String,
        override val arbeidsgiverFornavn: String,
        override val arbeidsgiverEtternavn: String,
        override val arbeidsgiverTlf: String,
        override val veilederNavIdent: String,
        override val bedriftNavn: String,
        override val bedriftNr: String,
        override val tilskuddsbeløp: Int,
        override val tilskuddFom: LocalDate,
        override val tilskuddTom: LocalDate,
        override val avtaleNr: Int,
        override val løpenummer: Int,
        override val resendingsnummer: Int? = null,
        override val enhet: String?,
        override val godkjentTidspunkt: LocalDateTime,
        override val arbeidsgiverKontonummer: String?,
        override val arbeidsgiverKid: String?,
        val feriepengerSats: Double?,
        val otpSats: Double?,
        val arbeidsgiveravgiftSats: Double?,
        val mentorTimelonn: Int?,
        val mentorAntallTimer: Double?,
    ) : TilskuddsperiodeGodkjentMelding {
        init {
            require(tiltakstype == Tiltakstype.MENTOR) { "Ugyldig tiltakstype $tiltakstype for mentormelding" }
        }
    }

    data class Vtao(
        override val avtaleId: String,
        override val tilskuddsperiodeId: String,
        override val avtaleInnholdId: String,
        override val avtaleFom: LocalDate? = null,
        override val avtaleTom: LocalDate? = null,
        override val tiltakstype: Tiltakstype = Tiltakstype.VTAO,
        override val deltakerFornavn: String,
        override val deltakerEtternavn: String,
        override val deltakerFnr: String,
        override val arbeidsgiverFornavn: String,
        override val arbeidsgiverEtternavn: String,
        override val arbeidsgiverTlf: String,
        override val veilederNavIdent: String,
        override val bedriftNavn: String,
        override val bedriftNr: String,
        override val tilskuddsbeløp: Int,
        override val tilskuddFom: LocalDate,
        override val tilskuddTom: LocalDate,
        override val avtaleNr: Int,
        override val løpenummer: Int,
        override val resendingsnummer: Int? = null,
        override val enhet: String?,
        override val godkjentTidspunkt: LocalDateTime,
        override val arbeidsgiverKontonummer: String?,
        override val arbeidsgiverKid: String?,
    ) : TilskuddsperiodeGodkjentMelding {
        init {
            require(tiltakstype == Tiltakstype.VTAO) { "Ugyldig tiltakstype $tiltakstype for VTAO-melding" }
        }
    }

    data class Inkluderingstilskudd(
        override val avtaleId: String,
        override val tilskuddsperiodeId: String,
        override val avtaleInnholdId: String,
        override val avtaleFom: LocalDate? = null,
        override val avtaleTom: LocalDate? = null,
        override val tiltakstype: Tiltakstype = Tiltakstype.INKLUDERINGSTILSKUDD,
        override val deltakerFornavn: String,
        override val deltakerEtternavn: String,
        override val deltakerFnr: String,
        override val arbeidsgiverFornavn: String,
        override val arbeidsgiverEtternavn: String,
        override val arbeidsgiverTlf: String,
        override val veilederNavIdent: String,
        override val bedriftNavn: String,
        override val bedriftNr: String,
        override val tilskuddsbeløp: Int,
        override val tilskuddFom: LocalDate,
        override val tilskuddTom: LocalDate,
        override val avtaleNr: Int,
        override val løpenummer: Int,
        override val resendingsnummer: Int? = null,
        override val enhet: String?,
        override val godkjentTidspunkt: LocalDateTime,
        override val arbeidsgiverKontonummer: String?,
        override val arbeidsgiverKid: String?,
        val inkluderingstilskuddsutgift: Utgift? = null,
    ) : TilskuddsperiodeGodkjentMelding {
        init {
            require(tiltakstype == Tiltakstype.INKLUDERINGSTILSKUDD) { "Ugyldig tiltakstype $tiltakstype for inkluderingstilskuddsmelding" }
        }

        data class Utgift(
            val id: String,
            val navn: String?,
            val beløp: Int,
            val beskrivelse: String?,
            val type: String?,
        )
    }

    companion object {
        val LONNSTILSKUDDSTYPER = setOf(
            Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            Tiltakstype.VARIG_LONNSTILSKUDD,
            Tiltakstype.FIREARIG_LONNSTILSKUDD,
            Tiltakstype.SOMMERJOBB,
        )
    }
}
