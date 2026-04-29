package br.com.senai.controller;

import br.com.senai.exception.Validation.PhoneNumberAlreadyExistsException;
import br.com.senai.model.DTO.SupabaseUserDTO;
import br.com.senai.model.DTO.UserDTO;
import br.com.senai.service.AuthService;
import br.com.senai.service.SupabaseAuthService;
import br.com.senai.util.JWTBlacklist;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private SupabaseAuthService supabaseAuthService;

    @Mock
    private JWTBlacklist jwtBlacklist;

    @InjectMocks
    private AuthController authController;

    @Test
    void registerDoesNotCreateSupabaseUserWhenPhoneAlreadyExists() {
        UserDTO userDTO = userDTO();
        doThrow(new PhoneNumberAlreadyExistsException(userDTO.getPhoneNumber().toString()))
                .when(authService)
                .validateRegistrationAvailable(userDTO);

        ResponseEntity<?> response = authController.register(userDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().toString()).contains(userDTO.getPhoneNumber().toString());
        verify(supabaseAuthService, never()).signUp(any(), any(), anyMap());
        verify(authService, never()).register(any(), any());
    }

    @Test
    void registerDeletesSupabaseUserWhenLocalRegistrationFailsAfterSignUp() {
        UserDTO userDTO = userDTO();
        SupabaseUserDTO supabaseUserDTO = SupabaseUserDTO.builder()
                .id("supabase-user-id")
                .email(userDTO.getEmail())
                .build();

        when(supabaseAuthService.signUp(eq(userDTO.getEmail()), eq(userDTO.getPassword()), anyMap()))
                .thenReturn(supabaseUserDTO);
        doThrow(new RuntimeException("Falha ao salvar app_user"))
                .when(authService)
                .register(userDTO, supabaseUserDTO.getId());

        ResponseEntity<?> response = authController.register(userDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Falha ao salvar app_user");
        verify(supabaseAuthService).deleteUser(supabaseUserDTO.getId());
    }

    private UserDTO userDTO() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("Usuario Teste");
        userDTO.setEmail("usuario.teste@chronora.test");
        userDTO.setPhoneNumber(11999999999L);
        userDTO.setPassword("ChronoraTeste123!");
        return userDTO;
    }
}
