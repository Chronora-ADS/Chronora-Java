package com.example.client_server.service;

import com.example.client_server.model.DTO.DocumentDTO;
import com.example.client_server.model.DTO.LoginDTO;
import com.example.client_server.model.DTO.UserDTO;
import com.example.client_server.model.entity.DocumentEntity;
import com.example.client_server.model.entity.UserEntity;
import com.example.client_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<UserEntity> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("Usuário não encontrado");
        }
        UserEntity userEntity = userOptional.get();

        return User.builder()
                .username(userEntity.getEmail())
                .password(userEntity.getPassword())
                .roles(userEntity.getRoles().toArray(new String[0]))
                .build();
    }

    public UserEntity authenticate(LoginDTO loginDTO) {
        Optional<UserEntity> userOptional = userRepository.findByEmail(loginDTO.getEmail());
        if (userOptional.isEmpty()) {
            throw new RuntimeException("Credenciais inválidas");
        }
        UserEntity userEntity = userOptional.get();

        if (!passwordEncoder.matches(loginDTO.getPassword(), userEntity.getPassword())) {
            throw new RuntimeException("Credenciais inválidas");
        }
        return userEntity;
    }

    public UserEntity register(UserDTO userDTO) throws RuntimeException {
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email já registrado");
        }

        if (userRepository.findByPhoneNumber(userDTO.getPhoneNumber()).isPresent()) {
            throw new RuntimeException("Número de celular já registrado");
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setName(userDTO.getName());
        userEntity.setEmail(userDTO.getEmail());
        userEntity.setPhoneNumber(userDTO.getPhoneNumber());
        userEntity.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        // Processa o documento
        DocumentDTO docDTO = userDTO.getDocument();
        if (docDTO.getData() == null) {
            throw new IllegalArgumentException("Documento é obrigatório");
        }

        DocumentEntity document = new DocumentEntity();
        document.setName(docDTO.getName() != null ? docDTO.getName() : "documento_sem_nome");
        document.setType(docDTO.getType() != null ? docDTO.getType() : "application/octet-stream");

        // Remove o prefixo "data:...;base64," se existir
        String base64Data = docDTO.getData().trim();
        if (base64Data.contains(",")) {
            base64Data = base64Data.substring(base64Data.indexOf(",") + 1);
        }

        byte[] fileBytes = Base64.getDecoder().decode(base64Data);
        document.setData(fileBytes);

        userEntity.setDocumentEntity(document);
        userEntity.setRoles(List.of("USER")); // não esqueça de definir as roles!

        return userRepository.save(userEntity);
    }
}
