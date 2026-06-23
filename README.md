# Chronora Java Backend

Backend Spring Boot da Chronora.


# Pré‑requisitos

- Java 17 (JDK)
- Maven (opcional – o wrapper `mvnw` já está incluso)
- PowerShell (para executar os scripts locais)
- PostgreSQL (para banco local, caso não use Supabase)


# Configuração

### Variáveis de ambiente

O backend precisa das seguintes variáveis para funcionar:

| Variável | Obrigatória | Descrição |
|----------|-------------|-----------|
| `SPRING_DATASOURCE_URL` | ✅ | URL de conexão com o banco PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | ✅ | Usuário do banco |
| `SPRING_DATASOURCE_PASSWORD` | ✅ | Senha do banco |
| `SUPABASE_URL` | ✅ | URL do projeto Supabase |
| `SUPABASE_ANON_KEY` | ✅ | Chave anônima do Supabase |
| `SUPABASE_SERVICE_ROLE` | ✅ | Chave service role do Supabase |
| `SUPABASE_STORAGE_BUCKET` | ❌ | Nome do bucket de storage (padrão: `service-images`) |
| `APP_SEED_ENABLED` | ❌ | Habilita seed de dados iniciais (padrão: `false`) |

### Arquivo `.env.local`

Para desenvolvimento local, crie um arquivo `.env.local` na raiz do projeto baseado no `.env.local.example`:

```text
SUPABASE_URL=https://seu-projeto.supabase.co
SUPABASE_ANON_KEY=cole_a_chave_anon_aqui
SUPABASE_SERVICE_ROLE=cole_a_chave_service_role_aqui
SPRING_DATASOURCE_URL=jdbc:postgresql://seu-host:5432/postgres?sslmode=require
SPRING_DATASOURCE_USERNAME=postgres.seu_project_ref
SPRING_DATASOURCE_PASSWORD=cole_a_senha_do_banco_aqui
SUPABASE_STORAGE_BUCKET=service-images
APP_SEED_ENABLED=false

```

# Execução local
- Execute o script que carrega as variáveis do .env.local e sobe o backend na porta 8085: .\scripts\run-local.ps1


# Deploy no Render
O deploy é feito via Docker (ou render.yaml) no Render.

## Produção (main branch)
- Branch: main
- Arquivo de configuração: render.yaml (ou Dockerfile)
- Variáveis obrigatórias: todas as listadas acima (preenchidas com dados de produção)

### Passos
- Conecte o repositório ao Render como Web Service.
- Selecione a branch main.
- Escolha Docker como runtime (ou use o render.yaml).
- Defina todas as variáveis de ambiente no painel do Render.
- Clique em Deploy.
- Valide o health check em /healthz.

## Desenvolvimento (master branch)
- Branch: master
- Arquivo de configuração: render.yaml (ou Dockerfile)
- Variáveis obrigatórias: mesmas da produção, com valores do ambiente de desenvolvimento

### Passos
- Conecte o repositório ao Render.
- Selecione a branch master.
- Configure o serviço com o render.yaml ou Docker.
- Preencha as variáveis com os dados de desenvolvimento.


# Health check
O backend disponibiliza os seguintes endpoints para verificação de saúde:
- GET /health
- GET /healthz
- HEAD /health
Use‑os para monitoramento no Render ou em outros sistemas.


# Estrutura de branches
- master → ambiente de desenvolvimento
- main → ambiente de produção
