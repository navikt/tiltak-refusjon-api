-- Opprydding i hendelsesloggen : Arbeidsgiver forlenger ikke frist men krysser av for fravær.
-- Endre event typen på de gamle så de skal bli likt med den nye event typen.
UPDATE hendelseslogg SET event = 'KryssetAvForFravær' WHERE event = 'FristForlenget' and utført_rolle = 'ARBEIDSGIVER';
