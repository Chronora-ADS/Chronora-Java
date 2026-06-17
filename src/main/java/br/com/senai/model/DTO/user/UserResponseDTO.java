package br.com.senai.model.DTO.user;

import br.com.senai.model.entity.UserEntity;
import lombok.Data;

@Data
public class UserResponseDTO {
    private Long id;
    private String name;
    private String email;
    private Long phoneNumber;
    private Integer timeChronos;
    private Double rating;
    private String profileImage;
    private DocumentResponseDTO document;

    public static UserResponseDTO fromEntity(UserEntity userEntity) {
        UserResponseDTO response = new UserResponseDTO();
        response.setId(userEntity.getId());
        response.setName(userEntity.getName());
        response.setEmail(userEntity.getEmail());
        response.setPhoneNumber(userEntity.getPhoneNumber());
        response.setTimeChronos(userEntity.getTimeChronos());
        response.setRating(userEntity.getRating() != null ? userEntity.getRating() : 0.0);
        response.setProfileImage(userEntity.getProfileImage());
        response.setDocument(DocumentResponseDTO.fromEntity(userEntity.getDocumentEntity()));
        return response;
    }
}
