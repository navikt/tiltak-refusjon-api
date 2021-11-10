insert into refusjonsgrunnlag(id,
                              tilskuddsgrunnlag_id,
                              inntektsgrunnlag_id,
                              beregning_id,
                              bedrift_kontonummer,
                              bedrift_kontonummer_innhentet_tidspunkt,
                              inntekter_kun_fra_tiltaket,
                              endret_brutto_lønn,
                              tidligere_utbetalt)
select id,
       tilskuddsgrunnlag_id,
       inntektsgrunnlag_id,
       beregning_id,
       bedrift_kontonummer,
       innhentet_bedrift_kontonummer_tidspunkt,
       inntekter_kun_fra_tiltaket,
       endret_brutto_lønn,
       0
from refusjon
where status != 'KORREKSJON_UTKAST';

update refusjon set refusjonsgrunnlag_id=id;

alter table refusjon drop column tilskuddsgrunnlag_id;
alter table refusjon drop column inntektsgrunnlag_id;
alter table refusjon drop column beregning_id;
alter table refusjon drop column bedrift_kontonummer;
alter table refusjon drop column innhentet_bedrift_kontonummer_tidspunkt;
alter table refusjon drop column inntekter_kun_fra_tiltaket;
alter table refusjon drop column endret_brutto_lønn;
alter table refusjon drop column korreksjonsnummer;
alter table refusjon drop column korreksjon_av_id;
alter table refusjon drop column korrigeres_av_id;
alter table refusjon drop column godkjent_av_saksbehandler;
drop table refusjon_korreksjonsgrunner;