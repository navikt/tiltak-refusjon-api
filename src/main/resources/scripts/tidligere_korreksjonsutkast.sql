create table tidligere_korreksjonsutkast as (select
 refusjon.id as refusjon_id,
 refusjon.tilskuddsgrunnlag_id as refusjon_tilskuddsgrunnlag_id,
 refusjon.inntektsgrunnlag_id as refusjon_inntektsgrunnlag_id,
 refusjon.beregning_id as refusjon_beregning_id,
 refusjon.status as refusjon_status,
 refusjon.frist_for_godkjenning as refusjon_frist_for_godkjenning,
 refusjon.godkjent_av_arbeidsgiver as refusjon_godkjent_av_arbeidsgiver,
 refusjon.godkjent_av_saksbehandler as refusjon_godkjent_av_saksbehandler,
 refusjon.deltaker_fnr as refusjon_deltaker_fnr,
 refusjon.bedrift_nr as refusjon_bedrift_nr,
 refusjon.bedrift_kontonummer as refusjon_bedrift_kontonummer,
 refusjon.innhentet_bedrift_kontonummer_tidspunkt as refusjon_innhentet_bedrift_kontonummer_tidspunkt,
 refusjon.korreksjon_av_id as refusjon_korreksjon_av_id,
 refusjon.korrigeres_av_id as refusjon_korrigeres_av_id,
 refusjon.inntekter_kun_fra_tiltaket as refusjon_inntekter_kun_fra_tiltaket,
 refusjon.endret_brutto_lønn as refusjon_endret_brutto_lønn,
 refusjon.forrige_frist_for_godkjenning as refusjon_forrige_frist_for_godkjenning,
 refusjon.godkjent_av_saksbehandler_nav_ident as refusjon_godkjent_av_saksbehandler_nav_ident,
 refusjon.korreksjonsnummer as refusjon_korreksjonsnummer,
 refusjon.beslutter_nav_ident as refusjon_beslutter_nav_ident,
 beregning.id as beregning_id,
 beregning.lønn as beregning_lønn,
 beregning.feriepenger as beregning_feriepenger,
 beregning.tjenestepensjon as beregning_tjenestepensjon,
 beregning.arbeidsgiveravgift as beregning_arbeidsgiveravgift,
 beregning.sum_utgifter as beregning_sum_utgifter,
 beregning.refusjonsbeløp as beregning_refusjonsbeløp,
 beregning.app_image_id as beregning_app_image_id,
 beregning.beregnet_beløp as beregning_beregnet_beløp,
 beregning.over_tilskuddsbeløp as beregning_over_tilskuddsbeløp,
 beregning.tidligere_utbetalt as beregning_tidligere_utbetalt
from
 refusjon
     left outer join beregning on
         refusjon.beregning_id = beregning.id
where
     refusjon.status = 'KORREKSJON_UTKAST');