package no.nav.arbeidsgiver.tiltakrefusjon.utregning

enum class UtregningsradType(val label: String) {
    TIMELONN_X_TIMER("Timelønn × antall timer"),
    FERIEPENGER("Feriepenger"),
    OBLIGATORISK_TJENESTEPENSJON("Obligatorisk tjenestepensjon"),
    ARBEIDSGIVERAVGIFT("Arbeidsgiveravgift"),
    SUM_TILSKUDD_FOR_EN_MND("Sum tilskudd for en måned"),
    REDUKSJON_FOR_DELVIS_PERIODE("Reduksjon for delvis periode"),
    SUM_BRUTTO_LONNSUTGIFTER("Sum brutto lønnsutgifter"),
    TIDLIGERE_REFUNDERBART_FOR_FRAVAER("Tidligere refunderbart beløp for fravær"),
    REFUSJONSGRUNNLAG("Refusjonsgrunnlag"),
    TILSKUDDSPROSENT("Tilskuddsprosent"),
    AVTALT_BELOP("Avtalt beløp"),
    RESTERENDE_FRATREKK_FOR_FERIE_FRA_TIDLIGERE_REFUSJONER("Resterende fratrekk for ferie fra tidligere refusjoner"),
    TIDLIGERE_UTBETALT("Tidligere utbetalt"),
    BRUTTOLONN_I_PERIODEN("Bruttolønn i perioden"),
    FERIETREKK("Fratrekk for ferie"),
    REFUSJONSBELØP_TIL_UTBETALING("Refusjonsbeløp til utbetaling"),
    BEREGNET_BELOP("Beregning basert på innhentede inntekter"),
    BEREGNET_BELOP_ETTER_RESTTREKK("Beregning etter resterende fratrekk"),
    AVTALT_BELOP_REST_5G("Avtalt tilskuddsbeløp (gjenstående etter 5G)"),
}
