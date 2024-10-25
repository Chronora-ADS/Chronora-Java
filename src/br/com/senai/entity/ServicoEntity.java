package br.com.senai.entity;

public class ServicoEntity {

    private String id;
    private String descricao;
    private UsuarioEntity beneficiario;
    private UsuarioEntity prestador;
    private EnderecoEntity endereco;
    private LogTransacaoEntity logTransacao;

    public ServicoEntity() {}

    public ServicoEntity(String id, String descricao, UsuarioEntity beneficiario, UsuarioEntity prestador, EnderecoEntity endereco, LogTransacaoEntity logTransacao) {
        this.id = id;
        this.descricao = descricao;
        this.beneficiario = beneficiario;
        this.prestador = prestador;
        this.endereco = endereco;
        this.logTransacao = logTransacao;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public UsuarioEntity getBeneficiario() {
        return beneficiario;
    }

    public void setBeneficiario(UsuarioEntity beneficiario) {
        this.beneficiario = beneficiario;
    }

    public UsuarioEntity getPrestador() {
        return prestador;
    }

    public void setPrestador(UsuarioEntity prestador) {
        this.prestador = prestador;
    }

    public EnderecoEntity getEndereco() {
        return endereco;
    }

    public void setEndereco(EnderecoEntity endereco) {
        this.endereco = endereco;
    }

    public LogTransacaoEntity getLogTransacao() {
        return logTransacao;
    }

    public void setLogTransacao(LogTransacaoEntity logTransacao) {
        this.logTransacao = logTransacao;
    }
}
