create table minusbelop
(
    id                     varchar primary key,
    avtale_nr              integer,
    beløp                  numeric,
    løpenummer             integer,
    gjort_opp              boolean
);

alter table refusjon add column minusbelop_id varchar references minusbelop (id);

create index on minusbelop(avtale_nr);
