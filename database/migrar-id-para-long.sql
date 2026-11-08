
BEGIN;

ALTER TABLE public.estabelecimento
ADD COLUMN id_novo BIGINT GENERATED ALWAYS AS IDENTITY;

ALTER TABLE public.estabelecimento DROP COLUMN id;

ALTER TABLE public.estabelecimento RENAME COLUMN id_novo TO id;

ALTER TABLE public.estabelecimento ADD PRIMARY KEY (id);

COMMIT;
