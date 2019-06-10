-- Create index for better sorting performance
CREATE INDEX travels_date_id_idx ON "travels"("date", "id");