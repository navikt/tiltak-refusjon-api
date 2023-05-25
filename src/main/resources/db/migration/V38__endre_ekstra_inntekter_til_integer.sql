alter table refusjon add column unntak_om_inntekter_fremitid numeric default 0;
update refusjon set unntak_om_inntekter_fremitid = 2 where unntak_om_inntekter_to_måneder_frem = true;
alter table refusjon drop column unntak_om_inntekter_to_måneder_frem;