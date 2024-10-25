package br.com.senai.service;

import br.com.senai.entity.UsuarioEntity;

import java.util.List;

public class UsuarioService {

    //Instância
    UsuarioEntity usuarioEntity = new UsuarioEntity();

    public UsuarioEntity cadastrarUsuario(UsuarioEntity usuarioEntity) {
        return usuarioEntity;
    }

    public UsuarioEntity editarUsuario(UsuarioEntity usuarioEntity) {
        return usuarioEntity;
    }

    public UsuarioEntity listarUmUsuario(String id) {
        return null;
    }

    public UsuarioEntity listarTodosUsuarios(List<UsuarioEntity> usuarioEntity) {
        return null;
    }

    public void adicionarChronos(int qntChronos) {}

    public void removerChronos(int qntChronos) {}

    public void inverterStatus() {}
}
