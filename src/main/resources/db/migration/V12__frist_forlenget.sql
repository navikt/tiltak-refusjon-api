create table frist_forlenget
(
    id           varchar primary key,
    refusjon_id  varchar references refusjon (id),
    gammel_frist date,
    ny_frist     date,
    årsak        varchar,
    utført_av    varchar,
    tidspunkt    timestamp without time zone
);

alter table refusjon add column forrige_frist_for_godkjenning date;