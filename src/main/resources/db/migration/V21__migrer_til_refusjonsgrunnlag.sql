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