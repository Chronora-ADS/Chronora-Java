package br.com.senai.model.DTO;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.senai.model.DTO.service.ServiceDTO;
import br.com.senai.model.DTO.user.DocumentDTO;
import br.com.senai.model.DTO.user.LoginDTO;
import br.com.senai.model.DTO.user.ResetPasswordDTO;
import br.com.senai.model.DTO.user.UserDTO;
import br.com.senai.model.enums.TrackingType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DtoValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void deveValidarLoginComEmailESenhaObrigatorios() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("cpf-nao-deve-ser-usado");
        dto.setPassword("");

        Set<ConstraintViolation<LoginDTO>> violacoes = validator.validate(dto);

        assertTrue(contemViolacao(violacoes, "email"));
        assertTrue(contemViolacao(violacoes, "password"));
    }

    @Test
    void deveAceitarLoginComEmailValidoESenha() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("ana@chronora.com");
        dto.setPassword("senha123");

        Set<ConstraintViolation<LoginDTO>> violacoes = validator.validate(dto);

        assertTrue(violacoes.isEmpty());
    }

    @Test
    void deveValidarCamposObrigatoriosDoCadastro() {
        UserDTO dto = new UserDTO();

        Set<ConstraintViolation<UserDTO>> violacoes = validator.validate(dto);

        assertTrue(contemViolacao(violacoes, "name"));
        assertTrue(contemViolacao(violacoes, "email"));
        assertTrue(contemViolacao(violacoes, "phoneNumber"));
        assertTrue(contemViolacao(violacoes, "password"));
        assertTrue(contemViolacao(violacoes, "document"));
    }

    @Test
    void deveValidarDocumentoObrigatorioNoCadastro() {
        DocumentDTO documentDTO = new DocumentDTO();
        UserDTO dto = criarUserDTO();
        dto.setDocument(documentDTO);

        Set<ConstraintViolation<UserDTO>> violacoes = validator.validate(dto);

        assertTrue(contemViolacao(violacoes, "document.name"));
        assertTrue(contemViolacao(violacoes, "document.type"));
        assertTrue(contemViolacao(violacoes, "document.data"));
    }

    @Test
    void deveValidarCamposObrigatoriosDoPedido() {
        ServiceDTO dto = new ServiceDTO();

        Set<ConstraintViolation<ServiceDTO>> violacoes = validator.validate(dto);

        assertTrue(contemViolacao(violacoes, "title"));
        assertTrue(contemViolacao(violacoes, "description"));
        assertTrue(contemViolacao(violacoes, "timeChronos"));
        assertTrue(contemViolacao(violacoes, "modality"));
        assertTrue(contemViolacao(violacoes, "deadline"));
        assertTrue(contemViolacao(violacoes, "categories"));
        assertTrue(contemViolacao(violacoes, "serviceImage"));
        assertTrue(contemViolacao(violacoes, "trackingType"));
    }

    @Test
    void deveValidarTempoDoPedidoEntreUmECemChronos() {
        ServiceDTO semTempoMinimo = criarServiceDTO(0);
        ServiceDTO acimaDoMaximo = criarServiceDTO(101);

        Set<ConstraintViolation<ServiceDTO>> violacoesMinimo = validator.validate(semTempoMinimo);
        Set<ConstraintViolation<ServiceDTO>> violacoesMaximo = validator.validate(acimaDoMaximo);

        assertTrue(contemViolacao(violacoesMinimo, "timeChronos"));
        assertTrue(contemViolacao(violacoesMaximo, "timeChronos"));
    }

    @Test
    void deveValidarLimiteMaximoDeCategoriasDoPedido() {
        ServiceDTO dto = criarServiceDTO(10);
        dto.setCategories(List.of(
                "C1", "C2", "C3", "C4", "C5", "C6",
                "C7", "C8", "C9", "C10", "C11"
        ));

        Set<ConstraintViolation<ServiceDTO>> violacoes = validator.validate(dto);

        assertTrue(contemViolacao(violacoes, "categories"));
    }

    @Test
    void deveAceitarPedidoValido() {
        ServiceDTO dto = criarServiceDTO(50);

        Set<ConstraintViolation<ServiceDTO>> violacoes = validator.validate(dto);

        assertTrue(violacoes.isEmpty());
    }

    @Test
    void deveValidarLimiteDaDescricaoDaMetrica() {
        ServiceDTO dto = criarServiceDTO(10);
        dto.setTrackingDescription("x".repeat(501));

        Set<ConstraintViolation<ServiceDTO>> violacoes = validator.validate(dto);

        assertTrue(contemViolacao(violacoes, "trackingDescription"));
    }

    @Test
    void deveValidarNovaSenhaDoResetEntreSeisESetentaEDoisCaracteres() {
        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setNewPassword("12345");

        Set<ConstraintViolation<ResetPasswordDTO>> violacoes = validator.validate(dto);

        assertFalse(violacoes.isEmpty());
        assertTrue(contemViolacao(violacoes, "newPassword"));
    }

    private boolean contemViolacao(Set<? extends ConstraintViolation<?>> violacoes, String campo) {
        return violacoes.stream()
                .anyMatch(violacao -> violacao.getPropertyPath().toString().equals(campo));
    }

    private UserDTO criarUserDTO() {
        DocumentDTO documentDTO = new DocumentDTO();
        documentDTO.setName("documento.png");
        documentDTO.setType("png");
        documentDTO.setData("base64-documento");

        UserDTO dto = new UserDTO();
        dto.setName("Ana Silva");
        dto.setEmail("ana@chronora.com");
        dto.setPhoneNumber(11999999999L);
        dto.setPassword("senha123");
        dto.setDocument(documentDTO);
        return dto;
    }

    private ServiceDTO criarServiceDTO(int chronos) {
        return new ServiceDTO(
                "Aula de Java",
                "Mentoria para projeto Spring Boot",
                chronos,
                "Remoto",
                LocalDate.now().plusDays(15),
                List.of("Programacao"),
                "base64-imagem",
                TrackingType.CUSTOM,
                "Por capitulo traduzido"
        );
    }
}
