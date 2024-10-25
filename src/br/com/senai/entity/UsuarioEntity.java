package br.com.senai.entity;

import java.util.List;

public class UsuarioEntity {

    // Atributos do objeto

    private String id;
    private String nome;
    private int idade;
    private int qntChronos;
    private String email;
    private String telefone;
    private boolean status;

    private List<HabilidadeEntity> habilidades;
    private List<CertificadoEntity> certificados;
    private List<LogTransacaoEntity> logTransacao;

    // ----------------------------------

    //Metodos

    public UsuarioEntity() {}

    //Construtor cheio
    public UsuarioEntity(String id, String nome, int idade, String email, String telefone, boolean status) {
        this.id = id;
        this.nome = nome;
        this.idade = idade;
        this.email = email;
        this.telefone = telefone;
        this.status = status;
    }

    //Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getIdade() {
        return idade;
    }

    public void setIdade(int idade) {
        this.idade = idade;
    }

    public int getQntChronos() {
        return qntChronos;
    }

    public void setQntChronos(int qntChronos) {
        this.qntChronos = qntChronos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    //---------------------------------------------
}
