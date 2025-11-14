package br.com.senai.service;

import br.com.senai.exception.Auth.AuthException;
import br.com.senai.exception.NotFound.UserNotFoundException;
import br.com.senai.exception.Validation.QuantityChronosInvalidException;
import br.com.senai.model.DTO.SupabaseUserDTO;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private final UserRepository userRepository;
    private final SupabaseAuthService supabaseAuthService;
    private final AuthService authService;

    public UserEntity buyChronos(String tokenHeader, Integer chronos) {
        UserEntity userEntity = getLoggedUser(tokenHeader);
        if (userEntity.getTimeChronos() + chronos > 300) {
            throw new QuantityChronosInvalidException("Excedido limite de chronos de 300 por usuário.");
        }
        userEntity.setTimeChronos(userEntity.getTimeChronos() + chronos);
        return userRepository.save(userEntity);
    }

    public UserEntity sellChronos(String tokenHeader, Integer chronos) {
        UserEntity userEntity = getLoggedUser(tokenHeader);
        if (userEntity.getTimeChronos() - chronos <= 0) {
            throw new QuantityChronosInvalidException("O limite mínimo de chronos é 0 por usuário.");
        }
        userEntity.setTimeChronos(userEntity.getTimeChronos() - chronos);
        return userRepository.save(userEntity);
    }

    public UserEntity getLoggedUser(String tokenHeader) {
        try {
            if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
                String token = tokenHeader.substring(7);

                // Valida o token no Supabase
                SupabaseUserDTO supabaseUserDTO = supabaseAuthService.validateToken(token);

                // Busca o usuário no banco local pelo ID do Supabase usando UserService
                UserEntity userEntity = authService.findBySupabaseUserId(supabaseUserDTO.getId());
                Optional<UserEntity> optionalUserEntity = userRepository.findById(userEntity.getId());
                if (optionalUserEntity.isPresent()) {
                    return optionalUserEntity.get();
                }
                throw new UserNotFoundException("Usuário não encontrado.");
            }
            throw new UserNotFoundException("Usuário não encontrado.");
        } catch (Exception e) {
            throw new AuthException("Token inválido.");
        }
    }
}
