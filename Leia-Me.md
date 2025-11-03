.\mvnw compile <- Compilar antes pra ter crtz

.\mvnw spring-boot:run <- faz o programa iniciar

Control + c Para encerrar o programa

Talvez colocar senha root no application.properties

--- MySQL

-- Cria o banco de dados
CREATE DATABASE faculdade;

-- Usa o banco de dados criado
USE faculdade;

-- Cria a tabela usuarios
CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, -- ID único para cada usuário
    nome VARCHAR(100) NOT NULL,           -- Nome do usuário
    senha VARCHAR(100) NOT NULL           -- Senha do usuário
);

--- Consulta MySQL

SELECT id, nome, senha
FROM usuario_entity;

--- Estabelecer conexão

http://localhost:8081/