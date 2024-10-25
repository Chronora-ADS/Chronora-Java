package br.com.senai.entity;

import java.util.Date;

public class LogTransacaoEntity {

    private String id;
    private int qntChronos;
    private Date data;
    private UsuarioEntity usuario;
    private ServicoEntity servico;

    public LogTransacaoEntity() {}

    public LogTransacaoEntity(String id, int qntChronos, Date data, UsuarioEntity usuario, ServicoEntity servico) {
        this.id = id;
        this.qntChronos = qntChronos;
        this.data = data;
        this.usuario = usuario;
        this.servico = servico;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getQntChronos() {
        return qntChronos;
    }

    public void setQntChronos(int qntChronos) {
        this.qntChronos = qntChronos;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public UsuarioEntity getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioEntity usuario) {
        this.usuario = usuario;
    }

    public ServicoEntity getServico() {
        return servico;
    }

    public void setServico(ServicoEntity servico) {
        this.servico = servico;
    }
}
