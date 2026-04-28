# Chronora Java Backend

Backend Spring Boot da Chronora.

## Render dev

O deploy de desenvolvimento esta preparado para a branch `master` com o arquivo `render.yaml`.

### Variaveis obrigatorias

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SUPABASE_URL`
- `SUPABASE_ANON_KEY`
- `SUPABASE_SERVICE_ROLE`

### Variaveis opcionais

- `SUPABASE_STORAGE_BUCKET` (padrao: `service-images`)
- `APP_SEED_ENABLED` (padrao: `false`)

### Subir no Render

1. Conecte este repositorio ao Render.
2. Use a branch `main`.
3. Crie o servico a partir do `render.yaml`.
4. Preencha as variaveis secretas no painel do Render.
5. Rode o deploy e valide `/healthz`.

### Health check

- `GET /health`
- `GET /healthz`
- `HEAD /health`

## Rodar localmente

Use Java 17.

### Opcao 1: arquivo local ignorado pelo Git

Preencha um arquivo `.env.local` na raiz com base em `.env.local.example` e execute:

```powershell
.\scripts\run-local.ps1
```

Esse script:

- carrega as variaveis de `.env.local`
- tenta usar um JDK 17 instalado na maquina
- sobe o backend na porta `8085`

### Opcao 2: variaveis no terminal

Defina as mesmas variaveis de ambiente e execute:

```powershell
.\mvnw spring-boot:run
```
### Observacoes

- Se o PowerShell bloquear a execucao do script, rode:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run-local.ps1
```

- Se a porta `8085` ja estiver em uso, feche a outra instancia do backend antes de subir novamente
