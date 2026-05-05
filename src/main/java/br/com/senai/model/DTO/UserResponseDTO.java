package br.com.senai.model.DTO;

import br.com.senai.model.entity.UserEntity;
import com.fasterxml.jackson.annotation.JsonProperty;

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
        response.setRating(0.0);
        response.setProfileImage(null);
        response.setDocument(DocumentResponseDTO.fromEntity(userEntity.getDocumentEntity()));
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Integer getTimeChronos() {
        return timeChronos;
    }

    public void setTimeChronos(Integer timeChronos) {
        this.timeChronos = timeChronos;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    @JsonProperty("saldoChronos")
    public Integer getSaldoChronos() {
        return timeChronos;
    }

    public DocumentResponseDTO getDocument() {
        return document;
    }

    public void setDocument(DocumentResponseDTO document) {
        this.document = document;
    }
}
