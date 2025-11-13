package br.com.senai.service;

import br.com.senai.exception.NotFound.UserNotFoundException;
import br.com.senai.model.DTO.UserDTO;
import br.com.senai.model.entity.DocumentEntity;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private final UserRepository userRepository;

    public UserEntity getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário com ID " + id + " não encontrado."));
    }

    public UserEntity getByEmail(String email) {
        return userRepository.findAllByEmail(email);
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
