package br.com.senai.entity;

public class CertificadoEntity {

    private String id;
    private String nomeCurso;
    private String nivel;
    private String periodo;
    private UsuarioEntity usuario;

    public CertificadoEntity() {}

    public CertificadoEntity(String id, String nomeCurso, String nivel, String periodo, UsuarioEntity usuario) {
        this.id = id;
        this.nomeCurso = nomeCurso;
        this.nivel = nivel;
        this.periodo = periodo;
        this.usuario = usuario;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNomeCurso() {
        return nomeCurso;
    }

    public void setNomeCurso(String nomeCurso) {
        this.nomeCurso = nomeCurso;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public UsuarioEntity getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioEntity usuario) {
        this.usuario = usuario;
    }
}
