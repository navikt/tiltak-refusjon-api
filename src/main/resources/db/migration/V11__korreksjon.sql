alter table refusjon add column korreksjon_av_id varchar references refusjon(id);
alter table refusjon add column korrigeres_av_id varchar references refusjon(id);
alter table beregning add column tidligere_utbetalt numeric default 0;