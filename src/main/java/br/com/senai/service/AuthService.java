package br.com.senai.service;

import br.com.senai.model.DTO.DocumentDTO;
import br.com.senai.model.DTO.LoginDTO;
import br.com.senai.model.DTO.UserDTO;
import br.com.senai.model.entity.DocumentEntity;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.repository.UserRepository;
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

    /**
     * Busca usuário pelo ID do Supabase
     */
    public UserEntity findBySupabaseUserId(String supabaseUserId) {
        return userRepository.findBySupabaseUserId(supabaseUserId)
                .orElse(null);
    }

    public UserEntity register(UserDTO userDTO, String supabaseUserId) throws RuntimeException {
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
        userEntity.setSupabaseUserId(supabaseUserId);

        // Processa o documento
        DocumentDTO docDTO = userDTO.getDocument();
        if (docDTO.getData() == null) {
            throw new IllegalArgumentException("Documento é obrigatório");
        }

        DocumentEntity document = new DocumentEntity();
        document.setName(docDTO.getName() != null ? docDTO.getName() : "documento_sem_nome");
        document.setType(docDTO.getType() != null ? docDTO.getType() : "application/octet-stream");

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

    /**
     * Autentica usuário
     */
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
}
