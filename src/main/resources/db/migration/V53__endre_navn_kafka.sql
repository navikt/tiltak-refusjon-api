-- Opprydding i systembrukernavn (admin-bruker endres ikke, men frontend endres til at kun rolle vises for systembrukere)
UPDATE hendelseslogg SET utført_av = 'system' where utført_av in ('kafka', 'Kafka') and utført_rolle = 'SYSTEM';
UPDATE hendelseslogg SET utført_av = 'system', utført_rolle = 'SYSTEM' where utført_av = 'Kafka' and utført_rolle is null;
