package br.com.senai.model.DTO;

import br.com.senai.model.entity.UserEntity;

public class UserResponseDTO {
    private Long id;
    private String name;
    private String email;
    private Long phoneNumber;
    private Integer timeChronos;
    private DocumentResponseDTO document;

    public static UserResponseDTO fromEntity(UserEntity userEntity) {
        UserResponseDTO response = new UserResponseDTO();
        response.setId(userEntity.getId());
        response.setName(userEntity.getName());
        response.setEmail(userEntity.getEmail());
        response.setPhoneNumber(userEntity.getPhoneNumber());
        response.setTimeChronos(userEntity.getTimeChronos());
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

    public DocumentResponseDTO getDocument() {
        return document;
    }

    public void setDocument(DocumentResponseDTO document) {
        this.document = document;
    }
}
