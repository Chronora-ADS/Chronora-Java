package br.com.senai.entity;

import br.com.senai.enums.StatusUsuario;
import br.com.senai.utils.UUIDGenerator;

import java.util.List;

public class UsuarioEntity {

    // Atributos do objeto

    private String id;
    private String nome;
    private int idade;
    private int qntChronos;
    private String email;
    private String telefone;
    private StatusUsuario status;

    private List<HabilidadeEntity> habilidades;
    private List<CertificadoEntity> certificados;
    private List<LogTransacaoEntity> logTransacao;

    // ----------------------------------

    //Metodos

    public UsuarioEntity() {}

    //Construtor cheio
    public UsuarioEntity(String nome, int idade, String email, String telefone) {
        this.id = UUIDGenerator.generateUUID();
        this.nome = nome;
        this.idade = idade;
        this.email = email;
        this.telefone = telefone;
        this.status = StatusUsuario.ATIVO;
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

    public StatusUsuario getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = StatusUsuario.valueOf(status);
    }

    @Override
    public String toString() {
        return this.getId() + " " + this.getNome() + " " + this.getIdade() + " " + this.getEmail() + " " + this.getTelefone() + " " + this.getStatus();
    }

    //---------------------------------------------
}
