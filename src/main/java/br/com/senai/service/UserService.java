package br.com.senai.service;

import br.com.senai.exception.Auth.AuthException;
import br.com.senai.exception.NotFound.UserNotFoundException;
import br.com.senai.exception.Validation.EmailAlreadyExistsException;
import br.com.senai.exception.Validation.PhoneNumberAlreadyExistsException;
import br.com.senai.exception.Validation.QuantityChronosInvalidException;
import br.com.senai.model.DTO.SupabaseUserDTO;
import br.com.senai.model.DTO.UserEditDTO;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.repository.UserRepository;
import java.util.Objects;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SupabaseAuthService supabaseAuthService;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            SupabaseAuthService supabaseAuthService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.supabaseAuthService = supabaseAuthService;
        this.passwordEncoder = passwordEncoder;
    }

    public UserEntity buyChronos(String tokenHeader, Integer chronos) {
        UserEntity userEntity = getLoggedUser(tokenHeader);
        validateChronosAmount(chronos);

        if (userEntity.getTimeChronos() + chronos > 300) {
            throw new QuantityChronosInvalidException("Excedido limite de chronos de 300 por usuario.");
        }

        userEntity.setTimeChronos(userEntity.getTimeChronos() + chronos);
        return userRepository.save(userEntity);
    }

    public UserEntity sellChronos(String tokenHeader, Integer chronos) {
        UserEntity userEntity = getLoggedUser(tokenHeader);
        validateChronosAmount(chronos);

        if (userEntity.getTimeChronos() - chronos < 0) {
            throw new QuantityChronosInvalidException("O limite minimo de chronos e 0 por usuario.");
        }

        userEntity.setTimeChronos(userEntity.getTimeChronos() - chronos);
        return userRepository.save(userEntity);
    }

    public UserEntity getLoggedUser(String tokenHeader) {
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            throw new AuthException("Token invalido.");
        }

        String token = tokenHeader.substring(7);
        SupabaseUserDTO supabaseUserDTO = supabaseAuthService.validateToken(token);

        return userRepository.findBySupabaseUserId(supabaseUserDTO.getId())
                .orElseThrow(() -> new UserNotFoundException("Usuario nao encontrado."));
    }

    public UserEntity put(UserEditDTO userEditDTO, String tokenHeader) {
        UserEntity userEntity = getLoggedUser(tokenHeader);

        if (!Objects.equals(userEditDTO.getId(), userEntity.getId())) {
            throw new AuthException("Credenciais invalidas.");
        }
        if (userEditDTO.getName() != null && !userEditDTO.getName().trim().isEmpty()) {
            userEntity.setName(userEditDTO.getName());
        }
        if (userEditDTO.getEmail() != null && !userEditDTO.getEmail().equals(userEntity.getEmail())) {
            if (userRepository.findByEmail(userEditDTO.getEmail()).isPresent()) {
                throw new EmailAlreadyExistsException(userEditDTO.getEmail());
            }
            userEntity.setEmail(userEditDTO.getEmail());
        }
        if (userEditDTO.getPhoneNumber() != null && !userEditDTO.getPhoneNumber().equals(userEntity.getPhoneNumber())) {
            if (userRepository.findByPhoneNumber(userEditDTO.getPhoneNumber()).isPresent()) {
                throw new PhoneNumberAlreadyExistsException(userEditDTO.getPhoneNumber().toString());
            }
            userEntity.setPhoneNumber(userEditDTO.getPhoneNumber());
        }
        if (userEditDTO.getDocument() != null) {
            userEntity.setDocumentEntity(userEditDTO.getDocument());
        }
        if (userEditDTO.getPassword() != null && !userEditDTO.getPassword().trim().isEmpty()) {
            userEntity.setPassword(passwordEncoder.encode(userEditDTO.getPassword()));
        }

        return userRepository.save(userEntity);
    }

    public void delete(String tokenHeader) {
        UserEntity userEntity = getLoggedUser(tokenHeader);
        userRepository.delete(userEntity);
    }

    private void validateChronosAmount(Integer chronos) {
        if (chronos == null || chronos <= 0) {
            throw new QuantityChronosInvalidException("A quantidade de chronos deve ser maior que zero.");
        }
    }
}
