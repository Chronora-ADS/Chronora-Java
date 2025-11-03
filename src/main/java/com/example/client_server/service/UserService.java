package com.example.client_server.service;

import com.example.client_server.model.DTO.UserDTO;
import com.example.client_server.model.entity.DocumentEntity;
import com.example.client_server.model.entity.UserEntity;
import com.example.client_server.repository.UserRepository;
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

    public Optional<UserEntity> getById(Long id) {
        return userRepository.findById(id);
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
