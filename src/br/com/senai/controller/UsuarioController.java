package br.com.senai.controller;

import br.com.senai.entity.UsuarioEntity;
import br.com.senai.service.UsuarioService;

import java.util.List;

public class UsuarioController {

    UsuarioService usuarioService = new UsuarioService();

    public UsuarioEntity cadastrarUsuario(UsuarioEntity usuarioEntity) {
        return usuarioService.cadastrarUsuario(usuarioEntity);
    }
}
