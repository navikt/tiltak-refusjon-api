CREATE INDEX IF NOT EXISTS refusjonsgrunnlag_beregning_id_idx ON refusjonsgrunnlag (beregning_id);
CREATE INDEX IF NOT EXISTS varsling_refusjon_id_idx ON varsling (refusjon_id);
CREATE INDEX IF NOT EXISTS tilskuddsgrunnlag_tilskuddperiode_id_idx ON tilskuddsgrunnlag (tilskuddsperiode_id);
