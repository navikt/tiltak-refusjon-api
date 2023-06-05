alter table refusjon add column sist_endret timestamp default now();
/* Bør det settes til godkjent dato for eldre refusjoner? */