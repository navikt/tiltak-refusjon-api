alter table refusjon add column inntekter_kun_fra_tiltaket boolean;
alter table refusjon add column korrigert_brutto_lønn numeric;
alter table inntektsgrunnlag add column brutto_lønn numeric;
