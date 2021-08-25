alter table refusjon add column korreksjon_av_id varchar references refusjon(id);
alter table refusjon add column korrigeres_av_id varchar references refusjon(id);
alter table beregning add column tidligere_utbetalt numeric default 0;

create table refusjon_korreksjonsgrunner
(
    refusjon_id  varchar references refusjon(id),
    korreksjonsgrunner varchar,
    primary key (refusjon_id, korreksjonsgrunner)
);

alter table inntektslinje add column er_i_periode boolean default null;