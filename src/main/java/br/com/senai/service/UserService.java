package br.com.senai.service;

import br.com.senai.exception.Auth.AuthException;
import br.com.senai.exception.NotFound.UserNotFoundException;
import br.com.senai.model.DTO.SupabaseUserDTO;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private final UserRepository userRepository;
    private final SupabaseAuthService supabaseAuthService;
    private final AuthService authService;

    public UserEntity getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário com ID " + id + " não encontrado."));
    }

    public UserEntity getByEmail(String email) {
        return userRepository.findAllByEmail(email);
    }

    public UserEntity getLoggedUser(String tokenHeader) {
        try {
            if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
                String token = tokenHeader.substring(7);

                // Valida o token no Supabase
                SupabaseUserDTO supabaseUser = supabaseAuthService.validateToken(token);

                // Busca o usuário no banco local pelo ID do Supabase usando UserService
                return authService.findBySupabaseUserId(supabaseUser.getId());
            }
            throw new UserNotFoundException("Usuário não encontrado.");
        } catch (Exception e) {
            throw new AuthException("Token inválido.");
        }
    }

//    public UserEntity create(UserDTO userDTO) {
//        DocumentEntity documentEntity = new DocumentEntity();
//
//        String[] parts = userDTO.getDocument().split(",");
//        String dataBase64 = (parts.length > 1) ? parts[1] : parts[0];
//        byte[] data = Base64.getDecoder().decode(dataBase64);
//
//        documentEntity.setName("foto.png");
//        documentEntity.setType("image/png");
//        documentEntity.setData(data);
//
//        UserEntity userEntity = new UserEntity();
//        userEntity.setName(userDTO.getName());
//        userEntity.setEmail(userDTO.getEmail());
//        userEntity.setPhone_number(userDTO.getPhone_number());
//        userEntity.setPasswod(userDTO.getPassword());
//        userEntity.setDocumentEntity(documentEntity);
//
//        return userRepository.save(userEntity);
//    }
}
