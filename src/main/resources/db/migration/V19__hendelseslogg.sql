create table hendelseslogg
(
    id            varchar primary key,
    app_image_id  varchar,
    refusjon_id   varchar,
    korreksjon_id varchar,
    event         varchar,
    utført_av     varchar,
    tidspunkt     timestamp without time zone
);