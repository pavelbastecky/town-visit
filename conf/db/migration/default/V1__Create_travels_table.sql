-- We need to use " character because of H2
-- Slick quotes by default but H2 interprets it as name of the table / entity
CREATE TABLE "travels" (
    "id"                SERIAL  NOT NULL,
    "date"              DATE    NOT NULL,
    "city"              VARCHAR NOT NULL,
    "kilometers"        INT     NOT NULL,
    "traveled_total"    BIGINT  NULL
);

CREATE INDEX travels_city_idx ON "travels"("city");