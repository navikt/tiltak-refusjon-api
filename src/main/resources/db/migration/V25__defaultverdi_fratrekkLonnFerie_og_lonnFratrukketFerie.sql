alter table beregning rename column fratrekk_lonn_ferie to fratrekk_lønn_ferie;

update beregning set fratrekk_lønn_ferie = 0 where fratrekk_lønn_ferie is null;
update beregning set lønn_fratrukket_ferie = 0 where lønn_fratrukket_ferie is null;

alter table beregning alter column fratrekk_lønn_ferie set default 0;
alter table beregning alter column lønn_fratrukket_ferie set default 0;