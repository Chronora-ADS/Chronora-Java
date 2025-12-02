package br.com.senai.model.DTO;

import br.com.senai.model.entity.DocumentEntity;
import lombok.Data;

@Data
public class UserEditDTO {
    private Long id;
    private String name;
    private String email;
    private Long phoneNumber;
    private String password;
    private DocumentEntity document;
}
