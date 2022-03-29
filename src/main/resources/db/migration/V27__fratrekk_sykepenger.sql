alter table refusjonsgrunnlag add column fratrekk_sykepenger boolean default null;
alter table refusjonsgrunnlag add column sykepenge_beløp numeric default null;

alter table beregning add column fratrekk_lonn_sykepenger numeric;
alter table beregning add column lønn_fratrukket_sykepenger numeric;