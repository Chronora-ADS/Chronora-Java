package br.com.senai.service;

import br.com.senai.exception.Auth.AuthException;
import br.com.senai.exception.NotFound.UserNotFoundException;
import br.com.senai.exception.Validation.EmailAlreadyExistsException;
import br.com.senai.exception.Validation.PhoneNumberAlreadyExistsException;
import br.com.senai.exception.Validation.QuantityChronosInvalidException;
import br.com.senai.model.DTO.DocumentDTO;
import br.com.senai.model.DTO.SupabaseUserDTO;
import br.com.senai.model.DTO.UserEditDTO;
import br.com.senai.model.entity.DocumentEntity;
import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.model.enums.ServiceStatus;
import br.com.senai.repository.NotificationRepository;
import br.com.senai.repository.ServiceRepository;
import br.com.senai.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SupabaseAuthService supabaseAuthService;
    private final SupabaseStorageService storageService;
    private final PasswordEncoder passwordEncoder;
    private final ServiceRepository serviceRepository;
    private final NotificationRepository notificationRepository;

    public UserService(
            UserRepository userRepository,
            SupabaseAuthService supabaseAuthService,
            SupabaseStorageService storageService,
            PasswordEncoder passwordEncoder,
            ServiceRepository serviceRepository,
            NotificationRepository notificationRepository
    ) {
        this.userRepository = userRepository;
        this.supabaseAuthService = supabaseAuthService;
        this.storageService = storageService;
        this.passwordEncoder = passwordEncoder;
        this.serviceRepository = serviceRepository;
        this.notificationRepository = notificationRepository;
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

        String updatedName = userEntity.getName();
        if (userEditDTO.getName() != null && !userEditDTO.getName().trim().isEmpty()) {
            updatedName = userEditDTO.getName().trim();
            userEntity.setName(updatedName);
        }

        String updatedEmail = userEntity.getEmail();
        if (userEditDTO.getEmail() != null && !userEditDTO.getEmail().trim().isEmpty()) {
            String email = userEditDTO.getEmail().trim();
            userRepository.findByEmail(email)
                    .filter(existingUser -> !Objects.equals(existingUser.getId(), userEntity.getId()))
                    .ifPresent(existingUser -> {
                        throw new EmailAlreadyExistsException(email);
                    });
            updatedEmail = email;
            userEntity.setEmail(updatedEmail);
        }

        Long updatedPhoneNumber = userEntity.getPhoneNumber();
        if (userEditDTO.getPhoneNumber() != null) {
            userRepository.findByPhoneNumber(userEditDTO.getPhoneNumber())
                    .filter(existingUser -> !Objects.equals(existingUser.getId(), userEntity.getId()))
                    .ifPresent(existingUser -> {
                        throw new PhoneNumberAlreadyExistsException(userEditDTO.getPhoneNumber().toString());
                    });
            updatedPhoneNumber = userEditDTO.getPhoneNumber();
            userEntity.setPhoneNumber(updatedPhoneNumber);
        }

        if (userEditDTO.getDocument() != null) {
            userEntity.setDocumentEntity(buildDocumentEntity(userEditDTO.getDocument(), tokenHeader));
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
        deleteUserDependencies(user);

        if (user.getSupabaseUserId() != null && !user.getSupabaseUserId().isBlank()) {
            supabaseAuthService.deleteUser(user.getSupabaseUserId());
        }

        userRepository.delete(user);
    }

    private DocumentEntity buildDocumentEntity(DocumentDTO documentDTO, String tokenHeader) {
        String documentUrl = storageService.uploadBase64Image(
                documentDTO.getData(),
                "users",
                extractBearerToken(tokenHeader),
                documentDTO.getType()
        );

        DocumentEntity documentEntity = new DocumentEntity();
        documentEntity.setName(documentDTO.getName());
        documentEntity.setType(documentDTO.getType());
        documentEntity.setUrl(documentUrl);
        return documentEntity;
    }

    private String extractBearerToken(String tokenHeader) {
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            return tokenHeader.substring(7);
        }
        return tokenHeader;
    }

    private void syncUserWithSupabase(
            String tokenHeader,
            String email,
            String password,
            String name,
            Long phoneNumber
    ) {
        Map<String, Object> metadata = new HashMap<>();
        if (name != null && !name.isBlank()) {
            metadata.put("name", name);
        }
        if (phoneNumber != null) {
            metadata.put("phone", phoneNumber);
        }

        supabaseAuthService.updateUser(
                extractBearerToken(tokenHeader),
                email,
                password,
                metadata
        );
    }

    private void deleteUserDependencies(UserEntity user) {
        notificationRepository.deleteAllByUser(user);

        for (ServiceEntity acceptedService : serviceRepository.findAllByUserAccepted(user)) {
            acceptedService.setUserAccepted(null);
            if (acceptedService.getStatus() == ServiceStatus.ACEITO) {
                acceptedService.setStatus(ServiceStatus.CRIADO);
            } else if (acceptedService.getStatus() == ServiceStatus.EM_ANDAMENTO) {
                acceptedService.setStatus(ServiceStatus.CANCELADO);
            }
            acceptedService.setVerificationCode(null);
            acceptedService.setVerificationCodeExpiresAt(null);
            serviceRepository.save(acceptedService);
        }

        java.util.List<ServiceEntity> createdServices = serviceRepository.findAllByUserCreator(user);
        if (!createdServices.isEmpty()) {
            notificationRepository.deleteAllByServiceIn(createdServices);
            serviceRepository.deleteAll(createdServices);
        }
    }

    private void validateChronosAmount(Integer chronos) {
        if (chronos == null || chronos <= 0) {
            throw new QuantityChronosInvalidException("A quantidade de chronos deve ser maior que zero.");
        }
    }
}
