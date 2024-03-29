package no.nav.arbeidsgiver.tiltakrefusjon.inntekt.response

data class InntektListe(
    val inntektType: String,
    val beloep: Int,
    val fordel: String?,
    val inntektskilde: String?,
    val inntektsperiodetype: String?,
    val inntektsstatus: String?,
    val leveringstidspunkt: String?,
    val opptjeningsland: String?,
    val opptjeningsperiodeFom: String?,
    val opptjeningsperiodeTom: String?,
    val utbetaltIMaaned: String?,
    val opplysningspliktig: Opplysningspliktig?,
    val virksomhet: Virksomhet?,
    val tilleggsinformasjon: Tilleggsinformasjon?,
    val inntektsmottaker: Inntektsmottaker?,
    val inngaarIGrunnlagForTrekk: Boolean,
    val utloeserArbeidsgiveravgift: Boolean,
    val informasjonsstatus: String?,
    val beskrivelse: String?,
    val skatteOgAvgiftsregel: String?,
    val antall: Int?
)