alter table refusjon add column opprettet_tidspunkt TIMESTAMP WITH TIME ZONE;

update refusjon
set opprettet_tidspunkt =
        (select hendelseslogg.tidspunkt at time zone 'UTC'
         from hendelseslogg
         where hendelseslogg.refusjon_id = refusjon.id and hendelseslogg.event = 'RefusjonOpprettet');
