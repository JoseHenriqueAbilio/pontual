CREATE SCHEMA IF NOT EXISTS extensions;
CREATE EXTENSION IF NOT EXISTS postgis WITH SCHEMA extensions;

CREATE TABLE public.estabelecimento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome TEXT NOT NULL,
    descricao TEXT,
    categoria TEXT NOT NULL,
    telefone TEXT,
    endereco TEXT,
    localizacao extensions.GEOGRAPHY(POINT, 4326) NOT NULL
);

CREATE INDEX idx_estabelecimento_localizacao
ON public.estabelecimento
USING GIST (localizacao);
