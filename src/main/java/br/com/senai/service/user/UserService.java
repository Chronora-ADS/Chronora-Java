package br.com.senai.service.user;

import br.com.senai.exception.Auth.AuthException;
import br.com.senai.exception.Validation.EmailAlreadyExistsException;
import br.com.senai.exception.Validation.PhoneNumberAlreadyExistsException;
import br.com.senai.exception.Validation.QuantityChronosInvalidException;
import br.com.senai.model.DTO.user.DocumentDTO;
import br.com.senai.model.DTO.user.SupabaseUserDTO;
import br.com.senai.model.DTO.user.UserEditDTO;
import br.com.senai.model.entity.DocumentEntity;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.repository.UserRepository;
import br.com.senai.service.auth.AuthService;
import br.com.senai.service.auth.SupabaseAuthService;
import br.com.senai.service.service.SupabaseStorageService;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AuthService authService;
    private final SupabaseAuthService supabaseAuthService;
    private final SupabaseStorageService storageService;
    private final PasswordEncoder passwordEncoder;

    public UserEntity buyChronos(String tokenHeader, Integer chronos) {
        UserEntity userEntity = getLoggedUser(tokenHeader);
        validateChronosAmount(chronos);

        if (userEntity.getTimeChronos() + chronos > 300) {
            throw new QuantityChronosInvalidException("Excedido limite de chronos de 300 por usuário.");
        }

        userEntity.setTimeChronos(userEntity.getTimeChronos() + chronos);
        return userRepository.save(userEntity);
    }

    public UserEntity sellChronos(String tokenHeader, Integer chronos) {
        UserEntity userEntity = getLoggedUser(tokenHeader);
        validateChronosAmount(chronos);

        if (userEntity.getTimeChronos() - chronos < 0) {
            throw new QuantityChronosInvalidException("O limite mínimo de chronos e 0 por usuário.");
        }

        userEntity.setTimeChronos(userEntity.getTimeChronos() - chronos);
        return userRepository.save(userEntity);
    }

    public UserEntity creditChronosToUser(UserEntity user, Integer amount) {
        validateChronosAmount(amount);
        if (user.getTimeChronos() + amount > 300) {
            throw new QuantityChronosInvalidException(
                "Sua carteira atingiu o limite de 300 Chronos. Venda ou use alguns Chronos para poder receber os proximos."
            );
        }
        user.setTimeChronos(user.getTimeChronos() + amount);
        return userRepository.save(user);
    }

    public UserEntity getLoggedUser(String tokenHeader) {
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            throw new AuthException("Token inválido.");
        }

        String token = tokenHeader.substring(7);
        SupabaseUserDTO supabaseUserDTO = supabaseAuthService.validateToken(token);
        return authService.resolveUserForSupabaseUser(supabaseUserDTO);
    }

    public Optional<UserEntity> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return userRepository.findById(id).filter(UserEntity::isActive);
    }

    public UserEntity put(UserEditDTO userEditDTO, String tokenHeader) {
        UserEntity userEntity = getLoggedUser(tokenHeader);

        if (!Objects.equals(userEditDTO.getId(), userEntity.getId())) {
            throw new AuthException("Credenciais inválidas.");
        }

        String updatedName = userEntity.getName();
        if (userEditDTO.getName() != null && !userEditDTO.getName().trim().isEmpty()) {
            updatedName = userEditDTO.getName().trim();
            userEntity.setName(updatedName);
        }

        String updatedEmail = userEntity.getEmail();
        if (userEditDTO.getEmail() != null && !userEditDTO.getEmail().trim().isEmpty()) {
            String email = userEditDTO.getEmail().trim();
            if (userRepository.existsByEmail(email)) {
                throw new EmailAlreadyExistsException(email);
            }
            updatedEmail = email;
            userEntity.setEmail(updatedEmail);
        }

        Long updatedPhoneNumber = userEntity.getPhoneNumber();
        if (userEditDTO.getPhoneNumber() != null) {
            if (userRepository.existsByPhoneNumber(userEditDTO.getPhoneNumber())) {
                throw new PhoneNumberAlreadyExistsException(String.valueOf(userEditDTO.getPhoneNumber()));
            }
            updatedPhoneNumber = userEditDTO.getPhoneNumber();
            userEntity.setPhoneNumber(updatedPhoneNumber);
        }

        if (userEditDTO.getDocument() != null) {
            DocumentDTO documentDTO = userEditDTO.getDocument();
            String documentUrl = storageService.uploadBase64Image(documentDTO.getData(),
                    "users", extractBearerToken(tokenHeader), documentDTO.getType());

            DocumentEntity documentEntity = new DocumentEntity();
            documentEntity.setName(documentDTO.getName());
            documentEntity.setType(documentDTO.getType());
            documentEntity.setUrl(documentUrl);
            userEntity.setDocumentEntity(documentEntity);
        }

        if (userEditDTO.getProfileImage() != null) {
            String urlImageUpload = storageService.uploadBase64Image(userEditDTO.getProfileImage().getData(),
                    "users", extractBearerToken(tokenHeader), userEditDTO.getProfileImage().getType());
            userEntity.setProfileImage(urlImageUpload);
        }

        String updatedPassword = null;
        if (userEditDTO.getPassword() != null && !userEditDTO.getPassword().trim().isEmpty()) {
            updatedPassword = userEditDTO.getPassword().trim();
            userEntity.setPassword(passwordEncoder.encode(updatedPassword));
        }

        syncUserWithSupabase(tokenHeader, updatedEmail, updatedPassword, updatedName, updatedPhoneNumber);
        return userRepository.save(userEntity);
    }

    @Transactional
    public void delete(String tokenHeader) {
        UserEntity user = getLoggedUser(tokenHeader);
        user.setActive(false);
        user.setDeletedAt(LocalDateTime.now(ZoneOffset.UTC));
        userRepository.save(user);
    }

    private String extractBearerToken(String tokenHeader) {
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            return tokenHeader.substring(7);
        }
        return tokenHeader;
    }

    private void syncUserWithSupabase(String tokenHeader, String email, String password, String name, Long phoneNumber) {
        Map<String, Object> metadata = new HashMap<>();
        if (name != null && !name.isBlank()) {
            metadata.put("name", name);
        }
        if (phoneNumber != null) {
            metadata.put("phone", phoneNumber);
        }
        supabaseAuthService.updateUser(extractBearerToken(tokenHeader), email, password, metadata);
    }

    private void validateChronosAmount(Integer chronos) {
        if (chronos == null || chronos <= 0) {
            throw new QuantityChronosInvalidException("A quantidade de chronos deve ser maior que zero.");
        }
    }
}
