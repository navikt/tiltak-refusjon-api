alter table refusjonsgrunnlag add column forrige_refusjon_minus_beløp numeric default 0;
alter table refusjon add column skal_forrige_refusjon_sendes_først boolean default false;