-- tidspunkt-feltet har ikke tidssone i databasen, og er en "localdatetime" i jpa-entiteten.
-- For å få tidspunktene til å bli riktige MED tidssone må vi derfor bruke "at time zone" med
-- europe/oslo i stedet for utc, slik vi gjorde med feltene hvor jpa-entiteten bruker instant.
alter table hendelseslogg alter column tidspunkt type timestamp with time zone
    using tidspunkt at time zone 'Europe/Oslo';
