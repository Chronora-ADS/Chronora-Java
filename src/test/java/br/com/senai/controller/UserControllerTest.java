package br.com.senai.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.senai.model.DTO.UserEditDTO;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private static final String TOKEN_HEADER = "Bearer token-valido";

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void deveComprarChronosViaController() {
        UserEntity user = criarUsuario(120);
        when(userService.buyChronos(TOKEN_HEADER, 20)).thenReturn(user);

        var response = userController.buyChronos(TOKEN_HEADER, 20);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(120, response.getBody().getTimeChronos());
        verify(userService).buyChronos(TOKEN_HEADER, 20);
    }

    @Test
    void deveVenderChronosViaController() {
        UserEntity user = criarUsuario(80);
        when(userService.sellChronos(TOKEN_HEADER, 20)).thenReturn(user);

        var response = userController.sellChronos(TOKEN_HEADER, 20);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(80, response.getBody().getTimeChronos());
        verify(userService).sellChronos(TOKEN_HEADER, 20);
    }

    @Test
    void deveBuscarUsuarioLogadoViaController() {
        UserEntity user = criarUsuario(100);
        when(userService.getLoggedUser(TOKEN_HEADER)).thenReturn(user);

        var response = userController.getLoggedUser(TOKEN_HEADER);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Ana Silva", response.getBody().getName());
        verify(userService).getLoggedUser(TOKEN_HEADER);
    }

    @Test
    void deveEditarPerfilViaController() {
        UserEditDTO dto = new UserEditDTO();
        dto.setId(1L);
        dto.setName("Ana Atualizada");
        UserEntity user = criarUsuario(100);
        user.setName("Ana Atualizada");
        when(userService.put(dto, TOKEN_HEADER)).thenReturn(user);

        var response = userController.put(TOKEN_HEADER, dto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Ana Atualizada", response.getBody().getName());
        verify(userService).put(dto, TOKEN_HEADER);
    }

    @Test
    void deveExcluirUsuarioViaController() {
        var response = userController.delete(TOKEN_HEADER);

        assertEquals(200, response.getStatusCode().value());
        verify(userService).delete(TOKEN_HEADER);
    }

    private UserEntity criarUsuario(int chronos) {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setName("Ana Silva");
        user.setEmail("ana@chronora.com");
        user.setPhoneNumber(11999999999L);
        user.setPassword("hash");
        user.setTimeChronos(chronos);
        user.setSupabaseUserId("supabase-123");
        return user;
    }
}
