package br.com.senai.entity;

public class HabilidadeEntity {

    private String id;
    private String nomeHabilidade;
    private String nivel;

    public HabilidadeEntity() {}

    public HabilidadeEntity(String id, String nomeHabilidade, String nivel) {
        this.id = id;
        this.nomeHabilidade = nomeHabilidade;
        this.nivel = nivel;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNomeHabilidade() {
        return nomeHabilidade;
    }

    public void setNomeHabilidade(String nomeHabilidade) {
        this.nomeHabilidade = nomeHabilidade;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }
}
