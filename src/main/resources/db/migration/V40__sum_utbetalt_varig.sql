alter table refusjonsgrunnlag add column sum_utbetalt_varig numeric default 0;
alter table beregning add column over_fem_grunnbeløp boolean;
create index on refusjon(deltaker_fnr);
