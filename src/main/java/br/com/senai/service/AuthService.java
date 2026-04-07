package br.com.senai.service;

import br.com.senai.exception.Auth.AuthException;
import br.com.senai.exception.NotFound.UserNotFoundException;
import br.com.senai.exception.Validation.EmailAlreadyExistsException;
import br.com.senai.exception.Validation.PhoneNumberAlreadyExistsException;
import br.com.senai.model.DTO.LoginDTO;
import br.com.senai.model.DTO.UserDTO;
import br.com.senai.model.entity.DocumentEntity;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SupabaseStorageService storageService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            SupabaseStorageService storageService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.storageService = storageService;
    }

    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(userEntity -> User.builder()
                        .username(userEntity.getEmail())
                        .password(userEntity.getPassword())
                        .roles(userEntity.getRoles().toArray(new String[0]))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));
    }

    public UserEntity findBySupabaseUserId(String supabaseUserId) {
        return userRepository.findBySupabaseUserId(supabaseUserId)
                .orElseThrow(() -> new UserNotFoundException("Usuário com ID Supabase " + supabaseUserId + " não encontrado."));
    }

    public UserEntity register(UserDTO userDTO, String supabaseUserId) {
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(userDTO.getEmail());
        }

        if (userRepository.findByPhoneNumber(userDTO.getPhoneNumber()).isPresent()) {
            throw new PhoneNumberAlreadyExistsException(userDTO.getPhoneNumber().toString());
        }

        String documentUrl = storageService.uploadBase64Image(
                userDTO.getDocument().getData(),
                "users",
                null
        );

        UserEntity userEntity = new UserEntity();
        userEntity.setName(userDTO.getName());
        userEntity.setEmail(userDTO.getEmail());
        userEntity.setPhoneNumber(userDTO.getPhoneNumber());
        userEntity.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        userEntity.setSupabaseUserId(supabaseUserId);
        userEntity.setRoles(List.of("USER"));
        userEntity.setTimeChronos(12);

        DocumentEntity doc = new DocumentEntity();
        doc.setName(userDTO.getDocument().getName());
        doc.setType(userDTO.getDocument().getType());
        doc.setUrl(documentUrl);
        userEntity.setDocumentEntity(doc);

        return userRepository.save(userEntity);
    }

    public Map<String, Object> buildUserMetadata(UserDTO userDTO) {
        Map<String, Object> userMetadata = new HashMap<>();
        userMetadata.put("name", userDTO.getName());
        userMetadata.put("phone", userDTO.getPhoneNumber());
        return userMetadata;
    }

    public UserEntity authenticate(LoginDTO loginDTO) {
        Optional<UserEntity> userOptional = userRepository.findByEmail(loginDTO.getEmail());
        if (userOptional.isEmpty()) {
            throw new AuthException("Credenciais inválidas");
        }

        UserEntity user = userOptional.get();
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new AuthException("Credenciais inválidas");
        }
        return user;
    }
}
