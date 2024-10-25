package br.com.senai.entity;

public class EnderecoEntity {

    public String id;
    public String pais;
    public String estado;
    public String cidade;
    public ServicoEntity servico;

    public EnderecoEntity() {}

    public EnderecoEntity(String id, String pais, String estado, String cidade, ServicoEntity servico) {
        this.id = id;
        this.pais = pais;
        this.estado = estado;
        this.cidade = cidade;
        this.servico = servico;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public ServicoEntity getServico() {
        return servico;
    }

    public void setServico(ServicoEntity servico) {
        this.servico = servico;
    }
}
