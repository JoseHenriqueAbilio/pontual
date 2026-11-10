# Pontual

A ideia do projeto é uma aplicação que oferece um serviço de rastreamento de estabelecimentos comerciais (cadastro, visualização e deleção, sem nenhum mecanismo de autorização atualmente).

O banco de dados utilizado é o Supabase.

## Como rodar localmente

1. Instale o JDK 21 e o Maven. Se usar IntelliJ, importe `backend/pom.xml` como projeto Maven.
2. Em um banco Supabase novo, execute [`database/schema.sql`](database/schema.sql) no SQL Editor para habilitar o PostGIS e criar a tabela `estabelecimento`. Pule esse passo se a tabela já estiver configurada.
3. Em **Supabase → Connect → Session pooler**, copie o host e o usuário. O projeto já tem um arquivo .env com um template pra preencher as credenciais.

Se estiver usando IntelliJ você pode simplesmente executar a classe GuiaEstabelecimentosApplication, ou você pode rodar:

`cd backend`
`mvn spring-boot:run`


O Pontual usa **Leaflet 1.9.4**. Estes são os links:

* [Página oficial de instalação do Leaflet](https://leafletjs.com/install.html#leaflet-19)
* [Guia rápido oficial do Leaflet 1.9](https://leafletjs.com/examples-v1/quick-start/)
* [Arquivo `leaflet.css` no jsDelivr](https://cdn.jsdelivr.net/npm/leaflet@1.9.4/dist/leaflet.css)
* [Arquivo `leaflet.js` no unpkg](https://unpkg.com/leaflet@1.9.4/dist/leaflet.js)
* [Pasta de imagens e ícones no jsDelivr](https://cdn.jsdelivr.net/npm/leaflet@1.9.4/dist/images/)

O link para acessar a aplicação pelo render é:
https://pontual-bfkg.onrender.com
