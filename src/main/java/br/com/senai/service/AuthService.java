package br.com.senai.service;

import br.com.senai.exception.NotFound.UserNotFoundException;
import br.com.senai.exception.Validation.EmailAlreadyExistsException;
import br.com.senai.exception.Validation.InvalidDocumentException;
import br.com.senai.exception.Validation.PhoneNumberAlreadyExistsException;
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
        return userRepository.findByEmail(email)
                .map(userEntity -> User.builder()
                        .username(userEntity.getEmail())
                        .password(userEntity.getPassword())
                        .roles(userEntity.getRoles().toArray(new String[0]))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));
    }

    /**
     * Busca usuário pelo ID do Supabase
     */
    public UserEntity findBySupabaseUserId(String supabaseUserId) {
        return userRepository.findBySupabaseUserId(supabaseUserId)
                .orElseThrow(() -> new UserNotFoundException("Usuário com ID Supabase " + supabaseUserId + " não encontrado."));
    }

    public UserEntity register(UserDTO userDTO, String supabaseUserId) throws RuntimeException {
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(userDTO.getEmail());
        }

        if (userRepository.findByPhoneNumber(userDTO.getPhoneNumber()).isPresent()) {
            throw new PhoneNumberAlreadyExistsException(userDTO.getPhoneNumber().toString());
        }

        // Processa o documento
        DocumentDTO docDTO = userDTO.getDocument();
        if (docDTO.getData() == null) {
            throw new InvalidDocumentException("Documento é obrigatório no cadastro.");
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setName(userDTO.getName());
        userEntity.setEmail(userDTO.getEmail());
        userEntity.setPhoneNumber(userDTO.getPhoneNumber());
        userEntity.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        userEntity.setSupabaseUserId(supabaseUserId);
        userEntity.setRoles(List.of("USER"));
        userEntity.setTimeChronos(12);

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

        UserEntity user = userOptional.get();
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("Credenciais inválidas");
        }
        return user;
    }
}
